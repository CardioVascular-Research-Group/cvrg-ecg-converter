package edu.jhu.icm.ecgFormatConverter.muse;

import java.util.ArrayList;

import edu.jhu.icm.ecgFormatConverter.WrapperLoader;
import edu.jhu.icm.parser.MuseBase64Parser;

public class MuseXML_wrapper implements WrapperLoader{
	private String fileLocation = "";
	private MuseBase64Parser base64Parser;
	private ArrayList<int[]> leadData;
	private int[][] data;
	private int aduGain;
	private int channels; 
	private int allocatedChannels;
	private int numberOfPoints;
	private float samplingRate;
	private int sampleCount;
	private String museRawXML;
	
	// Initialization happens outside of the constructor since the methods called throw exceptions.
	public MuseXML_wrapper () {
		base64Parser = new MuseBase64Parser();
		leadData = new ArrayList<int[]>();
	}
	
	public boolean parse(String filePath) {
		fileLocation = filePath;
		
		try {
			base64Parser.parse(fileLocation);
		
			samplingRate = base64Parser.getSamplingRate();
			leadData = base64Parser.getDecodedData();
			channels = leadData.size();
			allocatedChannels = base64Parser.getAllocatedChannels();
			numberOfPoints = base64Parser.getNumberOfPoints();
			int[] singleLead = leadData.get(0);
			int previousSample = singleLead.length;
			
			// Make sure all leads are the same size.
			for(int i=0; i<leadData.size(); i++) {
				int currentSample = leadData.get(i).length;
				
				if(currentSample == previousSample) {
					sampleCount = currentSample;
				}
				else {
					sampleCount = 0;
					return false;
				}
			}
			
			// Parse out each lead and add it to the data.  Allocate each new row and column size in a similar way to the GEMuseWrapper
			// Use the Sierra library in order to get each lead row
			data = new int[channels][sampleCount];
			
			for(int i=0; i<leadData.size(); i++) {
				data[i] = leadData.get(i);
			}
			
			aduGain = base64Parser.getAduGain();
			museRawXML = base64Parser.getInitialXML();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	@Override
	public int[][] getData() {
		return data;
	}
	
	@Override
	public int getChannels() {
		return channels;
	}
	
	@Override
	public float getSamplingRate() {
		return samplingRate;
	}
	
	@Override
	public int getAduGain() {
		return aduGain;
	}

	public int getAllocatedChannels() {
		return allocatedChannels;
	}

	@Override
	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	@Override
	public int getSamplesPerChannel() {
		return sampleCount;
	}
	
	public String getMuseXML() {
		return museRawXML;
	}
	
}
