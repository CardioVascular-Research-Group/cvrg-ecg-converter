package edu.jhu.icm.ecgFormatConverter;
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
/*
 * Author: Mike Shipway, Chris Jurado
 */
import java.io.InputStream;
import java.util.List;

public abstract class ECGFormatWrapper {
	
	protected ECGFile ecgFile;
	protected String filePath;
	protected InputStream inputStream;

	public abstract ECGFile parse();
	
	protected abstract void init(String filename);
	
	protected abstract void init(InputStream inputStream);

	/**
	 * Get the frequency
	 * */
	public float getSamplingRate(){
		return ecgFile.samplingRate;
	}
	/**
	 * Get the number of points per lead
	 * */
	public int getSamplesPerChannel(){
		return ecgFile.samplesPerChannel;
	}
	/**
	 * Get the number of leads
	 * */
	public int getChannels(){
		return ecgFile.channels;
	}
	/**
	 * Get the ECG point matrix
	 * */
	public int[][] getData(){
		return ecgFile.data;
	}
	
	/**
	 * Get the lead names in order. <br>
	 * <br>
	 * @return List of extracted lead names from original file. 
	 * */
	public List<String> getLeadNames(){
		return ecgFile.leadNamesList;
	}
}