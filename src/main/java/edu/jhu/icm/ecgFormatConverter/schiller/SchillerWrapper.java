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
* @author Dave Hopkins, Andre Vilardo, Chris Jurado
*/
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.cvrgrid.schiller.DecodedLead;
import org.cvrgrid.schiller.PreprocessReturn;
import org.cvrgrid.schiller.SchillerEcgFiles;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class SchillerWrapper extends ECGFormatWrapper{
	private DecodedLead[] leadData;
	
	public SchillerWrapper(String filePath) throws IOException, JAXBException{
		ecgFile = new ECGFile();
		init(filePath);
	}
	
	public SchillerWrapper(InputStream inputStream) throws IOException, JAXBException{
		ecgFile = new ECGFile();
		init(inputStream);
	}

	protected void init(String filePath) throws IOException, JAXBException {
		File inputFile = new File(filePath);
		PreprocessReturn ret = SchillerEcgFiles.preprocess(inputFile);
		init(ret);
	}
	
	protected void init(InputStream inputStream) throws IOException, JAXBException {
		PreprocessReturn ret = SchillerEcgFiles.preprocess(inputStream);
		init(ret);
	}
	
	private void init(PreprocessReturn ret){
		ecgFile.samplingRate = Float.valueOf(ret.getPrepSampleRate());
		leadData = ret.getDecodedLeads();
		ecgFile.leadNamesList = ret.getLeadNames();
		ecgFile.scalingFactor = 1;
	}
	
	public ECGFile parse() throws ECGConverterException, IOException {

		ecgFile.channels = leadData.length;
			
		int previousSample = leadData[0].size();

		for(int i=0; i < ecgFile.channels; i++) {
			int currentSample = leadData[i].size();
			if(currentSample == previousSample) {
				ecgFile.samplesPerChannel = currentSample;
			} else {
				ecgFile.samplesPerChannel = 0;
			}
		}
		ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			
		for(int i=0; i < ecgFile.channels; i++) {
			for(int j=0; j<leadData[i].size(); j++) {
				ecgFile.data[i][j] = leadData[i].get(j);
			}
		}
		return ecgFile;
	}
}