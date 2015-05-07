package edu.jhu.icm.ecgFormatConverter.philips;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class Philips103Loader extends ECGFileLoader{

	@Override
	public ECGFile load(InputStream inputStream) throws IOException, JAXBException, ECGConverterException {
		Philips103Wrapper philipsWrap = new Philips103Wrapper(inputStream);
		return load(philipsWrap);
	}

	@Override
	public ECGFile load(String filePath) throws IOException, JAXBException, ECGConverterException {
		Philips103Wrapper philipsWrap = new Philips103Wrapper(filePath);
		return load(philipsWrap);
	}

	@Override
	protected ECGFile load(ECGFormatWrapper wrapper) throws ECGConverterException, IOException {
		Philips103Wrapper philipsWrap = (Philips103Wrapper)wrapper;
		ecgFile = philipsWrap.parse();
		return ecgFile;
	}
}