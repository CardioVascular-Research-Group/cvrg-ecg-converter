package edu.jhu.icm.ecgFormatConverter.rdt;
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFileWriter;

public class RDTWriter extends ECGFileWriter{
	
	private ECGFileData ecgFile;

	@Override
	public File writeToFile(String outputPath, String subjectId, ECGFileData ecgFile) {
		this.ecgFile = ecgFile;
		String fileName = subjectId + ".rdt";
		File rdtFile = new File(outputPath + fileName);
		writeRDTtoFile(rdtFile);
		return rdtFile;
	}

	@Override
	public byte[] writeToByteArray(String recordName, ECGFileData file){
		this.ecgFile = file;
		return writeRDTtoByteArray();
	}
	
	private void writeRDTtoFile(File rdtFile) {
		FileOutputStream fos = null;
		DataOutputStream dos = null;
		try {
			fos = new FileOutputStream(rdtFile);
			dos = new DataOutputStream(fos);

			// Header 4 bytes [sample frequency, Hz][# of channels]
			w16(ecgFile.channels,dos);
			w16(Float.valueOf(ecgFile.samplingRate).intValue(), dos);
			
			// ********* samples 
			for (int s = 0; s < ecgFile.samplesPerChannel; s++) {
				for(int c = 0; c < ecgFile.channels;c++) {
					w16(ecgFile.data[c][s],dos);
				}					
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				dos.flush();
				dos.close();
				fos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	
	public byte[] writeRDTtoByteArray(){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {

			// Header 4 bytes [sample frequency, Hz][# of channels]
			w16(ecgFile.channels,outputStream);
			w16(Float.valueOf(ecgFile.samplingRate).intValue(), outputStream);
			
			// ********* samples 
			for (int s = 0; s < ecgFile.samplesPerChannel; s++) {
				for(int c = 0; c < ecgFile.channels;c++) {
					w16(ecgFile.data[c][s],outputStream);
				}					
			}
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputStream.toByteArray();
	}
	
	private void w16(int outWord, OutputStream dos){
		int l, h;
		l = outWord;
		h = outWord >> 8;
		w8(l, dos);
		w8(h, dos);
	}
	
	private void w8(int outWord, OutputStream dos) {
		try {
			dos.write(outWord);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}