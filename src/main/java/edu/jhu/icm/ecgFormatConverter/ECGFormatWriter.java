package edu.jhu.icm.ecgFormatConverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.hl7.HL7AecgWriter;
import edu.jhu.icm.ecgFormatConverter.muse.MuseTXTWriter;
import edu.jhu.icm.ecgFormatConverter.rdt.RDTWriter;
import edu.jhu.icm.ecgFormatConverter.wfdb.WFDBWriter;
import edu.jhu.icm.enums.DataFileFormat;

public class ECGFormatWriter {

	public File writeToFile(DataFileFormat outputFormat, String outputPath, String subjectId, ECGFile ecgFile) throws ECGConverterException, IOException {
		ECGFileWriter writer = buildWriter(outputFormat);
		return writer.writeToFile(outputPath, subjectId, ecgFile);
	}
	
	public InputStream writeToInputStream(DataFileFormat outputFormat, ECGFile ecgFile, String subjectId) throws ECGConverterException, IOException{
		ECGFileWriter writer = buildWriter(outputFormat);
		return writer.writeToInputStream(subjectId, ecgFile);
	}
	
	private ECGFileWriter buildWriter(DataFileFormat outputFormat) throws ECGConverterException, IOException{
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