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
* @author Mike Shipway, Chris Jurado
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFileWriter;
import edu.jhu.icm.ecgFormatConverter.utility.ConverterUtility;

public class WFDBWriter extends ECGFileWriter {

	private BufferedReader stdInputBuffer = null;
	private BufferedReader stdError = null;
	private int bitFormat = 16;

	public WFDBWriter(int bits) {

	}

	@Override
	public File writeToFile(String outputPath, String subjectId, ECGFileData ecgFile) {
		boolean isUnitTest = false;
		
		String path = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
		File targetFile;
		String contentFileName;
		if(path == null || path.isEmpty()){
			contentFileName = outputPath + subjectId + ".txt";
			targetFile 		= new File(outputPath + subjectId + ".hea");
			isUnitTest = false;
		}else{
			contentFileName = path + subjectId + ".txt";
			targetFile 		= new File(path + subjectId + ".hea");
			isUnitTest = true;
		}
		
		File contentFile = new File(contentFileName);
		BufferedWriter bWriter;
		try {
			bWriter = new BufferedWriter(new FileWriter(contentFile));

			if(ecgFile.leadNames != null){
				String[] leadNames = ecgFile.leadNames.split(",");
				if(ecgFile.channels == leadNames.length){
					for (String signalName : leadNames) {
						bWriter.write(signalName + "\t");
					}
					bWriter.newLine();		
				}
			}
			
			for (int i = 0; i < ecgFile.samplesPerChannel; i++) {
				for (int j = 0; j < ecgFile.channels; j++) {
					int item = ecgFile.data[j][i];
					bWriter.write(item + "\t");
				}
				bWriter.newLine();
			}
			bWriter.close();

			ecgFile.scalingFactor = (ecgFile.scalingFactor == 0) ? 200 : ecgFile.scalingFactor;
			ecgFile.samplingRate = (ecgFile.samplingRate == 0) ? 250 : ecgFile.samplingRate;

			String command = "wrsamp -i " + subjectId + ".txt" + " -o "
					+ subjectId + " -F " + ecgFile.samplingRate + " -G "
					+ ecgFile.scalingFactor + " -O " + bitFormat;

			WFDBUtilities.executeCommand(stdError, stdInputBuffer, command,	null, isUnitTest ? path : outputPath);
			stdErrorHandler();
			
			long waitingTime = 0;
			long maxWaitingTime = 30000;
			while(!targetFile.exists() && waitingTime < maxWaitingTime){
				try {
					Thread.sleep(100);
					waitingTime += 100;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(waitingTime >= maxWaitingTime){
				throw new RuntimeException("WFDB file generation timeout. Max = "+maxWaitingTime+"ms");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(!isUnitTest){
			contentFile.delete();
			return new File(outputPath);
		}else{
			return new File(path);
		}
	}
	
	protected void stdErrorHandler() {

		String error;
		StringBuilder message = new StringBuilder();
		if (stdError != null) {
			try {
				while ((error = stdError.readLine()) != null) {
					if (error.length() > 0) {
						message.append(error + "\n");
						throw new ECGConverterException("Error happened: " + message.toString());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ECGConverterException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public byte[] writeToByteArray(String subjectId, ECGFileData ecgFile) {
		byte[] fileBytes = null;
		String tempPath =  ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
		writeToFile(tempPath, subjectId, ecgFile);
		File sourceFile = new File(tempPath + subjectId + ".txt");
		Path path = Paths.get(sourceFile.getAbsolutePath());
		try {
			fileBytes = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sourceFile.delete();
		return fileBytes;
	}	
}