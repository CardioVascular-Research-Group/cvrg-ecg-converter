package edu.jhu.icm.ecgFormatConverter.philips;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class Philips104Loader extends ECGFileLoader{

	private Object philipsRestingecgdata;
	
	@Override
	public ECGFile load(InputStream inputStream) throws IOException, JAXBException, ECGConverterException {
		Philips104Wrapper philipsWrap = new Philips104Wrapper(inputStream);
		return load(philipsWrap);
	}

	@Override
	public ECGFile load(String filePath) throws IOException, JAXBException, ECGConverterException {
		Philips104Wrapper philipsWrap = new Philips104Wrapper(filePath);
		return load(philipsWrap);
	}

	@Override
	protected ECGFile load(ECGFormatWrapper wrapper) throws ECGConverterException, IOException {
		Philips104Wrapper philipsWrap = (Philips104Wrapper) wrapper;
		ecgFile = philipsWrap.parse();
		return ecgFile;
	}

	public Object getPhilipsRestingecgdata() {
		return philipsRestingecgdata;
	}
}