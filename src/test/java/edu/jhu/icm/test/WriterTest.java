package edu.jhu.icm.test;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFormatReader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWriter;
import edu.jhu.icm.ecgFormatConverter.utility.ConverterUtility;
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
		String outputFolder = ConverterUtility.getProperty(TEMP_FOLDER);
		File file = new File(outputFolder + filename);
		if(file.exists()){
			return true;
		}
    	return false;
    }
    
    private boolean compareDataFiles(ECGFileData firstFile, ECGFileData secondFile){

    	if(firstFile.channels == secondFile.channels){System.out.println("Channels match.");}
    	else{return false;}
    	if(Arrays.deepEquals(firstFile.data, secondFile.data)){System.out.println("Data matches.");}
    	else{return false;}
    	if(firstFile.leadNames == secondFile.leadNames){System.out.println("leadNames match.");}
    	else{return false;}
    	if(firstFile.sampleOffset == secondFile.sampleOffset){System.out.println("sampleOffset match.");}
    	else{return false;}
    	if(firstFile.samplesPerChannel == secondFile.samplesPerChannel){System.out.println("samplesPerChannel match.");}
    	else{return false;}
    	if(firstFile.samplingRate == secondFile.samplingRate){System.out.println("samplingRate match.");}
    	else{return false;}
    	if(firstFile.scalingFactor == secondFile.scalingFactor){System.out.println("scalingFactor match.");}
    	else{return false;}
    	return true;
    }

    private ECGFileData loadFile(String fileName, DataFileFormat format){
    	ECGFileData ecgFile = null;
    	ECGFormatReader reader = new ECGFormatReader();
    	File file = new File(getClass().getResource(fileName).getFile());
		if(file.exists()){
			ecgFile = reader.read(format, file.getAbsolutePath());
		} else {
		}
		return ecgFile;
    }
    
    private ECGFileData loadWFDBFiles(String headerFileName, String dataFileName, DataFileFormat format){
    	ECGFileData ecgFile = null;
    	ECGFormatReader reader = new ECGFormatReader();

		File headerFile = new File(getClass().getResource(headerFileName).getFile());
		File dataFile = new File(getClass().getResource(dataFileName).getFile());

		if(headerFile.exists() && dataFile.exists()){
			ecgFile = reader.read(format, headerFile.getAbsolutePath());
		}
		return ecgFile;
    }
  
    private boolean genericWriteFileTest(DataFileFormat format, String inputFilePath, String outputFileName, String subjectId){
    	ECGFileData ecgFile = null;
    	ECGFormatReader reader = new ECGFormatReader();
    	File file = new File(getClass().getResource(inputFilePath).getFile());
    	ecgFile = reader.read(format, file.getAbsolutePath());
    	ECGFormatWriter writer = new ECGFormatWriter();
		String outputFolder = ConverterUtility.getProperty(TEMP_FOLDER);
		writer.writeToFile(format, outputFolder, subjectId, ecgFile);
		if(!checkForFile(outputFileName)){
			return false;
		}
		
		ECGFormatReader newReader = new ECGFormatReader();
		ECGFileData newECGFile = newReader.read(format, file.getAbsolutePath());
		return compareDataFiles(ecgFile, newECGFile);
    }
    
    @Test
    public void testWriteRDTFile(){
    	assertTrue(genericWriteFileTest(DataFileFormat.RDT, RDT_INPUT_FILE_PATH, "RDTTest.rdt", "RDTTest"));
    }
    
    @Test
    public void testWriteRDTByteArray(){
    	ECGFileData ecgFile = loadFile(RDT_INPUT_FILE_PATH, DataFileFormat.RDT);
    	ECGFormatWriter writer = new ECGFormatWriter();
    	byte[] fileData = null;
    	fileData = writer.writeToByteArray(DataFileFormat.RDT, ecgFile, "RDTTest");
		if(fileData == null){
			assertTrue(false);
		}
    	assertTrue(true);
    }
    
    @Test
    public void testWriteHL7AecgFile(){
    	assertTrue(genericWriteFileTest(DataFileFormat.HL7, HL7AECG_INPUT_FILE_PATH, "HL7Test.xml", "HL7Test"));
    }
    
    @Test
    public void testWriteHL7AecgByteArray(){
    	ECGFileData ecgFile = loadFile(HL7AECG_INPUT_FILE_PATH, DataFileFormat.HL7);
    	ECGFormatWriter writer = new ECGFormatWriter();
    	byte[] fileData = null;
    	fileData = writer.writeToByteArray(DataFileFormat.HL7, ecgFile, "HL7Test");
		if(fileData == null){
			assertTrue(false);
		}
    	assertTrue(true);
    }
    
    @Test
    public void testWriteMuseTXTFile(){
    	assertTrue(genericWriteFileTest(DataFileFormat.GEMUSE, MUSE_TXT_INPUT_FILE_PATH, "MuseTXTTest.txt", "MuseTXTTest"));
    }
    
    @Test
    public void testWriteMuseTXTByteArray(){
    	ECGFileData ecgFile = loadFile(MUSE_TXT_INPUT_FILE_PATH, DataFileFormat.GEMUSE);
    	ECGFormatWriter writer = new ECGFormatWriter();
    	byte[] fileData = null;
    	fileData = writer.writeToByteArray(DataFileFormat.GEMUSE, ecgFile, "GeMuseTest");
		if(fileData == null){
			assertTrue(false);
		}
    	assertTrue(true);
    }
    
    @Test
    public void testWriteWFDBFile(){
    	ECGFileData ecgFile = loadWFDBFiles(WFDB_HEADER_FILE_PATH, WFDB_DATA_FILE_PATH, DataFileFormat.WFDB);
    	ECGFormatWriter writer = new ECGFormatWriter();
		String outputFolder = ConverterUtility.getProperty(TEMP_FOLDER);
    	writer.writeToFile(DataFileFormat.WFDB, outputFolder, "WFDBTest", ecgFile);
    	try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	boolean headerFile = checkForFile("WFDBTest.hea");
    	boolean dataFile = checkForFile("WFDBTest.dat");
    	assertTrue(headerFile && dataFile);
    }
}