package edu.jhu.icm.ecgFormatConverter.utility;
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
* @author Chris Jurado
*/
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ConverterUtility {
	
	public static String PROPS_FILE = "converter.properties";
	public static String TEMP_FOLDER = "temp.folder.path";
	public static String WFDB_FILE_PATH = "wfdb.file.path";
	
	private static Properties props;
	
	private static Logger log = Logger.getLogger(ConverterUtility.class);

	public static String addSeparator(String filePath){
		if(!filePath.endsWith(File.separator)){
			filePath = filePath + File.separator;
		}
		return filePath;
	}
	
	private static Properties getProperties(){
		if(props == null || props.isEmpty()){
			try {
				props = new Properties();
				InputStream resourceStream = Thread.currentThread().getClass().getResourceAsStream('/'+PROPS_FILE);
				props.load(resourceStream);
			} catch (IOException e) {
				log.error("Unable to load properties file. Please try again.");
				e.printStackTrace();
			}
		}
		return props;
	}
	
	public static String getProperty(String key){
		String value = getProperties().getProperty(key);
		if(value != null && !value.isEmpty()){
			return addSeparator(value);	
		}
		return value;
	}
	
	public static String getSubjectIdFromFilename(String filepath){
		String[] path = filepath.split(File.separator);
		String filename = path[path.length - 1];
		String name = filename.split("\\.")[0];
		return name;
	}
}