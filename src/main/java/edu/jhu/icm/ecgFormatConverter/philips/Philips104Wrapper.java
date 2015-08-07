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

import org.cvrgrid.philips.DecodedLead;
import org.cvrgrid.philips.PreprocessReturn;
import org.cvrgrid.philips.SierraEcgFiles;
import org.cvrgrid.philips.jaxb.beans.Leadmeasurement;
import org.cvrgrid.philips.jaxb.beans.Restingecgdata;
import org.cvrgrid.philips.jaxb.beans.Signalcharacteristics;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class Philips104Wrapper extends ECGFormatWrapper{
	private Restingecgdata philipsECG;
	private DecodedLead[] leadData;
	
	public Philips104Wrapper(String filePath){
		ecgFile = new ECGFileData();
		init(filePath);
	}
	
	public Philips104Wrapper(InputStream inputStream){
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
			
			List<Leadmeasurement> leads = philipsECG.getInternalmeasurements().getLeadmeasurements().getLeadmeasurement();
			if(leads != null){
				ecgFile.leadNamesList = new ArrayList<String>();
				for (Leadmeasurement lead : leads) {
					ecgFile.leadNamesList.add(lead.getLeadname().toUpperCase());
				}
			}
			ecgFile.samplingRate = Float.valueOf(signalMetaData.getSamplingrate());
			ecgFile.scalingFactor = 1;
			
			int allocatedChannels = signalMetaData.getNumberchannelsallocated().intValue(); // Method returns a BigInteger, so a conversion to int is required.
			int validChannels = signalMetaData.getNumberchannelsvalid().intValue();
			
			try {
				if (allocatedChannels != validChannels) {
					throw new ECGConverterException("Valid/Allocated Channels do not match.");
				} else {
					ecgFile.channels = allocatedChannels;
				}
			} catch (ECGConverterException e) {
				e.printStackTrace();
			}
			
			int previousSample = leadData[0].size();

			for(int i=0; i< ecgFile.channels; i++) {
				int currentSample = leadData[i].size();
				
				if(currentSample == previousSample) {
					ecgFile.samplesPerChannel = currentSample;
				} else {
					ecgFile.samplesPerChannel = 0;
				}
			}
			ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			
			for(int i=0; i< ecgFile.channels; i++) {
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
}