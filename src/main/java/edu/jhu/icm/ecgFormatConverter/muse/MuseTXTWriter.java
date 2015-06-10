package edu.jhu.icm.ecgFormatConverter.muse;
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
* @author Chris Jurado, Mike Shipway
*/
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileWriter;

public class MuseTXTWriter extends ECGFileWriter {

	private ECGFile ecgFile;

	@Override
	public File writeToFile(String outputPath, String recordName, ECGFile ecgFile){
		
		File outFile = null;
		FileOutputStream outStream = null;
		try {
			outFile = new File(outputPath + recordName + ".txt");
			outStream = new FileOutputStream(outFile);
			write(outStream, ecgFile);
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outFile;
	}

	@Override
	public byte[] writeToByteArray(String recordName, ECGFile file){
		
		ecgFile = file;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			write(outputStream, ecgFile);
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return outputStream.toByteArray();
	}
	
	private OutputStream write(OutputStream outStream, ECGFile ecgFile){
		String headerLine = "", dataLine = "", EOL = "\r\n";

		try {
			// ********* header
			headerLine = "Rhythm signal: " + ecgFile.samplesPerChannel + " X " + ecgFile.channels + " " + EOL;

			outStream.write(EOL.getBytes());
			outStream.write(EOL.getBytes());
			outStream.write(EOL.getBytes());
			outStream.write(EOL.getBytes());
			outStream.write(headerLine.getBytes());
			
			// ********* samples 
			outStream.write(EOL.getBytes());
			for (int s = 0; s < ecgFile.samplesPerChannel; s++) {
				dataLine = "";
				for(int c = 0; c < ecgFile.channels;c++) {
					dataLine += ecgFile.data[c][s] + " ";
				}					
				dataLine += EOL;
				outStream.write(dataLine.getBytes());
				outStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outStream;
	}
}