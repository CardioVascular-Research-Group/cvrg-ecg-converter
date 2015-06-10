package edu.jhu.icm.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFormatConverter;
import edu.jhu.icm.ecgFormatConverter.ECGFormatReader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWriter;
import edu.jhu.icm.ecgFormatConverter.utility.ConverterUtility;
import edu.jhu.icm.ecgFormatConverter.wfdb.WFDBUtilities;
import edu.jhu.icm.enums.DataFileFormat;

import junit.framework.TestCase;

public class ConverterTest extends TestCase{

	private final String HL7AECG_INPUT_FILE_PATH = "/hl7aecg-Example2.xml";
	private final String MUSE_TXT_INPUT_FILE_PATH = "/J123456_10sec.txt";
	private final String RDT_INPUT_FILE_PATH = "/DEM10000026.rdt";
	private final String WFDB_HEADER_FILE_PATH = "/jhu315.hea";
	private final String WFDB_DATA_FILE_PATH = "/jhu315.dat";
	
	
    public ConverterTest(String testName)
    {
        super(testName);
    }
    
    //File to File
    @Test
    public void testConvertHL7AecgFileToMuseTXTFile(){
    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	converter.convertFileToFile(file.getAbsolutePath(), DataFileFormat.HL7, DataFileFormat.GEMUSE, "hl7tomusesubject");
    	assertTrue(checkForFile("hl7tomusesubject.txt"));
    }
    
    @Test
    public void testConvertHL7AecgFileToRDTFile(){
    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	converter.convertFileToFile(file.getAbsolutePath(), DataFileFormat.HL7, DataFileFormat.RDT, "hl7tordtsubject");
    	assertTrue(checkForFile("hl7tordtsubject.rdt"));
    }
    
    @Test
    public void testConvertHL7AecgFileToWFDBFiles(){
    	boolean result = false;
    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	converter.convertFileToFile(file.getAbsolutePath(), DataFileFormat.HL7, DataFileFormat.WFDB, "hl7towfdbsubject");
    	try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	result = (checkForFile("hl7towfdbsubject.hea") && checkForFile("hl7towfdbsubject.dat"));
    	assertTrue(result);
    }
    
    @Test
    public void testConvertMuseTXTFileToHL7AecgFile(){
    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	converter.convertFileToFile(file.getAbsolutePath(), DataFileFormat.GEMUSE, DataFileFormat.HL7, "gemusetohl7subject");
    	assertTrue(checkForFile("gemusetohl7subject.xml"));
    }
    
    @Test
    public void testConvertMuseTXTFileToRDTFile(){
    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	converter.convertFileToFile(file.getAbsolutePath(), DataFileFormat.GEMUSE, DataFileFormat.RDT, "gemusetordtsubject");
    	assertTrue(checkForFile("gemusetordtsubject.rdt"));
    }
    
    @Test
    public void testConvertMuseTXTFileToWFDBFiles(){
    	boolean result = false;
    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	converter.convertFileToFile(file.getAbsolutePath(), DataFileFormat.HL7, DataFileFormat.WFDB, "gemusetowfdbsubject");
    	try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	result = (checkForFile("gemusetowfdbsubject.hea") && checkForFile("gemusetowfdbsubject.dat"));
    	assertTrue(result);
    }
    
    @Test
    public void testConvertRDTFileToHL7AecgFile(){
     	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
     	ECGFormatConverter converter = new ECGFormatConverter();
     	converter.convertFileToFile(file.getAbsolutePath(), DataFileFormat.RDT, DataFileFormat.HL7, "rdttohl7subject");
    	assertTrue(checkForFile("rdttohl7subject.xml"));
    }
    
    @Test
    public void testConvertRDTFileToMuseTXTFile(){
     	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
     	ECGFormatConverter converter = new ECGFormatConverter();
     	converter.convertFileToFile(file.getAbsolutePath(), DataFileFormat.RDT, DataFileFormat.GEMUSE, "rdttogemusesubject");
    	assertTrue(checkForFile("rdttogemusesubject.txt"));
    }
    
    @Test
    public void testConvertRDTFileToWFDBFiles(){
    	boolean result = false;
    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
     	ECGFormatConverter converter = new ECGFormatConverter();
     	converter.convertFileToFile(file.getAbsolutePath(), DataFileFormat.RDT, DataFileFormat.WFDB, "rdttowfdbsubject");
    	try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
     	result = (checkForFile("rdttowfdbsubject.hea") && checkForFile("rdttowfdbsubject.dat"));
    	assertTrue(result);
    }
    
    @Test
    public void testConvertWFDBFilesToHL7AecgFile(){
     	ECGFormatConverter converter = new ECGFormatConverter();
     	String outputFolder = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
     	String inputFolder = ConverterUtility.getProperty(ConverterUtility.WFDB_FILE_PATH);
     	converter.convertWFDBFilesToFile(inputFolder, outputFolder, DataFileFormat.HL7, "jhu315");
    	assertTrue(checkForFile("jhu315.xml"));
    }
    
    @Test
    public void testConvertWFDBFilesToMuseTXTFile(){
    	ECGFormatConverter converter = new ECGFormatConverter();
    	String outputFolder = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
    	String inputFolder = ConverterUtility.getProperty(ConverterUtility.WFDB_FILE_PATH);
     	System.out.println("Got this far.");
    	converter.convertWFDBFilesToFile(inputFolder, outputFolder, DataFileFormat.GEMUSE, "jhu315");
    	try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	assertTrue(checkForFile("jhu315.txt"));
    }
    
    @Test
    public void testConvertWFDBFilesToRDTFile(){
    	ECGFormatConverter converter = new ECGFormatConverter();
    	String outputFolder = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
    	String inputFolder = ConverterUtility.getProperty(ConverterUtility.WFDB_FILE_PATH);
    	converter.convertWFDBFilesToFile(inputFolder, outputFolder, DataFileFormat.RDT, "jhu315");
    	assertTrue(checkForFile("jhu315.rdt"));
    }
    
    //InputStream to File
    @Test
    public void testConvertHL7AecgInputStreamToMuseTXTFile(){
    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	converter.convertInputStreamToFile(inputStream, DataFileFormat.HL7, DataFileFormat.GEMUSE, "HL7inputStreamToMuse");
    	assertTrue(checkForFile("HL7inputStreamToMuse.txt"));
    }
    
    @Test
    public void testConvertHL7AecgInputStreamToRDTFile(){
    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	converter.convertInputStreamToFile(inputStream, DataFileFormat.HL7, DataFileFormat.RDT, "HL7inputStreamToRDT");
    	assertTrue(checkForFile("HL7inputStreamToRDT.rdt"));
    }
    
    @Test
    public void testConvertHL7AecgInputStreamToWFDBFiles(){
    	boolean result = false;
    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
    	String outputFolder = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
    	ECGFormatConverter converter = new ECGFormatConverter();
    	InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		converter.convertInputStreamToWFDBFiles(inputStream, DataFileFormat.HL7, "hl7toWFDB", outputFolder);
    	try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	result = (checkForFile("hl7toWFDB.hea") && checkForFile("hl7toWFDB.dat"));
    	assertTrue(result);
    }
    
    @Test
    public void testConvertMuseTXTInputStreamToHL7AecgFile(){
    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	converter.convertInputStreamToFile(inputStream, DataFileFormat.GEMUSE, DataFileFormat.HL7, "musetohl7");
    	assertTrue(checkForFile("musetohl7.xml"));
    }
    
    @Test
    public void testConvertMuseTXTInputStreamToRDTFile(){
    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	converter.convertInputStreamToFile(inputStream, DataFileFormat.GEMUSE, DataFileFormat.RDT, "musetordt");
    	assertTrue(checkForFile("musetordt.rdt"));
    }
    
    @Test
    public void testConvertMuseTXTInputStreamToWFDBFiles(){
    	boolean result = false;
    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
    	String outputFolder = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
    	ECGFormatConverter converter = new ECGFormatConverter();
    	InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		converter.convertInputStreamToWFDBFiles(inputStream, DataFileFormat.GEMUSE, "musetoWFDB", outputFolder);
    	try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	result = (checkForFile("musetoWFDB.hea") && checkForFile("musetoWFDB.dat"));
    	assertTrue(result);
    }
    
    @Test
    public void testConvertRDTInputStreamToHL7AecgFile(){
    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	converter.convertInputStreamToFile(inputStream, DataFileFormat.RDT, DataFileFormat.HL7, "rdttohl7");
    	assertTrue(checkForFile("rdttohl7.xml"));
    }
    
    @Test
    public void testConvertRDTInputStreamToMuseTXTFile(){
    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
    	ECGFormatConverter converter = new ECGFormatConverter();
    	InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	converter.convertInputStreamToFile(inputStream, DataFileFormat.RDT, DataFileFormat.GEMUSE, "rdttomuse");
    	assertTrue(checkForFile("rdttomuse.txt"));
    }
    
    @Test
    public void testConvertRDTInputStreamToWFDBFile(){
    	boolean result = false;
    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
    	String outputFolder = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
    	ECGFormatConverter converter = new ECGFormatConverter();
    	InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		converter.convertInputStreamToWFDBFiles(inputStream, DataFileFormat.RDT, "rdttoWFDB", outputFolder);
    	try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	result = (checkForFile("rdttoWFDB.hea") && checkForFile("rdttoWFDB.dat"));
    	assertTrue(result);
    }
    
    @Test
    public void testConvertWFDBInputStreamToHL7AecgFile(){
    	File headerFile = new File(getClass().getResource(WFDB_HEADER_FILE_PATH).getFile());
    	File dataFile = new File(getClass().getResource(WFDB_DATA_FILE_PATH).getFile());
    	String subjectId = ConverterUtility.getSubjectIdFromFilename(WFDB_HEADER_FILE_PATH);
    	InputStream headerInputStream = null;
    	InputStream dataInputStream = null;
    	
    	try {
			headerInputStream = new FileInputStream(headerFile);
	    	dataInputStream = new FileInputStream(dataFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

    	ECGFormatConverter converter = new ECGFormatConverter();
    	converter.convertWFDBInputStreamsToFile(headerInputStream, dataInputStream, subjectId, DataFileFormat.HL7);
    	assertTrue(checkForFile("jhu315.xml"));
    }
    
    @Test
    public void testConvertWFDBInputStreamToMuseTXTFile(){
    	File headerFile = new File(getClass().getResource(WFDB_HEADER_FILE_PATH).getFile());
    	File dataFile = new File(getClass().getResource(WFDB_DATA_FILE_PATH).getFile());
    	String subjectId = ConverterUtility.getSubjectIdFromFilename(WFDB_HEADER_FILE_PATH);
    	InputStream headerInputStream = null;
    	InputStream dataInputStream = null;
    	
    	try {
			headerInputStream = new FileInputStream(headerFile);
	    	dataInputStream = new FileInputStream(dataFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

    	ECGFormatConverter converter = new ECGFormatConverter();
    	converter.convertWFDBInputStreamsToFile(headerInputStream, dataInputStream, subjectId, DataFileFormat.GEMUSE);
    	assertTrue(checkForFile("jhu315.txt"));
    }
    
    @Test
    public void testConvertWFDBInputStreamToRDTFile(){
    	File headerFile = new File(getClass().getResource(WFDB_HEADER_FILE_PATH).getFile());
    	File dataFile = new File(getClass().getResource(WFDB_DATA_FILE_PATH).getFile());
    	String subjectId = ConverterUtility.getSubjectIdFromFilename(WFDB_HEADER_FILE_PATH);
    	InputStream headerInputStream = null;
    	InputStream dataInputStream = null;
    	
    	try {
			headerInputStream = new FileInputStream(headerFile);
	    	dataInputStream = new FileInputStream(dataFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

    	ECGFormatConverter converter = new ECGFormatConverter();
    	converter.convertWFDBInputStreamsToFile(headerInputStream, dataInputStream, subjectId, DataFileFormat.RDT);
    	assertTrue(checkForFile("jhu315.rdt"));
    }
       
    private boolean checkForFile(String filename){
    	String directoryPath = ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER);
    	File file = new File(directoryPath + filename);
    	return file.exists();
    }
}