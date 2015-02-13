package edu.jhu.icm.ecgFormatConverter;

import java.util.List;


public interface WrapperLoader {

	/**
	 * Get the frequency
	 * */
	public float getSamplingRate();
	/**
	 * Get the number of points per lead
	 * */
	public int getSamplesPerChannel();
	/**
	 * Get the number of leads
	 * */
	public int getChannels();
	/**
	 * Get the ECG point matrix
	 * */
	public int[][] getData();
	/**
	 * Get the ECG gain 
	 * */
	public int getAduGain();
	/**
	 * Get the number of points in the entire ECG. <br>
	 * <br>
	 * @return (getChannels() * getSampleCount())
	 * */
	public int getNumberOfPoints();
	/**
	 * Get the lead names in order. <br>
	 * <br>
	 * @return List of extracted lead names from original file. 
	 * */
	public List<String> getLeadNames();
	
}
