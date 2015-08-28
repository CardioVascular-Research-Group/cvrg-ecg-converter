package edu.jhu.icm.ecgFormatConverter.philips;

import org.cvrgrid.philips.jaxb.beans.Restingecgdata;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;

public class Philips104ECGFileData extends ECGFileData {

	public Restingecgdata restingecgdata;
	
	@Override
	public String toString(){
		String superString = super.toString();
		StringBuilder string = new StringBuilder();
		string.append(superString);
		string.append("Restingecgdata + " + restingecgdata + "/n");
		return string.toString();
	}
}
