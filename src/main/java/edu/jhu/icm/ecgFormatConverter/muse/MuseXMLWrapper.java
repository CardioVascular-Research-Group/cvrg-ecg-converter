package edu.jhu.icm.ecgFormatConverter.muse;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;
import edu.jhu.icm.parser.MuseBase64Parser;

public class MuseXMLWrapper extends ECGFormatWrapper{

	private InputStream inputStream = null;
	private MuseBase64Parser base64Parser;
	private ArrayList<int[]> leadData;
	
	public MuseXMLWrapper(String filePath){
		this.ecgFile = new ECGFile();
		init(filePath);
	}
	
	public MuseXMLWrapper(InputStream inputStream){
		this.ecgFile = new ECGFile();
		init(inputStream);
	}

	protected void init(String filePath){
		this.filePath = filePath;
		this.inputStream = null;
		init();
	}
	
	protected void init(InputStream inputStream){
		this.inputStream = inputStream;
		this.filePath = null;
		init();
	}
	
	private void init(){
		base64Parser = new MuseBase64Parser();
		leadData = new ArrayList<int[]>();
	}
		
	@Override
	public ECGFile parse() throws IOException, ECGConverterException {
		
		try {
			if(inputStream != null){
				base64Parser.parse(inputStream);
			} else {
				base64Parser.parse(filePath);
			}

			ecgFile.samplingRate = base64Parser.getSamplingRate();
			leadData = base64Parser.getDecodedData();
			ecgFile.channels = leadData.size();
			int[] singleLead = leadData.get(0);
			int previousSample = singleLead.length;

			for(int i=0; i < leadData.size(); i++) {
				int currentSample = leadData.get(i).length;
				
				if(currentSample == previousSample) {
					ecgFile.samplesPerChannel = currentSample;
				}
				else {
					ecgFile.samplesPerChannel = 0;
				}
			}

			ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			for(int i=0; i < leadData.size(); i++) {
				ecgFile.data[i] = leadData.get(i);
			}
			
			ecgFile.scalingFactor = base64Parser.getAduGain();
			ecgFile.leadNamesList = base64Parser.getLeadNames();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ecgFile;
	}
}