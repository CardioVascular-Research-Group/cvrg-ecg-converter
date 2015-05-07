package edu.jhu.icm.ecgFormatConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;

public abstract class ECGFormatWrapper {
	
	protected ECGFile ecgFile;
	protected String filePath;
	protected InputStream inputStream;

	public abstract ECGFile parse() throws IOException, ECGConverterException;
	
	protected abstract void init(String filename) throws ECGConverterException, IOException, JAXBException;
	
	protected abstract void init(InputStream inputStream) throws ECGConverterException, IOException, JAXBException;

	/**
	 * Get the frequency
	 * */
	public float getSamplingRate(){
		return ecgFile.samplingRate;
	}
	/**
	 * Get the number of points per lead
	 * */
	public int getSamplesPerChannel(){
		return ecgFile.samplesPerChannel;
	}
	/**
	 * Get the number of leads
	 * */
	public int getChannels(){
		return ecgFile.channels;
	}
	/**
	 * Get the ECG point matrix
	 * */
	public int[][] getData(){
		return ecgFile.data;
	}
	
	/**
	 * Get the lead names in order. <br>
	 * <br>
	 * @return List of extracted lead names from original file. 
	 * */
	public List<String> getLeadNames(){
		return ecgFile.leadNamesList;
	}
}