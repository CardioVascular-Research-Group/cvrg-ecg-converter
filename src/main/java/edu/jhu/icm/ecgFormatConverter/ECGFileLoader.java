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
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.enums.DataFileFormat;
import edu.jhu.icm.enums.LeadEnum;

public abstract class ECGFileLoader {

	protected ECGFile ecgFile = new ECGFile();
	public DataFileFormat inputFormat;
	
	public abstract ECGFile load(InputStream inputStream) throws IOException, JAXBException, ECGConverterException;
	
	public abstract ECGFile load(String filePath) throws IOException, JAXBException, ECGConverterException;
	
	protected abstract ECGFile load(ECGFormatWrapper wrapper) throws ECGConverterException, IOException;
	
	protected void setLeadNames(List<String> leadNames) throws ECGConverterException, IOException {
		String leadNamesOut = null;
		
		if(leadNames != null){
			boolean leadNamesOK = true;
			String lName = null;
			try{
				for (Iterator<String> iterator = leadNames.iterator(); iterator.hasNext();) {
					lName = iterator.next();
					if(LeadEnum.valueOf(lName) == null){
						leadNamesOK = false;
						break;
					}
				}
			}catch (Exception e){
				leadNamesOK = false;
				throw new ECGConverterException("Lead not found: " + lName);
			}
			
			if(!leadNamesOK){
				if(ecgFile.channels == 15){
					switch (inputFormat) {
						case MUSEXML:
						case PHILIPS103:
						case PHILIPS104:
							leadNamesOut = "I,II,III,aVR,aVL,aVF,V1,V2,V3,V4,V5,V6,V3R,V4R,V7";
							break;
						default:
							leadNamesOut = "I,II,III,aVR,aVL,aVF,V1,V2,V3,V4,V5,V6,VX,VY,VZ";
							break;
					}
				}else if(ecgFile.channels == 12){
					leadNamesOut = "I,II,III,aVR,aVL,aVF,V1,V2,V3,V4,V5,V6";
				}
			}else{
				StringBuilder sb = new StringBuilder();
				for (String l : leadNames) {
					sb.append(l).append(',');
				}
				sb.deleteCharAt(sb.length()-1);
				leadNamesOut = sb.toString();
			}
		}
		
		this.ecgFile.leadNames = leadNamesOut;
	}

	public ECGFile getECGFile(){
		return ecgFile;
	}
}