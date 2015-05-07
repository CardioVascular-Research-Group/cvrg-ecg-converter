package edu.jhu.icm.ecgFormatConverter.muse;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class MuseXMLLoader extends ECGFileLoader{
	
	@Override
	public ECGFile load(String filePath) throws IOException, JAXBException, ECGConverterException {
		MuseXMLWrapper museXMLWrap = new MuseXMLWrapper(filePath);
		return load(museXMLWrap);
	}
	
	@Override
	public ECGFile load(InputStream inputStream) throws IOException, JAXBException, ECGConverterException{
		MuseXMLWrapper museXMLWrap = new MuseXMLWrapper(inputStream);
		return load(museXMLWrap);
	}
	
	@Override
	protected ECGFile load(ECGFormatWrapper wrapper) throws ECGConverterException, IOException {

		MuseXMLWrapper museXMLWrap = (MuseXMLWrapper)wrapper;
		ecgFile = museXMLWrap.parse();
//		ecgFile.samplingRate = museXMLWrap.getSamplingRate();
//		ecgFile.samplesPerChannel = museXMLWrap.getSamplesPerChannel();
//		ecgFile.channels = museXMLWrap.getChannels();
//		ecgFile.data = museXMLWrap.getData();
//		ecgFile.aduGain = museXMLWrap.getAduGain();
//		ecgFile.numberOfPoints = museXMLWrap.getNumberOfPoints();
//		ecgFile.leadNamesList = museXMLWrap.getLeadNames();
		return ecgFile;
	}
}