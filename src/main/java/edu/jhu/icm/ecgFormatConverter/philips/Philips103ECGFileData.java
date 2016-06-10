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
import org.sierraecg.schema.Restingecgdata;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;

public class Philips103ECGFileData extends ECGFileData {

	public Restingecgdata restingecgdata;
	
	@Override
	public String toString(){
		String superString = super.toString();
		StringBuilder string = new StringBuilder();
		string.append(superString);
		string.append("Restingecgdata + " + restingecgdata + "/n");
		return string.toString();
	}
}