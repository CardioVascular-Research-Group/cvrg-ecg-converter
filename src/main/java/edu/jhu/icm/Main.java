package edu.jhu.icm;
import java.io.File;

import edu.jhu.icm.ecgFormatConverter.ECGformatConverter;


public class Main {
	public static void main(String[] args) {
	    System.out.println("main() started, init'ing WFDB_AnninfoArray");

		//ECGformatConverter.fileFormat ff = ECGformatConverter.fileFormat.SCHILLER;
		//String filename="/home/WIN/dhopki12/Desktop/QRS-score/APACE-1.xml";

/*	    //gemuse xml test data
		String inputPath = "/home/WIN/dhopki12/Desktop/muse-test-data/";
		String outputPath = "/home/WIN/dhopki12/Desktop/muse-test-data/output/";
		String fileSuffix = "xml"; */

	    //philips 104 test data
/*		String inputPath = "/home/WIN/dhopki12/Desktop/104-test-data/";
		String outputPath = "/home/WIN/dhopki12/Desktop/104-test-data/output/";
		String fileSuffix = "xml"; */
		
		//schiller test data
		String inputPath = "/home/WIN/dhopki12/Desktop/QRS-score-mod2/";
		String outputPath = "/home/WIN/dhopki12/Desktop/QRS-score-mod2/output/";
		String fileSuffix = "xml"; 
		
		convertSubDirectories(inputPath, outputPath, fileSuffix);
		
		//**************************************
		// QA testing, circular translation through all formats:
		// geMuse, RDT, HL7, WFDB 16, WFDB 61, WFDB 212 and then back to geMuse.
//		ECGformatConverter conv20 = new ECGformatConverter();
////		ff.RAW_XY_CONST_SAMPLE
//		conv20.convert(ff.GEMUSE, ff.WFDB_16, recordName, signalsRequested, inputPath, outputPath);
//		conv20 = null;
//
//		ECGformatConverter conv21 = new ECGformatConverter();
//		conv21.convert(ff.WFDB_16, ff.RDT, recordName, signalsRequested, outputPath, outputPath);
//		conv21 = null;

		/*ECGformatConverter conv25 = new ECGformatConverter();
		conv25.convert(ff.RDT, ff.WFDB_16, recordName, 3, outputPath, outputPath + "WFDB_16xyz/");
		conv25 = null;

		ECGformatConverter conv22 = new ECGformatConverter();
		conv22.convert(ff.WFDB_16, ff.WFDB_61, recordName, signalsRequested, outputPath + "WFDB_16/", outputPath + "WFDB_61/");
		conv22 = null;

		ECGformatConverter conv23 = new ECGformatConverter();
		conv23.convert(ff.WFDB_61, ff.WFDB_212, recordName, signalsRequested, outputPath + "WFDB_61/", outputPath + "WFDB_212/");
		conv23 = null;

		//**************************************
		/*
		ECGformatConverter conv10 = new ECGformatConverter();
		conv10.load_geMuse(filename);

		conv10.write_geMuse(fileOutput);
		conv10 = null;
		*/
		//---------------------------------------------	
/*		ECGformatConverter conv0 = new ECGformatConverter();
		conv0.loadRDT(filename);
		conv0.writeWFDB_16(recordName);
		//conv0.writeRDT(filePath + recordName + ".rdt");
		//conv0.writeHL7(filePath + recordName + ".xml");
		
		conv0 = null;
		//---------------------------------------------
		
		int signalsRequested = 3;
		
		ECGformatConverter conv1 = new ECGformatConverter();
		conv1.loadWFDB(filePath, recordName, signalsRequested);
		//---------------------------------------------
		conv1.writeHL7(filePath + recordName + ".xml");
		//conv1.writeRDT(filePath + recordName + ".rdt");
		conv1 = null;
				
*/		
		// list  data from the converter1.data array
		/*for (int row = 0; row < 10; row++) {  // try reading the first 10 rows. 
	        for (int sig = 0; sig < converter1.getChannelCount(); sig++) {
	        	System.out.print(converter1.getData()[sig][row]);
	        	System.out.print(", ");
	        }
	        System.out.println(" " + row + " of " + converter1.getSamplesPerChannel() + " rows");
	    }*/

		
		//**************************
		/*
		int count, rowCount;
		String filename="/mnt/hgfs/SharedFiles/RDT_101onward/jhu109.rdt";
		String recordNm = "jhu109";
		File rdtFile = new File(filename);
		
		RDTParser rdtP = new RDTParser(rdtFile);
		WFDB_wrapper wrap = new WFDB_wrapper();
		//wrap.parseHeaderFile("jhu109");
		
//		int signalsRequested = wrap.getSignalCount(recordNm);
//		samplesPerChannel = wrap.WFDBtoArray(recordNm, signalsRequested);
	    		
		rdtP.setData(wrap.data); 
		rdtP.setChannels((short)wrap.signalCount);
		rdtP.setCounts((short)wrap.samplesPerSignal);
		rdtP.viewData(10);
		
		// list same data from the WFDB_wrapper.data array for comparison
		for (int row = 0; row < 10; row++) {  // try reading the first 10 rows. 
	        for (int sig = 0; sig < signalsRequested; sig++) {
	        	System.out.print(wrap.data[sig][row]);
	        	System.out.print(" ");
	        }
	        System.out.println(" " + row + " of " + samplesPerSignal + " rows");
	    }
		*/
		/*
		int[] v;
		if (wrap.isigopen("jhu109", 3) != wrap.signalCount)
	        return;
		v = new int[wrap.signalCount];
	    for (int i = 0; i < 3; i++) {  // try reading the first 10 rows. 
	        if (wrap.getvec(v) < 0)
	            break;
	        for (int j = 0; j < wrap.signalCount; j++) {
	        	System.out.print(v[j]);
	        	System.out.print(" ");
	        }
	        System.out.println();
	    }
	    */
		/*
		RDTParser parser = new RDTParser(rdtFile);
		parser.parse();
		count = parser.getCounts();
		parser.viewHeader();
		//parser.viewData(count);
		//rowCount = parser.writeWFDB("/mnt/hgfs/SharedFiles/RDT_101onward/jhu109");
		
		rowCount = parser.writeWFDB_16("jhu109");
		System.out.println("# of samples(rows) written per channel: " + rowCount);
		System.out.println("# of samples written in total: " + rowCount*parser.getChannels());
		*/
	}

	/** Converts (recursively) all the files found in the parent directory which end with nameSuffix.
	 * 
	 * @param sParentDir
	 * @param sResultDir
	 * @param nameSuffix
	 */
	static void convertSubDirectories(String sParentDir, String sResultDir, String nameSuffix){
		int signalsRequested = 0; // zero means all.
		String currentFile="";
		//ECGformatConverter.fileFormat ff = ECGformatConverter.fileFormat.MUSEXML;
		//ECGformatConverter.fileFormat ff = ECGformatConverter.fileFormat.PHILIPS104;
		ECGformatConverter.fileFormat ff = ECGformatConverter.fileFormat.SCHILLER;
		
		File dir = new File(sParentDir);
		String[] inFiles = dir.list();
		
//		if(dir.exists() && dir.isDirectory()){ System.out.println ("Directory is a directory: " + dir); }
//		System.out.println ("Directory: " + dir);
//		System.out.println ("Directory listing: " + inFiles);

		if(inFiles!=null){
			for (int i=0; i < inFiles.length; i++) {
				currentFile = sParentDir + inFiles[i]; // trim off "-"
				File child = new File(currentFile);
				if(child.isDirectory()){
					convertSubDirectories(currentFile + "\\" , sResultDir, nameSuffix);
				}else{
					if ((currentFile.endsWith(nameSuffix))) {
						String baseFileName = child.getName();
						
						System.out.println ("FileName: " + baseFileName );
					
//						ECGformatConverter conv20 = new ECGformatConverter();
////					ff.RAW_XY_CONST_SAMPLE
//					conv20.convert(ff.GEMUSE, ff.WFDB_16, recordName, signalsRequested, inputPath, outputPath);
//					conv20 = null;

						ECGformatConverter convMUSE = new ECGformatConverter();
						//convMUSE.convert(ff.MUSEXML, ff.WFDB_16, baseFileName, signalsRequested, sParentDir, sResultDir);
						//convMUSE.convert(ff.PHILIPS104, ff.WFDB_16, baseFileName, signalsRequested, sParentDir, sResultDir);
						convMUSE.convert(ff.SCHILLER, ff.WFDB_16, baseFileName, signalsRequested, sParentDir, sResultDir);
						convMUSE = null;
					}
				}
				child = null;
			}
		}
	}
	
	/* (non-Java-doc)
	 * @see java.lang.Object#Object()
	 */
	public Main() {
		super();
		
	}

}