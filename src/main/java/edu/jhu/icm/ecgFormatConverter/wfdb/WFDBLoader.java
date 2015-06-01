package edu.jhu.icm.ecgFormatConverter.wfdb;
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
* @author Michael Shipway, Andre Vilardo, Chris Jurado
*/
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class WFDBLoader extends ECGFileLoader{
	
	private String subjectId = "DEFAULT";
	private InputStream headerStream = null;
	
	public WFDBLoader(){}

	public void setRecordName(String subjectId) {
		this.subjectId = subjectId;
	}

	public void setHeaderStream(InputStream headerStream) {
		this.headerStream = headerStream;
	}
	
	@Override
	public ECGFile load(InputStream dataStream) throws IOException, JAXBException, ECGConverterException {
		if(this.headerStream != null){
			WFDBWrapper wfdbWrap = new WFDBWrapper(headerStream, dataStream, subjectId);
			return load(wfdbWrap);
		} else{
			throw new ECGConverterException("Header Filestream not set.");
		}
	}

	@Override
	protected ECGFile load(ECGFormatWrapper wrapper) throws ECGConverterException, IOException {
		WFDBWrapper wfdbWrap = (WFDBWrapper)wrapper;
		ecgFile = wfdbWrap.parse();
		return ecgFile;
	}

	@Override
	public ECGFile load(String subjectId) throws IOException, JAXBException, ECGConverterException {
		WFDBWrapper wfdbWrap = new WFDBWrapper(subjectId);
		return load(wfdbWrap);	
	}
}