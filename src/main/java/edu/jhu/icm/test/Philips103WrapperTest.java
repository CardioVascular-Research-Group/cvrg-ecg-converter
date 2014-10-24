package edu.jhu.icm.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.jhu.icm.ecgFormatConverter.ECGformatConverter;
import edu.jhu.icm.ecgFormatConverter.philips.Philips103_wrapper;

public class Philips103WrapperTest {

	String fileName;
	String in;
	String out;
	
	@Before
	public void setUp() throws Exception {
		
		fileName = "PhilipsExample02.xml";
		in = "/home/WIN/avilard4/XML/Philips103/";
		out = "/home/WIN/avilard4/XML/Philips103/out/";
		
	}

	@Test
	public void convertionPhilips103Test(){
		
		Philips103_wrapper wrapper =  new Philips103_wrapper();
		
		try {
			long start = System.currentTimeMillis();
			
			wrapper.init(in+fileName);
			
			System.out.println(" ---- Sumary Old code ---- ");
			
			if(wrapper.parse()){
				System.out.println("SampleCount: " + wrapper.getSamplesPerChannel());
				System.out.println("Channels: " + wrapper.getChannels());
				System.out.println("AduGain: " + wrapper.getAduGain());
				System.out.println("NumberOfPoints: " + wrapper.getNumberOfPoints());
				System.out.println("SamplingRate: " + wrapper.getSamplingRate());
			}
			
			System.out.println(((System.currentTimeMillis() - start)/1000.0) + " sec(s).");
			
			int channels = wrapper.getChannels();
			int samples = wrapper.getSamplesPerChannel();
			
			for (int i = 0; i < samples; i++) {
				for (int j = 0; j < channels; j++) {
					int item = wrapper.getData()[j][i];
					System.out.print(item + "\t");
				}
				System.out.println();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	
	@Test
	public void convertTest(){
		
		long start = System.currentTimeMillis();
		
		ECGformatConverter c = new ECGformatConverter();
		c.convert(ECGformatConverter.fileFormat.PHILIPS103, ECGformatConverter.fileFormat.WFDB_16, fileName, 0, in, out);
		
		System.out.println(" ---- Sumary ---- ");
		System.out.println(((System.currentTimeMillis() - start)/1000.0) + " sec(s).");
	}
	
}
