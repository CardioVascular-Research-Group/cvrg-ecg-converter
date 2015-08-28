package edu.jhu.icm.ecgFormatConverter.schiller;
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
* @author David Hopkins, Andre Vilardo, Chris Jurado
*/
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.cvrgrid.schiller.DecodedLead;
import org.cvrgrid.schiller.PreprocessReturn;
import org.cvrgrid.schiller.SchillerEcgFiles;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;
import edu.jhu.icm.enums.DataFileFormat;

public class SchillerWrapper extends ECGFormatWrapper{
	private DecodedLead[] leadData;
	private List<String> leadNames;
	
	public SchillerWrapper(String filePath){
		ecgFile = new ECGFileData();
		init(filePath);
	}
	
	public SchillerWrapper(InputStream inputStream){
		ecgFile = new ECGFileData();
		init(inputStream);
	}

	protected void init(String filePath) {
		File inputFile = new File(filePath);
		PreprocessReturn ret;
		try {
			ret = SchillerEcgFiles.preprocess(inputFile);
			init(ret);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	protected void init(InputStream inputStream) {
		PreprocessReturn ret;
		try {
			ret = SchillerEcgFiles.preprocess(inputStream);
			init(ret);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	private void init(PreprocessReturn ret){
		ecgFile.samplingRate = Float.valueOf(ret.getPrepSampleRate());
		leadData = ret.getDecodedLeads();
		leadNames = ret.getLeadNames();
		ecgFile.scalingFactor = 200;
		ecgFile.annotationData = ret.getComXiriuzSemaXmlSchillerEDISchillerEDI();
	}
	
	public ECGFileData parse() {

		ecgFile.channels = leadData.length;	
		int previousSample = leadData[0].size();

		for(int i=0; i < ecgFile.channels; i++) {
			int currentSample = leadData[i].size();
			ecgFile.samplesPerChannel = (currentSample == previousSample) ? currentSample : 0;
		}
		ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			
		for(int i=0; i < ecgFile.channels; i++) {
			for(int j=0; j<leadData[i].size(); j++) {
				ecgFile.data[i][j] = leadData[i].get(j);
			}
		}
		
		ecgFile.leadNames = this.extractLeadNames(leadNames, ecgFile.channels);
		
		return ecgFile;
	}
	
	@Override
	protected DataFileFormat getFormat() {
		return DataFileFormat.SCHILLER;
	}
}
