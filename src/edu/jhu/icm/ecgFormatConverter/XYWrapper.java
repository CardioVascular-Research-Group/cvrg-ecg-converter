package edu.jhu.icm.ecgFormatConverter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.lang.Math;

public class XYWrapper {
	private File geMuseFile;
	private FileInputStream geMuseFis;
	private DataInputStream geMuseDis;
	private BufferedReader br;
	private int channels, samplingRate;
	private int sampleCount;
	private static int DEFAULT_HERTZ = 1000;
	private double errorTolerance = .10;
	private int aduGain = 200;

	private int[][] data; //[channel][index] or [column][row], changed from double, since the largest WFDB resolution is 16 bits.
	//private static final ByteOrder BYTEORDER = ByteOrder.LITTLE_ENDIAN;
//	private static final int HEADERBYTES = 4;
//	private static final int SHORTBYTES = 2;
	private static final boolean verbose = true;

	public XYWrapper() {
	}

	public XYWrapper(File geMuseFile) {
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
	public boolean parse(boolean useDefault) {
		if (!validate()) return false;

		if (!parseHeader()) return false;
		
		if (!parseECGdata(useDefault)) return false;

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
	private boolean parseECGdata(boolean useDefaultSampleRate) {
		try{

			// Open the file that is the first 
		    // command line parameter
			
			geMuseDis = new DataInputStream(geMuseFis);
		    br = new BufferedReader(new InputStreamReader(geMuseDis));
		    String strLine;
			
			int s = 0;
		    String[] numbers;
		    this.sampleCount = 0;
		    
		    
		    Vector<Double> mSecs = new Vector<Double>();
		    Vector<String[]> channelVals = new Vector<String[]>();
		    
		    double lastSec = 2.5;
		    double firstMsec = 0;
		    int numLines = 0;
		    
		    while ((strLine = br.readLine()) != null)   {
		    	if(strLine.length()>0) {
			    	numLines++;
		    		this.sampleCount++;
			    	numbers = strLine.split(",");
		    		firstMsec = Double.parseDouble(numbers[0]);
		    		mSecs.add(firstMsec);
		    		this.channels = numbers.length - 1;
		    		String [] channelValues = new String[channels];
		    		for (int c = 1; c <= channels; c++) {
		    			if (numbers[c] != null) {
		    				channelValues[c-1] = numbers[c];
		    			}
		    		}
		    		channelVals.add(channelValues);
		    		break;
		    	}
		    }
		    
		    
		    
		    while ((strLine = br.readLine()) != null)   {
		    	if (strLine.length() > 0) {
			    	numLines++;
			    	this.sampleCount++;
			    	numbers = strLine.split(",");
		    		lastSec = Double.parseDouble(numbers[0])/1000;
		    		mSecs.add(lastSec * 1000);
		    		
		    		String [] channelValues = new String[channels];
		    		for (int c = 1; c <= channels; c++) {
		    			channelValues[c-1] = new String(numbers[c]);
		    		}
		    		channelVals.add(channelValues);
		    	}
		    }
		    
		    lastSec = lastSec - (firstMsec/1000);
		    
		    double hertzDecimals = (new Integer(this.sampleCount).doubleValue())/lastSec;
		    if (useDefaultSampleRate) {
		    	this.samplingRate = this.DEFAULT_HERTZ;
		    	this.sampleCount = (int) Math.round(this.samplingRate * lastSec);
		    }
		    else {
		    	this.samplingRate = (int) Math.round(hertzDecimals);
		    }
		    
		    double sampleIntervalMs = new Double(1000/this.samplingRate); 
		    
		    this.data = new int[channels][sampleCount];
		    double [] prevMSec = new double[sampleCount+1];  
		    
		    //double currentMs = 0;
		    //double prevMs = -sampleIntervalMs;
		    int lineNumber = 0;
		    
		    for (int c = 0; c < this.channels; c++) {
				String[] values = channelVals.get(0);
				short value = (short) Math.round(new Double(values[c]));
				if ((s < 3) & verbose) {
					System.out.print(value + " ");
				}
				this.data[c][0] = value;
				
			}
		    prevMSec[0] = 0;
			prevMSec[1] = new Double(mSecs.get(0));
			
		    lineNumber = 1;
		    
		    short value = 0;
		    
		    //points
		    for (int i = 1; i<sampleCount; i++) {
	    		if (!useDefaultSampleRate) { 
	    			String[] values = channelVals.get(i);
	    			for (int c = 0; c < this.channels; c++) {
	    				value = (short) Math.round(new Double(values[c]));
						if ((s < 3) & verbose) {
							System.out.print(value + " ");
						}
						this.data[c][i] = value;
					}
	    		} else if (lineNumber < channelVals.size()) {
	    			boolean useLineNumber = false;
	    			
	    			String[] values = channelVals.get(lineNumber);
	    			double interval = mSecs.get(lineNumber) - prevMSec[i];
					if ( (interval > 0) && 
						 ((lineNumber == (mSecs.size() - 1)) || (interval > (sampleIntervalMs - (sampleIntervalMs * this.errorTolerance))) && (interval < (sampleIntervalMs + (sampleIntervalMs * this.errorTolerance)))))  {
						useLineNumber = true;
						prevMSec[i + 1] = mSecs.get(lineNumber);
					} else { 
						useLineNumber = false; 
					}
					double doubVal = 0;
					if (useLineNumber) {
						for (int c = 0; c < this.channels; c++) {
							
							//if (values[c] != null) {
								doubVal = new Double(values[c]);
								value = (short) Math.round(new Double(doubVal));
								this.data[c][i] = value;
							//}
						}
						lineNumber++;
						
					} else if (lineNumber < (mSecs.size() - 1)){
						
						//keep iterating until you get within the error tolerance
						while ((interval < (sampleIntervalMs - (sampleIntervalMs * this.errorTolerance))) && (lineNumber < (mSecs.size() - 1)) ) {
							lineNumber++;
							interval = mSecs.get(lineNumber) - prevMSec[i];
						}

						//if within tolerance set it
						if (interval < (sampleIntervalMs + (sampleIntervalMs * this.errorTolerance))) {
							values = channelVals.get(lineNumber);
							for (int c = 0; c < this.channels; c++) {
								
								//if (values[c] != null) {
									doubVal  = new Double(values[c]);
									value = (short) Math.round(new Double(doubVal));
									this.data[c][i] = value;
								//}
							}
							prevMSec[i + 1] = mSecs.get(lineNumber);
							lineNumber++;
							
						} else {
							//if next millisecond in file is greater than interval, use a step-averaging to work up to next line
							//numStepsNeeded should be at least 2
							
							int numStepsNeeded = (int) (Math.ceil(interval / sampleIntervalMs));
							double remainder = interval % sampleIntervalMs;
							if (remainder < (sampleIntervalMs * this.errorTolerance)) {
								numStepsNeeded--;
							}
							values = channelVals.get(lineNumber);
							double [] stepYInterval = new double[this.channels];
							int [] startVals = new int[this.channels];
							//calculate "averaging" interval for each channel
							for (int c = 0; c < this.channels; c++) {
								double doubEndVal = new Double(values[c]);
								startVals[c] = this.data[c][i-1];
								stepYInterval[c] = (doubEndVal - startVals[c])/numStepsNeeded;
								
							}
							
							//calculate y values step by step
							for (int j = 0; (j < (numStepsNeeded)) && (i<sampleCount); j++) {
								for (int c = 0; c < this.channels; c++) {
									double offset = stepYInterval[c] * (j+1);
									value = (short) Math.round(new Double(offset + startVals[c]));
									this.data[c][i] = value;
									
								}
								prevMSec[i+1] = prevMSec[i] + sampleIntervalMs;
								i++;
							}
							i--;
							lineNumber++;
						}
					}
	    		}
	    		else { 
	    			for (int c = 0; c < this.channels; c++) {
	    				this.data[c][i] = this.data[c][i-1];
					}
	    		}

		    }

		}catch (Exception e){//Catch exception if any
			e.printStackTrace();
			return false;
		}finally {
			try {
			    geMuseDis.close();
				geMuseFis.close();
			} catch (IOException e2) {
				//System.err.println("Error: " + e2.getMessage());
				e2.printStackTrace();
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
}
