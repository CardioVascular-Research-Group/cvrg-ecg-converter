package edu.jhu.icm.ecgFormatConverter;

public interface WrapperWriter {

	public void setSamplesPerChannel(int samplesPerChannel);
	public void setChannels(int channels);
	public void setSamplingRate(float frequency); // Hz
	public void setData(int[][] data);
}
