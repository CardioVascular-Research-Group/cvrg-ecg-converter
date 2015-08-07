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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.utility.ConverterUtility;

public class WFDBUtilities {
	
    public static void clearTempFiles(String tempFilePath, String subjectId){
//		String tempFilePath = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
		File folder = new File(tempFilePath);
		if(folder.isDirectory()){
			for(File file : folder.listFiles()){
				if(subjectId != null){
					if(file.getName().startsWith(subjectId)){
						file.delete();
					}
				}else{
					file.delete();
				}
			}
		}
    }

	public static BufferedReader executeCommand(BufferedReader stdError, BufferedReader stdInputBuffer, String sCommand, 
			String[] asEnvVar, String sWorkingDir){

		if (asEnvVar == null) {
			asEnvVar = new String[0];
		}

		File fWorkingDir = new File(sWorkingDir); // converts the dir name to File for exec command.

		Runtime rt = Runtime.getRuntime();
		String[] commandArray = sCommand.split("\\|");
		try{
			if (commandArray.length == 1) {
	
				Process process = rt.exec(sCommand, asEnvVar, fWorkingDir);
				InputStream is = process.getInputStream(); // The input stream for
															// this method comes
															// from the output from
															// rt.exec()
				InputStreamReader isr = new InputStreamReader(is);
				stdInputBuffer = new BufferedReader(isr);
				InputStream errs = process.getErrorStream();
				InputStreamReader esr = new InputStreamReader(errs);
				stdError = new BufferedReader(esr);
			} else {
				Process[] processArray = new Process[commandArray.length];
				// Start processes: ps ax | grep rbe | grep JavaVM
				for (int i = 0; i < commandArray.length; i++) {
					processArray[i] = rt.exec(commandArray[i].trim(), asEnvVar,	fWorkingDir);
				}
				// Start piping
				java.io.InputStream in = Piper.pipe(processArray);
	
				// Show output of last process
				InputStreamReader isr = new InputStreamReader(in);
				stdInputBuffer = new BufferedReader(isr);
				InputStream errs = processArray[processArray.length - 1].getErrorStream();
				InputStreamReader esr = new InputStreamReader(errs);
				stdError = new BufferedReader(esr);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return stdInputBuffer;
	}
	
	public static String getRecordName(String filePath){
		String[] filePathElements = filePath.split(File.separator);
		int size = 0;
		try {
			size = filePathElements.length;
			if (size == 0) {
				throw new ECGConverterException("Invalid File Path for WFDB Read.");
			}
		} catch (ECGConverterException e) {
			e.printStackTrace();
		}
		String fileName = filePathElements[size - 1];
		String[] fileNameElements = fileName.split("\\.");
		return fileNameElements[0];
	}

	public static String createFile(InputStream inputStream, String path) {
		File file = new File(path);
		try {
			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			OutputStream outputStream = new FileOutputStream(file);
			outputStream.write(buffer);
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file.getAbsolutePath();
	}
	
	public static String createTempFiles(InputStream headerStream, InputStream dataStream, String subjectId){
		String path = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
		createFile(headerStream, path + subjectId + ".hea");
		return createFile(dataStream, path + subjectId + ".dat");
	}

	protected void stdCSVReturnHandler(BufferedReader stdInputBuffer, String outputFilename, String[] headers) {
		String line;
		FileWriter fstream = null;
		BufferedWriter bwOut = null;

		try {
			// Create file
			fstream = new FileWriter(outputFilename);
			bwOut = new BufferedWriter(fstream);

			if (headers != null) {
				String headerLine = "";
				for (String string : headers) {
					headerLine += (string + ',');
				}
				headerLine = headerLine.substring(0, headerLine.length() - 1);
				bwOut.write(headerLine);
				bwOut.newLine();
			}

			while ((line = stdInputBuffer.readLine()) != null) {

				line = line.replaceAll("\\s+", ",").replaceAll("\\t", ", ");
				if (line.charAt(0) == ',') {
					line = line.substring(1, line.length());
				}

				bwOut.write(line);
				bwOut.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bwOut.flush();
				bwOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}