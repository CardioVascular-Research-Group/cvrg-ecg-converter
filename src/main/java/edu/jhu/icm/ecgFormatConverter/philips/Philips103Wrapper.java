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

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;
import edu.jhu.icm.enums.DataFileFormat;

public class Philips103Wrapper extends ECGFormatWrapper{
	private Restingecgdata philipsECG;
	private DecodedLead[] leadData;
	
	public Philips103Wrapper(String filePath){
		ecgFile = new ECGFileData();
		init(filePath);
	}
	
	public Philips103Wrapper(InputStream inputStream){
		ecgFile = new ECGFileData();
		init(inputStream);
	}

	protected void init(String filePath) {
		File inputFile = new File(filePath);
		PreprocessReturn ret;
		try {
			ret = SierraEcgFiles.preprocess(inputFile);
			philipsECG = ret.getRestingEcgData();
			leadData = ret.getDecodedLeads();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	protected void init(InputStream inputStream) {
		PreprocessReturn ret;
		try {
			ret = SierraEcgFiles.preprocess(inputStream);
			philipsECG = ret.getRestingEcgData();
			leadData = ret.getDecodedLeads();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	public ECGFileData parse() {
		if(philipsECG != null) {
			Signalcharacteristics signalMetaData = philipsECG.getDataacquisition().getSignalcharacteristics();
			ecgFile.samplingRate = Float.valueOf(signalMetaData.getSamplingrate());
			ecgFile.channels = Integer.valueOf(signalMetaData.getNumberchannelsvalid());
			
			List<Leadmeasurement> leads = philipsECG.getMeasurements().getLeadmeasurements().getLeadmeasurement();
			if(leads != null){
				List<String> leadNamesList = new ArrayList<String>();
				for (Leadmeasurement lead : leads) {
					leadNamesList.add(lead.getLeadname().toUpperCase());
				}
				ecgFile.leadNames = extractLeadNames(leadNamesList, ecgFile.channels);
			}
			
			int previousSample = leadData[0].size();
			ecgFile.scalingFactor = 200;
			for(int i=0; i < leadData.length; i++) {
				int currentSample = leadData[i].size();
				ecgFile.samplesPerChannel = (currentSample == previousSample) ? currentSample : 0;
			}

			ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			
			for(int i=0; i<leadData.length; i++) {
				for(int j=0; j<leadData[i].size(); j++) {
					ecgFile.data[i][j] = leadData[i].get(j);
				}
			}
			ecgFile.annotationData = philipsECG;
		}
		return ecgFile;
	}

	public Restingecgdata getPhilipsECG() {
		return philipsECG;
	}
	
	@Override
	protected DataFileFormat getFormat() {
		return DataFileFormat.PHILIPS103;
	}
}
