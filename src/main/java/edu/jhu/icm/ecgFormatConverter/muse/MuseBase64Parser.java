package edu.jhu.icm.ecgFormatConverter.muse;
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
* @author Mike Shipway, Chris Jurado
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class MuseBase64Parser {
	
	private StringBuilder initialXML = new StringBuilder();
	private ArrayList<String> base64Strings;
	private ArrayList<int[]> decodedData;
	private int samplingRate = 0;
	private int aduGain;
	private int allocatedChannels = 0;
	private int numberOfPoints = 0;
	private ArrayList<String> leadNames;
	
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
	
	public void parse(BufferedReader xmlBuf) throws IOException, JDOMException{
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
	
	public void parse(InputStream inputStream) throws IOException, JDOMException{
		BufferedReader xmlBuf = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
		parse(xmlBuf);
	}
	
	public void parse(String fileName) throws FileNotFoundException, IOException, JDOMException {
		File xmlFile = new File(fileName);	
		BufferedReader xmlBuf = new BufferedReader(new FileReader(xmlFile));
		parse(xmlBuf);
	}
	
	private void retrieveWaveformData() throws JDOMException {

			Document xmlDoc = buildDOM(this.initialXML.toString());
			Element rootElement = xmlDoc.getRootElement();
			List waveformElements = rootElement.getChildren("Waveform");
			
			// traverse through each occurance of the Waveform element to find the one the Rhythm type
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
							leadNames = new ArrayList<String>();
							
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
								
								Element leadID = leadData.getChild("LeadID");
								if(leadID != null){
									leadNames.add(leadID.getText().toUpperCase());
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
		leadNames.add(2, "III");

		// lead aVR = -(I + II)/2
		for (int i = 0; i < leadAVR.length; i++) {
			leadAVR[i] = -((leadI[i] + leadII[i]) / 2);
		}
		
		decodedData.add(leadAVR);
		leadNames.add(3, "AVR");

		// lead aVL = I - II/2
		for (int i = 0; i < leadAVL.length; i++) {
			leadAVL[i] = ((leadI[i] - leadIII[i]) / 2);
		}
		
		decodedData.add(leadAVL);
		leadNames.add(4, "AVL");

		// lead aVF = II - I/2
		for (int i = 0; i < leadAVF.length; i++) {
			leadAVF[i] = ((leadII[i] + leadIII[i]) / 2);
		}
		
		decodedData.add(leadAVF);
		leadNames.add(5, "AVF");
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
	public static Document buildDOM(String xmlDocAsString){
		Document doc = null;
	    SAXBuilder builder = new SAXBuilder();
	    Reader stringreader = new StringReader(xmlDocAsString);
	    try {
	    	doc = builder.build(stringreader);
	    } catch(IOException ioex) {
	    	ioex.printStackTrace();
	    } catch (JDOMException e) {
			e.printStackTrace();
		}
	    return doc;
	}

	public int getAllocatedChannels() {
		return allocatedChannels;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public ArrayList<String> getLeadNames() {
		return leadNames;
	}	
}