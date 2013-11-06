package edu.jhu.icm.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.jhu.icm.ecgFormatConverter.ECGformatConverter;
import edu.jhu.icm.ecgFormatConverter.Philips103_wrapper;

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
				System.out.println("SampleCount: " + wrapper.getSampleCount());
				System.out.println("Channels: " + wrapper.getChannels());
			}
			
			System.out.println(((System.currentTimeMillis() - start)/1000.0) + " sec(s).");
			
			
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	
	@Test
	public void convertionPhilips103NewTest(){
		
		Philips103_wrapper wrapper =  new Philips103_wrapper();
		
		try {
			long start = System.currentTimeMillis();
			
			wrapper.init(in + fileName);
			
			System.out.println(" ---- Sumary New code ---- ");
			
			if(wrapper.parse()){
				System.out.println("SampleCount: " + wrapper.getSampleCount());
				System.out.println("Channels: " + wrapper.getChannels());
			}
			
			System.out.println(((System.currentTimeMillis() - start)/1000.0) + " sec(s).");
			
			
		} catch (Exception e) {
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
