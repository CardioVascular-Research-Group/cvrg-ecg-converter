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
* @author Chris Jurado
*/
import java.io.InputStream;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class MuseXMLLoader extends ECGFileLoader{
	
	@Override
	public ECGFileData load(String filePath) {
		MuseXMLWrapper museXMLWrap = new MuseXMLWrapper(filePath);
		return load(museXMLWrap);
	}
	
	@Override
	public ECGFileData load(InputStream inputStream){
		MuseXMLWrapper museXMLWrap = new MuseXMLWrapper(inputStream);
		return load(museXMLWrap);
	}
	
	@Override
	protected ECGFileData load(ECGFormatWrapper wrapper) {

		MuseXMLWrapper museXMLWrap = (MuseXMLWrapper)wrapper;
		ecgFile = museXMLWrap.parse();
		return ecgFile;
	}
}