package edu.jhu.icm.ecgFormatConverter.hl7;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cvrgrid.hl7aecg.HL7PreprocessReturn;
import org.cvrgrid.hl7aecg.Hl7Ecg;
import org.cvrgrid.hl7aecg.jaxb.beans.PORTMT020001Component9;
import org.jfree.data.xy.XYDataset;

import edu.jhu.icm.ecgFormatConverter.WrapperLoader;

public class HL7_wrapper implements WrapperLoader{
	
	private String hl7FileName;
	private File hl7File;
	private List<PORTMT020001Component9> components;
	
	private int[][] data;
	private int channels, samplingRate;
	private int counts;
	private int aduGain = 200;
	private List<String> leadNames;
	
	
	public HL7_wrapper(String file) throws Exception {
		this.hl7FileName = file;
		init();
	}
	
	private void init() throws Exception {
		this.hl7File = new File(hl7FileName);
		// validate the file 
		if (!hl7File.exists()) {
			throw new Exception(hl7File.getName() + " does not exist.");
			
		}

		if (hl7File.length() > Long.MAX_VALUE) {
			throw new Exception(hl7File.getName() + " file size exceeding maximum long value.");
		}
		
		HL7PreprocessReturn ret = Hl7Ecg.preprocess(hl7File);
		components = ret.getComponents();
	}
	
	public boolean parse() {
		
		HL7_EcgLeadData ds = new HL7_EcgLeadData(components);
		
		double time=0;
		double volt=0;
       	int leadCount = components.size()-1;
		int pageCount = ds.getPageCount();
		int page=1;// 1 based dta page number currently being read
       	int sampleOffset=0;
       	int sampleCount=ds.getNumberOfPoints();
        data = new int[leadCount][sampleCount];

		for (page=1; page<=pageCount; page++){
			ds.setPageNumber(page);		
	        XYDataset[] allDatasets = ds.getPagedXYDatasets();
	        
//	        sampleCount = ds.totalRead;

	       	int itemCount=0;
	       	int setCount= allDatasets.length;
	        //System.out.println("Dataset count:" + setCount);
	        
	        for (int s = 0; s < setCount; s++) {
	            XYDataset oneDataset = allDatasets[s];
	            itemCount = oneDataset.getItemCount(0);
	    		//System.out.println(s + ")" + itemCount );
        		for (int i=0;i<itemCount;i++){
            		time = oneDataset.getXValue(s, i); // all leads should have the same time samples.
        			volt = oneDataset.getYValue(s, i);
        			
        			data[s][i + sampleOffset] =   (int) (volt * ds.getLeadScaleValue(s));
        		}
	        }
			sampleOffset += itemCount;
		}
		
		this.channels = leadCount;
		this.counts = sampleOffset;
		if(ds.getTimeUnit().equalsIgnoreCase("s")){
			samplingRate = (int)(1/ds.getTimeIncrement());
		}
		
		this.leadNames = Arrays.asList(ds.getLeadName());
		
		viewData(10);
		
		System.out.println("Time Unit: " + ds.getTimeUnit());
		System.out.println("Time Increment: " + ds.getTimeIncrement());
		System.out.println("SamplingRate: " + this.samplingRate + "/second");
		System.out.println("Time scale: " + ds.getLeadScaleValue(0));
		System.out.println("Lead scale Unit: " + ds.getLeadScaleUnit(0));

		return true;
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
	
	@Override
	public float getSamplingRate() {
		return samplingRate;
	}

	@Override
	public int getSamplesPerChannel() {
		return counts;
	}

	@Override
	public int getChannels() {
		return channels;
	}

	@Override
	public int[][] getData() {
		return data;
	}

	@Override
	public int getAduGain() {
		return aduGain;
	}

	@Override
	public int getNumberOfPoints() {
		return this.getChannels() + this.getSamplesPerChannel();
	}

	@Override
	public List<String> getLeadNames() {
		return leadNames;
	}
}
