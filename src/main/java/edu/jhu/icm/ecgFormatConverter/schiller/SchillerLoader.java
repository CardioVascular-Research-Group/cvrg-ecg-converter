package edu.jhu.icm.ecgFormatConverter.schiller;
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
* @author David Hopkins, Chris Jurado
*/
import java.io.InputStream;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class SchillerLoader extends ECGFileLoader{
	
	private Object comXiriuzSemaXmlSchillerEDI;

	@Override
	public ECGFileData load(String filePath) {
		SchillerWrapper schillerWrap = new SchillerWrapper(filePath);
		return load(schillerWrap);	
	}
	
	@Override
	public ECGFileData load(InputStream inputStream) {
		SchillerWrapper schillerWrap = new SchillerWrapper(inputStream);
		return load(schillerWrap);
	}
	
	@Override
	protected ECGFileData load(ECGFormatWrapper wrapperLoader) {
		SchillerWrapper schillerWrapper = (SchillerWrapper)wrapperLoader;
		ecgFile = schillerWrapper.parse();
		return ecgFile;
	}
	
	public Object getComXiriuzSemaXmlSchillerEDI() {
		return comXiriuzSemaXmlSchillerEDI;
	}
}