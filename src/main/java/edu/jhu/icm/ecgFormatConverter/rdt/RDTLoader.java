package edu.jhu.icm.ecgFormatConverter.rdt;
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

import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class RDTLoader extends ECGFileLoader{

	@Override
	public ECGFile load(String fileName){
		RDTWrapper wrapper = new RDTWrapper(fileName);
		return load(wrapper);
	}

	@Override
	public ECGFile load(InputStream inputStream){
		RDTWrapper wrapper = new RDTWrapper(inputStream);
		return load(wrapper);
	}	
	
	protected ECGFile load(ECGFormatWrapper wrapper){
		RDTWrapper rdtWrapper = (RDTWrapper)wrapper;
		ecgFile = rdtWrapper.parse();
		return ecgFile;
	}
}