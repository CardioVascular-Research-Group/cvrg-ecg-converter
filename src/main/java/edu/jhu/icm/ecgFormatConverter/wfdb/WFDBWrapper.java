package edu.jhu.icm.ecgFormatConverter.wfdb;
/*
Copyright 2015 Johns Hopkins University Institute for Computational Medicine

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
* @author Michael Shipway, Andre Vilardo, Chris Jurado
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;
import edu.jhu.icm.ecgFormatConverter.utility.ConverterUtility;
import edu.jhu.icm.enums.DataFileFormat;

public class WFDBWrapper extends ECGFormatWrapper{

	private String subjectId, sourceFilePath;
	private List<String> leadNames;
	private BufferedReader stdInputBuffer = null;
	private BufferedReader stdError = null;
	private InputStream headerStream;

	public WFDBWrapper(InputStream headerStream, InputStream dataStream, String subjectId){
		sourceFilePath = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
		ecgFile = new ECGFileData();
		this.headerStream = headerStream;
		this.subjectId = subjectId;
		init(dataStream);
	}
	
	public WFDBWrapper(String inputFile){
		
		int lastSlash = inputFile.lastIndexOf('/');
		if(lastSlash > -1){
			this.sourceFilePath = inputFile.substring(0, lastSlash+1);
			this.subjectId = inputFile.substring(lastSlash+1, inputFile.lastIndexOf('.')); 	
		}else{
			this.sourceFilePath = ConverterUtility.getProperty(ConverterUtility.WFDB_FILE_PATH);
			this.subjectId = inputFile;
		}
		
		ecgFile = new ECGFileData();
		init(inputFile);
	}
	
	@Override
	protected void init(String subjectId) {
		init();
	}

	@Override
	protected void init(InputStream inputStream) {
		init();
		WFDBUtilities.createTempFiles(headerStream, inputStream, this.subjectId);
		this.sourceFilePath = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
	}
	
	private void init(){
		Properties pr = System.getProperties();
		pr.put("java.library.path", "/usr/lib");
		System.setProperties(pr);
		this.leadNames = new ArrayList<String>();
	}

	private int getSignalCount() {
		int count = 0;
		File headerFile = null;
		try {
			headerFile = new File(sourceFilePath + subjectId + ".hea");

			if (!headerFile.exists()) {// unable to read header file
				throw new ECGConverterException("Missing WFDB header file.");
			}
		} catch (ECGConverterException e) {
			e.printStackTrace();
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(headerFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int lineCount = 0;
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					if (line.length() > 0) { // first non-comment line is the
												// record line.
						if (lineCount == 0) {
							count = parseHeaderRecordLine(line);
							if (count == -1) {
								reader.close();
								return -2; // incorrect header file format
							}
							break;
						}
						lineCount++;
					}
				}
			}
			reader.close();
			String command = "signame -r " + this.subjectId;
			WFDBUtilities.executeCommand(stdError, stdInputBuffer, command,	null, sourceFilePath);
			String signameRet = stdReturnHandler(true);

			if (signameRet != null) {
				String[] signames = signameRet.split("\n");
				for (String name : signames) {
					leadNames.add(name.toUpperCase());
				}
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

	@Override
	public ECGFileData parse() {

		String command = "sampfreq -H " + this.subjectId;
		try {
			stdInputBuffer = WFDBUtilities.executeCommand(stdError, stdInputBuffer, command, null, sourceFilePath);
			String freq = stdReturnHandler(false);
			ecgFile.samplingRate = Float.parseFloat(freq);
			ecgFile.channels = getSignalCount();
			ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			command = "rdsamp -r " + this.subjectId + " -c -p -v -H";
			WFDBUtilities.executeCommand(stdError, stdInputBuffer, command, null, sourceFilePath);
			stdReturnMethodHandler(stdInputBuffer);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		WFDBUtilities.clearTempFiles(sourceFilePath, subjectId);
		return ecgFile;
	}

	private int parseHeaderRecordLine(String recordLine) {
		
		String[] sub2; // for parsing the 0th and 2nd sections of the line.
		String[] fields = recordLine.split("[ \\t\\n\\f\\r]");
		int fieldCount = fields.length;

		try {
			if (fieldCount >= 2) {
				ecgFile.channels = Integer.parseInt(fields[1]);
				if (fieldCount > 2) {
					sub2 = fields[2].split("[/()]");
					ecgFile.samplingRate = Float.parseFloat(sub2[0]);
				}
				if (fieldCount > 3) { // "& sampleFrequency exists" is implied.
					ecgFile.samplesPerChannel = Integer.parseInt(fields[3]);
				}
				return ecgFile.channels;
			} else {
				throw new ECGConverterException("Channel count is less than 2.");
			}
		} catch (ECGConverterException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	protected void processReturnLine(String line, int lineNum){
		String[] aSigNames;
		int signalCount = 0;
		String[] signalNames;
		if(lineNum == 0){
    		aSigNames = line.split(",");
   			signalCount = (aSigNames.length-1);
    		signalNames = new String[signalCount];
    		for(int sig=1;sig <= signalCount; sig++){ // zeroth column is time, not a signal
    			signalNames[sig-1] = aSigNames[sig];// column names to be used later to verify the order.
    		}			    	  
    	}
		else if (lineNum > 1){
		    // data.
    		String[] aSample = line.split(",");
   			signalCount = (aSample.length-1);
			for(int sig=1;sig <= signalCount;sig++){ // zeroth column is time, not a signal
				float fSamp;
				try{ // Check if value is a not a number, e.g. "-" or "na", substitute zero so rdsamp won't break; Mike Shipway (7/21/2014)
					fSamp  = Float.parseFloat(aSample[sig]); // assumes unit is float millivolts.
				}catch(NumberFormatException nfe){
					fSamp = 0;
				}
					
				ecgFile.data[sig-1][lineNum-2] = (int)(fSamp*1000);// convert float millivolts to integer microvolts.
			}		    	  
		}	
    }
	
	private String stdReturnHandler(boolean withLineBreak){
	    
		StringBuilder sb = new StringBuilder();
		String tempLine;

	    try {
			while ((tempLine = stdInputBuffer.readLine()) != null) {
				sb.append(tempLine);
				if(withLineBreak){
					sb.append('\n');
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return sb.toString();
	}
	
	public void stdReturnMethodHandler(BufferedReader stdInputBuffer){
		String tempLine;
		int lineNumber = 0;

	    try {
			while ((tempLine = stdInputBuffer.readLine()) != null) {
				processReturnLine(tempLine, lineNumber);
				lineNumber ++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected DataFileFormat getFormat() {
		return DataFileFormat.WFDB;
	}
}