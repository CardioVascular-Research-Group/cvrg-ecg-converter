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
/*
 * Author: Mike Shipway, Chris Jurado
 */
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import edu.jhu.icm.enums.DataFileFormat;
import edu.jhu.icm.enums.LeadEnum;

public abstract class ECGFormatWrapper {
	
	protected ECGFileData ecgFile;
	protected String filePath;
	protected InputStream inputStream;

	public abstract ECGFileData parse();
	
	protected abstract void init(String filename);
	
	protected abstract void init(InputStream inputStream);

	protected abstract DataFileFormat getFormat();

	public float getSamplingRate(){
		return ecgFile.samplingRate;
	}

	public int getSamplesPerChannel(){
		return ecgFile.samplesPerChannel;
	}

	public int getChannels(){
		return ecgFile.channels;
	}

	public int[][] getData(){
		return ecgFile.data;
	}
	
	protected String extractLeadNames(List<String> leadNames, int channels){
		String leadNamesOut = null;
		
		if(leadNames != null){
			boolean leadNamesOK = true;
			String lName = null;

			for (Iterator<String> iterator = leadNames.iterator(); iterator.hasNext();) {
				lName = iterator.next();
				if(LeadEnum.valueOf(lName.toUpperCase()) == null){
					leadNamesOK = false;
					break;
				}
			}
			
			System.out.println("DEBUG: ECGFormatWrapper.extractLeadNames.channels = " + channels);
			
			if(!leadNamesOK){
				if(channels == 15){
					switch (this.getFormat()) {
						case MUSEXML:
						case PHILIPS103:
						case PHILIPS104:
							leadNamesOut = "I,II,III,aVR,aVL,aVF,V1,V2,V3,V4,V5,V6,V3R,V4R,V7";
							break;
						default:
							leadNamesOut = "I,II,III,aVR,aVL,aVF,V1,V2,V3,V4,V5,V6,VX,VY,VZ";
							break;
					}
				}else if(channels == 12){
					leadNamesOut = "I,II,III,aVR,aVL,aVF,V1,V2,V3,V4,V5,V6";
					System.out.println("DEBUG: ECGFormatWrapper.extractLeadNames.getFormat() = " + this.getFormat());
				}
			}else{
				StringBuilder sb = new StringBuilder();
				for (String l : leadNames) {
					if(l.startsWith("AV")){
						l = l.replace('A', 'a');
					}
					sb.append(l).append(',');
				}
				sb.deleteCharAt(sb.length()-1);
				leadNamesOut = sb.toString();
			}
		}
		
		return leadNamesOut;
	}
}