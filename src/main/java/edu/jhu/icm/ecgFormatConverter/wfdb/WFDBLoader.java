package edu.jhu.icm.ecgFormatConverter.wfdb;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileLoader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class WFDBLoader extends ECGFileLoader{
	
	private String recordName = "DEFAULT";
	private InputStream headerStream = null;
	
	public WFDBLoader(){}

	public WFDBLoader(String fullFilePath) throws ECGConverterException, IOException{
		System.out.println("Let's get a record name from file " + fullFilePath);
		this.recordName = WFDBUtilities.getRecordName(fullFilePath);
	}

	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	public void setHeaderStream(InputStream headerStream) {
		this.headerStream = headerStream;
	}
	
	@Override
	public ECGFile load(InputStream inputStream) throws IOException, JAXBException, ECGConverterException {
		if(this.headerStream != null){
			WFDBWrapper wfdbWrap = new WFDBWrapper(headerStream, inputStream);
			System.out.println("IS Wrapper Created.");
			return load(wfdbWrap);
		} else{
			throw new ECGConverterException("Header Filestream not set.");
		}
	}

	@Override
	protected ECGFile load(ECGFormatWrapper wrapper) throws ECGConverterException, IOException {
		WFDBWrapper wfdbWrap = (WFDBWrapper)wrapper;
		System.out.println("FormatWrapper Wrapper Created.");
		ecgFile = wfdbWrap.parse();
		return ecgFile;
	}

	@Override
	public ECGFile load(String fullFilePath) throws IOException, JAXBException,	ECGConverterException {
		WFDBWrapper wfdbWrap = new WFDBWrapper(fullFilePath);
		System.out.println("File Wrapper Created.");
		return load(wfdbWrap);	
	}
}