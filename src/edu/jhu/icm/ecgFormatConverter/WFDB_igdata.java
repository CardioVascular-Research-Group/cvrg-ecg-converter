package edu.jhu.icm.ecgFormatConverter;
import java.io.File;
import java.io.FileInputStream;


public class WFDB_igdata {		/* shared by all signals in a group (file) */
	int data;			/* raw data read by r*() */
	int datb;			/* more raw data used for bit-packed formats */
	File dataFile;
	FileInputStream fis;
	byte[] buf = new byte[1];
	
	long start;			/* signal file byte offset to sample 0 */
	int bsize;			/* if non-zero, all reads from the input file
						are in multiples of bsize bytes */
	char count;			/* input counter for bit-packed signal */
	char seek;			/* 0: do not seek on file, 1: seeks permitted */
	int stat;			/* signal file status flag */
}