package edu.jhu.icm.ecgFormatConverter.muse;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class MuseTXTLoader extends ECGFileLoader{
	
	@Override
	public ECGFile load(InputStream inputStream) throws IOException, JAXBException, ECGConverterException {
		MuseTXTWrapper geMuseWrap = new MuseTXTWrapper(inputStream);
		return load(geMuseWrap);
	}

	@Override
	public ECGFile load(String filePath) throws IOException, JAXBException, ECGConverterException {
		MuseTXTWrapper geMuseWrap = new MuseTXTWrapper(filePath);
		return load(geMuseWrap);
	}

	@Override
	protected ECGFile load(ECGFormatWrapper wrapper) throws ECGConverterException, IOException {
		MuseTXTWrapper geMuseWrap = (MuseTXTWrapper)wrapper;
		ecgFile = geMuseWrap.parse();
		return ecgFile;
	}
}