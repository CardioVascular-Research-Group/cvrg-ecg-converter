package edu.jhu.icm.test;

import java.io.File;
import java.io.InputStream;

import junit.framework.TestCase;

import org.junit.Test;

import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFormatReader;
import edu.jhu.icm.ecgFormatConverter.utility.ConverterUtility;
import edu.jhu.icm.ecgFormatConverter.wfdb.WFDBUtilities;
import edu.jhu.icm.enums.DataFileFormat;

public class LoaderTest extends TestCase{
	
	private final String HL7AECG_INPUT_FILE_PATH = "/hl7aecg-Example2.xml";
	private final String SCHILLER_INPUT_FILE_PATH = "/schiller.xml";
	private final String PHILIPS103_INPUT_FILE_PATH = "/Philips103Example01.xml";
	private final String PHILIPS104_INPUT_FILE_PATH = "/ecg_900657176_1.xml";
	private final String MUSE_TXT_INPUT_FILE_PATH = "/J123456_10sec.txt";
	private final String MUSE_XML_INPUT_FILE_PATH = "/MUSE_20080710_165457_96000.xml";
	private final String RDT_INPUT_FILE_PATH = "/DEM10000026.rdt";
	private final String WFDB_HEADER_FILE_PATH = "/jhu315.hea";
	private final String WFDB_DATA_FILE_PATH = "/jhu315.dat";
	
	private ECGFileData ecgFile = null;

    public LoaderTest(String testName)
    {
        super(testName);
        WFDBUtilities.clearTempFiles(ConverterUtility.getProperty(ConverterUtility.TEMP_FOLDER), null);
    }
    
    private boolean loadFileTest(String fileName, DataFileFormat format){
    	boolean result = true;
    	ecgFile = null;
    	ECGFormatReader reader = new ECGFormatReader();
		File file = new File(getClass().getResource(fileName).getFile());
		if(!file.exists()){
			result = false;
		} else {
			ecgFile = reader.read(format, file.getAbsolutePath());
		}
		if(ecgFile == null){
			result = false;
		}
		if(ecgFile.data != null){
			result = true;
		}

		return result;
    }
    
    private boolean loadWFDBFilesTest(String headerFileName, String dataFileName, DataFileFormat format){
    	boolean result = true;
    	ecgFile = null;
    	ECGFormatReader reader = new ECGFormatReader();

		File headerFile = new File(getClass().getResource(headerFileName).getFile());
		File dataFile = new File(getClass().getResource(dataFileName).getFile());
		if(!headerFile.exists() || !dataFile.exists()){
			result = false;
		} else {
			String subjectId = headerFileName.split("\\.")[0];
			subjectId = subjectId.substring(1);
			ecgFile = reader.read(format, subjectId);
		}
		if(ecgFile == null){
			result = false;
		}
		if(ecgFile.data != null){
			result = true;
		}
		return result;
    }
    
    private boolean loadInputStreamTest(String fileName, DataFileFormat format){
    	boolean result = true;
    	ecgFile = null;
    	InputStream inputStream = getClass().getResourceAsStream(fileName);
    	if(inputStream == null){
    		result = false;
    	}
		ECGFormatReader reader = new ECGFormatReader();
		ecgFile = reader.read(format, inputStream);
		if(ecgFile == null){
			result = false;
		}
		if(ecgFile.data != null){
			result = true;
		}

		return result;
    }
    
    private boolean loadWFDBInputStreamTest(String fileName, DataFileFormat format){
    	boolean result = true;
    	ecgFile = null;
    	InputStream headerInputStream = getClass().getResourceAsStream(WFDB_HEADER_FILE_PATH);
    	InputStream dataInputStream = getClass().getResourceAsStream(WFDB_DATA_FILE_PATH);
    	if(headerInputStream == null || dataInputStream == null){
    		result = false;
    	}
		ECGFormatReader reader = new ECGFormatReader();
		ecgFile = reader.read(format, dataInputStream, headerInputStream, "jhu315IS");
		if(ecgFile == null){
			result = false;
		}
		if(ecgFile.data != null){
			result = true;
		}

		return result;
    }
    
    private boolean testResult(int channels, int samplesPerChannel, int samplingRate, double scalingFactor){

    	if(channels == ecgFile.channels &&
    			samplesPerChannel == ecgFile.samplesPerChannel &&
    			samplingRate == ecgFile.samplingRate &&
    			ecgFile.scalingFactor == scalingFactor)
    	{
    		return true;
    	}
    	return false;
    }
    
    @Test
    public void testLoadHL7AecgFile(){																	
		assertTrue(loadFileTest(HL7AECG_INPUT_FILE_PATH, DataFileFormat.HL7));
    }
    
    @Test
    public void testLoadHL7AecgInputStream(){
    	assertTrue(loadInputStreamTest(HL7AECG_INPUT_FILE_PATH, DataFileFormat.HL7));
    }
    
    @Test 
    public void testHL7AccuracyFromInputStream(){
    	boolean result = false;
    	if(loadInputStreamTest(HL7AECG_INPUT_FILE_PATH, DataFileFormat.HL7)){
    		result = testResult(12, 10000, 1000, 2.5);
    	}
    	assertTrue(result);
    }
    
    @Test 
    public void testHL7AccuracyFromFile(){
    	boolean result = false;
    	if(loadFileTest(HL7AECG_INPUT_FILE_PATH, DataFileFormat.HL7)){
    		result = testResult(12, 10000, 1000, 2.5);
    	}
    	assertTrue(result);
    }
    
    @Test
    public void testLoadSchillerFile(){
    	assertTrue(loadFileTest(SCHILLER_INPUT_FILE_PATH, DataFileFormat.SCHILLER));
    }
    
    @Test
    public void testLoadSchillerInputStream(){
    	assertTrue(loadInputStreamTest(SCHILLER_INPUT_FILE_PATH, DataFileFormat.SCHILLER));
    }
    
    @Test 
    public void testSchillerAccuracyFromInputStream(){
    	boolean result = false;
    	if(loadInputStreamTest(SCHILLER_INPUT_FILE_PATH, DataFileFormat.SCHILLER)){
    		result = testResult(12, 4998, 500, 1);
    	}
    	assertTrue(result);
    }
    
    @Test 
    public void testSchillerAccuracyFromFile(){
    	boolean result = false;
    	if(loadFileTest(SCHILLER_INPUT_FILE_PATH, DataFileFormat.SCHILLER)){
    		result = testResult(12, 4998, 500, 1);
    	}
    	assertTrue(result);
    }
    
    @Test
    public void testLoadPhilips103File(){
    	assertTrue(loadFileTest(PHILIPS103_INPUT_FILE_PATH, DataFileFormat.PHILIPS103));
    }
    
    @Test
    public void testLoadPhilips103InputStream(){
    	assertTrue(loadInputStreamTest(PHILIPS103_INPUT_FILE_PATH, DataFileFormat.PHILIPS103));
    }
    
    @Test 
    public void testPhilips103AccuracyFromInputStream(){
    	boolean result = false;
    	if(loadInputStreamTest(PHILIPS103_INPUT_FILE_PATH, DataFileFormat.PHILIPS103)){
    		result = testResult(12, 5500, 500, 1);
    	}
    	assertTrue(result);
    }
    
    @Test 
    public void testPhilips103AccuracyFromFile(){
    	boolean result = false;
    	if(loadFileTest(PHILIPS103_INPUT_FILE_PATH, DataFileFormat.PHILIPS103)){
    		result = testResult(12, 5500, 500, 1);
    	}
    	assertTrue(result);
    }
    
    @Test
    public void testLoadPhilips104File(){
    	assertTrue(loadFileTest(PHILIPS104_INPUT_FILE_PATH, DataFileFormat.PHILIPS104));
    }
    
    @Test
    public void testLoadPhilips104InputStream(){
    	assertTrue(loadInputStreamTest(PHILIPS104_INPUT_FILE_PATH, DataFileFormat.PHILIPS104));
    }
    
    @Test 
    public void testPhilips104AccuracyFromInputStream(){
    	boolean result = false;
    	if(loadInputStreamTest(PHILIPS104_INPUT_FILE_PATH, DataFileFormat.PHILIPS104)){
    		result = testResult(15, 5500, 500, 1);
    	}
    	assertTrue(result);
    }
    
    @Test 
    public void testPhilips104AccuracyFromFile(){
    	boolean result = false;
    	if(loadFileTest(PHILIPS104_INPUT_FILE_PATH, DataFileFormat.PHILIPS104)){
    		result = testResult(15, 5500, 500, 1);
    	}
    	assertTrue(result);
    }
    
    @Test
    public void testLoadMuseTXTFile(){
    	assertTrue(loadFileTest(MUSE_TXT_INPUT_FILE_PATH, DataFileFormat.GEMUSE));
    }
    
    @Test
    public void testLoadMuseTXTInputStream(){
    	assertTrue(loadInputStreamTest(MUSE_TXT_INPUT_FILE_PATH, DataFileFormat.GEMUSE));
    }
    
    @Test 
    public void testMuseTXTAccuracyFromInputStream(){
    	boolean result = false;
    	if(loadInputStreamTest(MUSE_TXT_INPUT_FILE_PATH, DataFileFormat.GEMUSE)){
    		result = testResult(12, 5000, 500, 1);
    	}
    	assertTrue(result);
    }
    
    @Test 
    public void testMuseTXTAccuracyFromFile(){
    	boolean result = false;
    	if(loadFileTest(MUSE_TXT_INPUT_FILE_PATH, DataFileFormat.GEMUSE)){
    		result = testResult(12, 5000, 500, 1);
    	}
    	assertTrue(result);
    }
    
    @Test
    public void testLoadMuseXMLFile(){
    	assertTrue(loadFileTest(MUSE_XML_INPUT_FILE_PATH, DataFileFormat.MUSEXML));
    }
    
    @Test
    public void testLoadMuseXMLInputStream(){
    	assertTrue(loadInputStreamTest(MUSE_XML_INPUT_FILE_PATH, DataFileFormat.MUSEXML));
    }
    
    @Test 
    public void testMuseXMLAccuracyFromInputStream(){
    	boolean result = false;
    	if(loadInputStreamTest(MUSE_XML_INPUT_FILE_PATH, DataFileFormat.MUSEXML)){
    		result = testResult(12, 2500, 250, 205);
    	}
    	printStuff();
    	assertTrue(result);
    }
    
    @Test 
    public void testMuseXMLAccuracyFromFile(){
    	boolean result = false;
    	if(loadFileTest(MUSE_XML_INPUT_FILE_PATH, DataFileFormat.MUSEXML)){
    		result = testResult(12, 2500, 250, 205);
    	}
    	assertTrue(result);
    }
   
    @Test
    public void testLoadRDTFile(){
    	assertTrue(loadFileTest(RDT_INPUT_FILE_PATH, DataFileFormat.RDT));
    }

    @Test
    public void testLoadRDTInputStream(){
    	assertTrue(loadInputStreamTest(RDT_INPUT_FILE_PATH, DataFileFormat.RDT));
    }  
    
    @Test 
    public void testRDTAccuracyFromInputStream(){
    	boolean result = false;
    	if(loadInputStreamTest(RDT_INPUT_FILE_PATH, DataFileFormat.RDT)){
    		result = testResult(3, 303364, 1000, 1);
    	}
    	assertTrue(result);
    }
    
    @Test 
    public void testRDTAccuracyFromFile(){
    	boolean result = false;
    	if(loadFileTest(RDT_INPUT_FILE_PATH, DataFileFormat.RDT)){
    		result = testResult(3, 303364, 1000, 1);
    	}
    	assertTrue(result);
    }
    
    @Test
    public void testLoadWFDBFile(){
    	assertTrue(loadWFDBFilesTest(WFDB_HEADER_FILE_PATH, WFDB_DATA_FILE_PATH, DataFileFormat.WFDB));
    }

    @Test
    public void testLoadWFDBInputStream(){
    	assertTrue(loadWFDBInputStreamTest(WFDB_HEADER_FILE_PATH, DataFileFormat.WFDB));
    } 
    
    @Test 
    public void testWFDBAccuracyFromInputStream(){
    	boolean result = false;
    	if(loadWFDBInputStreamTest(WFDB_HEADER_FILE_PATH, DataFileFormat.WFDB)){
    		result = testResult(3, 319096, 1000, 1);
    	}
    	assertTrue(result);
    }
    
    @Test 
    public void testWFDBAccuracyFromFile(){
    	boolean result = false;
    	if(loadWFDBFilesTest(WFDB_HEADER_FILE_PATH, WFDB_DATA_FILE_PATH, DataFileFormat.WFDB)){
    		result = testResult(3, 319096, 1000, 1);
    	}
    	assertTrue(result);
    }
      
    private void printStuff(){
		System.out.println("Sampling Rate is " + ecgFile.samplingRate);
		System.out.println("Channels is " + ecgFile.channels);
		System.out.println("Lead names is " + ecgFile.printLeadNameList());
		System.out.println("Samples per channel is " + ecgFile.samplesPerChannel);
		System.out.println("Scale is " + ecgFile.scalingFactor);
		System.out.println("The ECGFile itself:");
		System.out.println(ecgFile.toString());
    }
}