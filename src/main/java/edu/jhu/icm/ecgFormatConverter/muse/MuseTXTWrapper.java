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
* @author Andre Vilardo, Chris Jurado
*/
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWrapper;

public class MuseTXTWrapper extends ECGFormatWrapper{

	private File geMuseFile = null;
	private InputStream geMuseFis = null;
	private DataInputStream geMuseDis;
	private BufferedReader bufferedReader;

	public MuseTXTWrapper(String filename) {
		ecgFile = new ECGFile();
		init(filename);
	}
	
	public MuseTXTWrapper(InputStream inputStream) {
		ecgFile = new ECGFile();
		init(inputStream);
	}

	protected void init(String filename) {
		this.geMuseFile = new File(filename);
		this.geMuseFis = null;
	}
	
	protected void init(InputStream inputStream){
		this.geMuseFis = inputStream;
		this.geMuseFile = null;
	}

	@Override
	public ECGFile parse() {
		validate();
		parseHeader();
		parseECGdata();
		return ecgFile;
	}

	private void validate() {
		try {
			if (geMuseFile != null) {
				if (!geMuseFile.exists()) {
					throw new ECGConverterException("Muse data file does not exist.");
				}

				long fileSize = geMuseFile.length();
				if (fileSize > Integer.MAX_VALUE) {
					throw new ECGConverterException("file size exceeding maximum int value.");
				}

				try {
					geMuseFis = new FileInputStream(geMuseFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new ECGConverterException(e.getStackTrace()
							.toString());
				}
			}
		} catch (ECGConverterException e) {
			e.printStackTrace();
		}
	}

	private void parseHeader(){

		try{
		    geMuseDis = new DataInputStream(geMuseFis);
		    bufferedReader = new BufferedReader(new InputStreamReader(geMuseDis));
		    String strLine;
		    String[] words;
		    ecgFile.scalingFactor = 1;

		    while ((strLine = bufferedReader.readLine()) != null)   {
		    	if(strLine.length() > 0) {
		    		words = strLine.split("\\s");
		    		ecgFile.samplingRate = 500; // 500 samples per second (Hz) fixed
		    		ecgFile.samplesPerChannel = Integer.parseInt(words[2]);
		    		ecgFile.channels = Integer.parseInt(words[4]);
		    		break;
		    	}
		    }
		    ecgFile.data = new int[ecgFile.channels][ecgFile.samplesPerChannel];

		}catch (IOException e){
			e.printStackTrace();
		}
	}

	private void parseECGdata(){
		try{
			int s = 0;
		    String strLine;
		    String[] numbers;
		    while ((strLine = bufferedReader.readLine()) != null){
		    	strLine = strLine.trim();
		    	if(strLine.length() > 0){
		    		numbers = strLine.split("\\s");
					for (int c = 0; c < ecgFile.channels; c++){
						short value = Short.parseShort(numbers[c]);
						ecgFile.data[c][s] = value;
					}
					s++;
		    	}
		    }
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				geMuseDis.close();
				geMuseFis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
}