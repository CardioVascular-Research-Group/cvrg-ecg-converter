package edu.jhu.icm.ecgFormatConverter.schiller;

import org.cvrgrid.schiller.jaxb.beans.ComXiriuzSemaXmlSchillerEDISchillerEDI;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;

public class SchillerECGFileData extends ECGFileData {

	public ComXiriuzSemaXmlSchillerEDISchillerEDI schillerEDI;
	
	@Override
	public String toString(){
		String superString = super.toString();
		StringBuilder string = new StringBuilder();
		string.append(superString);
		string.append("schillerEDI + " + schillerEDI + "/n");
		return string.toString();
	}
}
