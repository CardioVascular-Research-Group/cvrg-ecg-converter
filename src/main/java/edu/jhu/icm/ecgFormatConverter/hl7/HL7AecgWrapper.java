package edu.jhu.icm.ecgFormatConverter.hl7;
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
* @author Andre Vilardo, Chris Jurado
*/
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.cvrgrid.hl7aecg.HL7PreprocessReturn;
import org.cvrgrid.hl7aecg.Hl7Ecg;
import org.jfree.data.xy.XYDataset;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;
import edu.jhu.icm.enums.DataFileFormat;

public class HL7AecgWrapper extends ECGFormatWrapper{

	private HL7PreprocessReturn preprocessReturn;
	
	public HL7AecgWrapper(String filename){
		ecgFile = new ECGFileData();
		init(filename);
	}
	
	public HL7AecgWrapper(InputStream inputStream){
		ecgFile = new ECGFileData();
		init(inputStream);
	}

	protected void init(String filename){
		File hl7File = new File(filename);
		try{
		if (!hl7File.exists()) {
			throw new ECGConverterException(hl7File.getName() + " does not exist.");
		}

		if (hl7File.length() > Long.MAX_VALUE) {
			throw new ECGConverterException(hl7File.getName() + " file size exceeding maximum long value.");
		}
		
		preprocessReturn = Hl7Ecg.preprocess(hl7File);

		}catch(ECGConverterException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	protected void init(InputStream inputStream){
		try {
			preprocessReturn = Hl7Ecg.preprocess(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ECGFileData parse() {
		
		org.cvrgrid.hl7aecg.Hl7EcgLeadData ds = preprocessReturn.getLeadData();	
		double volt=0;
       	int leadCount = preprocessReturn.getLeadCount();
		int pageCount = ds.getPageCount();
		int page=1;// 1 based dta page number currently being read
       	int sampleOffset=0;
       	ecgFile.scalingFactor = ds.getLeadScaleValue(0);
       	int sampleCount = ds.getNumberOfPoints();

       	ecgFile.data = new int[leadCount][sampleCount];

		for (page=1; page<=pageCount; page++){
			ds.setPageNumber(page);		
	        XYDataset[] allDatasets = ds.getPagedXYDatasets();

	       	int itemCount=0;
	       	int setCount= allDatasets.length;
	        
	        for (int s = 0; s < setCount; s++) {
	            XYDataset oneDataset = allDatasets[s];
	            itemCount = oneDataset.getItemCount(0);

        		for (int i=0;i < itemCount;i++){
        			volt = oneDataset.getYValue(s, i);
        			ecgFile.data[s][i + sampleOffset] =   (int) (volt * ds.getLeadScaleValue(s));
        		}
	        }
			sampleOffset += itemCount;
		}
		
		ecgFile.channels = leadCount;
		ecgFile.sampleOffset = sampleOffset;
		if(ds.getTimeUnit().equalsIgnoreCase("s")){
			ecgFile.samplingRate = (int)(1/ds.getTimeIncrement());
		}
		
		ecgFile.leadNames = this.extractLeadNames(Arrays.asList(ds.getLeadName()), ecgFile.channels);
		ecgFile.samplesPerChannel = sampleCount;
		return ecgFile;
	}
	
	@Override
	protected DataFileFormat getFormat() {
		return DataFileFormat.HL7;
	}
}