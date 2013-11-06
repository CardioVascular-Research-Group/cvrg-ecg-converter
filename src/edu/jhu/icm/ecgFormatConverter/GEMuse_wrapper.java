package edu.jhu.icm.ecgFormatConverter;
// package nodeDataService;

import java.io.*;

public class GEMuse_wrapper {

	private File geMuseFile;
	private FileInputStream geMuseFis;
	private DataInputStream geMuseDis;
	private BufferedReader br;
	private int channels, samplingRate;
	private int sampleCount;
	private int[][] data; 
	private int aduGain = 200;
	//[channel][index] or [column][row], changed from double, since the largest WFDB resolution is 16 bits.
	//private static final ByteOrder BYTEORDER = ByteOrder.LITTLE_ENDIAN;
//	private static final int HEADERBYTES = 4;
//	private static final int SHORTBYTES = 2;
	private static final boolean verbose = true;

	public GEMuse_wrapper() {
	}

	public GEMuse_wrapper(File geMuseFile) {
		this.geMuseFile = geMuseFile;
	}

	
	/** Reads the requested number of the channels from a WFDB record file set into the converter's work space.
	 * 
	 * @param filePath - path of the input files, e.g. "/mnt/hgfs/SharedFiles/"
	 * @param recordName - name of the record, used to build file names by adding file extensions.
 	 * @param signalsRequested - Number of signals to read, starting with 1st signal.
	 * @return - success/fail 
	 */
	/* 
	public boolean load_geMuse (String filePath, String recordName, int signalsRequested) {
		String path = filePath + recordName; // is a separate step because they may need to be passed as separate parameters in future versions.
		return load_geMuse(path, signalsRequested);
	}
		
	public boolean load_geMuse (String filePath, int signalsRequested) {
		geMuseFile = new File(filePath);
		if(!parse()) return false;
		
		if (sampleCount > 0 ) {
			return true;
		}else { 
			return false;
		}
	}*/
	

	/** Opens the File object which was passed into the constructor, 
	 *  validate it, parse out the header data, and then parse the 
	 *  ECG data, saving the results in private variables.
	 * 
	 * @return - success/fail
	 */
	public boolean parse() {
		if (!validate()) return false;

		if (!parseHeader()) return false;
		
		if (!parseECGdata()) return false;

		return true;
	}

	/**
	 * Confirms that the input file exists, is not too large and that it opens.
	 * @return - true if file is valid and FileInputStream is usable.
	 */
	private boolean validate() {
		
		// validate the file 
		if (!geMuseFile.exists()) {
			if (verbose) {
				System.err.println(this.geMuseFile.getName() + " does not exist.");
			}
			return false;
		}
	
		long fileSize = geMuseFile.length();
		if (fileSize > Integer.MAX_VALUE) {
			System.err.println("file size exceeding maximum int value.");
			return false;
		}
		
		try {
			geMuseFis = new FileInputStream(geMuseFile);
		} catch (FileNotFoundException e) {
			geMuseFis = null;
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}
		
	/**
	 * Opens a BufferedInputStream and parses the geMuse header. 
	 * @return  - success/fail
	 */
	private boolean parseHeader() {
		try{
		    // Open the file that is the first 
		    // command line parameter
		    // FileInputStream fstream = new FileInputStream("textfile.txt");
		    // Get the object of DataInputStream
		    geMuseDis = new DataInputStream(geMuseFis);
		    br = new BufferedReader(new InputStreamReader(geMuseDis));
		    String strLine;
		    String[] words;
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		      // Print the content on the console
		    	if(strLine.length()>0) {
		    		System.out.println (strLine);
		    		words = strLine.split("\\s");
		    		this.samplingRate = 500; // 500 samples per second (Hz) fixed
		    		this.sampleCount = Integer.parseInt(words[2]);
		    		this.channels = Integer.parseInt(words[4]);
		    		break;
		    	}
		    }
			this.data = new int[channels][sampleCount];
			if (verbose) {
				System.out.println("'channels' is " + channels + " 'count' is "+ this.sampleCount);
			}


		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			try {
			    //Close the input stream
			    geMuseDis.close();
				geMuseFis.close();
			} catch (IOException e2) {
				System.err.println("Error: " + e2.getMessage());
			}
			return false;
		}	
		return true;
	}

	/**
	 * Using the BufferedInputStream in parsHeader(), reads the data lines into data[][]. 
	 * @return  - success/fail
	 */
	private boolean parseECGdata() {
		try{

			// Open the file that is the first 
		    // command line parameter
			int s = 0;
		    String strLine;
		    String[] numbers;
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		      // Print the content on the console
		    	strLine = strLine.trim();
		    	if(strLine.length()>0) {
		    		// if (verbose) System.out.println (strLine);
		    		numbers = strLine.split("\\s");
					//for (int s = 0; s < sampleCount; s++) {
						for (int c = 0; c < channels; c++) {
							short value = Short.parseShort(numbers[c]);
							if ((s < 3) & verbose) {
								System.out.print(value + " ");
							}
							this.data[c][s] = value;
						}
						if ((s < 3) & verbose) {
							System.out.println("  s(" + s + ")");
						}
					//}
						s++;
		    	}
		    }
		    //Close the input stream

		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			return false;
		}finally {
			try {
			    geMuseDis.close();
				geMuseFis.close();
			} catch (IOException e2) {
				System.err.println("Error: " + e2.getMessage());
			}
		}	
		return true;
	}

	public int write_geMuse() {
		String headerLine="",dataLine="", EOL = "\r\n";
		FileOutputStream fos;
		int s=0;

		try {
			fos = new FileOutputStream(geMuseFile);

			// ********* header
			headerLine = "Rhythm signal: " + sampleCount + " X " + channels + " " + EOL;
			fos.write(EOL.getBytes());
			fos.write(EOL.getBytes());
			fos.write(EOL.getBytes());
			fos.write(EOL.getBytes());
			fos.write(headerLine.getBytes());
			
			// ********* samples 
			if (verbose) {
				System.out.println("First three rows of values written:");
			}
			fos.write(EOL.getBytes());
			for (s = 0; s < sampleCount; s++) {
				dataLine="";
				for(int c = 0; c < channels;c++) {
					dataLine += data[c][s] + " ";
					if ((s < 3) & verbose) {
						System.out.print(data[c][s] + " ");
					}
				}					
				dataLine += EOL;
				fos.write(dataLine.getBytes());
				fos.flush();
				if ((s < 3) & verbose) {
					System.out.println();
				}
			}
			fos.flush();
			fos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	//	*********** properties *******
	/*public File getFile () {
		return this.geMuseFile;
	}
	public void setFile (File geMuseFile) {
		this.geMuseFile = geMuseFile;
	}
	*/
	public void viewData(int count) {
		if (this.data != null) {
			for (int index = 0; index < count; index++) {
				String line = "";
				for (int channel = 0; channel < this.channels; channel++) {
					line += this.data[channel][index] + ", ";
				}
				System.out.println(line);
			}
		}
	}

	public void viewHeader() {
		System.out.println("(Header) # of channels is " + this.channels
				+ "; sampling rate is " + this.samplingRate + "Hz");
	}

	public int[][] getData() {
		return data;
	}

	public void setData(int[][] dataExternal) {
		data = dataExternal;
	}
	
	public int getChannels() {
		return this.channels;
	}

	public void setChannels(int channelsIn) {
		channels = channelsIn;
	}
	
	public int getCounts() {
		return sampleCount;
	}
	
	public void setCounts(int countsIn) {
		sampleCount = countsIn;
	}

	public int getSamplingRate() {
		return samplingRate;
	}
	
	public void setSamplingRate(int samplingRateIn) {
		samplingRate = samplingRateIn;
	}
	
	public int getAduGain() {
		return aduGain;
	}
	
};
