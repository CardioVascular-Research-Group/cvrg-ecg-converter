package edu.jhu.icm.ecgFormatConverter.rdt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileWriter;

public class RDTWriter extends ECGFileWriter{
	
	private ECGFile ecgFile;
	private OutputStream outStream;
	/**
	 * writes the data array out in RDT format
	 * @param fileName - full path of RDT output file
	 * @return - rowsWritten
	 */
	@Override
	public File writeToFile(String outputPath, String recordName, ECGFile ecgFile) {
		String fileName = recordName + ".rdt";
		File rdtFile = new File(outputPath + sep + fileName);
		RDTParser rdtPar = new RDTParser();
		rdtPar.setChannels(ecgFile.channels);
		rdtPar.setSamplesPerChannel(ecgFile.samplesPerChannel);
		rdtPar.setSamplingRate(ecgFile.samplingRate);
		rdtPar.setData(ecgFile.data);	
		rdtPar.writeRDTtoFile();
		
		return rdtFile;
	}

	@Override
	public InputStream writeToInputStream(String recordName, ECGFile file) throws IOException {

		RDTParser rdtPar = new RDTParser();
		rdtPar.setChannels(ecgFile.channels);
		rdtPar.setSamplesPerChannel(ecgFile.samplesPerChannel);
		rdtPar.setSamplingRate(ecgFile.samplingRate);
		rdtPar.setData(ecgFile.data);	
		
		new Thread(new Runnable() {
			public void run(){
				try {
					writeRDTtoInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		outStream.close();
		
		
		return rdtPar.writeRDTtoInputStream();
	}

	private InputStream writeRDTtoInputStream() throws IOException {
		PipedInputStream inputStream = new PipedInputStream();
		OutputStream outputStream = new PipedOutputStream(inputStream);//BADBADBAD TODO: Multi-thread
		
		int s=0;

		try {

			// Header 4 bytes [sample frequency, Hz][# of channels]
			w16(ecgFile.channels,outputStream);
			w16((int)Math.ceil(ecgFile.samplingRate), outputStream);
			
			// ********* samples 
			for (s = 0; s < ecgFile.samplesPerChannel; s++) {
				for(int c = 0; c < ecgFile.channels;c++) {
					w16(ecgFile.data[c][s],outputStream);
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
}