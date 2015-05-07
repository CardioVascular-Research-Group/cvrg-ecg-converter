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
* @author Chris Jurado
*/
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class SchillerLoader extends ECGFileLoader{
	
	private Object comXiriuzSemaXmlSchillerEDI;

	@Override
	public ECGFile load(String filePath) throws IOException, JAXBException, ECGConverterException {
		SchillerWrapper schillerWrap = new SchillerWrapper(filePath);
		return load(schillerWrap);	
	}
	
	@Override
	public ECGFile load(InputStream inputStream) throws IOException, JAXBException, ECGConverterException {
		SchillerWrapper schillerWrap = new SchillerWrapper(inputStream);
		return load(schillerWrap);
	}
	
	@Override
	protected ECGFile load(ECGFormatWrapper wrapperLoader) throws ECGConverterException, IOException {
		SchillerWrapper schillerWrapper = (SchillerWrapper)wrapperLoader;
		ecgFile = schillerWrapper.parse();
		return ecgFile;
	}
	
	public Object getComXiriuzSemaXmlSchillerEDI() {
		return comXiriuzSemaXmlSchillerEDI;
	}
}