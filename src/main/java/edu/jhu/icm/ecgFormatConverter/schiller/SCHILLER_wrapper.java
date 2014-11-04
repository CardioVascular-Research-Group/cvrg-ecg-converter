package edu.jhu.icm.ecgFormatConverter.schiller;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.cvrgrid.schiller.DecodedLead;
import org.cvrgrid.schiller.PreprocessReturn;
import org.cvrgrid.schiller.SchillerEcgFiles;
import org.cvrgrid.schiller.jaxb.beans.ComXiriuzSemaXmlSchillerEDISchillerEDI;

import edu.jhu.icm.ecgFormatConverter.WrapperLoader;
//import org.cvrgrid.schiller.jaxb.beans.Wavedata;

public class SCHILLER_wrapper implements WrapperLoader{
	private ComXiriuzSemaXmlSchillerEDISchillerEDI comXiriuzSemaXmlSchillerEDISchillerEDI;
	private DecodedLead[] leadData;
	private int[][] data;
	private int allocatedChannels;
	private int numberOfPoints;
	private float samplingRate;
	private int sampleCount;
	private int aduGain = 200;
	
	// Initialization happens outside of the constructor since the methods called throw exceptions.
	public void init(String filePath) throws IOException, JAXBException {
		File inputFile = new File(filePath);
		PreprocessReturn ret = SchillerEcgFiles.preprocess(inputFile);

		samplingRate = Float.valueOf(ret.getPrepSampleRate());
		comXiriuzSemaXmlSchillerEDISchillerEDI = ret.getComXiriuzSemaXmlSchillerEDISchillerEDI();
		leadData = ret.getDecodedLeads();
	}
	
	public boolean parse() {
		if(this.isInitialized()) {
			allocatedChannels = leadData.length;
			
			int previousSample = leadData[0].size();
			numberOfPoints = previousSample * allocatedChannels;
			
			// Make sure all leads are the same size.
			for(int i=0; i<allocatedChannels; i++) {
				int currentSample = leadData[i].size();
				if(currentSample == previousSample) {
					sampleCount = currentSample;
				} else {
					sampleCount = 0;
					return false;
				}
			}
			
			// Parse out each lead and add it to the data.  Allocate each new row and column size
			// Use the Sierra library in order to get each lead row
			data = new int[allocatedChannels][sampleCount];
			
			for(int i=0; i<allocatedChannels; i++) {
				for(int j=0; j<leadData[i].size(); j++) {
					data[i][j] = leadData[i].get(j);
				}
			}
			return true;
		}
		return false;
	}
	
	public int[][] getData() {
		return data;
	}
	
	public int getChannels() {
		return allocatedChannels;
	}
	
	public float getSamplingRate() {
		return samplingRate;
	}
	
	public int getSamplesPerChannel() {
		// TODO:  Take the size of the length of each DecodedLead, multiply that by the number of DecodedLeads
		// Not sure if the durationChannelProperty would be the correct thing to use here.  If it is then
		// just get that
		return sampleCount;
	}
	
	public int getAduGain() {
		return aduGain;
	}
	
	// for debugging use
	public boolean isInitialized() {
		if(comXiriuzSemaXmlSchillerEDISchillerEDI != null) {
			return true;
		}
		return false;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public int getAllocatedChannels() {
		return allocatedChannels;
	}

	public ComXiriuzSemaXmlSchillerEDISchillerEDI getComXiriuzSemaXmlSchillerEDISchillerEDI() {
		return comXiriuzSemaXmlSchillerEDISchillerEDI;
	}
	
}
