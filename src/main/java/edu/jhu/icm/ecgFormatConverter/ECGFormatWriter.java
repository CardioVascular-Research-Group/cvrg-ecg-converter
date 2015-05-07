package edu.jhu.icm.ecgFormatConverter;

import java.io.File;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.hl7.HL7AecgWriter;
import edu.jhu.icm.ecgFormatConverter.rdt.RDTParser;
import edu.jhu.icm.ecgFormatConverter.rdt.RDTWriter;
import edu.jhu.icm.enums.DataFileFormat;

public class ECGFormatWriter {

//	public static void write(DataFileFormat outputFormat, String outputPath, String recordName, 
//			ECGFile ecgFile, boolean physicalFile) {
//
//		ECGFileWriter writer = null;
//		switch(outputFormat) {
//			case RDT:		writer = new RDTWriter();									break;
//			case HL7:		writer = new HL7AecgWriter();								break;
//			case WFDB: 		// defaults to sub-format 16  fallthrough
//			case WFDB_16:	writer = writeWFDB(outputPath, recordName, 16);				break;
//			case WFDB_61:	writer = writeWFDB(outputPath, recordName, 61);				break;
//			case WFDB_212:	writer = writeWFDB(outputPath, recordName, 212);			break;
//			case GEMUSE:	writer = write_geMuse(outputPath, recordName);				break;
//			default:		throw new ECGConverterException("Format not supported.");	
//		}
//		if(physicalFile){
//			writer.writeToFile(outputPath, recordName, ecgFile);
//		} else {
//			writer.writeToInputStream(recordName, ecgFile);
//		}
//	}
//	
//	private ECGFileWriter createWFDBWriter(int bits){
//		
//	}
}