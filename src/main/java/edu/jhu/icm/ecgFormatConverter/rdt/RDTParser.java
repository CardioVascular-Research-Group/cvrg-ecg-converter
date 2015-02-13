package edu.jhu.icm.ecgFormatConverter.rdt;
// package nodeDataService;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import edu.jhu.icm.ecgFormatConverter.WrapperLoader;
import edu.jhu.icm.ecgFormatConverter.WrapperWriter;

public class RDTParser implements WrapperWriter, WrapperLoader{

	private File rdtFile;
	private int channels, samplingRate;
	private int counts;
	private int[][] data; //[channel][index] or [column][row], changed from double, since the largest WFDB resolution is 16 bits.
	private static final ByteOrder BYTEORDER = ByteOrder.LITTLE_ENDIAN;
	private static final int HEADERBYTES = 4;
	private static final int SHORTBYTES = 2;
	private static final boolean verbose = true;
	private int aduGain = 200;
	private List<String> leadNames;

	public RDTParser(File rdtFile) {
		this.rdtFile = rdtFile;
	}

	
	/** Opens the File object which was passed into the constructor, 
	 *  validate it, parse out the header data, and then parse the 
	 *  ECG data, saving the results in private variables.
	 * 
	 * @return - success/fail
	 */
	public boolean parse() {
		// validate the file 
		if (!rdtFile.exists()) {
			if (verbose) {
				System.err.println(this.rdtFile.getName() + " does not exist.");
			}
			return false;
		}

		long fileSize = rdtFile.length();
		if (fileSize > Integer.MAX_VALUE) {
			System.err.println("file size exceeding maximum int value.");
			return false;
		}
		FileInputStream rdtFis;
		try {
			rdtFis = new FileInputStream(rdtFile);
		} catch (FileNotFoundException e) {
			rdtFis = null;
			System.err.println(e.getMessage());
			return false;
		}

		BufferedInputStream rdtBis = new BufferedInputStream(rdtFis);
		// 	parse the file's header for the short values "channels" and "samplingRate"
		byte[] header = new byte[HEADERBYTES];
		try {
			int result = rdtBis.read(header);
			if (result != HEADERBYTES) {
				System.err.println("error occured while reading header.");
				rdtBis.close();
				return false;
			}
			ByteBuffer bbHead = ByteBuffer.wrap(header);
			bbHead.order(BYTEORDER);
			this.channels = bbHead.getShort();
			this.samplingRate = bbHead.getShort();
		} catch (IOException e) {
			if (verbose) {
				System.err.println(e.getMessage());
			}
			try {
				rdtBis.close();
			} catch (IOException e1) {
			}
			return false;
		}
		
		// Parse ECG data
		final int REALBUFFERSIZE = (int) fileSize - HEADERBYTES;
		if (REALBUFFERSIZE % (channels * SHORTBYTES) != 0) {
			System.err.println("rdt file is not aligned.");
			try {
				rdtBis.close();
			} catch (IOException e1) {
			}
			return false;
		}

		this.counts = REALBUFFERSIZE / (channels * SHORTBYTES);
		this.data = new int[channels][counts];
		if (verbose) {
			System.out.println("'channels' is " + channels + " 'count' is "
					+ this.counts);
		}
		byte[] body = new byte[REALBUFFERSIZE];
		boolean ret = false;
		try {
			int length = rdtBis.read(body);
			if (length != REALBUFFERSIZE) {
				System.err.println("error while reading data into buffer");
				try {
					rdtBis.close();
					rdtFis.close();
				} catch (IOException e2) {
				}
				return false;
			}

			ByteBuffer bbBody = ByteBuffer.wrap(body);
			bbBody.order(BYTEORDER);
			if (verbose) {
				System.out.println("First three rows of (RDT) values:");
			}
			for (int index = 0; index < this.counts; index++) {
				for (int channel = 0; channel < this.channels; channel++) {
					short value = bbBody.getShort();
					if ((index < 3) & verbose) {
						System.out.print(value + " ");
					}
					this.data[channel][index] = value;
				}
				if ((index < 3) & verbose) {
					System.out.println();
				}
			}
			ret = true;
		} catch (IOException e1) {

		} finally {
			try {
				rdtBis.close();
				rdtFis.close();
			} catch (IOException e2) {
			}
		}
		return ret;
	}

	public int writeRDT() {
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
			if (verbose) {
				System.out.println("First three rows of values written:");
			}
			for (s = 0; s < counts; s++) {
				for(int c = 0; c < channels;c++) {
					w16(data[c][s],dos);
					if ((s < 3) & verbose) {
						System.out.print(data[c][s] + " ");
					}
				}					
				if ((s < 3) & verbose) {
					System.out.println();
				}
			}
			dos.flush();
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
//	
	/**
	 * write only the least significant byte of the int to the output stream.
	 * 
	 * @param outByte
	 * @param og
	 */
	public void w8(int outWord, DataOutputStream dos) {
		try {
			dos.writeByte(outWord);
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	public void w16(int outWord, DataOutputStream dos){
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
	public void w61(int outWord, DataOutputStream dos){
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
	
	public void viewData(int count) {
		if (this.data != null) {
			for (int index = 0; index < count; index++) {
				String line = "";
				for (int channel = 0; channel < this.channels; channel++) {
					line += this.data[channel][index] + ", ";
				}
				System.out.println(line);
			}
		}
	}

	public void viewHeader() {
		System.out.println("(Header) # of channels is " + this.channels
				+ "; sampling rate is " + this.samplingRate + "Hz");
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

	@Override
	public void setSamplesPerChannel(int samplesPerChannel) {
		counts = samplesPerChannel;
	}

	@Override
	public void setSamplingRate(float frequency) {
		samplingRate = Float.valueOf(frequency).intValue();
	}


	@Override
	public float getSamplingRate() {
		return Integer.valueOf(samplingRate).floatValue();
	}


	@Override
	public int getSamplesPerChannel() {
		return counts;
	}


	@Override
	public int getNumberOfPoints() {
		return counts * channels;
	}


	@Override
	public List<String> getLeadNames() {
		return leadNames;
	}
	
};
