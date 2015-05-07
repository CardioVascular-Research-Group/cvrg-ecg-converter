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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class WFDBWrapper extends ECGFormatWrapper{

	private String recordName, subjectId, sourceFilePath;

	private String[] aSigNames;
	private List<String> leadNames;

	private String[] outputFilenames = null;
	private BufferedReader stdInputBuffer = null;
	private BufferedReader stdError = null;
	private InputStream headerStream;
	
	public WFDBWrapper(InputStream headerStream, InputStream dataStream) throws ECGConverterException, IOException, JAXBException{
		ecgFile = new ECGFile();
		this.headerStream = headerStream;
		System.out.println("IS About to initialize.");
		init(dataStream);
		System.out.println("IS Wrapper Instantiated.");
	}
	
	public WFDBWrapper(String fullFilePath) throws ECGConverterException, IOException, JAXBException{
		ecgFile = new ECGFile();
		System.out.println("file about to initialize:" + fullFilePath);
		init(fullFilePath);
		System.out.println("String Wrapper Instantiated.");
	}

	private void WFDBtoArray() throws IOException, InterruptedException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		// Execute command
		String command = "sampfreq -H " + filePath + recordName;

		WFDBUtilities.executeCommand(stdError, stdInputBuffer, command, null, "/");

		String freq = WFDBUtilities.stdReturnHandler(stdInputBuffer, false);
		ecgFile.samplingRate = Float.parseFloat(freq);

		// read data into the local array, count samplesPerSignal

		ecgFile.samplesPerChannel = getSignalCount();
//		ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];

		command = "rdsamp -r " + filePath + recordName + " -c -p -v -H";

		WFDBUtilities.executeCommand(stdError, stdInputBuffer, command, null, "/");
		ecgFile.data = WFDBUtilities.stdReturnMethodHandler(stdInputBuffer);
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
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private int getSignalCount() throws IOException, InterruptedException {
		int count = 0;
		File headerFile = new File(sourceFilePath + recordName + ".hea");

		if (!headerFile.exists()) {
			return -1; // unable to read header file
		}

		BufferedReader reader = new BufferedReader(new FileReader(headerFile));

		int lineCount = 0;
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (!line.startsWith("#")) {
				if (line.length() > 0) { // first non-comment line is the record line.
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
		String command = "signame -r " + sourceFilePath + recordName;
		WFDBUtilities.executeCommand(stdError, stdInputBuffer, command, null, "/");
		String signameRet = WFDBUtilities.stdReturnHandler(stdInputBuffer, true);

		if (signameRet != null) {
			String[] signames = signameRet.split("\n");
			for (String name : signames) {
				leadNames.add(name.toUpperCase());
			}
		}

		reader.close();
		return count;
	}
	
	/** reads one line from a file input stream
	 * 
	 * @param fis
	 * @param isEOF
	 * @return
	 * @throws IOException 
	 */
	private String readLine(FileInputStream fis) throws IOException {
		String ret = "", oneChar;
		int bytesRead = 0;
		byte[] b = new byte[1];

		while ((bytesRead = fis.read(b)) != -1) {
			oneChar = new String(b, 0, 1);
			if (oneChar.compareTo("\n") == 0)
				break;
			if (oneChar.compareTo("\r") == 0)
				break;

			ret += oneChar;
		}

		if (bytesRead == -1) {
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
			ecgFile.channels = Integer.parseInt(fields[1]);
			if(fieldCount>2) {
				sub2 = fields[2].split("[/()]");
				ecgFile.samplingRate = Float.parseFloat(sub2[0]);
			}
			if(fieldCount>3) { // "& sampleFrequency exists" is implied.
				ecgFile.samplesPerChannel = Integer.parseInt(fields[3]);
			}

			return ecgFile.channels;
		}else {
			return -1;
		}
	}

	@Override
	public ECGFile parse() throws IOException, ECGConverterException {

		String command = "sampfreq -H " + sourceFilePath + recordName;
		try {
			WFDBUtilities.executeCommand(stdError, stdInputBuffer, command, null, "/");
			String freq = WFDBUtilities.stdReturnHandler(stdInputBuffer, false);
			ecgFile.samplingRate = Float.parseFloat(freq);
			ecgFile.channels = getSignalCount();
			ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			command = "rdsamp -r " + sourceFilePath + recordName + " -c -p -v -H";
			WFDBUtilities.executeCommand(stdError, stdInputBuffer, command, null, "/");
			WFDBUtilities.stdReturnMethodHandler(stdInputBuffer);
			WFDBtoArray();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ECGConverterException(e.getStackTrace().toString());
		} 

		return ecgFile;
	}

	@Override
	protected void init(String fullFilePath) throws ECGConverterException, IOException, JAXBException {
		init();
		this.sourceFilePath = WFDBUtilities.getDirectory(fullFilePath);
		this.recordName = WFDBUtilities.getRecordName(fullFilePath);
	}

	@Override
	protected void init(InputStream inputStream) throws ECGConverterException, IOException, JAXBException {
		init();
		String dataFileFullName = WFDBUtilities.createTempFiles(headerStream, inputStream, this.subjectId);
		
	}
	
	private void init(){
		Properties pr = System.getProperties();
		pr.put("java.library.path", "/usr/lib");
		System.setProperties(pr);
		this.leadNames = new ArrayList<String>();
		this.subjectId = (new Date()).toString().replaceAll(" ", "");
	}
}