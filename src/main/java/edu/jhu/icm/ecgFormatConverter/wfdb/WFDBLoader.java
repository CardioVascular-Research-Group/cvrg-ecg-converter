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
import java.io.InputStream;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;

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
	public ECGFileData load(InputStream dataStream) {
		try {
			if (this.headerStream != null) {
				WFDBWrapper wfdbWrap;
				wfdbWrap = new WFDBWrapper(headerStream, dataStream, subjectId);
				return load(wfdbWrap);

			} else {
				throw new ECGConverterException("Header Filestream not set.");
			}
		} catch (ECGConverterException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ECGFileData load(String subjectId) {
		WFDBWrapper wfdbWrap = null;
		wfdbWrap = new WFDBWrapper(subjectId);
		return load(wfdbWrap);	
	}
}