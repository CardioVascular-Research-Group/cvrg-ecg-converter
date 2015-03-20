package edu.jhu.icm.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.jhu.icm.ecgFormatConverter.ECGformatConverter;
import edu.jhu.icm.ecgFormatConverter.hl7.HL7Reader;
import edu.jhu.icm.ecgFormatConverter.hl7.HL7_wrapper;

public class HL7WrapperTest {

	String fileName;
	String in;
	String out;
	
	@Before
	public void setUp() throws Exception {
		
		fileName = "Example5.xml";
		in = "/opt/liferay/mavenTestResources/ECG_Converter4/hl7/";
		out = in + "out/";
		
	}

	@Test
	public void convertionNewHL7Test(){
		
		try {
			
			HL7_wrapper wrapper =  new HL7_wrapper(in+fileName);
			
			long start = System.currentTimeMillis();
			
			System.out.println(" ---- Sumary New code ---- ");
			
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
					//System.out.print(item + "\t");
				}
				//System.out.println();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void convertionOLdHL7Test(){
		
		try {
			
			HL7Reader hl7Par =  new HL7Reader(in+fileName);
			
			long start = System.currentTimeMillis();
			
			System.out.println(" ---- Sumary Old code ---- ");
			
			if(hl7Par.parse()){
				System.out.println("SampleCount: " + hl7Par.getSamplesPerChannel());
				System.out.println("Channels: " + hl7Par.getChannels());
				System.out.println("AduGain: " + hl7Par.getAduGain());
				System.out.println("NumberOfPoints: " + hl7Par.getNumberOfPoints());
				System.out.println("SamplingRate: " + hl7Par.getSamplingRate());
			}
			
			System.out.println(((System.currentTimeMillis() - start)/1000.0) + " sec(s).");
			
			int channels = hl7Par.getChannels();
			int samples = hl7Par.getSamplesPerChannel();
			
			for (int i = 0; i < samples; i++) {
				for (int j = 0; j < channels; j++) {
					int item = hl7Par.getData()[j][i];
					//System.out.print(item + "\t");
				}
				//System.out.println();
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
		
		new File(out).mkdirs();
		
		c.convert(ECGformatConverter.fileFormat.HL7, ECGformatConverter.fileFormat.WFDB_16, fileName, 0, in, out);
		
		System.out.println(" ---- Sumary ---- ");
		System.out.println(((System.currentTimeMillis() - start)/1000.0) + " sec(s).");
	}
	
}
