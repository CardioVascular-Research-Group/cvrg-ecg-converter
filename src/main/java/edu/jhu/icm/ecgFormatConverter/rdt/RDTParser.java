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
* @author Michael Shipway, Andre Vilardo, Chris Jurado
*/
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;

public class RDTParser {

	private File rdtFile;
	private InputStream inputStream;
	private int channels, samplingRate;
	private int counts;
	private int[][] data; //[channel][index] or [column][row], changed from double, since the largest WFDB resolution is 16 bits.
	private static final ByteOrder BYTEORDER = ByteOrder.LITTLE_ENDIAN;
	private static final int HEADERBYTES = 4;
	private static final int SHORTBYTES = 2;
	private int aduGain = 200;
	private List<String> leadNames;
	private long fileSize;

	public RDTParser(){
		this.inputStream = null;
		this.rdtFile = null;
	}
	
	public RDTParser(File rdtFile) {
		this.inputStream = null;
		this.rdtFile = rdtFile;
	}
	
	public RDTParser(InputStream inputStream) {
		this.rdtFile = null;
		this.inputStream = inputStream;
	}

	public void parse() throws ECGConverterException, IOException{
		if(inputStream != null){
			parseInputStream();
		} else if(rdtFile != null) {
			parseFile();
		} else {
			throw new ECGConverterException("RDT Parser has nothing to parse.");
		}
	}
	
	private void parseFile() throws ECGConverterException, IOException{

		this.fileSize = rdtFile.length();
		if (fileSize > Integer.MAX_VALUE) {
			throw new ECGConverterException("file size exceeding maximum int value.");
		}

		try {
			this.inputStream = new FileInputStream(rdtFile);
		} catch (FileNotFoundException e) {
			throw new ECGConverterException("RDT source file not found.\n" + e.getMessage());
		}	
		parseInputStream();
	}
	
	/** Opens the File object which was passed into the constructor, 
	 *  validate it, parse out the header data, and then parse the 
	 *  ECG data, saving the results in private variables.
	 * 
	 * @return - success/fail
	 * @throws IOException 
	 * @throws ECGConverterException 
	 */
	private void parseInputStream() throws ECGConverterException, IOException {

		// 	parse the file's header for the short values "channels" and "samplingRate"
		byte[] header = new byte[HEADERBYTES];
		try {
			int result = inputStream.read(header);
			if (result != HEADERBYTES) {
				throw new ECGConverterException ("An error occured while reading header.");
			}
			ByteBuffer bbHead = ByteBuffer.wrap(header);
			bbHead.order(BYTEORDER);
			this.channels = bbHead.getShort();
			this.samplingRate = bbHead.getShort();
			
			// Parse ECG data
			final int REALBUFFERSIZE = (int) fileSize - HEADERBYTES;
			if (REALBUFFERSIZE % (channels * SHORTBYTES) != 0) {
				throw new ECGConverterException("RDT file is not aligned.");
			}
	
			this.counts = REALBUFFERSIZE / (channels * SHORTBYTES);
			this.data = new int[channels][counts];
			byte[] body = new byte[REALBUFFERSIZE];
			
			int length = inputStream.read(body);
			if (length != REALBUFFERSIZE) {
				throw new ECGConverterException("An error occurred while reading data into buffer");
			}

			ByteBuffer bbBody = ByteBuffer.wrap(body);
			bbBody.order(BYTEORDER);
			for (int index = 0; index < this.counts; index++) {
				for (int channel = 0; channel < this.channels; channel++) {
					short value = bbBody.getShort();
					this.data[channel][index] = value;
				}
			}

		} catch (IOException e) {
			throw new ECGConverterException(e.getMessage());
		} finally {
			inputStream.close();
		}
	}

	public int writeRDTtoFile() {
		FileOutputStream fos;
		DataOutputStream dos;
		int s=0;

		try {
			fos = new FileOutputStream(rdtFile);
			dos = new DataOutputStream(fos);


			// Header 4 bytes [sample frequency, Hz][# of channels]
			w16(channels,dos);
			w16(samplingRate, dos);
			
			// ********* samples 
			for (s = 0; s < counts; s++) {
				for(int c = 0; c < channels;c++) {
					w16(data[c][s],dos);
				}					
			}
			dos.flush();
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public InputStream writeRDTtoInputStream() throws IOException {
		PipedInputStream inputStream = new PipedInputStream();
		OutputStream outputStream = new PipedOutputStream(inputStream);//BADBADBAD TODO: Multi-thread
		
		int s=0;

		try {

			// Header 4 bytes [sample frequency, Hz][# of channels]
			w16(channels,outputStream);
			w16(samplingRate, outputStream);
			
			// ********* samples 
			for (s = 0; s < counts; s++) {
				for(int c = 0; c < channels;c++) {
					w16(data[c][s],outputStream);
				}					
			}
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return inputStream;
	}

	
	/**
	 * write only the least significant byte of the int to the output stream.
	 * 
	 * @param outByte
	 * @param og
	 */
	private void w8(int outWord, OutputStream dos) {
		try {
			dos.write(outWord);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * write 16-bit two’s complement amplitude stored least significant byte
	 * first
	 * 
	 * @param outWord
	 * @param og - WFDB_ogdata
	 */
	private void w16(int outWord, OutputStream dos){
		try {
			int l, h;

			l = outWord;
			h = outWord >> 8;

			w8(l, dos);
			w8(h, dos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * write 16-bit two’s complement amplitude 
	 * stored least significant byte first
	 * 
	 * @param outWord  - one sample from one signal(channel), 12 bits
	 * @param og
	 */
	private void w61(int outWord, OutputStream dos){
		try {
			int l, h;

			l =  outWord;
			h =  outWord >> 8;

			w8(h, dos);
			w8(l, dos);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		return counts;
	}
	
	public int getAduGain() {
		return aduGain;
	}

	public void setSamplesPerChannel(int samplesPerChannel) {
		counts = samplesPerChannel;
	}

	public void setSamplingRate(float frequency) {
		samplingRate = Float.valueOf(frequency).intValue();
	}

	public float getSamplingRate() {
		return Integer.valueOf(samplingRate).floatValue();
	}

	public int getSamplesPerChannel() {
		return counts;
	}

	public int getNumberOfPoints() {
		return counts * channels;
	}

	public List<String> getLeadNames() {
		return leadNames;
	}
}