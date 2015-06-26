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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class RDTWrapper extends ECGFormatWrapper{

	private static final int HEADERBYTES = 4;
	private static final int SHORTBYTES = 2;
	private static final ByteOrder BYTEORDER = ByteOrder.LITTLE_ENDIAN;
	
	public RDTWrapper(String filePath){
		this.ecgFile = new ECGFileData();
		init(filePath);
	}
	
	public RDTWrapper(InputStream inputStream){
		this.ecgFile = new ECGFileData();
		init(inputStream);
	}
	
	@Override
	protected void init(String filePath){
		this.inputStream = null;
		this.filePath = filePath;
	}
	
	@Override
	protected void init(InputStream inputStream){
		this.filePath = null;
		this.inputStream = inputStream;
	}

	@Override
	public ECGFileData parse(){
		if (inputStream != null) {
			parseInputStream();
		} else if (filePath != null) {
			parseFile();
		} 
		return ecgFile;
	}

	private void parseFile(){

		try {
			int fileSize = filePath.length();
			if (fileSize > Integer.MAX_VALUE) {
				throw new ECGConverterException("file size exceeding maximum int value.");
			}
			this.inputStream = new FileInputStream(filePath);
			parseInputStream();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ECGConverterException e) {
			e.printStackTrace();
		}	
	}
	
	private void parseInputStream(){
		int fileSize = 0;
		try {
			fileSize = inputStream.available();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// 	parse the file's header for the short values "channels" and "samplingRate"
		byte[] header = new byte[HEADERBYTES];
		try {
			int result = inputStream.read(header);
			if (result != HEADERBYTES) {
				throw new ECGConverterException ("An error occured while reading header.");
			}
			ByteBuffer bbHead = ByteBuffer.wrap(header);
			bbHead.order(BYTEORDER);
			ecgFile.channels = bbHead.getShort();
			ecgFile.samplingRate = bbHead.getShort();
			ecgFile.scalingFactor = 1;
			
			// Parse ECG data
			final int REALBUFFERSIZE = (int) fileSize - HEADERBYTES;
			if (REALBUFFERSIZE % (ecgFile.channels * SHORTBYTES) != 0) {
				throw new ECGConverterException("RDT file is not aligned.");
			}
	
			ecgFile.samplesPerChannel = REALBUFFERSIZE / (ecgFile.channels * SHORTBYTES);
			ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			byte[] body = new byte[REALBUFFERSIZE];
			
			int length = inputStream.read(body);
			if (length != REALBUFFERSIZE) {
				throw new ECGConverterException("An error occurred while reading data into buffer");
			}

			ByteBuffer bbBody = ByteBuffer.wrap(body);
			bbBody.order(BYTEORDER);
			for (int index = 0; index < ecgFile.samplesPerChannel; index++) {
				for (int channel = 0; channel < ecgFile.channels; channel++) {
					short value = bbBody.getShort();
					ecgFile.data[channel][index] = value;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ECGConverterException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}