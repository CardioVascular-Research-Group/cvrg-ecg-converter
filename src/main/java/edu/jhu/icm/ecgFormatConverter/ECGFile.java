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
/**
* @author Chris Jurado
*/
import java.util.ArrayList;
import java.util.List;

public class ECGFile {

	public int data[][]; // common, shared work space populated by LoadXXX, used by WriteXXX methods
	public int channels = 0; // number of channels read by LoadXXX methods
	public int samplesPerChannel = 0 ; // rows read by LoadXXX methods
	public float samplingRate = 0; // Hz read by LoadXXX methods
	public String leadNames;
	public double scalingFactor = 1;
	public List<String> leadNamesList = new ArrayList<String>();
	public int sampleOffset = 0;
	
	@Override
	public String toString(){
		StringBuilder string = new StringBuilder();
		string.append("Channels = " + channels + "\n");
		string.append("Sample Offset = " + sampleOffset + "\n");
		string.append("Lead Names = " + printLeadNameList() + "\n");
		string.append("Samples per Channel = " + samplesPerChannel + "\n");
		string.append("Sampling Rate = " + samplingRate + "\n");
		string.append("Scaling Factor = " + scalingFactor + "\n");
		return string.toString();
	}
	
	public String printLeadNameList(){
		if(leadNamesList == null){
			return "null";
		}
		if(leadNamesList.size() == 0){
			return "empty";
		}
		StringBuilder string = new StringBuilder();
		for(String name : leadNamesList){
			string.append(name + "\t");
		}
		return string.toString();
	}
	
	public String printData(){
		StringBuilder string = new StringBuilder();
		for(int i = 0; i < data.length; i++){
			for(int j = 0; j < data[i].length; j++){
				string.append(data[i][j] + "\t");
			}
			string.append("\n");
		}
		return string.toString();
	}
}