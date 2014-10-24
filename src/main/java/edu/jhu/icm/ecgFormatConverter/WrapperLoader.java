package edu.jhu.icm.ecgFormatConverter;

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
	
}
