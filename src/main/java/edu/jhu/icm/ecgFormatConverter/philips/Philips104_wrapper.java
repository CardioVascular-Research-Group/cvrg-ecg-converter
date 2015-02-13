package edu.jhu.icm.ecgFormatConverter.philips;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.cvrgrid.philips.DecodedLead;
import org.cvrgrid.philips.PreprocessReturn;
import org.cvrgrid.philips.SierraEcgFiles;
import org.cvrgrid.philips.jaxb.beans.Leadmeasurement;
import org.cvrgrid.philips.jaxb.beans.Restingecgdata;
import org.cvrgrid.philips.jaxb.beans.Signalcharacteristics;

import edu.jhu.icm.ecgFormatConverter.WrapperLoader;
//import org.sierraecg.schema.*;
//import org.sierraecg.*;

public class Philips104_wrapper implements WrapperLoader{
	private Restingecgdata philipsECG;
	private DecodedLead[] leadData;
	private int[][] data;
	private int validChannels;
	private int allocatedChannels;
	private int numberOfPoints;
	private float samplingRate;
	private int sampleCount;
	private int aduGain = 200;
	private List<String> leadNames;
	
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
			
			List<Leadmeasurement> leads = philipsECG.getInternalmeasurements().getLeadmeasurements().getLeadmeasurement();
			if(leads != null){
				leadNames = new ArrayList<String>();
				for (Leadmeasurement lead : leads) {
					leadNames.add(lead.getLeadname().toUpperCase());
				}
			}
			
			samplingRate = Float.valueOf(signalMetaData.getSamplingrate());
			
			allocatedChannels = signalMetaData.getNumberchannelsallocated().intValue(); // Method returns a BigInteger, so a conversion to int is required.
			validChannels = signalMetaData.getNumberchannelsvalid().intValue();
			
			int previousSample = leadData[0].size();
			
			numberOfPoints = previousSample * allocatedChannels;
			
			// Make sure all leads are the same size.
			for(int i=0; i<validChannels; i++) {
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
			data = new int[validChannels][sampleCount];
			
			for(int i=0; i<validChannels; i++) {
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

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public int getAllocatedChannels() {
		return allocatedChannels;
	}

	public Restingecgdata getPhilipsECG() {
		return philipsECG;
	}

	@Override
	public List<String> getLeadNames() {
		return leadNames;
	}
	
}
