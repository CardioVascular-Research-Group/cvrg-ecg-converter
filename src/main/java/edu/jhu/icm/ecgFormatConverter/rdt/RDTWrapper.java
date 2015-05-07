package edu.jhu.icm.ecgFormatConverter.rdt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class RDTWrapper extends ECGFormatWrapper{

	private static final int HEADERBYTES = 4;
	private static final int SHORTBYTES = 2;
	private static final ByteOrder BYTEORDER = ByteOrder.LITTLE_ENDIAN;
	
	public RDTWrapper(String filePath){
		this.ecgFile = new ECGFile();
		init(filePath);
	}
	
	public RDTWrapper(InputStream inputStream){
		this.ecgFile = new ECGFile();
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
	public ECGFile parse() throws ECGConverterException, IOException {
		if(inputStream != null){
			parseInputStream();
		} else if(filePath != null) {
			parseFile();
		} else {
			throw new ECGConverterException("RDT Parser has nothing to parse.");
		}
		return ecgFile;
	}

	private void parseFile() throws ECGConverterException, IOException{

		int fileSize = filePath.length();
		if (fileSize > Integer.MAX_VALUE) {
			throw new ECGConverterException("file size exceeding maximum int value.");
		}

		try {
			this.inputStream = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			throw new ECGConverterException("RDT source file not found.\n" + e.getMessage());
		}	
		parseInputStream();
	}
	
	private void parseInputStream() throws ECGConverterException, IOException {
		
		int fileSize = inputStream.available();

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
			throw new ECGConverterException(e.getMessage());
		} finally {
			inputStream.close();
		}
	}
}