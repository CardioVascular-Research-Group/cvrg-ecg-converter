package edu.jhu.icm.ecgFormatConverter.muse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileWriter;

public class MuseTXTWriter extends ECGFileWriter {
	
	private InputStream inputStream;
	private OutputStream outStream;
	private ECGFile ecgFile;

	@Override
	public File writeToFile(String outputPath, String recordName, ECGFile ecgFile) throws ECGConverterException, IOException{
		
		File outFile = null;
		try {
			outFile = new File(outputPath);
			outStream = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			throw new ECGConverterException(e.getMessage());
		}
		write(outStream, ecgFile);
		outStream.close();
		return outFile;
	}

	@Override
	public InputStream writeToInputStream(String recordName, ECGFile file) throws IOException {
		
		ecgFile = file;
		inputStream = new PipedInputStream();
		outStream = new PipedOutputStream((PipedInputStream)inputStream);
		
		new Thread(new Runnable() {
			public void run(){
				try {
					write(outStream, ecgFile);
				} catch (ECGConverterException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		outStream.close();
		return inputStream;
	}
	
	private OutputStream write(OutputStream outStream, ECGFile ecgFile) throws ECGConverterException, IOException {
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
			throw new ECGConverterException(e.getMessage());
		}
		return outStream;
	}
}