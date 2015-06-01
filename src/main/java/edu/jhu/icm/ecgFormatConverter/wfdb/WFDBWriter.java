package edu.jhu.icm.ecgFormatConverter.wfdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileWriter;

public class WFDBWriter extends ECGFileWriter{
	
	private BufferedReader stdInputBuffer = null;
	private BufferedReader stdError = null;
	private int bitFormat = 16;
	private static final String WFDB_FILE_PATH = "wfdb.file.path";
	
	public WFDBWriter(int bits){
		
	}
	
	@Override
	public File writeToFile(String outputPath, String subjectId, ECGFile ecgFile) 
			throws ECGConverterException, IOException {
		String contentFileName = WFDBUtilities.TEMP_FOLDER + subjectId + ".txt";
		File contentFile = new File(contentFileName);
		
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(contentFile));
		
		for (int i = 0; i < ecgFile.samplesPerChannel; i++) {
			for (int j = 0; j < ecgFile.channels; j++) {
				int item = ecgFile.data[j][i];
				bWriter.write(item + "\t");
			}
			bWriter.newLine();
		}
		bWriter.close();
		
		if(ecgFile.scalingFactor == 0){
			ecgFile.scalingFactor = 200;
		}
		if(ecgFile.samplingRate == 0){
			ecgFile.samplingRate = 250;
		}
		
		String command = "wrsamp -i " + subjectId + ".txt" + " -o " + subjectId + " -F " + ecgFile.samplingRate + " -G "+ ecgFile.scalingFactor + " -O " + bitFormat;
		try {
			WFDBUtilities.executeCommand(stdError, stdInputBuffer, command, null, WFDBUtilities.TEMP_FOLDER);
		} catch (InterruptedException e) {
			throw new ECGConverterException(e.getStackTrace().toString());
		}
		stdErrorHandler();

		return new File(WFDB_FILE_PATH);
	}
	
	protected void stdErrorHandler() throws IOException{

		String error;
	    StringBuilder message = new StringBuilder();

        while ((error = stdError.readLine()) != null) {
        	if(error.length() > 0){
        		message.append(error + "\n");
        	}
        }
	}

	@Override
	public InputStream writeToInputStream(String subjectId, ECGFile ecgFile)
			throws IOException, ECGConverterException {
		File sourceFile = writeToFile(WFDBUtilities.TEMP_FOLDER, subjectId, ecgFile);
		InputStream inStream = new FileInputStream(sourceFile);
		return inStream;
	}	
}