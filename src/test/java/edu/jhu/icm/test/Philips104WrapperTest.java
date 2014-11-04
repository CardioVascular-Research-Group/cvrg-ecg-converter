package edu.jhu.icm.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.jhu.icm.ecgFormatConverter.ECGformatConverter;
import edu.jhu.icm.ecgFormatConverter.philips.Philips104_wrapper;

public class Philips104WrapperTest {

	String fileName;
	String in;
	String out;
	
	@Before
	public void setUp() throws Exception {
		
		fileName = "ecg_97086579_1.xml";
		in = "/opt/liferay/mavenTestResources/ECG_Converter4/philips104/";
		out = in + "out/";
		
	}
	
	@Test
	public void convertionPhilips104Test(){
		
		Philips104_wrapper wrapper =  new Philips104_wrapper();
		
		try {
			long start = System.currentTimeMillis();
			
			wrapper.init(in+fileName);
			
			System.out.println(" ---- Sumary Old code ---- ");
			
			if(wrapper.parse()){
				System.out.println("SampleCount: " + wrapper.getSamplesPerChannel());
				System.out.println("Channels: " + wrapper.getChannels());
				System.out.println("AduGain: " + wrapper.getAduGain());
				System.out.println("AllocatedChannels: " + wrapper.getAllocatedChannels());
				System.out.println("NumberOfPoints: " + wrapper.getNumberOfPoints());
				System.out.println("SamplingRate: " + wrapper.getSamplingRate());
			}
			
			System.out.println(((System.currentTimeMillis() - start)/1000.0) + " sec(s).");
			
			for (int i = 0; i < wrapper.getData().length; i++) {
				for (int j = 0; j < wrapper.getData()[i].length; j++) {
					int item = wrapper.getData()[i][j];
					//System.out.print(item + "\t");
				}
				//System.out.println();
			}			
			
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	
	@Test
	public void convertTest(){
		
		long start = System.currentTimeMillis();
		
		new File(out).mkdirs();
		
		ECGformatConverter c = new ECGformatConverter();
		c.convert(ECGformatConverter.fileFormat.PHILIPS104, ECGformatConverter.fileFormat.WFDB_16, fileName, 0, in, out);
		
		System.out.println(" ---- Sumary ---- ");
		System.out.println(((System.currentTimeMillis() - start)/1000.0) + " sec(s).");
	}
	
}
