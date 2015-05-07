package edu.jhu.icm.ecgFormatConverter.wfdb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileWriter;
import edu.jhu.icm.ecgFormatConverter.ECGFormatWriter;

public class WFDBWriter extends ECGFileWriter{

	/** Takes the ECG samples which are in the data[][] array and write them out as a WFDB file. 
	 * @throws InterruptedException 
	 * @throws IOException */
	public int arrayToWFDB() throws IOException, InterruptedException {
//		
//		int ret = 0;
//		String contentFileName = filePath + recordName+".txt";
//		File contentFile = new File(contentFileName);
//		
//		//Create a temporary file with data content
//
//			BufferedWriter bWriter = new BufferedWriter(new FileWriter(contentFile));
//			
//			for (int i = 0; i < samplesPerSignal; i++) {
//				for (int j = 0; j < signalCount; j++) {
//					int item = data[j][i];
//					bWriter.write(item + "\t");
//				}
//				bWriter.newLine();
//			}
//			bWriter.close();
//
//		
//		boolean result = false;
//
//			//Check the parameters with the default values
//			if(gain == 0){
//				gain = 200;
//			}
//			if(sampleFrequency == 0){
//				sampleFrequency = 250;
//			}
//			if(fmt == 0){
//				fmt = 16;
//			}
//			
//			String command = "wrsamp -i " + recordName + ".txt" +" -o " + recordName + " -F "+ sampleFrequency + " -G "+ gain + " -O " + fmt;
//			result = WFDBUtilities.executeCommand(stdError, stdInputBuffer, command, null, filePath);
//			result &= WFDBUtilities.stdErrorHandler(stdError);
//			
//			if(result){
//				outputFilenames = new String[2];
//				outputFilenames[0] = filePath + recordName + ".dat";
//				outputFilenames[1] = filePath + recordName + ".hea";
//				ret = samplesPerSignal;
//			}
//
//		
//		if(contentFile.exists()){
//			contentFile.delete();
//		}
//		
//		return ret;
		return 0;
	}

	@Override
	public File writeToFile(String outputPath, String recordName, ECGFile ecgFile) 
			throws ECGConverterException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream writeToInputStream(String recordName, ECGFile ecgFile)
			throws IOException, ECGConverterException {
		// TODO Auto-generated method stub
		return null;
	}

	
}
