package edu.jhu.icm.ecgFormatConverter.hl7;
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
import java.io.InputStream;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;

public class HL7AecgLoader extends ECGFileLoader{

	@Override
	public ECGFileData load(InputStream inputStream) {
		HL7AecgWrapper hl7 = null;
		hl7 = new HL7AecgWrapper(inputStream);
		return load(hl7);
	}

	@Override
	public ECGFileData load(String filePath) {
		HL7AecgWrapper hl7 = null;
		hl7 = new HL7AecgWrapper(filePath);
		return load(hl7);	
	}
}