package edu.jhu.icm.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFormatConverter;
import edu.jhu.icm.ecgFormatConverter.ECGFormatReader;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWriter;
import edu.jhu.icm.enums.DataFileFormat;

import junit.framework.TestCase;

public class ConverterTest extends TestCase{

	private final String HL7AECG_INPUT_FILE_PATH = "/hl7aecg-Example2.xml";
	private final String MUSE_TXT_INPUT_FILE_PATH = "/J123456_10sec.txt";
	private final String RDT_INPUT_FILE_PATH = "/DEM10000026.rdt";
	private final String WFDB_HEADER_FILE_PATH = "/jhu315.hea";
	private final String WFDB_DATA_FILE_PATH = "/jhu315.dat";
	private final String TEMP_FOLDER = "temp.folder.path";
	
    public ConverterTest(String testName)
    {
        super(testName);
    }
    
    //File to File
//    @Test
//    public void testConvertHL7AecgFileToMuseTXTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.GEMUSE);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "hl7aecg-Example2.txt");
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertHL7AecgFileToRDTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "hl7aecg-Example2.rdt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertHL7AecgFileToWFDBFiles(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "//hl7aecg-Example2.rdt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertMuseTXTFileToHL7AecgFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.GEMUSE, DataFileFormat.HL7);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "J123456_10sec.xml");
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertMuseTXTFileToRDTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.GEMUSE, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "J123456_10sec.rdt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertMuseTXTFileToWFDBFiles(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "//hl7aecg-Example2.rdt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertRDTFileToHL7AecgFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.HL7);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "DEM10000026.xml");
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertRDTFileToMuseTXTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.GEMUSE);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "DEM10000026.txt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertRDTFileToWFDBFiles(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "//hl7aecg-Example2.rdt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertWFDBFilesToHL7AecgFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.HL7);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "jhu315.xml");
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertWFDBFilesToMuseTXTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.GEMUSE);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "jhu315.txt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertWFDBFilesToRDTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "jhu315.rdt");
//    	assertTrue(result);
//    }
    
  //File to InputStream
//    @Test
//    public void testConvertHL7AecgFileToMuseTXTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToInputStream(file, DataFileFormat.HL7, DataFileFormat.GEMUSE);
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertHL7AecgFileToRDTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertHL7AecgFileToWFDBInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertMuseTXTFileToHL7AecgInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.GEMUSE, DataFileFormat.HL7);
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertMuseTXTFileToRDTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.GEMUSE, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertMuseTXTFileToWFDBInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertRDTFileToHL7AecgInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.HL7);
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertRDTFileToMuseTXTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.GEMUSE);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertRDTFileToWFDBInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "//hl7aecg-Example2.rdt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertWFDBFilesToHL7AecgFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.HL7);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "jhu315.xml");
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertWFDBFilesToMuseTXTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.GEMUSE);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "jhu315.txt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertWFDBFilesToRDTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "jhu315.rdt");
//    	assertTrue(result);
//    }
    
    //InputStream to File
//    @Test
//    public void testConvertHL7AecgInputStreamToMuseTXTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToInputStream(file, DataFileFormat.HL7, DataFileFormat.GEMUSE);
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertHL7AecgInputStreamToRDTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertHL7AecgInputStreamToWFDBFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertMuseTXTInputStreamToHL7AecgFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.GEMUSE, DataFileFormat.HL7);
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertMuseTXTInputStreamToRDTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.GEMUSE, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertMuseTXTInputStreamToWFDBFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertRDTInputStreamToHL7AecgFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.HL7);
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertRDTInputStreamToMuseTXTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.GEMUSE);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertRDTInputStreamToWFDBFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "//hl7aecg-Example2.rdt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertWFDBInputStreamToHL7AecgFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.HL7);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "jhu315.xml");
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertWFDBInputStreamToMuseTXTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.GEMUSE);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "jhu315.txt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertWFDBInputStreamToRDTFile(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "jhu315.rdt");
//    	assertTrue(result);
//    }
    

    //InputStream to InputStream
//    @Test
//    public void testConvertHL7AecgInputStreamToMuseTXTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToInputStream(file, DataFileFormat.HL7, DataFileFormat.GEMUSE);
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertHL7AecgInputStreamToRDTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertHL7AecgInputStreamToWFDBInputStreams(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertMuseTXTInputStreamToHL7AecgInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.GEMUSE, DataFileFormat.HL7);
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertMuseTXTInputStreamToRDTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.GEMUSE, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertMuseTXTInputStreamToWFDBInputStreams(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(MUSE_TXT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertRDTInputStreamToHL7AecgInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.HL7);
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertRDTInputStreamToMuseTXTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(RDT_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.GEMUSE);
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertRDTInputStreamToWFDBInputStreams(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "//hl7aecg-Example2.rdt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertWFDBInputStreamToHL7AecgInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.HL7);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "jhu315.xml");
//    	assertTrue(result);
//    }
//    
//    @Test
//    public void testConvertWFDBInputStreamToMuseTXTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.RDT, DataFileFormat.GEMUSE);
//    	result = checkForFile(TEMP_FOLDER + File.separator + "jhu315.txt");
//    	assertTrue(result);
//    }
    
//    @Test
//    public void testConvertWFDBInputStreamToRDTInputStream(){
//    	boolean result = false;
//    	File file = new File(getClass().getResource(HL7AECG_INPUT_FILE_PATH).getFile());
//    	ECGFormatConverter.convertFileToFile(file, DataFileFormat.HL7, DataFileFormat.RDT);
//    	result = checkForFile(TEMP_FOLDER + "jhu315.rdt");
//    	assertTrue(result);
//    }
    
    private boolean checkForFile(String filename){
    	File directory = new File(TEMP_FOLDER);
    	for(File file : directory.listFiles()){
    		if(file.getName().equals(filename)){
    			return true;
    		}
    	}
    	return false;
    }
    
}
