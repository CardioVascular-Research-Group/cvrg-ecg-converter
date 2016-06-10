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
import java.io.InputStream;
import java.util.ArrayList;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;
import edu.jhu.icm.enums.DataFileFormat;

public class MuseXMLWrapper extends ECGFormatWrapper{

	private InputStream inputStream = null;
	private MuseBase64Parser base64Parser;
	private ArrayList<int[]> leadData;
	private MuseXMLECGFileData ecgFile;
	
	public MuseXMLWrapper(String filePath){
		this.ecgFile = new MuseXMLECGFileData();
		init(filePath);
	}
	
	public MuseXMLWrapper(InputStream inputStream){
		this.ecgFile = new MuseXMLECGFileData();
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
	public ECGFileData parse() {

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
				ecgFile.samplesPerChannel = (currentSample == previousSample) ? currentSample : 0;
			}

			ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];
			for(int i=0; i < leadData.size(); i++) {
				ecgFile.data[i] = leadData.get(i);
			}
			
			ecgFile.scalingFactor = base64Parser.getAduGain();
			ecgFile.leadNames = this.extractLeadNames(base64Parser.getLeadNames(), ecgFile.channels);
			ecgFile.museRawXML = base64Parser.getInitialXML();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ecgFile;
	}
	
	@Override
	protected DataFileFormat getFormat() {
		return DataFileFormat.MUSEXML;
	}
}