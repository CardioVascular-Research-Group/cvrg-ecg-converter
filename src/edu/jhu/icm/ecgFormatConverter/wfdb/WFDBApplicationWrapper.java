package edu.jhu.icm.ecgFormatConverter.wfdb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import edu.jhu.cvrg.waveform.service.ApplicationWrapper;
import edu.jhu.icm.ecgFormatConverter.WrapperLoader;
import edu.jhu.icm.ecgFormatConverter.WrapperWriter;

public class WFDBApplicationWrapper extends ApplicationWrapper implements WrapperLoader, WrapperWriter{
	
	private String filePath;
	
	public int fmt; /// WFDB encoding format (8,16 ...)	
	private int signalCount;// number of signals
	private float sampleFrequency; // Hz
	public float counterFrequency=0; /// Hz counter frequency (in ticks per second) [optional]
	public float counterBase=0; /// base counter value [optional]if the counterFrequency is present
	private int samplesPerSignal; // number of samples per signal 
	public int segmentCount; /// number of segments [optional] (for header file).
	public String baseTime = "";/// HH:MM:SS [optional]if the samplesPerSignal is present.
	public String baseDate = ""; /// DD/MM/YYYY [optional]if the baseTime is present. 
	public int sampleADCResolution =12;
	public String recordName;
	public int gain = 200;
	public String[] signalName;
	private int[][] data;
	private String sep = File.separator;
	protected String[] aSigNames;
	
	public WFDBApplicationWrapper() {
		Properties pr = System.getProperties();
		pr.put("java.library.path", "/usr/lib");
		System.setProperties(pr);
	}
	
	/** Reads the specified WFDB record into the data array
	 * 
	 * @param recordNm - Name of the record to read.
	 * @param signalsRequested - Number of signals to read, starting with 1st signal.
	 * @return samplesPerSignal
	 */
	public int WFDBtoArray(String recordNm, int signalsRequested) {
		log.info("************** Running New WFDB reading process ************** ");
		
		try {
		    // Execute command
		    String command = "sampfreq -H " + filePath + recordNm;
		    log.info("WFDBtoArray command: " + command);
		    
		    this.executeCommand(command, null, "/");
		    
		    String freq = this.stdReturnHandler();
		    
		    log.info("WFDBtoArray frequency: " + freq);
			sampleFrequency = Float.parseFloat(freq);

		} catch (IOException ioe) {
			log.error("IOException Message: sampfreq " + ioe.getMessage());
		} catch (Exception e) {
			log.error("Exception Message: sampfreq " + e.getMessage());
		}
		

		//---------------------------
		// read data into the local array, count samplesPerSignal
	    try{ 
	    	
	    	signalCount = getSignalCount(recordNm);
	    	log.info("samplesPerSignal: " + samplesPerSignal);
	    	data = new int[signalCount][samplesPerSignal];
	    	
			String command = "rdsamp -r " + filePath + recordNm + " -c -p -v -H";
			
			this.executeCommand(command, null, "/");
			
			this.stdReturnMethodHandler();
			
		    if(log.isDebugEnabled()){
		    	log.debug("First 10 rows of data read:");
			    for (int row = 0; row < 10; row++) {  // try reading the first 10 rows. 
			        for (int sig = 0; sig < signalCount; sig++) {
						log.debug(data[sig][row] + " ");
			        }
				}
		    }
		
		} catch (IOException ioe) {
			log.error("IOException Message: rdsamp " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (Exception e) {
			System.err.println("Exception Message: rdsamp " + e.getMessage());
			e.printStackTrace();
		}
		
	    return samplesPerSignal;
	}
	
	protected void processReturnLine(String line){
		if(lineNum==0){
    		aSigNames = line.split(",");
    		if(signalCount > (aSigNames.length-1)){
    			signalCount = (aSigNames.length-1);
    		}
    		signalName = new String[signalCount];
    		for(int sig=1;sig<=signalCount;sig++){ // zeroth column is time, not a signal
    			signalName[sig-1] = aSigNames[sig];// column names to be used later to verify the order.
    		}			    	  
    	}else if (lineNum > 1){
		    // data.
    		String[] aSample = line.split(",");
    		if(signalCount > (aSample.length-1)){
    			signalCount = (aSample.length-1);
    		}
			for(int sig=1;sig<=signalCount;sig++){ // zeroth column is time, not a signal
				data[sig-1][lineNum-2] = (int)(Float.parseFloat(aSample[sig])*1000);// convert float millivolts to integer microvolts.
			}		    	  
		}		    	  
    }

	/** Takes the ECG samples which are in the data[][] array and write them out as a WFDB file. */
	public int arrayToWFDB() {
		
		int ret = 0;
		String contentFileName = filePath+recordName+".txt";
		File contentFile = new File(contentFileName);
		
		//Create a temporary file with data content
		try {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(contentFile));
			
			for (int i = 0; i < samplesPerSignal; i++) {
				for (int j = 0; j < signalCount; j++) {
					int item = data[j][i];
					bWriter.write(item + "\t");
				}
				bWriter.newLine();
			}
			bWriter.close();
		} catch (IOException e1) {
			log.error("arrayToWFDB() failed. Creating temp content file. " + e1.getMessage());
		}
		
		boolean result = false;
		
		try {
			
			//Check the parameters with the default values
			if(gain == 0){
				gain = 200;
			}
			if(sampleFrequency == 0){
				sampleFrequency = 250;
			}
			if(fmt == 0){
				fmt = 16;
			}
			
			String command = "wrsamp -i " + recordName + ".txt" +" -o " + recordName + " -F "+ sampleFrequency + " -G "+ gain + " -O " + fmt;
			result = this.executeCommand(command, null, filePath);
			result &= stdErrorHandler();
			
			if(result){
				outputFilenames = new String[2];
				outputFilenames[0] = filePath + recordName + ".dat";
				outputFilenames[1] = filePath + recordName + ".hea";
				ret = samplesPerSignal;
			}
			
		} catch (IOException e1) {
			log.error("arrayToWFDB() failed. Creating WFDB files. " + e1.getMessage());
		}
		
		if(contentFile.exists()){
			contentFile.delete();
		}
		
		return ret;
	}

	
	
	/** Returns the number of signals in record without opening them. 
	 * Use this feature to determine the amount of storage needed for 
	 * signal-related variables, as in the example below, 
	 * 
	 * This action also sets internal WFDB library variables that 
	 * record the base time and date, the length of the record, 
	 * and the sampling and counter frequencies, so that time conversion 
	 * functions such as strtim that depend on these quantities will work properly.
	 * 
	 * @param record - name of the record's header file to read, not including the extension.
	 * @return - 
	 * 	>0 Success: the returned value is the number of input signals 
	 *		(i.e., the number of valid entries in siarray) 
	 *	 0 Failure: no input signals available
	 * 	-1 Failure: unable to read header file (probably incorrect record name)
	 * 	-2 Failure: incorrect header file format
	 */
	public int getSignalCount(String recordNm) {
		int count = 0;
		File headerFile = new File(filePath + recordNm + ".hea");
		FileInputStream fis;
		boolean isEOF = false;

		if (!headerFile.exists()) {
			log.error(headerFile.getName() + " does not exist.");
			return -1; // unable to read header file
		}
		
		try {
			fis = new FileInputStream(headerFile);
		} catch (FileNotFoundException e) {
			fis = null;
			log.error(e.getMessage());
			return -3;
		}
		
		while(!isEOF) {
			String line =readLine(fis);
			if(line==null) {
				isEOF=true;
			}else {
				if(!line.startsWith("#")) {
					if(line.length()>0) { // first non-comment line is the record line.
						count = parseHeaderRecordLine(line);
						if(count == -1){
							return -2; // incorrect header file format
						}
						break;
					}
				}
			}
		}
		return count;
	}
	
	/** reads one line from a file input stream
	 * 
	 * @param fis
	 * @param isEOF
	 * @return
	 */
	public String readLine(FileInputStream fis) {
		String ret="", oneChar;
		int bytesRead=0;
		byte[] b = new byte[1];
		
		try {
			while((bytesRead = fis.read(b)) != -1) {
				oneChar = new String(b,0,1);
				if(oneChar.compareTo("\n")==0)
					break;
				if(oneChar.compareTo("\r")==0)
					break;
				
				ret += oneChar; 
			}
		} catch (IOException e) {
			log.error("readLine() error. " + e.getMessage());
		}
		
		if (bytesRead == -1){
			ret = null;
		}
		
		return ret;
	}
	
	
	/**
	 * Parses the Record Line of a WFDB .hea file.<br/>
	 * <br/>
	 * Syntax of Record Line<br/>
	 *	Record_Name[/SEG] S [FREQ[/CNTRfreq[(CNTRbase)]] [SpS [baseT [baseD]]]]<br/>

	 *  Record_Name = letters, digits and underscores (‘_’) only. String<br/>
 		SEG			= number of segments [optional] , integer<br/>
		S			= number of signals, a value of zero is legal, positive integer.<br/>
		FREQ		= sampling frequency (in Hz) [optional],floating-point<br/>
 		CNTRfreq	= counter frequency (in ticks per second) [optional]if the FREQ is present, floating-point<br/>
  		CNTRbase	= base counter value [optional]if the CNTRfreq is present, floating-point<br/>
 		SpS			= number of samples per signal [optional]if the FREQ is present, integer<br/>
  		baseT		= base time [optional]if the SpS is present. HH:MM:SS<br/>
   		baseD		= base date [optional]if the baseT is present. DD/MM/YYYY<br/>
	 * @param recordLine - the record line from a header file.
	 * @return - total signal count.
	 */
	private int parseHeaderRecordLine(String recordLine) {
		String[] sub0, sub2; // for parsing the 0th and 2nd sections of the line.
		String[] fields = recordLine.split("[ \\t\\n\\f\\r]");
		int fieldCount = fields.length;

		if(fieldCount >=2)
		{
			sub0 = fields[0].split("/");
			recordName = sub0[0];
			if(sub0.length==2) segmentCount = Integer.parseInt(sub0[1]);
			signalCount = Integer.parseInt(fields[1]);
			if(fieldCount>2) {
				sub2 = fields[2].split("[/()]");
				sampleFrequency = Float.parseFloat(sub2[0]);
				if(sub2.length>=2) counterFrequency = Integer.parseInt(sub2[1]);
				if(sub2.length==3) counterBase = Integer.parseInt(sub2[2]);
			}
			if(fieldCount>3) { // "& sampleFrequency exists" is implied.
				samplesPerSignal = Integer.parseInt(fields[3]);
			}
			if(fieldCount>4) { // "& samplesPerSignal exists" is implied.
				baseTime = fields[3];
			}
			if(fieldCount>5) { // "& baseTime exists" is implied.
				baseDate = fields[3];
			} 

			return signalCount;
		}else {
			return -1;
		}
	}
	
	public int getAduGain() {
		return gain;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		if(!filePath.endsWith(sep)){
			filePath = filePath + sep; // Because this class was written with the assumption that the path ends with "/".
		}
		this.filePath = filePath;
	}

	@Override
	public float getSamplingRate() {
		return sampleFrequency;
	}

	@Override
	public int getSamplesPerChannel() {
		return samplesPerSignal;
	}

	@Override
	public int getChannels() {
		return signalCount;
	}

	@Override
	public int[][] getData() {
		return data;
	}

	@Override
	public int getNumberOfPoints() {
		return this.getChannels() * this.getSamplesPerChannel();
	}

	@Override
	public void setSamplesPerChannel(int samplesPerChannel) {
		samplesPerSignal = samplesPerChannel;
	}

	@Override
	public void setChannels(int channels) {
		signalCount = channels;
		
	}

	@Override
	public void setSamplingRate(float frequency) {
		sampleFrequency = frequency;
		
	}

	public void setData(int[][] data) {
		this.data = data;
	}

}
