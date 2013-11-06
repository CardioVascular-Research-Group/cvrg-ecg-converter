package edu.jhu.icm;
/*
 * Created on Oct 30, 2009
 *
 * Copyright Stephen J. Granite, CardioVascular Research Grid (http://www.cvrgrid.org)
 *
 * The software below is licensed under Apache License, v 2.0.
 * (http://www.apache.org/licenses/LICENSE-2.0)
 *
 * @author Stephen J. Granite
 */
//package org.cvrgrid.tools;


import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/*
 * This is the main class to generate GE MUSE compatible output files, 
 * along with other files.  The tool itself looks for 3 parameters:
 * 
 * @param zipFileName - chosen file name for the zip that will be generated
 * @param startPattern - text string pattern to match at the beginning of your file names
 * @param endPattern - text string pattern to match at the end of your file names
 * 
 * Once the code has those parameters, it will search for all the files in the current
 * directory that match the start and end patterns specified.  As each of the matching files is
 * parsed, the tool generates a directory corresponding to the matching file.  The tool then divides
 * the contents of the input file into 3 output files: an XML metadata file for subject information
 * and previously calculated values, a 10 second rhythm strip file that conforms to the GE MUSE output
 * format produced by the MESA ECG Reading Center at Wake Forest University and a median file that
 * conforms to the GE MUSE output format produced by the MESA ECG Reading Center at Wake Forest University.
 * After generating directories and output files for each of the matching input files, the tool creates a 
 * manifest for the zip archive that it will generate.  The tool then generates a zip archive containing 
 * the manifest and all the folders it created.  Once the zip file has been created, the tool cleans up
 * after itself, removing all the folders and files it generated for the corresponding input files.
 * 
 * The tool requires the standard Java IO and Utility libraries to function properly.
 */

public class GEMUSESplitter {
	static final int BUFFER = 2048;
	private File fFile;
	private int iCount;
	private static StringBuffer sb = new StringBuffer();
	private String outputDir, metaDataFileName, tenSecFileName;
	private String medianFileName, zipManifestFileName, zipFileName;
	private ArrayList<String> files = new ArrayList<String>();
	boolean extraLine = false;


	/** Returns the metaDataFileName variable. 
	 * @return - file name for the metaData file generated.
	 */
	public String getMetaDataFileName() {
		return metaDataFileName;
	}

	/** Procedure to set the metaDataFileName variable.
	 * @param metaDataFileName - file name for the metaData file generated
	 */
	public void setMetaDataFileName(String metaDataFileName) {
		metaDataFileName += "_metadata.xml";
		this.metaDataFileName = getOutputDir() + File.separator + metaDataFileName;
	}

	/** Returns the tenSecFileName variable. 
	 * @return - file name for the 10 second rhythm file generated.
	 */
	public String getTenSecFileName() {
		return tenSecFileName;
	}

	/** Procedure to set the tenSecFileName variable.
	 * @param tenSecFileName - file name for the 10 second rhythm file generated
	 */
	public void setTenSecFileName(String tenSecFileName) {
		tenSecFileName += "_12345678_10sec.txt";
		this.tenSecFileName = getOutputDir() + File.separator + tenSecFileName;
	}

	/** Returns the medianFileName variable. 
	 * @return - file name for the median file generated.
	 */
	public String getMedianFileName() {
		return medianFileName;
	}

	/** Procedure to set the medianFileName variable.
	 * @param medianFileName - file name for the median file generated
	 */
	public void setMedianFileName(String medianFileName) {
		medianFileName += "_12345678_median.txt";
		this.medianFileName = getOutputDir() + File.separator + medianFileName;
	}

	/** Returns the list of files for compression. 
	 * @return - an array of fileNames to pass to the zipFile.
	 */
	public Object[] getFiles() {
		return this.files.toArray();
	}

	/** Procedure to set the files to be added to the zip archive.
	 * @param fileName - file name for the file to be placed in the zip
	 */
	public void setFiles(String fileName) {
		this.files.add(fileName);
	}

	/** Returns the zipFileName variable. 
	 * @return - file name for the zip file generated.
	 */
	public String getZipFileName() {
		return zipFileName;
	}

	/** Procedure to set the zipFileName variable.
	 * @param zipFileName - file name for the zip file generated
	 */
	public void setZipFileName(String zipFileName) {
		zipFileName = "." + File.separator + zipFileName;
		zipFileName += ".zip";
		this.zipFileName = zipFileName;
	}

	/** Returns the zipManifestFileName variable.
	 * @return - file name for the zip manifest file generated.
	 */
	public String getZipManifestFileName() {
		return zipManifestFileName;
	}

	/** Procedure to set the zipManifestFileName variable.
	 * @param zipManifestFileName - file name for the manifest file generated
	 */
	public void setZipManifestFileName(String zipManifestFileName) {
		//zipManifestFileName = "." + File.separator + zipManifestFileName;
		zipManifestFileName += ".csv";
		this.zipManifestFileName = zipManifestFileName;
	}

	
	/** Returns the extraLine variable. 
	 * @return - boolean variable to assist in formatting of output files.
	 */
	public boolean isExtraLine() {
		return extraLine;
	}

	/** Procedure to set the extraLine variable.
	 * @param extraLine - boolean variable to assist in formatting of output files
	 */
	public void setExtraLine(boolean extraLine) {
		this.extraLine = extraLine;
	}

	/** Returns the outputDir variable. 
	 * @return - file path for the output directory generated.
	 */
	public String getOutputDir() {
		return outputDir;
	}

	/** Procedure to set the outputDir variable.
	 * @param outputDir - name of the output directory generated
	 */
	public void setOutputDir(String outputDir) {
		File makeOutputDir = new File(outputDir);
		makeOutputDir.mkdir();
		this.outputDir = outputDir;
	}

	/** main for GEMUSESplitter class
	 * java -jar GEMUSESplitter.jar <filename of your zip file> <string at the start of your file names> <file extension of your input files>
	 * Example: java -jar GEMUSESplitter.jar "MESA_ECGs" "JHU" "txt"
	 * @param args - parameters entered in the command line conforming to the example provided
	 */
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length > 2) {
			GEMUSESplitter parser = new GEMUSESplitter(args[0],args[1],args[2]);
			parser.cleanUp(args[1]);
			auditlogger("Done.");
		} else {
			auditlogger("java -jar GEMUSESplitter.jar <filename of your zip file> <string at the start of your file names> <file extension of your input files>");
			auditlogger("Example: java -jar GEMUSESplitter.jar \"MESA_ECGs\" \"JHU\" \"txt\"");
		}
	}

	/** Constructor for the GEMUSESplitter class
	 * @param zipFileName - chosen file name for the zip that will be generated
	 * @param startPattern - text string pattern to match at the beginning of your file names
	 * @param endPattern - text string pattern to match at the end of your file names
	 */
	public GEMUSESplitter(String zipFileName, String startPattern, String endPattern){

		try {

			File dir = new File(".");
			String[] inFiles = dir.list();
			setZipFileName(zipFileName);
			for (int i=0; i < inFiles.length; i++) {
				if ((inFiles[i].startsWith(startPattern)) && (inFiles[i].endsWith(endPattern))) {
					auditlogger("Running: "+ (String)inFiles[i]);
					fFile = new File(inFiles[i]);
					String baseFileName = fFile.getName().substring(0, fFile.getName().lastIndexOf("."));
					setOutputDir(baseFileName);
					setMetaDataFileName(baseFileName);
					setTenSecFileName(baseFileName);
					setMedianFileName(baseFileName);
					setFiles(getMetaDataFileName());
					setFiles(getTenSecFileName());
					setFiles(getMedianFileName());
					processLineByLine();
				}
			}
			setZipManifestFileName(zipFileName);
			generateZipManifest(getFiles(), getZipManifestFileName());
			setFiles(getZipManifestFileName());
			addFileToZip(getFiles(), getZipFileName());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/** Procedure to process the input line by line
	 */
	public final void processLineByLine() throws FileNotFoundException {
		Scanner scanner = new Scanner(fFile);
		try {
			//first use a Scanner to get each line
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			sb.append("<ancillaryMESAECGMetaData>\n");
			while ( scanner.hasNextLine() ){
				processLine( scanner.nextLine() );
			}
			writeFile(sb.toString(), getTenSecFileName());
			iCount = 0;
			sb = new StringBuffer();
		}
		finally {
			//ensure the underlying stream is always closed
			scanner.close();
		}
	}

	/** Procedure to perform individual line processing
	 * @param aLine - line from file to be processed
	 */
	protected void processLine(String aLine){
		Scanner scanner = new Scanner(aLine);
		scanner.useDelimiter(":");
		String field,tmpXML;

		if (scanner.hasNext() ){
			field = scanner.next().trim();
			field = field.replaceAll(" ", "_");
			if(iCount==0){
				if (scanner.hasNext() ){
					tmpXML = "\t<" + field + ">" + scanner.next().trim() + "</" + field + ">";
					sb.append(tmpXML + "\n");
				}
				if (field.equalsIgnoreCase("Sample_Interval")) {
					iCount++;
					sb.append("</ancillaryMESAECGMetaData>\n");
					writeFile(sb.toString(), getMetaDataFileName());
					sb = new StringBuffer();
					sb.append("\n\n");
					setExtraLine(true);
				}
			} else if (iCount==1) {
				if (field.equalsIgnoreCase("Rhythm_signal")) {
					iCount++;
					writeFile(sb.toString(), getMedianFileName());
					sb = new StringBuffer();
					sb.append("\n\n\n\n");
					setExtraLine(true);
				}
				sb.append(aLine + "\n");
				if (extraLine) {
					sb.append("\n");
					setExtraLine(false);
				}
			} else {
				sb.append(aLine + "\n");				
			}
		}

	}

	/** Procedure to output information extracted from the input file
	 * @param aText - String variable containing the output
	 * @param outputFileName - file name for the output file generated 
	 */
	protected void writeFile(String aText, String outputFileName){
		try {
			// Create file
			FileWriter fstream = new FileWriter(new File(outputFileName));
			BufferedWriter out1 = new BufferedWriter(fstream);
			out1.write(aText);
			out1.close();
			fstream.close();
		} catch (Exception ex){
			System.err.println("Error: " + ex.getMessage());
		}    
	}

	/** Procedure to generate a manifest file for the zip file
	 * @param zipFileEntryNames - list of files to be zipped
	 * @param zipManifestFileName - file name for the manifest that will be generated
	 */
	protected void generateZipManifest(Object[] zipFileEntryNames, String zipManifestFileName){

		File temp = null;
		String subjectId = null;
		try {
			for (int i=0; i < zipFileEntryNames.length; i++) {
				temp = new File((String) zipFileEntryNames[i]);
				subjectId = temp.getParent();
				sb.append(subjectId + "," + temp.getPath() + "\n");
			}
			writeFile(sb.toString(),zipManifestFileName);
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	/** Procedure to zip up all the output files
	 * @param zipFileEntryNames - list of files to be zipped
	 * @param zipFileName - file name for the zip that will be generated
	 */
	protected void addFileToZip(Object[] zipFileEntryNames, String zipFileName){

		try {
			FileOutputStream dest = new FileOutputStream(zipFileName);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];
			for (int i=0; i < zipFileEntryNames.length; i++) {
				FileInputStream fi = new FileInputStream((String) zipFileEntryNames[i]);
				ZipEntry entry = new ZipEntry(((String) zipFileEntryNames[i]).replace(File.separator, "/"));
				out.putNextEntry(entry);
				int count;
				while((count = fi.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				fi.close();
			}

			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

	/** Procedure to remove all the directories and files generated to make the zip
	 * @param startPattern - text string pattern to match at the beginning of your file names
	 */
	protected void cleanUp(String startPattern) {

		String fileDeleting = null;

		Object[] filesToRemove = getFiles();
		File parentFolder = null;

		for (int i=0; i < filesToRemove.length; i++) {

			fileDeleting = (String)filesToRemove[i];
			File fileToRemove = new File(fileDeleting);
			fileToRemove.delete();
			if (fileDeleting.startsWith(startPattern)) {
				parentFolder = new File(fileToRemove.getParent());
				String[] check = parentFolder.list();
				if (check.length == 0) parentFolder.delete();
			}
		}

	}

	/** Procedure to echo values to the System out
	 * @param aObject - Object to be converted to a String for printing
	 */
	private static void auditlogger(Object aObject){
		System.out.println(String.valueOf(aObject));
	}

}