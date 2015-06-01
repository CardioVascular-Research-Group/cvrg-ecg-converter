package edu.jhu.icm.ecgFormatConverter.rdt;

import java.io.InputStream;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class RDTLoader extends ECGFileLoader{

	@Override
	public ECGFile load(String fileName) throws ECGConverterException{
		RDTWrapper wrapper = new RDTWrapper(fileName);
		return load(wrapper);
	}

	@Override
	public ECGFile load(InputStream inputStream) throws ECGConverterException {
		RDTWrapper wrapper = new RDTWrapper(inputStream);
		return load(wrapper);
	}	
	
	protected ECGFile load(ECGFormatWrapper wrapper) throws ECGConverterException{
		RDTWrapper rdtWrapper = (RDTWrapper)wrapper;
		ecgFile = rdtWrapper.parse();
		return ecgFile;
	}
}