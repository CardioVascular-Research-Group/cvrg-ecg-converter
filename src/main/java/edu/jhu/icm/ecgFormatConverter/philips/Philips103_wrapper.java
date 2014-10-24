package edu.jhu.icm.ecgFormatConverter.philips;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.sierraecg.schema.*;
import org.sierraecg.*;

import edu.jhu.icm.ecgFormatConverter.WrapperLoader;

public class Philips103_wrapper implements WrapperLoader{
	private Restingecgdata philipsECG;
	private DecodedLead[] leadData;
	private int[][] data;
	private int validChannels;
	private int numberOfPoints;
	private float samplingRate;
	private int sampleCount;
	private int aduGain = 200;
	
	// Initialization happens outside of the constructor since the methods called throw exceptions.
	public void init(String filePath) throws IOException, JAXBException {
		File inputFile = new File(filePath);
		
		PreprocessReturn ret = SierraEcgFiles.preprocess(inputFile);
		
		philipsECG = ret.getRestingEcgData();
		leadData = ret.getDecodedLeads();
	}
	
	public boolean parse() {
		if(this.isInitialized()) {
			Signalcharacteristics signalMetaData = philipsECG.getDataacquisition().getSignalcharacteristics();
			
			samplingRate = Float.valueOf(signalMetaData.getSamplingrate());
			validChannels = Integer.valueOf(signalMetaData.getNumberchannelsvalid());
			int previousSample = leadData[0].size();
			
			numberOfPoints = previousSample * validChannels;
			// Make sure all leads are the same size.
			for(int i=0; i<leadData.length; i++) {
				int currentSample = leadData[i].size();
				
				if(currentSample == previousSample) {
					sampleCount = currentSample;
				}
				else {
					sampleCount = 0;
					return false;
				}
			}
			
			// Parse out each lead and add it to the data.  Allocate each new row and column size
			// Use the Sierra library in order to get each lead row
			data = new int[validChannels][sampleCount];
			
			for(int i=0; i<leadData.length; i++) {
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
		return validChannels;
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
		if(philipsECG != null) {
			return true;
		}
		
		return false;
	}

	public Restingecgdata getPhilipsECG() {
		return philipsECG;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}
	
}
