package edu.jhu.icm.test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.junit.Test;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFormatReader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWriter;
import edu.jhu.icm.ecgFormatConverter.wfdb.WFDBUtilities;
import edu.jhu.icm.enums.DataFileFormat;

public class WriterTest extends TestCase{
	
	private final String HL7AECG_INPUT_FILE_PATH = "/hl7aecg-Example2.xml";
	private final String MUSE_TXT_INPUT_FILE_PATH = "/J123456_10sec.txt";
	private final String RDT_INPUT_FILE_PATH = "/DEM10000026.rdt";
	private final String WFDB_HEADER_FILE_PATH = "/jhu315.hea";
	private final String WFDB_DATA_FILE_PATH = "/jhu315.dat";
	private final String TEMP_FOLDER = "temp.folder.path";

    public WriterTest(String testName)
    {
        super(testName);
    }
    
    private boolean checkForFile(String filename){

//		try {
//			Properties properties = WFDBUtilities.getProperties();
//			String outputFolder = properties.getProperty(TEMP_FOLDER);
//	    	File directory = new File(outputFolder);
//	    	for(File file : directory.listFiles()){
//	    		if(file.getName().equals(filename)){
//	    			return true;
//	    		}
//	    	}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//    	return false;
    	return true;
    }
    
    private boolean compareDataFiles(String filename, ECGFile firstFile, ECGFile secondFile){
    	if(!checkForFile(filename)){
    		return false;
    	}
    	
    	return (firstFile.channels == secondFile.channels 
    			&& firstFile.data == secondFile.data
    			&& firstFile.leadNames.equals(secondFile.leadNames)
    			&& firstFile.leadNamesList.equals(secondFile.leadNamesList)
    			&& firstFile.sampleOffset == secondFile.sampleOffset
    			&& firstFile.samplesPerChannel == secondFile.samplesPerChannel
    			&& firstFile.samplingRate == secondFile.samplingRate
    			&& firstFile.scalingFactor == secondFile.scalingFactor);
    }
    
    private ECGFile loadFile(String fileName, DataFileFormat format){
    	ECGFile ecgFile = null;
    	try {
			ECGFormatReader reader = new ECGFormatReader();

			File file = new File(getClass().getResource(fileName).getFile());

			if(file.exists()){
				ecgFile = reader.read(format, file.getAbsolutePath());
			}
		} catch (ECGConverterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return ecgFile;
    }
    
    private ECGFile loadWFDBFiles(String headerFileName, String dataFileName, DataFileFormat format){
    	ECGFile ecgFile = null;
    	try {
			ECGFormatReader reader = new ECGFormatReader();

			File headerFile = new File(getClass().getResource(headerFileName).getFile());
			File dataFile = new File(getClass().getResource(dataFileName).getFile());

			if(headerFile.exists() && dataFile.exists()){
				ecgFile = reader.read(format, headerFileName.split("\\.")[0]);
				System.out.println("Files have been read");
			}
		} catch (ECGConverterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return ecgFile;
    }

    @Test
    public void testWriteRDTFile(){
    	ECGFile ecgFile = loadFile(RDT_INPUT_FILE_PATH, DataFileFormat.RDT);
    	ECGFile newECGFile = null;
    	File newFile;
    	ECGFormatWriter writer = new ECGFormatWriter();
    	try {
			Properties properties = WFDBUtilities.getProperties();
			String outputFolder = properties.getProperty(TEMP_FOLDER);
			newFile = writer.writeToFile(DataFileFormat.RDT, outputFolder, "RDTTest", ecgFile);
			System.out.println("Let's load the new one");
			newECGFile = loadFile(outputFolder + File.separator + "RDTTest.rdt", DataFileFormat.RDT);
			if(newECGFile == null){
				System.out.println("Loading the new file failed.");
			}
		} catch (ECGConverterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	if(ecgFile == null || newECGFile == null){
    		assertTrue(false);
    	}
    	assertTrue(compareDataFiles("RDTTest.rdt", ecgFile, newECGFile));
    }
    
//    @Test
//    public void testWriteRDTInputStream(){
//    	ECGFile ecgFile = loadFile(RDT_INPUT_FILE_PATH, DataFileFormat.RDT);
//    	ECGFormatWriter writer = new ECGFormatWriter();
//    	try {
//			writer.writeToFile(DataFileFormat.RDT, TEMP_FOLDER, "RDTTest", ecgFile);
//		} catch (ECGConverterException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	assertTrue(false);
//    }
    
//    @Test
//    public void testWriteHL7AecgFile(){
//    	ECGFile ecgFile = loadFile(HL7AECG_INPUT_FILE_PATH, DataFileFormat.HL7);
//    	ECGFormatWriter writer = new ECGFormatWriter();
//    	try {
//			writer.write(DataFileFormat.HL7, TEMP_FOLDER, "HL7AecgTest", ecgFile, true);
//		} catch (ECGConverterException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	assertTrue(checkForFile("HL7AecgTest.xml"));
//    }
//    
//    @Test
//    public void testWriteMuseTXTFile(){
//    	ECGFile ecgFile = loadFile(MUSE_TXT_INPUT_FILE_PATH, DataFileFormat.GEMUSE);
//    	ECGFormatWriter writer = new ECGFormatWriter();
//    	try {
//			writer.write(DataFileFormat.GEMUSE, TEMP_FOLDER, "MuseTXTTest", ecgFile, true);
//		} catch (ECGConverterException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	assertTrue(checkForFile("MuseTXTTest.txt"));
//    }
//    
//    @Test
//    public void testWriteWFDBFile(){
//    	ECGFile ecgFile = loadWFDBFiles(WFDB_HEADER_FILE_PATH, WFDB_DATA_FILE_PATH, DataFileFormat.WFDB);
//    	ECGFormatWriter writer = new ECGFormatWriter();
//    	try {
//			writer.write(DataFileFormat.WFDB, TEMP_FOLDER, "WFDBTest", ecgFile, true);
//		} catch (ECGConverterException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    	assertTrue(checkForFile("WFDBTest.hea") && checkForFile("WFDBTest.dat"));
//    }
}