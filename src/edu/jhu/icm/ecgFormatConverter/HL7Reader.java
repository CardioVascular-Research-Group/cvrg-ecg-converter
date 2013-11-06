package edu.jhu.icm.ecgFormatConverter;
//package edu.jhu.icm.ecgFormatConverter;

import java.io.*;
import org.jfree.data.xy.XYDataset;

import edu.jhu.icm.parser.EcgLeadData;


public class HL7Reader {

	private File hl7File;
	private String hl7FileName="";
	private long hl7FileSize=0;
	private int channels, samplingRate;
	private int counts;
	private int[][] data; 
	private int aduGain = 200;
	//[channel][index] or [column][row], changed from double, since the largest WFDB resolution is 16 bits.
//	private static final ByteOrder BYTEORDER = ByteOrder.LITTLE_ENDIAN;
//	private static final int HEADERBYTES = 4;
//	private static final int SHORTBYTES = 2;
	private static final boolean verbose = true;
//
//	public HL7Reader(File hl7File) {
//		this.hl7File = hl7File;
//	}
//	
	public HL7Reader (String hl7FileName){
		this.hl7FileName = hl7FileName;
	}

	
	/** Opens the File object which was passed into the constructor, 
	 *  validate it, parse out the header data, and then parse the 
	 *  ECG data, saving the results in private variables.
	 * 
	 * @return - success/fail
	 */
	public boolean parse() {
		boolean ret = true;
		
		this.hl7File = new File(hl7FileName);
		// validate the file 
		if (!hl7File.exists()) {
			if (verbose) {
				System.err.println(this.hl7File.getName() + " does not exist.");
			}
			return false;
		}

		hl7FileSize = hl7File.length();
		if (hl7FileSize > Long.MAX_VALUE) {
			System.err.println("file size exceeding maximum long value.");
			return false;
		}
		//**************************************

//        Reader.initLogger();
        Reader r = new Reader(hl7FileName);
        EcgLeadData ds = new EcgLeadData(r.getC9s());
        
    	double time=0;
		double volt=0;
       	int leadCount = r.getC9s().length-1;
		int pageCount = ds.getPageCount();
		int page=1;// 1 based dta page number currently being read
       	int sampleOffset=0;
       	int sampleCount=ds.getNumberOfPoints();
        data = new int[leadCount][sampleCount];

		for (page=1; page<=pageCount; page++){
			ds.setPageNumber(page);		
	        XYDataset[] allDatasets = ds.getPagedXYDatasets();
	        
//	        sampleCount = ds.totalRead;

	       	int itemCount=0;
	       	int setCount= allDatasets.length;
	        //System.out.println("Dataset count:" + setCount);
	        
	        for (int s = 0; s < setCount; s++) {
	            XYDataset oneDataset = allDatasets[s];
	            itemCount = oneDataset.getItemCount(0);
	    		//System.out.println(s + ")" + itemCount );
        		for (int i=0;i<itemCount;i++){
            		time = oneDataset.getXValue(s, i); // all leads should have the same time samples.
        			volt = oneDataset.getYValue(s, i);
        			
        			data[s][i + sampleOffset] =   (int) (volt * ds.getLeadScaleValue(s));
        		}
	        }
			sampleOffset += itemCount;
		}
        
//        ds.pageForward(2);
//        ds.writeToFile("total4.png");

	
//        for (int L=0;L<leadCount;L++){
//        	System.out.print("\n" + L + ": ");
//        	for (int i=0;i<15;i++){
//        		System.out.print(data[L][i] + ", ");
//        	}
//        	System.out.print(". . . " + sampleOffset + ") ");
//        	for (int i=sampleOffset-5;i<sampleOffset;i++){
//        		System.out.print(data[L][i] + ", ");
//        	}
//        }
			
		this.channels = leadCount;
		this.setCounts(sampleOffset);
		this.setSamplingRate(ds.getTimeIncrement(), ds.getTimeUnit());
		
		if (verbose) {
			viewData(10);
//			viewRawData(15, ds.getLeadScaleValue(0));
			System.out.println("Time Unit: " + ds.getTimeUnit());
			System.out.println("Time Increment: " + ds.getTimeIncrement());
			System.out.println("SamplingRate: " + this.samplingRate + "/second");
			System.out.println("Time scale: " + ds.getLeadScaleValue(0));
			System.out.println("Lead scale Unit: " + ds.getLeadScaleUnit(0));
		}

		return ret;
	}

	public void viewData(int count) {
		if (this.data != null) {
			for (int index = 0; index < count; index++) {
				String line = "";
				for (int channel = 0; channel < this.channels; channel++) {
					line += this.data[channel][index] + ", ";
				}
				System.out.println(line);
			}
		}
	}

	public void viewRawData(int count, double LeadScale) {
		if (this.data != null) {
			for (int index = 0; index < count; index++) {
				String line = "";
				for (int channel = 0; channel < this.channels; channel++) {
					line += this.data[channel][index]/LeadScale + ", ";
				}
				System.out.println(line);
			}
		}
	}


	public void viewHeader() {
		System.out.println("(Header) # of channels is " + this.channels
				+ "; sampling rate is " + this.samplingRate + "Hz");
	}

	public int[][] getData() {
		return data;
	}

	public void setData(int[][] dataExternal) {
		data = dataExternal;
	}
	
	public int getChannels() {
		return this.channels;
	}

	public void setChannels(int channelsIn) {
		channels = channelsIn;
	}
	
	public int getCounts() {
		return counts;
	}
	
	public void setCounts(int countsIn) {
		counts = countsIn;
	}

	public int getSamplingRate() {
		return samplingRate;
	}
	
	public int getAduGain() {
		return aduGain;
	}
	
	public void setSamplingRate(double samplingRateIn, String timeUnit) {
		if(timeUnit.equalsIgnoreCase("s")){
			samplingRate = (int)(1/samplingRateIn);
		}
	}
};
