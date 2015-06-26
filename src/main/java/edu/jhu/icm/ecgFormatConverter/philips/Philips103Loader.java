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
/**
* @author Andre Vilardo, Chris Jurado
*/
import java.io.InputStream;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class Philips103Loader extends ECGFileLoader{

	@Override
	public ECGFileData load(InputStream inputStream) {
		Philips103Wrapper philipsWrap = new Philips103Wrapper(inputStream);
		return load(philipsWrap);
	}

	@Override
	public ECGFileData load(String filePath){
		Philips103Wrapper philipsWrap = new Philips103Wrapper(filePath);
		return load(philipsWrap);
	}

	@Override
	protected ECGFileData load(ECGFormatWrapper wrapper){
		Philips103Wrapper philipsWrap = (Philips103Wrapper)wrapper;
		ecgFile = philipsWrap.parse();
		return ecgFile;
	}
}