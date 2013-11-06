package edu.jhu.icm.ecgFormatConverter;
import java.io.DataOutputStream;

/** WFDB signal information structure, converted to Java Class
 * 
 * @author Michael Shipway
 *
 */
public class WFDB_siginfo { 
	/** filename of signal file */
	String fname; 
	/** Data stream */
	DataOutputStream dos;
	/** signal description */
	String desc; 
	/** physical units (mV unless otherwise specified) */
	String units; 
	 /** gain (ADC units/physical unit, 0: uncalibrated, WFDB_Gain) */
	double gain;
	 /** initial value (of sample 0 in the signal, but is used only if the signal is stored in difference format. 
			If this field is missing, a value equal to the ADC zero is assumed.)*/
	int initval;
	/** signal group number, unsigned, WFDB_Group */
	int group; 
	/** format (8, 16, etc.) */
	int fmt; 
	/** samples per frame (>1 for oversampled signals) */
	int spf; 
	/** block size (for character special files only) */
	int bsize; 
	/** ADC resolution in bits */
	int adcres; 
	/** ADC output given 0 VDC input */
	int adczero; 
	/** ADC output given 0 physical units input */
	int baseline; 
	 /** number of samples (0: unspecified) */
	long nsamp;
	/** 16-bit checksum of all samples */
	int cksum; 
	/** signal file byte offset to sample 0 (MPS moved here from hsdata struct) */ 
	long start;	
	/** intersignal skew (in frames) (MPS moved here from hsdata struct)*/
	int skew;	
	/** most recent sample read/written */
	int samp;	
	}
