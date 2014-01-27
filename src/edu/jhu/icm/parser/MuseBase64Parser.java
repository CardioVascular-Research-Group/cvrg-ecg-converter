package edu.jhu.icm.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cvrgrid.philips.codecs.Base64;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.codec.binary.StringUtils;

public class MuseBase64Parser {
	
	private StringBuilder initialXML = new StringBuilder();
	private ArrayList<String> base64Strings;
	private ArrayList<int[]> decodedData;
	private int samplingRate = 0;
	private int aduGain;
	private int allocatedChannels = 0;
	private int numberOfPoints = 0;
	
	
	public MuseBase64Parser() {
		base64Strings = new ArrayList<String>();
		decodedData = new ArrayList<int[]>();
	}
	
	public String getInitialXML() {
		return initialXML.toString(); 
	}
	
	public ArrayList<String> getBase64Strings() {
		return base64Strings;
	}
	
	public ArrayList<int[]> getDecodedData() {
		return decodedData;
	}
	
	public int getSamplingRate() {
		return samplingRate;
	}
	
	public int getAduGain() {
		return aduGain;
	}
	
	public void parse(String fileName) throws FileNotFoundException, IOException, JDOMException {
		File xmlFile = new File(fileName);
		
		BufferedReader xmlBuf = new BufferedReader(new FileReader(xmlFile));
		
		String oneLine = xmlBuf.readLine();
		while(oneLine != null) {
			// Since there were parsing problems when using a DTD instead of schema (mainly, we don't have the DTD and no online location was given),
			// the DOCTYPE tag that declares it will be taken out.
			if(!(oneLine.contains("!DOCTYPE"))) {
				initialXML.append(oneLine);
			}
			oneLine = xmlBuf.readLine();
		}
		
		xmlBuf.close();
		
		this.retrieveWaveformData();
		this.decodeWaveformData();
		
	}
	
	private void retrieveWaveformData() throws JDOMException {

			Document xmlDoc = build(this.initialXML.toString());
			Element rootElement = xmlDoc.getRootElement();
			List waveformElements = rootElement.getChildren("Waveform");
			
			// Since the DTD was unable to be found, the XML had to be traversed one level at a time
			if(!(waveformElements.isEmpty())) {
				Iterator waveformIter = waveformElements.iterator();
				while(waveformIter.hasNext()) {
					Element nextWaveform = (Element)waveformIter.next();
					Element waveformType = nextWaveform.getChild("WaveformType");
					
					// Check to make sure there are valid waveforms, then get each WaveFormData tag, which is a child of a LeadData tag
					if((waveformType != null) && (waveformType.getText().equals("Rhythm"))) {
						
						// get the Sampling Rate of the waveform in the process
						samplingRate = Integer.valueOf(nextWaveform.getChild("SampleBase").getText());
						
						List leadDataList = nextWaveform.getChildren("LeadData");
						
						if(!(leadDataList.isEmpty())) {
							
							allocatedChannels = leadDataList.size();
							
							Iterator leadIter = leadDataList.iterator();
							while(leadIter.hasNext()) {
								Element leadData = (Element)leadIter.next();
								
								// Get the number to be used to calculate the ADU gain
								Element leadAmpUnitsPerBit = leadData.getChild("LeadAmplitudeUnitsPerBit");
								if(leadAmpUnitsPerBit != null) {
									double leadAmp = Double.valueOf(leadAmpUnitsPerBit.getText());
									aduGain = (int)Math.round(1.0/leadAmp*1000);
								}
								
								Element waveformData = leadData.getChild("WaveFormData");
								if(waveformData != null) {
									base64Strings.add(waveformData.getText());
								}
								
								if(numberOfPoints == 0){
									Element sampleCount = leadData.getChild("LeadSampleCountTotal");
									numberOfPoints = Integer.valueOf(sampleCount.getText()) * allocatedChannels;
								}
							}
						}
					}
				}
			}

	}
	
	/**
	 * Some of this code is based on code from the jsierraecg library by Christopher A. Watford.  This code accesses
	 * the Base64.java file that has been pulled from that library.
	 * 
	 * Check the Base64.java file for Copyright information
	 * @throws IOException 
	 * 
	 */
	private void decodeWaveformData() throws IOException {
		boolean recheckNumberOfPoints = false;
		for(String base64String : base64Strings) {
			ArrayList<Integer> intList = new ArrayList<Integer>();
			
				byte[] encodedBytes = base64String.getBytes();
				
				byte[] uncodedDataByte = Base64.decode( encodedBytes );
				ByteBuffer bb = ByteBuffer.allocate(2);
				bb.order(ByteOrder.nativeOrder());
				for (int t = 0, len = uncodedDataByte.length; t < len; t+=2) {
					double doubleVal = (double)(((uncodedDataByte[t+1])<<8) | (uncodedDataByte[t] & 0xFF));
					//doubleVal = doubleVal * 4.88;
					int intVal = (int)doubleVal;
					intList.add(intVal);
				}
				
	            int[] payload = new int[intList.size()];
	            
	            for(int i=0; i<payload.length; i++) {
	            	payload[i] = intList.get(i);
	            }
	            
	            decodedData.add(payload);
	            
	            if(decodedData.size() == 2) {
	            	reconstructLeads();
	            	recheckNumberOfPoints = true;
	            }
				
		}
		
		if(recheckNumberOfPoints){
			int missingLead = decodedData.size() - allocatedChannels;
			numberOfPoints += (numberOfPoints/allocatedChannels) * missingLead;
		}
	}
	
	/**
	 * Reconstructs leads III, aVR, aVL, and aVF.  These are not stored directly in the Muse XML file
	 * but are derived from other leads.
	 * 
	 * This does not need to be multiplied by any value, since it will already be using previously decoded values.
	 */
	private void reconstructLeads() {
		int[] leadI = decodedData.get(0);
		int[] leadII = decodedData.get(1);
		int[] leadIII = new int[decodedData.get(0).length];
		int[] leadAVR = new int[decodedData.get(0).length];
		int[] leadAVL = new int[decodedData.get(0).length];
		int[] leadAVF = new int[decodedData.get(0).length];
		
		
		// lead III = II - I
		for (int i = 0; i < leadIII.length; i++) {
			leadIII[i] = leadII[i] - leadI[i];
		}
		
		decodedData.add(leadIII);

		// lead aVR = -(I + II)/2
		for (int i = 0; i < leadAVR.length; i++) {
			leadAVR[i] = -((leadI[i] + leadII[i]) / 2);
		}
		
		decodedData.add(leadAVR);

		// lead aVL = I - II/2
		for (int i = 0; i < leadAVL.length; i++) {
			leadAVL[i] = ((leadI[i] - leadIII[i]) / 2);
		}
		
		decodedData.add(leadAVL);

		// lead aVF = II - I/2
		for (int i = 0; i < leadAVF.length; i++) {
			leadAVF[i] = ((leadII[i] + leadIII[i]) / 2);
		}
		
		decodedData.add(leadAVF);
		
		
	}
	
	/**
	 * Helper method to build a <code>jdom.org.Document</code> from an 
	 * XML document represented as a String
	 * @param  xmlDocAsString  <code>String</code> representation of an XML
	 *         document with a document declaration.
	 *         e.g., <?xml version="1.0" encoding="UTF-8"?>
	 *                  <root><stuff>Some stuff</stuff></root>
	 * @return Document from an XML document represented as a String
	 */
	private static Document build(String xmlDocAsString) 
	        throws JDOMException {
		Document doc = null;
	    SAXBuilder builder = new SAXBuilder();
	    Reader stringreader = new StringReader(xmlDocAsString);
	    try {
	    	doc = builder.build(stringreader);
	    } catch(IOException ioex) {
	    	ioex.printStackTrace();
	    }
	    return doc;
	}

	public int getAllocatedChannels() {
		return allocatedChannels;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}
	
}


