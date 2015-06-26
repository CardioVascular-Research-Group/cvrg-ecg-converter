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
/**
* @author Chris Jurado
*/
import java.io.File;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.hl7.HL7AecgWriter;
import edu.jhu.icm.ecgFormatConverter.muse.MuseTXTWriter;
import edu.jhu.icm.ecgFormatConverter.rdt.RDTWriter;
import edu.jhu.icm.ecgFormatConverter.wfdb.WFDBWriter;
import edu.jhu.icm.enums.DataFileFormat;

public class ECGFormatWriter {

	public File writeToFile(DataFileFormat outputFormat, String outputPath, String subjectId, ECGFileData ecgFile){
		ECGFileWriter writer = null;
		try {
			writer = buildWriter(outputFormat);
		} catch (ECGConverterException e) {
			e.printStackTrace();
		}
		return writer.writeToFile(outputPath, subjectId, ecgFile);
	}
	
	public byte[] writeToByteArray(DataFileFormat outputFormat, ECGFileData ecgFile, String subjectId){
		ECGFileWriter writer = null;
		try {
			writer = buildWriter(outputFormat);
		} catch (ECGConverterException e) {
			e.printStackTrace();
		}
		return writer.writeToByteArray(subjectId, ecgFile);
	}
	
	private ECGFileWriter buildWriter(DataFileFormat outputFormat) throws ECGConverterException{
		ECGFileWriter writer = null;
		switch(outputFormat) {
			case RDT:		writer = new RDTWriter();					break;
			case HL7:		writer = new HL7AecgWriter();				break;
			case WFDB: 		// defaults to sub-format 16  fallthrough
			case WFDB_16:	writer = new WFDBWriter(16);				break;
			case WFDB_61:	writer = new WFDBWriter(61);				break;
			case WFDB_212:	writer = new WFDBWriter(212);				break;
			case GEMUSE:	writer = new MuseTXTWriter();				break;
			default:		throw new ECGConverterException("Format not supported.");	
		}
		return writer;
	}
}