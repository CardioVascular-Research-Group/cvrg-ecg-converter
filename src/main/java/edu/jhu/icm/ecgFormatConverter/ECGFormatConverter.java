package edu.jhu.icm.ecgFormatConverter;
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
/** Loads ECG data from one of several file formats and 
 * writes it out in another file format  
 * 
 * @author Michael Shipway, Chris Jurado
 *
 */
import java.io.File;
import java.io.InputStream;

import edu.jhu.icm.ecgFormatConverter.utility.ConverterUtility;
import edu.jhu.icm.enums.DataFileFormat;

public class ECGFormatConverter { 
	
    private ECGFileData loadFileData(String absoluteFileName, DataFileFormat format){
    	ECGFileData ecgFile = null;
    	ECGFormatReader reader = new ECGFormatReader();
		ecgFile = reader.read(format, absoluteFileName);
		return ecgFile;
    }
    
    private ECGFileData loadWFDBFilesData(String headerFileName, String dataFileName, DataFileFormat format){
    	ECGFileData ecgFile = null;
    	ECGFormatReader reader = new ECGFormatReader();
		String subjectId = ConverterUtility.getSubjectIdFromFilename(headerFileName);
		ecgFile = reader.read(format, subjectId);
		return ecgFile;
    }
    
    private File writeFile(DataFileFormat format, String subjectId, ECGFileData ecgFile){
    	ECGFormatWriter writer = new ECGFormatWriter();
		String outputFolder = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
		return writer.writeToFile(format, outputFolder, subjectId, ecgFile);
    }
    
    private ECGFileData loadWFDBInputStreams(InputStream headerInputStream, InputStream dataInputStream, String subjectId, DataFileFormat format){
    	ECGFileData ecgFile = null;
		ECGFormatReader reader = new ECGFormatReader();
		ecgFile = reader.read(format, dataInputStream, headerInputStream, subjectId);

		return ecgFile;
    }
    
    private ECGFileData loadInputStream(InputStream inputStream, DataFileFormat format){
    	ECGFileData ecgFile = null;
		ECGFormatReader reader = new ECGFormatReader();
		ecgFile = reader.read(format, inputStream);
		return ecgFile;
    }
	
	public File convertWFDBFilesToFile(String WFDBSourceFilePath, String outputFolder, DataFileFormat outputFormat, String subjectId){
		String header = ConverterUtility.addSeparator(WFDBSourceFilePath) + subjectId + ".hea";
		String data = ConverterUtility.addSeparator(WFDBSourceFilePath) + subjectId + ".dat";
		ECGFileData ecgFile = loadWFDBFilesData(header, data, DataFileFormat.WFDB);
		return writeFile(outputFormat, subjectId, ecgFile);
	}

	public File convertWFDBInputStreamsToFile(InputStream headerInputStream, InputStream dataInputStream, String subjectId, DataFileFormat outputFormat){
		ECGFileData ecgFile = loadWFDBInputStreams(headerInputStream, dataInputStream, subjectId, DataFileFormat.WFDB);
		return writeFile(outputFormat, subjectId, ecgFile);
	}
	
	public byte[] convertWFDBInputStreamsToByteArray(InputStream headerInputStream, InputStream dataInputStream, String subjectId, DataFileFormat outputFormat){
		ECGFileData ecgFile = loadWFDBInputStreams(headerInputStream, dataInputStream, subjectId, DataFileFormat.WFDB);
    	ECGFormatWriter writer = new ECGFormatWriter();
    	byte[] fileData = null;
    	fileData = writer.writeToByteArray(outputFormat, ecgFile, "subjectId");
    	return fileData;
	}
	
	public File convertFileToWFDBFiles(String absoluteFileName, DataFileFormat inputFormat, String subjectId, String outputFolder){
		ECGFileData ecgFile = loadFileData(absoluteFileName, inputFormat);
		ECGFormatWriter writer = new ECGFormatWriter();
		return writer.writeToFile(DataFileFormat.WFDB, outputFolder, subjectId, ecgFile);
	}

	public File convertInputStreamToWFDBFiles(InputStream inputStream, DataFileFormat inputFormat, String subjectId, String outputFolder){
		ECGFileData ecgFile = loadInputStream(inputStream, inputFormat);
		ECGFormatWriter writer = new ECGFormatWriter();
		return writer.writeToFile(DataFileFormat.WFDB, outputFolder, subjectId, ecgFile);
	}
	
	public File convertFileToFile(String inputFilename, DataFileFormat inputFormat, DataFileFormat outputFormat, String subjectId){
		ECGFileData ecgFile = loadFileData(inputFilename, inputFormat);
		return writeFile(outputFormat, subjectId, ecgFile);
	}

	public File convertInputStreamToFile(InputStream inputStream, DataFileFormat inputFormat, DataFileFormat outputFormat, String subjectId){
		ECGFileData ecgFile = loadInputStream(inputStream, inputFormat);
		return writeFile(outputFormat, subjectId, ecgFile);
	}
	
	public File convertWFDBFileToWFDBFile(String WFDBSourceFilePath, String WFDBoutputFilePath, DataFileFormat inputFormat, DataFileFormat outputFormat, String subjectId){
		String header = ConverterUtility.addSeparator(WFDBSourceFilePath) + subjectId + ".hea";
		String data = ConverterUtility.addSeparator(WFDBSourceFilePath) + subjectId + ".dat";
		ECGFileData ecgFile = loadWFDBFilesData(header, data, inputFormat);
		ECGFormatWriter writer = new ECGFormatWriter();
		return writer.writeToFile(outputFormat, WFDBoutputFilePath, subjectId, ecgFile);
	}
}