package edu.jhu.icm.ecgFormatConverter.wfdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

public abstract class ApplicationWrapper {

	protected String[] outputFilenames = null;
//	protected BufferedReader stdInputBuffer = null;
//	protected BufferedReader stdError = null;
	protected Logger log = Logger.getLogger(ApplicationWrapper.class);
	protected int lineNum = 0;
	
	/** Executes the command and pipes the response and errors to stdInputBuffer and stdError respectively.
	 * 
	 * @param sCommand - a specified system command.
	 * @param asEnvVar - array of strings, each element of which has environment variable settings in format name=value.
	 * @param sWorkingDir - the working directory of the subprocess, or null if the subprocess should inherit the working directory of the current process. 
	 * @return 
	 */
//	protected boolean executeCommand(String sCommand, String[] asEnvVar, String sWorkingDir){
//		
//		if(asEnvVar == null){
//			asEnvVar = new String[0];
//		}
//		
//		debugPrintln("++ executeCommand(" + sCommand + ")" );
//		debugPrintln(", asEnvVar[" + asEnvVar.length + "]");
//		debugPrintln(", " + sWorkingDir + ")");
//		boolean bRet = true;	
//		
//		try {
//			File fWorkingDir = new File(sWorkingDir); //converts the dir name to File for exec command.
//			Runtime rt = Runtime.getRuntime();
//			String[] commandArray = sCommand.split("\\|");
//			if(commandArray.length == 1 ){
//				Process process = rt.exec(sCommand, asEnvVar, fWorkingDir);
//				InputStream is = process.getInputStream();  // The input stream for this method comes from the output from rt.exec()
//				InputStreamReader isr = new InputStreamReader(is);
//				stdInputBuffer = new BufferedReader(isr);
//				InputStream errs = process.getErrorStream();
//				InputStreamReader esr = new InputStreamReader(errs);
//				stdError = new BufferedReader(esr);	
//			}else{
//				
//				Process[] processArray = new Process[commandArray.length];
//				// Start processes: ps ax | grep rbe | grep JavaVM
//				for (int i = 0; i < commandArray.length; i++) {
//					processArray[i] =  rt.exec(commandArray[i].trim(), asEnvVar, fWorkingDir);
//				}
//		        // Start piping
//		        java.io.InputStream in = Piper.pipe(processArray);
//		        
//		        // Show output of last process
//		        InputStreamReader isr = new InputStreamReader(in);
//				stdInputBuffer = new BufferedReader(isr);
//				InputStream errs = processArray[processArray.length-1].getErrorStream();
//				InputStreamReader esr = new InputStreamReader(errs);
//				stdError = new BufferedReader(esr);	
//			}
//			
//		} catch (IOException ioe) {
//			log.error("IOException Message: executeCommand(" + sCommand + ")" + ioe.getMessage());
//			bRet = false;
//		} catch (Exception e) {
//			log.error("Exception Message: executeCommand(" + sCommand + ")" + e.getMessage());
//			bRet = false;
//		}
//		debugPrintln("++ returning: " + bRet);
//		return bRet;
//	}
	
	/** This writes the output to the standard output
	 * 
	 * @throws IOException
	 */	
	
//	protected String stdReturnHandler(boolean withLineBreak) throws IOException{
//	    
//		StringBuilder sb = new StringBuilder();
//		lineNum = 0;
//		String tempLine;
//		
//	    debugPrintln("Here is the returned text of the command (if any):");
//	    while ((tempLine = stdInputBuffer.readLine()) != null) {
//	    	debugPrintln(lineNum + ")" + tempLine);
//	    	sb.append(tempLine);
//	    	if(withLineBreak){
//	    		sb.append('\n');
//	    	}
//	    	lineNum++;
//	    }
//	    
//	    return sb.toString();
//	}
	
//	protected void stdReturnMethodHandler() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
//	    
//		lineNum = 0;
//		String tempLine;
//		
//	    debugPrintln("Here is the returned text of the command (if any):");
//	    while ((tempLine = stdInputBuffer.readLine()) != null) {
//	    	debugPrintln(lineNum + ")" + tempLine);
//	    	this.processReturnLine(tempLine);
//	    	lineNum++;
//	    }
//	}
	
	protected abstract void processReturnLine(String line);
	
//	/** This writes the output of the execution to a file instead of standard output
//	 * 
//	 * @param outputFilename
//	 * @throws IOException
//	 */
//	protected void stdReturnHandler(String outputFilename) throws IOException{
//	    String line;
//		try{
//			// Create file 
//			debugPrintln("stdReturnHandler(FName) Creating output file: " + outputFilename);
//			FileWriter fstream = new FileWriter(outputFilename);
//			BufferedWriter bwOut = new BufferedWriter(fstream);
//
//			lineNum = 0;
//		    debugPrintln("Here is the returned text of the command (if any): \"");
//		    while ((line = stdInputBuffer.readLine()) != null) {
//		    	
//		    	bwOut.write(line);
//		    	bwOut.newLine();
//		    	if (lineNum<10){
//		    		debugPrintln(lineNum + ")" + line);
//		    	}
//		    	
//		    	lineNum++;
//		    }
//		    debugPrintln(". . . ");
//		    debugPrintln(lineNum + ")" + line);
//	        debugPrintln("\"");
//			bwOut.flush();
//			//Close the output stream
//			bwOut.close();
//		}catch (Exception e){//Catch exception if any
//		   log.error("Error: " + e.getMessage());
//		}
//	}
	
	
//	/** This writes the output of the execution to a file instead of standard output
//	 * 
//	 * @param outputFilename
//	 * @throws IOException
//	 */
//	protected void stdCSVReturnHandler(String outputFilename, String[] headers) throws IOException{
//	    String line;
//		try{
//			// Create file 
//			debugPrintln("stdReturnHandler(FName) Creating output file: " + outputFilename);
//			FileWriter fstream = new FileWriter(outputFilename);
//			BufferedWriter bwOut = new BufferedWriter(fstream);
//
//			lineNum = 0;
//		    debugPrintln("Here is the returned text of the command (if any): \"");
//		    
//		    if(headers != null ){
//		    	String headerLine = "";
//	    		for (String string : headers) {
//					headerLine += (string+','); 
//				}
//	    		headerLine = headerLine.substring(0, headerLine.length()-1);
//	    		bwOut.write(headerLine);
//		    	bwOut.newLine();
//		    }
//		    
//		    while ((line = stdInputBuffer.readLine()) != null) {
//		    	
//		    	line = line.replaceAll("\\s+",",").replaceAll("\\t",", "); 
//		    	if(line.charAt(0) == ','){
//		    		line = line.substring(1, line.length());
//		    	}
//		    	
//		    	bwOut.write(line);
//		    	bwOut.newLine();
//		    	if (lineNum<10){
//		    		debugPrintln(lineNum + ")" + line);
//		    	}
//		    	
//		    	lineNum++;
//		    }
//		    debugPrintln(". . . ");
//		    debugPrintln(lineNum + ")" + line);
//	        debugPrintln("\"");
//			bwOut.flush();
//			//Close the output stream
//			bwOut.close();
//		}catch (Exception e){//Catch exception if any
//		   log.error("Error: " + e.getMessage());
//		}
//	}
	
//	/** This function prints messages resulting from runtime problems to the system standard error
//	 * @return Boolean variable:  True if there are no errors, false if there are errors.
//	 * 
//	 * @throws IOException
//	 */	
//	protected boolean stdErrorHandler() throws IOException{
//		boolean bRet = true;
//		String error;
//	    lineNum = 0;
//
//	    // read any errors from the attempted command
//	    debugPrintln("");
//	    debugPrintln("Here is the standard error of the command (if any): \"");
//        while ((error = stdError.readLine()) != null) {
//        	if(error.length() > 0){
//	        	log.error(lineNum + ">" + error);
//	            lineNum++;
//				bRet = false;
//        	}
//        }
//        debugPrintln("\"");
//		return bRet;
//
//	}
	
	protected void debugPrintln(String text){
		log.debug("- ApplicationWrapper - " + text);
	}
	
	public String[] getOutputFilenames() {
		return outputFilenames;
	}
	
}
