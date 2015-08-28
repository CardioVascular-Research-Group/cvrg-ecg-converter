package edu.jhu.icm.ecgFormatConverter.muse;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;

public class MuseXMLECGFileData extends ECGFileData {

	public String museRawXML;
	
	@Override
	public String toString(){
		String superString = super.toString();
		StringBuilder string = new StringBuilder();
		string.append(superString);
		string.append("raw XML + " + museRawXML + "/n");
		return string.toString();
	}
	
}
