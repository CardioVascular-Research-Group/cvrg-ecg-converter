package edu.jhu.icm.ecgFormatConverter;
import java.io.File;

import edu.jhu.icm.ecgFormatConverter.hl7.HL7Reader;
import edu.jhu.icm.ecgFormatConverter.muse.GEMuse_wrapper;
import edu.jhu.icm.ecgFormatConverter.muse.MuseXML_wrapper;
import edu.jhu.icm.ecgFormatConverter.philips.Philips103_wrapper;
import edu.jhu.icm.ecgFormatConverter.philips.Philips104_wrapper;
import edu.jhu.icm.ecgFormatConverter.rdt.RDTParser;
import edu.jhu.icm.ecgFormatConverter.wfdb.WFDBApplicationWrapper;
import edu.jhu.icm.ecgFormatConverter.xy.XYWrapper;
import edu.jhu.icm.parser.Writer;

/** Loads ECG data from one of several file formats and 
 * writes it out in another file format  
 * 
 * @author Michael Shipway
 *
 */
public class ECGformatConverter { 
	private int data[][]; // common, shared work space populated by LoadXXX, used by WriteXXX methods
	private int aduGain = 200;
	private int channels=0; // number of channels read by LoadXXX methods
	private int samplesPerChannel=0; // rows read by LoadXXX methods
	private float samplingRate=0; // Hz read by LoadXXX methods	
	
	private int numberOfPoints;
	
	static public enum fileFormat  {RDT, HL7, WFDB, WFDB_16, WFDB_61, WFDB_212, GEMUSE, RAW_XY_CONST_SAMPLE, RAW_XY_VAR_SAMPLE, PHILIPS103, PHILIPS104, MUSEXML};
	private static final boolean verbose = true;
	private String sep = File.separator;
	
	private Object philipsRestingecgdata;
	private String museXMLData;
	
/*********** Get result properties ********************************/
	/** Returns the ECG data array which was produced by the last Load method */
	public int[][] getData() {
		return this.data;
	}

	/** Returns the number of channels in the ECG data which was produced by the last Load method */
	public int getChannelCount() {
		return this.channels;
	}

	/** Returns the Samples(rows) for each channel in the ECG data which was produced by the last Load method */
	public int getSamplesPerChannel() {
		return this.samplesPerChannel;
	}
	
	/** Returns the ADC Unit gain number, measured in bits/microVolt */
	public int getAduGain() {
		return this.aduGain;
	}
/*********** Main method ********************************/
		
	/**
	 * Converts an electrocardiogram (ECG) file from one format to another.
	 * Currently supported formats are RDT, WFDB, geMuse and writing only to HL7.
	 * 
	 * @param inputFormat - format of the input file
	 * @param outputFormat - format of the output file(s)
	 * @param fileName - input and output filename, minus the paths and extensions
	 * @param signalsRequested - Number of signals to read, starting with 1st signal. 
	 * 		Can be less than or equal to # of signals in the file.
	 * 		Only used when reading WFDB format.   
	 * @param inputPath - location of the input file.
	 * @param outputPath - location to put the output file(s)
	 * 
	 * @return - number of rows written, -1 on error.
	 */
	public int convert(fileFormat inputFormat, fileFormat outputFormat, String fileName, int signalsRequested, String inputPath, String outputPath) {
		
		String recordName = fileName.substring(0, fileName.lastIndexOf(".")); // trim off the extension
		
		boolean ret = read(inputFormat, fileName, signalsRequested, inputPath, recordName);
		
		if(!ret) return -1;
		
		int rowsWritten = write(outputFormat, outputPath, recordName);
		
		return rowsWritten;
	}

	public  int write(fileFormat outputFormat, String outputPath, String recordName) {
		int rowsWritten;
		// 	write functions populate and return this.rowsWritten
		if (verbose) System.err.println("Write format:" + outputFormat.toString());
		switch(outputFormat) {
			case RDT:
				rowsWritten = writeRDT(outputPath, recordName);
				break;
			case HL7:
				rowsWritten = writeHL7(outputPath, recordName);
				break;
			case WFDB: // defaults to sub-format 16
			case WFDB_16:
				rowsWritten = writeWFDB(outputPath, recordName, 16);
				break;
			case WFDB_61:
				rowsWritten = writeWFDB(outputPath, recordName, 61);
				break;
			case WFDB_212:
				rowsWritten = writeWFDB(outputPath, recordName, 212);
				break;
			case  GEMUSE:
				rowsWritten = write_geMuse(outputPath, recordName);
				break;
			default:
				rowsWritten=-1; // write format not specified.
				break;		
		}
		return rowsWritten;
	}

	public boolean read(fileFormat inputFormat, String fileName,
			int signalsRequested, String inputPath, String recordName) {
		boolean ret;
		if (verbose) System.err.println("Load format:" + inputFormat.toString());
		switch(inputFormat) {
			case RDT:
				ret = loadRDT(inputPath + fileName);
				break;
			case HL7:
				ret = loadHL7(inputPath + fileName);
//				ret = false; // load HL7 has not been implemented.
				break;
			case WFDB:
			case WFDB_16:
			case WFDB_61:
			case WFDB_212: // loadWFDB() determines sub-format of input file from its header.
				if(signalsRequested==0) ret = loadWFDB(inputPath, recordName);
				else ret = loadWFDB(inputPath, recordName, signalsRequested);
				break;
			case  GEMUSE:
				ret = load_geMuse(inputPath + fileName);
				break;
			case RAW_XY_CONST_SAMPLE: 
				ret = load_XY(inputPath + fileName, false);
				break;
			case RAW_XY_VAR_SAMPLE: 
				ret = load_XY(inputPath + fileName, true);
				break;
			case PHILIPS103:
				ret = loadPhilips103(inputPath + fileName);
				break;
			case MUSEXML:
				ret = loadMuseXML(inputPath + fileName);
				break;
			case PHILIPS104:
				ret = loadPhilips104(inputPath + fileName);
				break;
			default:
				ret = false; // load format not specified.
				break;		
		}
		return ret;
	}
	

/*********** Loading methods **********************************/
	
	private boolean load_XY(String filePath, boolean variableSample) {
		boolean ret = false;
		File geMuseFile = new File(filePath);
		XYWrapper geMuseWrap = new XYWrapper(geMuseFile);
		if(geMuseWrap.parse(variableSample)) {
			samplingRate = (float)geMuseWrap.getSamplingRate();
			samplesPerChannel = geMuseWrap.getSamplesPerChannel();
			channels = geMuseWrap.getChannels();
			data = geMuseWrap.getData();
			aduGain = geMuseWrap.getAduGain();
			numberOfPoints = geMuseWrap.getNumberOfPoints();
			ret = true;
		}
		
		return ret; 
	}

	/** Loads the named RDT formatted file into the converter's work space 
	 * 
	 * @param filename - path/name.ext of the RDT file to load
	 * @return - success/fail 
	 */
	public boolean loadRDT(String fileName) {
		boolean ret = false;
		
		File rdtFile = new File(fileName);
		RDTParser rdtPar = new RDTParser(rdtFile);
		if(rdtPar.parse()) {
			samplingRate = (float)rdtPar.getSamplingRate();
			samplesPerChannel = rdtPar.getCounts();
			channels = rdtPar.getChannels();
			data = rdtPar.getData();
			aduGain = rdtPar.getAduGain();
			numberOfPoints = rdtPar.getNumberOfPoints();
			ret = true;
		}

		return ret;
	}
	
	/** Loads the named RDT formatted file into the converter's work space 
	 * 
	 * @param filename - path/name.ext of the RDT file to load
	 * @return - success/fail 
	 */
	public boolean loadHL7(String hl7FileName) {
		boolean ret = false;
		if (verbose) System.err.println("loadHL7 called for:" + hl7FileName);
		
		HL7Reader hl7Par =  new HL7Reader(hl7FileName);
		
		if(hl7Par.parse()) {
			samplingRate = (float)hl7Par.getSamplingRate();
			samplesPerChannel = hl7Par.getSamplesPerChannel();
			channels = hl7Par.getChannels();
			data = hl7Par.getData();
			aduGain = hl7Par.getAduGain();
			numberOfPoints = hl7Par.getNumberOfPoints();
			ret = true;
			if (verbose) System.err.println("HL7 file parsed successfully, found " + channels + " leads, with " + samplesPerChannel + " data points.");
		}
		
		return ret;
	}
	

	/** Reads all of the data from a WFDB record file set into the converter's work space.
	 * 
	 * @param filePath - path of the input files
	 * @param recordName - name of the record, used to build file names by adding file extensions.
	 * @return - success/fail 
	 */ 
	public boolean loadWFDB(String filePath, String recordName) {
		WFDBApplicationWrapper wrapper = new WFDBApplicationWrapper();
		wrapper.setFilePath(filePath);
		
		int signalsRequested = wrapper.getSignalCount(recordName);
		if (signalsRequested <=0) 
			return false;

		return loadWFDB(filePath, recordName, signalsRequested);
	}
	
	/** Reads the requested number of the channels from a WFDB record file set into the converter's work space.
	 * 
	 * @param filePath - path of the input files, e.g. "/mnt/hgfs/SharedFiles/"
	 * @param recordName - name of the record, used to build file names by adding file extensions.
 	 * @param signalsRequested - Number of signals to read, starting with 1st signal.
	 * @return - success/fail 
	 */ 
	public boolean loadWFDB(String filePath, String recordName, int signalsRequested) {
		WFDBApplicationWrapper wfdbWrap = new WFDBApplicationWrapper();
		wfdbWrap.setFilePath(filePath);
		
		samplesPerChannel = wfdbWrap.WFDBtoArray(recordName, signalsRequested);

		if (samplesPerChannel > 0 ) {
			samplingRate = wfdbWrap.getSamplingRate();
			channels = wfdbWrap.getChannels();
			data = wfdbWrap.getData();
			aduGain = wfdbWrap.getAduGain();
			numberOfPoints = wfdbWrap.getNumberOfPoints();
			return true;
		}else { 
			return false;
		}
	}
	
	/** Reads the requested number of the channels from a geMuse record file into the converter's work space.
	 * 
	 * @param filePath - full path of the input file, e.g. "/mnt/hgfs/SharedFiles/70183993_10sec.txt"
	 * @return - success/fail 
	 */ 
	public boolean load_geMuse(String filePath) {
		File geMuseFile = new File(filePath);
		GEMuse_wrapper geMuseWrap = new GEMuse_wrapper(geMuseFile);
		if(geMuseWrap.parse()) {
			samplingRate = (float)geMuseWrap.getSamplingRate();
			samplesPerChannel = geMuseWrap.getSamplesPerChannel();
			channels = geMuseWrap.getChannels();
			data = geMuseWrap.getData();
			aduGain = geMuseWrap.getAduGain();
			numberOfPoints = geMuseWrap.getNumberOfPoints();
			return true;
		}

		return false;
	}
	
	private boolean loadPhilips103(String filePath) {
		// Put in calls to the Sierra ECG Library here.  Then call the Philips103_wrapper
		try {
			Philips103_wrapper philipsWrap = new Philips103_wrapper();
			philipsWrap.init(filePath);
			
			if(philipsWrap.parse()) {
				samplingRate = philipsWrap.getSamplingRate();
				samplesPerChannel = philipsWrap.getSamplesPerChannel();
				channels = philipsWrap.getChannels();
				data = philipsWrap.getData();
				aduGain = philipsWrap.getAduGain();
				numberOfPoints = philipsWrap.getNumberOfPoints();
				philipsRestingecgdata = philipsWrap.getPhilipsECG();
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	private boolean loadMuseXML(String filePath) {
		MuseXML_wrapper museXMLWrap = new MuseXML_wrapper();
		
		if(museXMLWrap.parse(filePath)) {
			samplingRate = museXMLWrap.getSamplingRate();
			samplesPerChannel = museXMLWrap.getSamplesPerChannel();
			channels = museXMLWrap.getChannels();
			data = museXMLWrap.getData();
			aduGain = museXMLWrap.getAduGain();
			numberOfPoints = museXMLWrap.getNumberOfPoints();
			museXMLData = museXMLWrap.getMuseXML();
			return true;
		}
		
		return false;
	}
	
	private boolean loadPhilips104(String filePath) {
		// Put in calls to the Sierra ECG Library here.  Then call the Philips103_wrapper
		try {
			Philips104_wrapper philipsWrap = new Philips104_wrapper();
			philipsWrap.init(filePath);
			
			if(philipsWrap.parse()) {
				samplingRate = philipsWrap.getSamplingRate();
				samplesPerChannel = philipsWrap.getSamplesPerChannel();
				channels = philipsWrap.getChannels();
				data = philipsWrap.getData();
				aduGain = philipsWrap.getAduGain();
				numberOfPoints = philipsWrap.getNumberOfPoints();
				philipsRestingecgdata = philipsWrap.getPhilipsECG();
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}


/*********** Writing (output) methods **********************************/
	
	/**
	 * writes the data array out in RDT format
	 * @param fileName - full path of RDT output file
	 * @return - rowsWritten
	 */
	public int writeRDT(String filePath, String recordName) {
		
		String fileName = recordName + ".rdt";
		File rdtFile = new File(filePath + sep + fileName);
		RDTParser rdtPar = new RDTParser(rdtFile);
		rdtPar.setChannels(channels);
		rdtPar.setSamplesPerChannel(samplesPerChannel);
		rdtPar.setSamplingRate(samplingRate);
		rdtPar.setData(data);	
		
		return rdtPar.writeRDT();
	}
	
	/** Writes the data array out in HL7 format 
	 * @param fileName - full path of HL7 output file
	 * @return - rowsWritten
	 * */
	public int writeHL7(String filePath, String fileName) {
		Writer.writeHL7(filePath + fileName, data, samplingRate);
		
		return samplesPerChannel;
	}
	
	/**
	 * writes the data array out in one of 3 WFDB formats (16, 61, or 212)
	 * @param outRecordName - Used as the file name, suffixes will be added
	 * @param Format - one of the following WFDB formats: 16, 61, or 212
	 */
	public int writeWFDB(String filePath, String outRecordName, int Format){
		int rowsWritten = 0;
		WFDBApplicationWrapper wrap = new WFDBApplicationWrapper();
		wrap.setSamplesPerChannel(samplesPerChannel);
		wrap.setChannels(channels);
		wrap.setSamplingRate(samplingRate); // Hz
		wrap.setFilePath(filePath);
		wrap.recordName = outRecordName;		
		wrap.fmt = Format;
		wrap.sampleADCResolution = 1;
		wrap.gain = aduGain;
		
		try {
			wrap.setData(data);
			rowsWritten = wrap.arrayToWFDB();
		} catch (Exception e) {
			e.printStackTrace();
			rowsWritten = -1;
		}
		
		return rowsWritten;
	}

	/**
	 * writes the data array out in geMuse format.
	 * @param fileName - full path of geMuse output file
	 * @return - rowsWritten
	 */
	public int write_geMuse(String filePath, String recordName) {
		String fileName = recordName + ".txt";
		File geMuseFile = new File(filePath + fileName);
		GEMuse_wrapper geMuseWrap = new GEMuse_wrapper(geMuseFile);
		geMuseWrap.setChannels(channels);
		geMuseWrap.setSamplesPerChannel(samplesPerChannel);
		geMuseWrap.setSamplingRate((int) samplingRate);
		geMuseWrap.setData(data);		
		
		return geMuseWrap.write_geMuse();
	}

	public Object getPhilipsRestingecgdata() {
		return philipsRestingecgdata;
	}
	
	public String getMuseRawXML() {
		return museXMLData;
	}

	public int getNumberOfPoints() {
		return numberOfPoints;
	}

	public float getSamplingRate() {
		return samplingRate;
	}

}
