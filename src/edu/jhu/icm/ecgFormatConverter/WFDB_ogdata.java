package edu.jhu.icm.ecgFormatConverter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class WFDB_ogdata { /* shared by all signals in a group (file) */
		int data;			/* raw data to be written by w*() */
		int datb;			/* more raw data used for bit-packed formats */
		File dataFile;
		FileOutputStream fos;
		DataOutputStream dos; // data stream for the output signal
		long start;			/* byte offset to be written by setheader() */
		int bsize;			/* block size (for character special files only) */
							/* if non-zero, all writes to the output file
							are in multiples of bsize bytes */
		char count;		/* output counter for bit-packed signal */
}
