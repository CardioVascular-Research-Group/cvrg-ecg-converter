package edu.jhu.icm.ecgFormatConverter.wfdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;

public class WFDBUtilities {
	
	private static final String TEMP_FOLDER = "temp.folder.path";

	public static boolean executeCommand(BufferedReader stdError, BufferedReader stdInputBuffer, String sCommand, 
			String[] asEnvVar, String sWorkingDir) throws IOException, InterruptedException {

		if (asEnvVar == null) {
			asEnvVar = new String[0];
		}

		File fWorkingDir = new File(sWorkingDir); // converts the dir name to
													// File for exec command.
		Runtime rt = Runtime.getRuntime();
		String[] commandArray = sCommand.split("\\|");
		if (commandArray.length == 1) {
			Process process = rt.exec(sCommand, asEnvVar, fWorkingDir);
			InputStream is = process.getInputStream(); // The input stream for
														// this method comes
														// from the output from
														// rt.exec()
			InputStreamReader isr = new InputStreamReader(is);
			stdInputBuffer = new BufferedReader(isr);
			InputStream errs = process.getErrorStream();
			InputStreamReader esr = new InputStreamReader(errs);
			stdError = new BufferedReader(esr);
		} else {

			Process[] processArray = new Process[commandArray.length];
			// Start processes: ps ax | grep rbe | grep JavaVM
			for (int i = 0; i < commandArray.length; i++) {
				processArray[i] = rt.exec(commandArray[i].trim(), asEnvVar,
						fWorkingDir);
			}
			// Start piping
			java.io.InputStream in = Piper.pipe(processArray);

			// Show output of last process
			InputStreamReader isr = new InputStreamReader(in);
			stdInputBuffer = new BufferedReader(isr);
			InputStream errs = processArray[processArray.length - 1]
					.getErrorStream();
			InputStreamReader esr = new InputStreamReader(errs);
			stdError = new BufferedReader(esr);
		}

		return true;
	}
	
	public static String stdReturnHandler(BufferedReader stdInputBuffer, boolean withLineBreak) 
			throws IOException{
	    
		StringBuilder sb = new StringBuilder();
		String tempLine;

	    while ((tempLine = stdInputBuffer.readLine()) != null) {
	    	sb.append(tempLine);
	    	if(withLineBreak){
	    		sb.append('\n');
	    	}
	    }
	    return sb.toString();
	}
	
	public static int[][] stdReturnMethodHandler(BufferedReader stdInputBuffer) 
			throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		int[][] data = null;
		String tempLine;
		int lineNumber = 0;

	    while ((tempLine = stdInputBuffer.readLine()) != null) {
	    	data = processReturnLine(tempLine, lineNumber, data);
	    	lineNumber ++;
	    }
	    return data;
	}
	
	/** This function prints messages resulting from runtime problems to the system standard error
	 * @return Boolean variable:  True if there are no errors, false if there are errors.
	 * 
	 * @throws IOException
	 */	
	public static boolean stdErrorHandler(BufferedReader stdError) throws IOException{
		boolean bRet = true;
		String error;

	    // read any errors from the attempted command
        while ((error = stdError.readLine()) != null) {
        	if(error.length() > 0){
				bRet = false;
        	}
        }
		return bRet;
	}
	
	public static String getRecordName(String filePath) throws ECGConverterException, IOException{
		String[] filePathElements = filePath.split(File.separator);
		int size = filePathElements.length;
		if(size == 0){
			throw new ECGConverterException("Invalid File Path for WFDB Read.");
		}
		String fileName = filePathElements[size - 1];
		String[] fileNameElements = fileName.split("\\.");
		return fileNameElements[0];
	}
	
	/** This writes the output of the execution to a file instead of standard output
	 * 
	 * @param outputFilename
	 * @throws IOException
	 */
	protected void stdReturnHandler(BufferedReader stdInputBuffer,	String outputFilename) throws IOException {
		String line;

		// Create file
		FileWriter fstream = new FileWriter(outputFilename);
		BufferedWriter bwOut = new BufferedWriter(fstream);

		while ((line = stdInputBuffer.readLine()) != null) {

			bwOut.write(line);
			bwOut.newLine();
		}

		bwOut.flush();
		// Close the output stream
		bwOut.close();
	}
	
	/** This writes the output of the execution to a file instead of standard output
	 * 
	 * @param outputFilename
	 * @throws IOException
	 */
	protected void stdCSVReturnHandler(BufferedReader stdInputBuffer, String outputFilename, String[] headers) throws IOException {
		String line;

		// Create file
		FileWriter fstream = new FileWriter(outputFilename);
		BufferedWriter bwOut = new BufferedWriter(fstream);

		if (headers != null) {
			String headerLine = "";
			for (String string : headers) {
				headerLine += (string + ',');
			}
			headerLine = headerLine.substring(0, headerLine.length() - 1);
			bwOut.write(headerLine);
			bwOut.newLine();
		}

		while ((line = stdInputBuffer.readLine()) != null) {

			line = line.replaceAll("\\s+", ",").replaceAll("\\t", ", ");
			if (line.charAt(0) == ',') {
				line = line.substring(1, line.length());
			}

			bwOut.write(line);
			bwOut.newLine();
		}
		bwOut.flush();
		// Close the output stream
		bwOut.close();
	}
	
	protected static int[][] processReturnLine(String line, int lineNum, int[][] data){
		String[] aSigNames;
		int signalCount = 0;
		String[] signalNames;
		if(lineNum == 0){
    		aSigNames = line.split(",");
   			signalCount = (aSigNames.length-1);
    		signalNames = new String[signalCount];
    		for(int sig=1;sig <= signalCount; sig++){ // zeroth column is time, not a signal
    			signalNames[sig-1] = aSigNames[sig];// column names to be used later to verify the order.
    		}			    	  
    	}else if (lineNum > 1){
		    // data.
    		String[] aSample = line.split(",");
   			signalCount = (aSample.length-1);
			for(int sig=1;sig <= signalCount;sig++){ // zeroth column is time, not a signal
				float fSamp;
				try{ // Check if value is a not a number, e.g. "-" or "na", substitute zero so rdsamp won't break; Mike Shipway (7/21/2014)
					fSamp  = Float.parseFloat(aSample[sig]); // assumes unit is float millivolts.
				}catch(NumberFormatException nfe){
					fSamp = 0;
				}
					
				data[sig-1][lineNum-2] = (int)(fSamp*1000);// convert float millivolts to integer microvolts.
			}		    	  
		}	
		return data;
    }
	
	public static String createTempFiles(InputStream dataStream, InputStream headerStream, String subjectId) throws IOException{

		String path = getProperties().getProperty(TEMP_FOLDER);
		createFile(headerStream, path + subjectId + ".hea");
		return createFile(dataStream, path + subjectId + ".dat");
	}
	
	public static String createFile(InputStream inputStream, String path) throws IOException{
		File file = new File(path);
		byte[] buffer = new byte[inputStream.available()];
		inputStream.read(buffer);
		OutputStream outputStream = new FileOutputStream(file);
		outputStream.write(buffer);
		inputStream.close();
		outputStream.close();
		return file.getAbsolutePath();
	}
	
	private static Properties getProperties() throws IOException{
		Properties props = new Properties();
		try(InputStream resourceStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
		    props.load(resourceStream);
		}
		return props;
	}
	
	public static String getDirectory(String fullFilePath){
		String[] fullPath = fullFilePath.split(File.separator);
		int endIndex = fullFilePath.length() - fullPath[fullPath.length -1].length();
		return fullFilePath.substring(0, endIndex);
	}
}