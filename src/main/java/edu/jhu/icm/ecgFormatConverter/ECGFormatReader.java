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
import java.io.InputStream;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.hl7.HL7AecgLoader;
import edu.jhu.icm.ecgFormatConverter.muse.MuseTXTLoader;
import edu.jhu.icm.ecgFormatConverter.muse.MuseXMLLoader;
import edu.jhu.icm.ecgFormatConverter.philips.Philips103Loader;
import edu.jhu.icm.ecgFormatConverter.philips.Philips104Loader;
import edu.jhu.icm.ecgFormatConverter.rdt.RDTLoader;
import edu.jhu.icm.ecgFormatConverter.schiller.SchillerLoader;
import edu.jhu.icm.ecgFormatConverter.wfdb.WFDBLoader;
import edu.jhu.icm.enums.DataFileFormat;

public class ECGFormatReader {

	//All formats
	public ECGFileData read(DataFileFormat inputFormat, String fileName){
		System.out.println("DEBUG: ECGFormatReader.read(" + inputFormat + "," + fileName);
		ECGFileLoader loader = createLoader(inputFormat);
		return loader.load(fileName);
	}
	
	//Non-WFDB formats
	public ECGFileData read(DataFileFormat inputFormat, InputStream dataStream){
		System.out.println("DEBUG: ECGFormatReader.read(" + inputFormat + ",<InputStream>)");
		ECGFileLoader loader = null;
		ECGFileData ecgFile = null;
		loader = createLoader(inputFormat);
		System.out.println("DEBUG: ECGFormatReader.read loader is null:" + (loader == null));
		ecgFile = loader.load(dataStream);
		return ecgFile;
	}

	//WFDB format
	public ECGFileData read(DataFileFormat inputFormat, InputStream dataStream, InputStream headerStream, String subjectId){
		WFDBLoader loader = null;
		try{
			switch(inputFormat) {
				default:	throw new ECGConverterException("Unsupported Header File Format");	
				case WFDB:					//fallthrough
				case WFDB_16:				//fallthrough
				case WFDB_61:				//fallthrough
				case WFDB_212: 				loader = createWFDBLoader();	break;
			}
		}catch(ECGConverterException e){
			e.printStackTrace();
		}
		
		loader.setHeaderStream(headerStream);
		loader.setRecordName(subjectId);
		return loader.load(dataStream);
	}
	
	private ECGFileLoader createLoader(DataFileFormat inputFormat){
		System.out.println("DEBUG: ECGFormatReader.createLoader(" + inputFormat + ")");
		ECGFileLoader loader = null;
		try{
			switch(inputFormat) {
				case RDT:					loader = new RDTLoader();				break;
				case HL7:   				loader = new HL7AecgLoader();			break;
				case WFDB:					//fallthrough
				case WFDB_16:				//fallthrough
				case WFDB_61:				//fallthrough
				case WFDB_212: 				loader = createWFDBLoader();	
																					break;
				case GEMUSE:				loader = new MuseTXTLoader();			break;
				case MUSEXML:				loader = new MuseXMLLoader();			break;
				case PHILIPS103:			loader = new Philips103Loader();		break;
				case PHILIPS104:			loader = new Philips104Loader();		break;
				case SCHILLER:				loader = new SchillerLoader();			break;
				default:					throw new ECGConverterException("Format not supported.");	
			}
		}catch (ECGConverterException e){
			e.printStackTrace();
		}
		return loader;
	}
	
	private WFDBLoader createWFDBLoader(){
		return new WFDBLoader();
	}
}