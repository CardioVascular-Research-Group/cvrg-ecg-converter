package edu.jhu.icm.ecgFormatConverter.philips;
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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.sierraecg.DecodedLead;
import org.sierraecg.PreprocessReturn;
import org.sierraecg.SierraEcgFiles;
import org.sierraecg.schema.Leadmeasurement;
import org.sierraecg.schema.Restingecgdata;
import org.sierraecg.schema.Signalcharacteristics;

import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class Philips103Wrapper extends ECGFormatWrapper{
	private Restingecgdata philipsECG;
	private DecodedLead[] leadData;
	
	public Philips103Wrapper(String filePath) throws IOException, JAXBException{
		ecgFile = new ECGFile();
		init(filePath);
	}
	
	public Philips103Wrapper(InputStream inputStream) throws IOException, JAXBException{
		ecgFile = new ECGFile();
		init(inputStream);
	}

	protected void init(String filePath) throws IOException, JAXBException {
		File inputFile = new File(filePath);
		PreprocessReturn ret = SierraEcgFiles.preprocess(inputFile);
		philipsECG = ret.getRestingEcgData();
		leadData = ret.getDecodedLeads();
	}
	
	protected void init(InputStream inputStream) throws IOException, JAXBException {
		PreprocessReturn ret = SierraEcgFiles.preprocess(inputStream);
		philipsECG = ret.getRestingEcgData();
		leadData = ret.getDecodedLeads();
	}
	
	public ECGFile parse() {
		if(philipsECG != null) {
			Signalcharacteristics signalMetaData = philipsECG.getDataacquisition().getSignalcharacteristics();
			List<Leadmeasurement> leads = philipsECG.getMeasurements().getLeadmeasurements().getLeadmeasurement();
			if(leads != null){
				ecgFile.leadNamesList = new ArrayList<String>();
				for (Leadmeasurement lead : leads) {
					ecgFile.leadNamesList.add(lead.getLeadname().toUpperCase());
				}
			}
			ecgFile.samplingRate = Float.valueOf(signalMetaData.getSamplingrate());
			ecgFile.channels = Integer.valueOf(signalMetaData.getNumberchannelsvalid());
			int previousSample = leadData[0].size();
			ecgFile.scalingFactor = 1;
//			ecgFile.numberOfPoints = previousSample * ecgFile.channels;
			for(int i=0; i < leadData.length; i++) {
				int currentSample = leadData[i].size();
				if(currentSample == previousSample) {
					ecgFile.samplesPerChannel = currentSample;
				}
				else {
					ecgFile.samplesPerChannel = 0;
				}
			}

			ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			
			for(int i=0; i<leadData.length; i++) {
				for(int j=0; j<leadData[i].size(); j++) {
					ecgFile.data[i][j] = leadData[i].get(j);
				}
			}
		}
		return ecgFile;
	}

	public Restingecgdata getPhilipsECG() {
		return philipsECG;
	}	
}