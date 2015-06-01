package edu.jhu.icm.ecgFormatConverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;

public abstract class ECGFileWriter{
	
	public abstract File writeToFile(String outputPath, String recordName, ECGFile ecgFile) throws ECGConverterException, IOException;
	
	public abstract InputStream writeToInputStream(String recordName, ECGFile ecgFile) throws IOException, ECGConverterException;
}