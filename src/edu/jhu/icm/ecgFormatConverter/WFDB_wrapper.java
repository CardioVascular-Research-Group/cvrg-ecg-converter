package edu.jhu.icm.ecgFormatConverter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties; 
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class WFDB_wrapper {
	public short fmt; /// WFDB encoding format (8,16 ...)	
	public int signalCount;// number of signals
	public float sampleFrequency; // Hz
	public float counterFrequency=0; /// Hz counter frequency (in ticks per second) [optional]
	public float counterBase=0; /// base counter value [optional]if the counterFrequency is present
	public int samplesPerSignal; // number of samples per signal 
	public int segmentCount; /// number of segments [optional] (for header file).
	public String baseTime = "";/// HH:MM:SS [optional]if the samplesPerSignal is present.
	public String baseDate = ""; /// DD/MM/YYYY [optional]if the baseTime is present. 
	public int sampleADCResolution =12;
	public int sampleADCZero = 0;
	public String recordName;
	private String filePath;
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		if(!filePath.endsWith(sep)) filePath = filePath + sep; // Because this class was written with the assumption that the path ends with "/".
		this.filePath = filePath;
	}

	public int gain = 200;
	public String[] signalName;
	public int[][] data;
	public int[] WFDB_FMT_LIST = {0, 8, 16, 61, 80, 160, 212, 310, 311};	
	private int WFDB_INVALID_SAMPLE = -32768; /// samples from getvec or getframe with this value are not valid 
	private WFDB_ogdata ogd[];
	private WFDB_igdata igd[];
	WFDB_siginfo [] siarray;
	public boolean in_msrec =false; /// record is in multi-segment record  
	private static final boolean verbose = true;
	private String sep = File.separator;

	/* These variables relate to open input signals. */
	int maxisig;	// max number of input signals 	
	int maxigroup;	// max number of input signal groups 
	int nisig;// number of open input signals
	int nigroups;	// number of open input signal groups
	int maxspf;	// max allowed value for ispfmax 	
	int ispfmax=1;	// max number of samples of any open signal per input frame 
	long istime = 0;	// time of next input sample , units are sample intervals     
	int gvpad = 0;		// getvec padding (if non-zero, replace invalid samples with previous valid samples) 
	int spfTotal=0; // Total # of samples per frame, from all signals.
	public int gvmode = 1, WFDB_HIGHRES=1;
	public boolean dsbuf=false;

	
	public WFDB_wrapper() {
		Properties pr = System.getProperties();
		pr.put("java.library.path", "/usr/lib");
		System.setProperties(pr);
	}
	
	/** Reads the specified WFDB record into the data array
	 * 
	 * @param recordNm - Name of the record to read.
	 * @param signalsRequested - Number of signals to read, starting with 1st signal.
	 * @return samplesPerSignal
	 */
	public int WFDBtoArray(String recordNm, int signalsRequested) {
		 System.out.println("************** Running New WFDB reading process ************** ");
		 
		try {
		    // Execute command
		    String command = "sampfreq -H " + filePath + recordNm;
		    System.out.println("WFDBtoArray command: " + command);
		    
		    Process child = Runtime.getRuntime().exec(command);

		    // Get the input stream and read from it
		    InputStream in = child.getInputStream();
		    int c;
		    String freq = "";
		    while ((c = in.read()) != -1) {
		        freq = freq + ((char)c);
		    }
		    in.close();
	        System.out.println("WFDBtoArray frequency: " + freq);
			sampleFrequency = Float.parseFloat(freq);

		} catch (IOException ioe) {
			System.err.println("IOException Message: sampfreq " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (Exception e) {
			System.err.println("Exception Message: sampfreq " + e.getMessage());
			e.printStackTrace();
		}
		

		//---------------------------
	    try{ // read data into the local array, count samplesPerSignal
	    	
	    	signalCount = getSignalCount(recordNm);
	    	System.out.println("samplesPerSignal: " + samplesPerSignal);
	    	data = new int[signalCount][samplesPerSignal];
	    	
			String command = "rdsamp -r " + filePath + recordNm + " -c -p -v -H";
			
			Process process = Runtime.getRuntime().exec(command);
		    InputStream is = process.getInputStream();
		    InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader stdInputBuffer = new BufferedReader(isr);
		    
		    InputStream errs = process.getErrorStream();
		    InputStreamReader esr = new InputStreamReader(errs);
		    BufferedReader stdError = new BufferedReader(esr);

		    
		    String line, error;
	
		    String[] aSigNames, aSample;
		    int lineNum = 0;
		    while ((line = stdInputBuffer.readLine()) != null) {
		    	if(lineNum==0){
		    		aSigNames = line.split(",");
		    		signalName = new String[signalCount];
		    		for(int sig=1;sig<=signalCount;sig++){ // zeroth column is time, not a signal
		    			signalName[sig-1] = aSigNames[sig];// column names to be used later to verify the order.
		    		}			    	  
		    	}else if (lineNum > 1){
	    		    // data.
	    			aSample = line.split(",");
	    			for(int sig=1;sig<=signalCount;sig++){ // zeroth column is time, not a signal
	    				data[sig-1][lineNum-2] = (int)(Float.parseFloat(aSample[sig])*1000);// convert float millivolts to integer microvolts.
	    			}			    	  
	    		}		    	  
		    	
		    	lineNum++;
		    }		    
		    
		    if(verbose){
		    	System.out.print("");
		    	System.out.println("First 10 rows of data read:");
			    for (int row = 0; row < 10; row++) {  // try reading the first 10 rows. 
			        for (int sig = 0; sig < signalCount; sig++) {
						System.out.print(data[sig][row] + " ");
			        }
					System.out.println();
			    }
		    }
		    // read any errors from the attempted command
		    System.out.println("");
	        System.out.println("Here is the standard error of the command (if any):\n");
	        while ((error = stdError.readLine()) != null) {
	            System.out.println(error);
	        }

		
		} catch (IOException ioe) {
			System.err.println("IOException Message: rdsamp " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (Exception e) {
			System.err.println("Exception Message: rdsamp " + e.getMessage());
			e.printStackTrace();
		}
		
	    return samplesPerSignal;
	}

	/** Takes the ECG samples which are in the data[][] array and write them out as a WFDB file. */
	public int arrayToWFDB(int aduGain) {
		gain = aduGain;
		int row = 0;
		int putRet = 0;
		DataOutputStream dos;
		FileOutputStream fos;
		int[] vec = new int[signalCount]; // a single row of data, one sample for each signal.
		File dataFile = new File(filePath + recordName + ".dat");

		siarray = new WFDB_siginfo[signalCount];
		// write data file
		// transfer data to the sample array.
		try {
			fos = new FileOutputStream(dataFile);
			dos = new DataOutputStream(fos);
			for (int sc = 0; sc < signalCount; sc++) {
				// System.err.println("Creating SignalInfo for channel #" + sc);
				WFDB_siginfo si = new WFDB_siginfo();
				si.fname = recordName + ".dat";
				si.dos = dos;
				si.desc= "column " + sc + " ";;
				si.units = "";
				si.group = 0;
				si.fmt = fmt;
				si.spf = 1;
				si.bsize = 0;
				si.adcres= sampleADCResolution;
				si.adczero = sampleADCZero;
				si.baseline = 0;
				si.gain=gain;
				si.spf = 1; // samples per frame
				si.cksum =0;				
				
				siarray[sc] = si;
			}
 
			osigfopen(siarray); //open the output file(s)
			
			if (verbose) {
				System.out.println("First three rows of values written:");
			}
			for (row = 0; row < samplesPerSignal; row++) {
				//if((row/1000)-((int)row/1000) == 0) System.err.print(".");

				//System.err.print("Row #" + row);
				for (int sc = 0; sc < signalCount; sc++) {
					//System.err.print("  " + (int) data[sc][row]);
					vec[sc]=data[sc][row];
					if ((row < 3) & verbose) {
						System.out.print(data[sc][row] + " ");
					}
				}
				putRet = putvec(vec, siarray);
				if ((row < 3) & verbose) {
					System.out.println();
				}
			}
			System.err.println();
			osigclose();
		} catch (Exception e) {
			System.err.println("Exception Message: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// write header file
			writeMFDBHeaderFile();
		}

		return row;
	}

	/** Opens input signal files for a selected record, sets this.recordName, then calls the overload 
	 * 
	 * @param record - record name, without a file extension.
	 * @param nsigToRead - number of signals to read, can be less than or equal to # of signals in the file.
	 * @return - success/failure
	 * >0 Success: the returned value is the number of input signals (i.e., the number of valid entries in siarray) 
	 *  0 Failure: no input signals available 
	 * -1 Failure: unable to read header file (probably incorrect record name) 
	 * -2 Failure: incorrect header file format
	 */
	int isigopen(String record, int nsigToRead)
	{
		// Remove trailing .hea, if any, from record name. 
		recordName = wfdb_striphea(record);
		return (isigopen( nsigToRead));
	}
	
	/** Opens input signal files for a selected record, using this.recordName 
	 * As a special case, if nsigToRead is 0. 
	 * In this case, then returns the number of signals in record without opening them.
	 * 
	 * If nsigToRead is greater than 0, isigopen normally returns the number of input signals 
	 * it actually opened, which may be less than nsigToRead but is never greater than nsigToRead.
	 * Signal numbers are assigned in the order in which signals are specified in the 
	 * ‘hea’ file for the record; on return from isigopen, information for signal i will be found in siarray[i]. 
	 * 
	 * @param nsigToRead - number of signals to read, can be less than or equal to # of signals in the file.
	 * @return - success/failure
	 * >0 Success: the returned value is the number of input signals (i.e., the number of valid entries in siarray) 
	 *  0 Failure: no input signals available 
	 * -1 Failure: unable to read header file (probably incorrect record name) 
	 * -2 Failure: incorrect header file format
	 */
	int isigopen(int nsigToRead) {

		int navail; //, ngroups, nn;
		//struct hsdata *hs;
		//struct isdata *is;
		//struct igdata *ig;
		int s, si; //, sj; // was WFDB_Signal
		//int g=0; // was WFDB_Group

		/* Close previously opened input signals unless otherwise requested. */
		//if (*record == '+') record++;
		//else isigclose();


		/* Save the current record name. */
		//if (!in_msrec) wfdb_setirec(recordName);

		/* Read the header and determine how many signals are available. */
		if ((navail = parseHeaderFile(recordName)) <= 0) {
			/*
			if (navail == 0 && segments) {	// this is a multi-segment record 
				in_msrec = true;
				// Open the first segment to get signal information. 
				if ((navail = readheader(segp->recname)) >= 0) {
					if (msbtime == 0L) msbtime = btime;
					if (msbdate == (WFDB_Date)0) msbdate = bdate;
				}
			}
			*/
			if (navail == 0 && (nsigToRead>0))
				System.err.println("isigopen: record " + recordName + " has no signals\n");
			if (navail <= 0)
				return (navail);
		}

		/* Determine how many new signals we should attempt to open.  The caller's
		upper limit on this number is nsigToRead, and the upper limit defined by the
		header is navail. */
		if (nsigToRead > navail) nsigToRead = navail;

		/* Allocate input signals and signal group workspace. */
		/*
		nn = nisig + nsigToRead;
		if (igd.length != nn)
			return (-1);	// failed, nisig is unchanged, allocisig emits error 
		else
			nsigToRead = nn;
		
		nn = nigroups + hsd[nsigToRead-nisig-1]->info.group + 1;
		igd = new WFDB_igdata[nn];
		if (igd.length != nn)
			return (-1);	// failed, allocigroup emits error
		else
			ngroups = nn;
*/
		/* Set default buffer size (if not set already by setibsize). */
		// if (ibsize <= 0) ibsize = BUFSIZ;

		/* Open the signal files.  One signal group is handled per iteration.  In
		this loop, si counts through the entries that have been read from hsd,
		and s counts the entries that have been added to isd. */
		nigroups=0;
		int groupPrev=0;
		igd = new WFDB_igdata[maxigroup];
		for(s=0;s<signalCount;s++) {
			if (s==0){// it is the first signal
				igd[groupPrev] = newIngroupData(siarray[s]);
			}else{		
				if (groupPrev != siarray[s].group){ //This signal is in a different group than the previous signal.
					groupPrev++;
					igd[groupPrev] = newIngroupData(siarray[s]);
				}else{ // This signal belongs to the same group as the previous signal.		
					
					if(siarray[s].fmt != siarray[s-1].fmt){
						System.err.println("isigfopen: error in specification of signals. Format is different on signals " + (s-1) + " and " + s);
					}
				}
			}				
		}
		nisig = s;
		/*
		for (g = si = s = 0; si < navail && s < nsigToRead; si = sj) {
			hs = hsd[si];
			is = isd[nisig+s];
			ig = igd[nigroups+g];

			// Find out how many signals are in this group. 
			for (sj = si + 1; sj < navail; sj++)
				if (hsd[sj]->info.group != hs->info.group) break;

			// Skip this group if there are too few slots in the caller's array. 
			if (sj - si > nsigToRead - s) continue;

			// Set the buffer size and the seek capability flag. 
			if (hs->info.bsize < 0) {
				ig->bsize = hs->info.bsize = -hs->info.bsize;
				ig->seek = 0;
			}
			else {
				if ((ig->bsize = hs->info.bsize) == 0) ig->bsize = ibsize;
				ig->seek = 1;
			}
			SALLOC(ig->buf, 1, ig->bsize);

			// Check that the signal file is readable. 
			if (hs->info.fmt == 0)
				ig->fp = NULL;	// Don't open a file for a null signal. /
			else { 
				ig->fp = wfdb_open(hs->info.fname, (char *)NULL, WFDB_READ);
				// Skip this group if the signal file can't be opened. /
				if (ig->fp == NULL) {
					SFREE(ig->buf);
					continue;
				}
			}

			// All tests passed -- fill in remaining data for this group. 
			ig->be = ig->bp = ig->buf + ig->bsize;
			ig->start = hs->start;
			ig->stat = 1;
			while (si < sj && s < nsigToRead) {
				copysi(&is->info, &hs->info);
				is->info.group = nigroups + g;
				is->skew = hs->skew;
				++s;
				if (++si < sj) {
					hs = hsd[si];
					is = isd[nisig + s];
				}
			}
			g++;
		}
*/
		/* Produce a warning message if none of the requested signals could be
		opened. */
		/*
		 * if (s == 0 && nsigToRead)
			wfdb_error("isigopen: none of the signals for record %s is readable\n",
			record);
		*/

		/* Copy the WFDB_Siginfo structures to the caller's array.  Use these
		data to construct the initial sample vector, and to determine the
		maximum number of samples per signal per frame and the maximum skew. */
		/*
		for (si = 0; si < s; si++) {
			is = isd[nisig + si];
			if (siarray) 
				copysi(&siarray[si], &is->info);
			is->samp = is->info.initval;
			if (ispfmax < is->info.spf) ispfmax = is->info.spf;
			if (skewmax < is->skew) skewmax = is->skew;
		}
		*/
		//setgvmode(gvmode);	/* Reset sfreq if appropriate. */
		//gvc = ispfmax;	/* Initialize getvec's sample-within-frame counter. */
		//nisig += s;		/* Update the count of open input signals. */
		//nigroups += g;	/* Update the count of open input signal groups. */

		//if (sigmap_init() < 0)
		//	return (-1);

		/* Determine the total number of samples per frame. */
		for (si = spfTotal = 0; si < nisig; si++)
		spfTotal += siarray[si].spf;

		/* Allocate workspace for getvec, isgsettime, and tnextvec. */
		/*
		if (framelen > tuvlen) {
			SREALLOC(tvector, framelen, sizeof(WFDB_Sample));
			SREALLOC(uvector, framelen, sizeof(WFDB_Sample));
			if (nvsig > nisig) {
				int vframelen;
				for (si = vframelen = 0; si < nvsig; si++)
					vframelen += vsd[si]->info.spf;
				SREALLOC(vvector, vframelen, sizeof(WFDB_Sample));
			}
			else
				SREALLOC(vvector, framelen, sizeof(WFDB_Sample));
		}
		tuvlen = framelen;
		*/
		/* If deskewing is required, allocate the deskewing buffer (unless this is
		a multi-segment record and dsbuf has been allocated already). */
		/*if (skewmax != 0 && (!in_msrec || dsbuf == null)) {
			dsbi = -1;	// mark buffer contents as invalid 
			dsblen = framelen * (skewmax + 1);
			SALLOC(dsbuf, dsblen, sizeof(WFDB_Sample));
		}*/
		return (s);
	}
	
	public int osigfopen(WFDB_siginfo[] siarray){
		int ret = 0;
		int fnameLen=0, descLen=0;
		int nosig = 0;		// number of open output signals 
		WFDB_siginfo si;
		
		// Close any open output signals. 
		osigclose();

		// Do nothing further if there are no signals to open. 
		if (siarray.length == 0 || signalCount == 0) return (0);
		
		int groupPrev = -1, groupCount=0;
		for(int s=0; s < signalCount; s++){
			si = siarray[s];
			// test that the signal specification is not blatantly wrong.
			fnameLen = si.fname.length();
			descLen = si.desc.length();
			if((fnameLen + descLen) > 80){
				if(si.fname.length() >80){
					System.err.println("Exception: osigfopen: error in specification of signal " + s +  ": The length of the fname string should be 80 characters or less, but is " + fnameLen);
					return -2;
				}else{
					//truncate the description to the space available.
					si.desc = si.desc.substring(0, fnameLen);
					System.err.println("Warning: osigfopen: error in specification of signal " + s +  ": The combined lengths of the fname and desc strings should be 80 characters or less, but are " + (fnameLen + descLen));
					System.err.println("Warning: osigfopen: description truncated to: \"" + si.desc + "\"");
				}
			}

			if(si.bsize < 0){
				System.err.println("Exception: osigfopen: error in specification of signal " + s +  ": bsize field must not be negative");
				return -2;
			}
			
			if(!isfmt(si.fmt)){
				System.err.println("Exception: osigfopen: error in specification of signal " + s +  ": " + si.fmt + " is not a valid format.");
				return -2;
			}
			
			//count how many groups are needed.
			if (groupPrev != si.group){
				groupCount++;
				groupPrev = si.group;
			}
		}
		
		
		/* Allocate workspace for output signal groups. */
		ogd = new WFDB_ogdata[groupCount];

		/* Open the signal files.  One signal is handled per iteration. */
		groupPrev=0; 
		for(int s=0; s < signalCount; s++){
			if (siarray[s].spf <1) {
				siarray[s].spf = 1;
			}
			siarray[0].cksum = 0;
			siarray[s].nsamp = 0L;
			
			if (s==0){// it is the first signal
				ogd[groupPrev] = newOutgroupData(siarray[s]);
			}else{		
				if (groupPrev != siarray[s].group){ //This signal is in a different group as the previous signal.
					groupPrev++;
					ogd[groupPrev] = newOutgroupData(siarray[s]);
				}else{ // This signal belongs to the same group as the previous signal.		
					
					if(siarray[s].fmt != siarray[s-1].fmt){
						System.err.println("osigfopen: error in specification of signals. Format is different on signals " + (nosig-1) + " and " + nosig);
					}
				}
			}				
		}		
		return ret;
	}
	
	public int copysi(WFDB_siginfo to, WFDB_siginfo from){
		if (to == null || from == null) return (0);
		to = from;
		return (1);
	}
	
	public void osigclose(){
		if(ogd != null){
			if (ogd.length >0){
				for(int s=0; s < (ogd.length); s++){
					try {
						ogd[s].dos.close();
						ogd[s].fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}	
	
	public WFDB_igdata newIngroupData(WFDB_siginfo si){
		WFDB_igdata ig = new WFDB_igdata();
		ig.dataFile = new File(filePath + si.fname);

		try {
			ig.fis = new FileInputStream(ig.dataFile);
		} catch (Exception e) {
			System.err.println("Exception Message: " + e.getMessage());
			e.printStackTrace();
		}
			
		return ig;
	}
	

	/** Creates a new WFDB_ogdata object
	 * opens out put file, and configures output data stream attached to it.
	 * 
	 * @param si
	 * @return
	 */
	public WFDB_ogdata newOutgroupData(WFDB_siginfo si){
		WFDB_ogdata og = new WFDB_ogdata();
		og.dataFile = new File(filePath + si.fname);

		try {
			og.fos = new FileOutputStream(og.dataFile);
			og.dos = new DataOutputStream(og.fos);
		} catch (Exception e) {
			System.err.println("Exception Message: " + e.getMessage());
			e.printStackTrace();
		}
			
		return og;
	}
	
	public boolean isfmt(int fmt){
		for(int i=0;i<WFDB_FMT_LIST.length;i++){
			if (fmt== WFDB_FMT_LIST[i]) return true;
		}		
		return false;
	}
	
	/**
	 *  Remove trailing '.hea' from a record name, if present. 
	 *  
	 *  */
	public String  wfdb_striphea(String rn)
	{
	    if (rn != null) {
			int len = rn.length();		
		    
		    if ( (len >4) && (rn.endsWith(".hea")) ){
		    	rn = rn.substring(0, len-4);	    	
		    }
	    }
	    return rn;
	}
	
	/*
	 * creates a MFDB header file. Example header file contents: 
	 * 
	 * #[record name] [# of signals] [sample frequency, Hz]
	 * # [# of samples per signal] 
	 * test10 3  1000 113908 
	 * #
	 * #[filename] [format (16)] [ADC gain, default 200] 
	 * #[ADC resolution, default 12 bit] [ADC zero] [initial value] 
	 * #[checksum] [block size] [description] 
	 * test10.dat 16 200 12 0 59 -28998 0 RDTinTab.txt, column 0 
	 * test10.dat 16 200 12 0 -40 2335 0 RDTinTab.txt, column 1
	 * test10.dat 16 200 12 0 111 556 0 RDTinTab.txt, column 2
	 */
	String writeMFDBHeaderFile() {
		String header = "";
		String recordLine = "";
		String SignalSpecLine = "";
		FileOutputStream fos;
		DataOutputStream dos;
		File headerFile = new File(filePath + recordName + ".hea");

		try {
			fos = new FileOutputStream(headerFile);
			dos = new DataOutputStream(fos);

			// [record name]
			recordLine = recordName;
			 if (segmentCount>1)
			 recordLine = recordLine + "/" + segmentCount;

			// [# of signals] [sample frequency, Hz]
			recordLine += " " + signalCount + " " + sampleFrequency; // Hz
			if (counterFrequency > 0)
				recordLine = recordLine + "/" + counterFrequency;
			if ((counterFrequency > 0) & (counterBase!=0))
				recordLine = recordLine + "(" + counterBase + ")";

			// [# of samples per signal]
			if (samplesPerSignal > 0) {
				recordLine += " " + samplesPerSignal;
			}
			// base time and base date data not figured out yet.
			recordLine += "\n"; // Line Feed
			dos.writeBytes(recordLine);
			header = recordLine;

			// ********* Signal Specification Lines
			// [filename] [format (16)] [ADC gain, default 200]
			// [ADC resolution, default 12 bit]
			// [ADC zero] [initial value] [checksum] [block size] [description]
			for (int s = 0; s < signalCount; s++) {
				SignalSpecLine = siarray[s].fname + " "; // recordName + ".dat "; // [filename]
				SignalSpecLine += siarray[s].fmt + " "; // [format (16)]
				SignalSpecLine += siarray[s].gain + " "; // [ADC gain, default 200]
				SignalSpecLine += siarray[s].adcres + " "; // [ADC resolution, default 12
				// bit]
				SignalSpecLine += siarray[s].adczero + " "; // [ADC zero]
				SignalSpecLine += (int) data[s][0] + " "; // [initial value]
				SignalSpecLine += siarray[s].cksum  + " "; // [checksum]
				SignalSpecLine += 0 + " "; // [block size]
//				SignalSpecLine += "ECG from a file in another format " + recordName
//				+ ", column " + s + " "; // [description]
				SignalSpecLine +=  "converted, col " + s + " "; // [description]
				SignalSpecLine += "\n"; // end of line
				dos.writeBytes(SignalSpecLine);
				header += SignalSpecLine;
			}
			dos.flush();
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return header;
	}

		
	public int putvec(int[] vec, WFDB_siginfo[] siarray) {
		WFDB_siginfo osinfo;
		WFDB_ogdata og;
		int g; // WFDB_Group
		int samp = 0;
		int dif=0;
		//int dum=1;
		int stat = 0;
		
		for (int s = 0; s < vec.length; s++) {// for each signal sample in vector
			osinfo = siarray[s];
			g = osinfo.group; // from signal information, signal group number
			og = ogd[g]; //data shared by all signals in a group (file)
			
			// set a default value if the sample is bad.
			if (vec[s] == WFDB_INVALID_SAMPLE){	/* use lowest possible value */
				switch (osinfo.fmt) {
					case 0:
					case 8:
					case 16:
					case 61:
					default:
						break;
					case 80:
					case 160:
						vec[s] = 0; break;
					case 212:
						vec[s] = -2048; break;
					case 310:
					case 311:
						vec[s] = -512; break;
				}
			}
			
			try {
				switch (osinfo.fmt) {
				case 0: // null signal (do not write)
					samp = vec[s];
					break;
				case 8: // 8-bit first differences
				default: // Handle large slew rates sensibly.
					//dum = 1; //;
					dif = vec[s] - samp;
					if (dif < -128) { //
						dif = -128;
						stat = 0;
					} else if (dif > 127) {
						dif = 127;
						stat = 0;
					}
					samp += dif;
					w8(dif, og);
					break;
				case 16: // 16-bit amplitudes
					w16( vec[s], og);
					samp =  vec[s];
					break;
				case 61: // 16-bit amplitudes, bytes swapped
					w61( vec[s], og);
					samp =  vec[s];
					break;
				/*
				 * case 80: // 8-bit offset binary amplitudes w80(data[s][row], og); 
				 * 	samp = data[s][row]; 
				 * 	break; 
				 * case 160: // 16-bit offset binary amplitudes 
				 * 	w160(data[s][row], og); 
				 * 	samp = data[s][row]; 
				 * break; */
				case 212: // 2 12-bit amplitudes bit-packed in 3 bytes, called twice before writing 
					w212( vec[s], og); 
					samp =  vec[s]; 
					break; 
				/*	case 310: // 3 10-bit amplitudes bit-packed in 4 bytes 
				 * w310(data[s][row], og); 
				 * samp =
				 * data[s][row]; 
				 * break; 
				 * case 311: // 3 10-bit amplitudes  bit-packed in 4 bytes 
				 * w311(data[s][row], og); 
				 * samp = data[s][row];
				 * break;
				 */
				}
				stat = s + 1;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				stat=-1;
			}
			siarray[s].cksum += (short)samp;		
		}
		return stat;
	}

	/**
	 * write only the least significant byte of the int to the output stream.
	 * 
	 * @param outByte
	 * @param og
	 */
	public void w8(int outWord, WFDB_ogdata og) {
		// original C function definition
		// w8(V,G) // V is int, G is output group data structure
		// (((*(G->bp++) = (char)V)), // bp is a pointer to next location in
		// buf[];
		// (_l = (G->bp != G->be) ? 0 :
		// ((_n = (G->bsize > 0) ? G->bsize : obsize),
		// wfdb_fwrite((G->bp = G->buf), 1, _n, G->fp))))		
		try {
			og.dos.writeByte(outWord);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int r8(WFDB_igdata ig) {
		// original C function definition
		// r8(G) ((G->bp < G->be) ? *(G->bp++) : \
		//   ((_n = (G->bsize > 0) ? G->bsize : ibsize), \
		//  (G->stat = _n = wfdb_fread(G->buf, 1, _n, G->fp)), \
		//  (G->be = (G->bp = G->buf) + _n),\
		//  *(G->bp++)))
		
		try {
			ig.stat = ig.fis.read(ig.buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (int)ig.buf[0];
	}				


	/** Reads a 16-bit two’s complement amplitude stored 
	 * least significant byte first
	 * 
	 * @param ig
	 * @return
	 */	
	public short r16(WFDB_igdata ig)
	{
		int l, h;

		l = r8(ig);
		h = r8(ig);			
		return ((short)((h << 8) | (l & 0xff)));
	}
	
	/**
	 * write 16-bit two's complement amplitude stored least significant byte
	 * first
	 * 
	 * @param outWord
	 * @param og - WFDB_ogdata
	 */
	public void w16(int outWord, WFDB_ogdata og){
		try {
			int l, h;

			l = outWord;
			h = outWord >> 8;

			w8(l, og);
			w8(h, og);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
 	 * read a 16-bit two’s complement amplitude 
	 * stored least significant byte first
	 *
	 * @param ig
	 * @return
	 */
	int r61(WFDB_igdata ig)
	{
		int l, h;

		h = r8(ig);
		l = r8(ig);
		return ((int)((short)((h << 8) | (l & 0xff))));
	}
	
	/**
	 * write 16-bit two’s complement amplitude 
	 * stored least significant byte first
	 * 
	 * @param outWord  - one sample from one signal(channel), 12 bits
	 * @param og
	 */
	public void w61(int outWord, WFDB_ogdata og){
		try {
			int l, h;

			l =  outWord;
			h =  outWord >> 8;

			w8(h, og);
			w8(l, og);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 
	 *  w212: write the next sample to a format 212 signal file 
	 *  Each sample is represented by a 12-bit two’s complement amplitude. 
	 *  The first sample is obtained from the 12 least significant bits of 
	 *  the first byte pair (stored least significant byte first). 
	 *  The second sample is formed from the 4 remaining bits of the 
	 *  first byte pair (which are the 4 high bits of the 12-bit sample) 
	 *  and the next byte (which contains the remaining 8 bits of the second sample).
	 *  The process is repeated for each successive pair of samples. 
	 *  Most of the signal files in PhysioBank are written in format 212.
	 *  @param outWord - one sample from one signal(channel), 12 bits
	 *  @param og - WFDB_ogdata
	*/
	public void w212(int outWord, WFDB_ogdata og){
		// Samples are buffered here and written in pairs, as three bytes. 
		// original C code, called twice with g->count tracking which call this is.:
		//  v is a two bytes integer in each call, g-data persists between calls.
  		switch (og.count++) {
		  case 0:	
			  og.data = outWord & 0xfff; 
			  break;
		  case 1:	og.count = 0;
		  og.data |= (outWord << 4) & 0xf000;
			  w16(og.data, og);
			  w8(outWord, og);
			  break;
		}
				
	}
	
	
	int getskewedframe(int[] vector)
	{
		int c, stat;
		WFDB_siginfo is; // same as WFDB_isdata
		WFDB_igdata ig; // same as WFDB_igdata
		//int g; //WFDB_Group
		int v; // WFDB_Sample
		int s; // Signal number
		int i = 0; // index into vector

		if ((stat = (int)nisig) == 0) return (0);
		if (istime == 0L) {
			for (s = 0; s < nisig; s++)
				siarray[s].samp = siarray[s].initval;
			//for (g = nigroups+1; g>0;g--) {
				// Go through groups in reverse order since seeking on group 0
				//should always be done last. 
				//if (g == 0 || igd[g].start > 0)
					/* Do nothing if there is no more than one input signal group and
					the input pointer is correct already. */
					// isgsetframe(g, 0L);
			//}
		}

		for (s = 0; s < nisig; s++) {
			is =  siarray[s];
			ig = igd[is.group];
			for (c = 0; c < is.spf; c++) {
				switch (is.fmt) {
					case 0:	// null signal: return sample tagged as invalid
						vector[i] = v = (gvpad != 0) ? is.samp : WFDB_INVALID_SAMPLE;
						if (is.nsamp == 0) 
							ig.stat = -1;
						break;
					case 8:	// 8-bit first differences
					default:
						vector[i] = v = is.samp += r8(ig); 
					  	break;
					case 16: // 16-bit amplitudes
						vector[i] = v = r16(ig); 
						break;
					case 61:	// 16-bit amplitudes, bytes swapped
						vector[i] = v = r61(ig); 
						break;
  /*
			  case 80:	// 8-bit offset binary amplitudes 
				  *vector[i] = v = r80(ig);
				  if (v == -128)
					  *vector[i] = gvpad ? is->samp : WFDB_INVALID_SAMPLE;
				  else
					  is->samp = *vector[i];
				  break;
				  
			  case 160:	//16-bit offset binary amplitudes 
				  *vector[i] = v = r160(ig);	
				  break;
				  */
				  /*
			  case 212:	//2 12-bit amplitudes bit-packed in 3 bytes 
				  *vector[i] = v = r212(ig);
				  if (v == -2048)
					  *vector[i] = gvpad ? is.samp : WFDB_INVALID_SAMPLE;
				  else
					  is.samp = *vector[i];
				  break;
				  */
			  /*
			   * case 310:	// 3 10-bit amplitudes bit-packed in 4 bytes 
				  *vector[i] = v = r310(ig);
				  if (v == -512)
					  *vector[i] = gvpad ? is.samp : WFDB_INVALID_SAMPLE;
				  else
					  is->samp = *vector[i];
				  break;
			  case 311:	// 3 10-bit amplitudes bit-packed in 4 bytes 
				  *vector[i] = v = r311(ig);
				  if (v == -512)
					  *vector[i] = gvpad ? is.samp : WFDB_INVALID_SAMPLE;
				  else
					  is.samp = *vector[i];
				  break;
				  */
	
				}
				if (ig.stat <= 0) {
					// End of file -- reset input counter. 
					ig.count = 0;
					/*
					if (is.nsamp > 0L) {
						System.err.println("getvec: unexpected EOF in signal " + s);
						stat = -3;
					}
					
					else if (in_msrec && segp < segend) {
						segp++;
						if (isigopen(segp.recname, NULL, (int)nvsig) < 0) {
							wfdb_error("getvec: error opening segment %s\n",
								segp.recname);
							stat = -3;
						}
						else {
							istime = segp.samp0;
							return (getskewedframe(vector));
						}
					}
					else
						stat = -1;
					*/
					if (is.nsamp > 0L) {
						System.err.println("getvec: unexpected EOF in signal " + s);
						stat = -3;
					}
					else
						stat = -1;
				}
				is.cksum -= v;
			}
			is.nsamp--;
			if (is.nsamp == 0L) {
				if ((is.cksum & 0xffff)!= 0) { // checksum has a value, TODO: this could fail if the checksum really is zero
					//if (!in_msrec && !isedf) {
						if (is.fmt != 0) {
							System.err.println("getvec: checksum error in signal " + s);
							stat = -4;
						}
					//}
				}
			}
			i++;
		}
		return (stat);
	}
	
	// skips to a specified frame number in a specified signal group
	int isgsetframe(int g, long t)
	{
		// TODO: add this function so multiple sample rates are supported.
		return (0);
	}
	
	/** Parse the header file with name that matches the recordName property
	 * same as getSignalCount(), 
	 * plus it creates the siarray from the signal lines
	 * @return
	 */
	public int parseHeaderFile(String recordNm) {
		int count = 0;
		int linecount = 0;
		int signal = 0;
		File headerFile = new File(filePath + recordNm + ".hea");
		FileInputStream fis;
		//int groupPrev = 0;
		String fileNamePrev = "";
		boolean isEOF = false;

		if (!headerFile.exists()) {
			if (verbose) {
				System.err.println(headerFile.getName() + " does not exist.");
			}
			return -1; // unable to read header file
		}
		
		try {
			fis = new FileInputStream(headerFile);
		} catch (FileNotFoundException e) {
			fis = null;
			System.err.println(e.getMessage());
			return -3;
		}

		maxigroup = 0;
		while(!isEOF) {
			String line =readLine(fis);
			if((line == null)) {
				isEOF=true;
			}else {
				if(!line.startsWith("#")) {
					if(line.length()>0) { 
						if(linecount == 0) {// first non-comment line is the record line.
							count = parseHeaderRecordLine(line);
							linecount++;
							if(count==-1) return -2; // incorrect header file format		
							siarray = new WFDB_siginfo[count];
						}else { // the rest are signal lines
							siarray[signal] = parseHeaderSignalLine(line);
							maxspf =+ siarray[signal].spf;
							if (fileNamePrev.compareTo(siarray[signal].fname) != 0){ //This signal is in a different file (group) than the previous signal.
								maxigroup++;
							}else{ // This signal belongs to the same group as the previous signal.		
							}
							signal++;
						}
					}
				}
			}
		}
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count;
	}
	
	/** Returns the number of signals in record without opening them. 
	 * Use this feature to determine the amount of storage needed for 
	 * signal-related variables, as in the example below, 
	 * 
	 * This action also sets internal WFDB library variables that 
	 * record the base time and date, the length of the record, 
	 * and the sampling and counter frequencies, so that time conversion 
	 * functions such as strtim that depend on these quantities will work properly.
	 * 
	 * @param record - name of the record's header file to read, not including the extension.
	 * @return - 
	 * 	>0 Success: the returned value is the number of input signals 
	 *		(i.e., the number of valid entries in siarray) 
	 *	 0 Failure: no input signals available
	 * 	-1 Failure: unable to read header file (probably incorrect record name)
	 * 	-2 Failure: incorrect header file format
	 */
	public int getSignalCount(String recordNm) {
		int count = 0;
		File headerFile = new File(filePath + recordNm + ".hea");
		FileInputStream fis;
		boolean isEOF = false;

		if (!headerFile.exists()) {
			if (verbose) {
				System.err.println(headerFile.getName() + " does not exist.");
			}
			return -1; // unable to read header file
		}
		
		try {
			fis = new FileInputStream(headerFile);
		} catch (FileNotFoundException e) {
			fis = null;
			System.err.println(e.getMessage());
			return -3;
		}
		
		while(!isEOF) {
			String line =readLine(fis);
			if(line==null) {
				isEOF=true;
			}else {
				if(!line.startsWith("#")) {
					if(line.length()>0) { // first non-comment line is the record line.
						count = parseHeaderRecordLine(line);
						if(count==-1) return -2; // incorrect header file format
						break;
					}
				}
			}
		}
		return count;
	}
	
	/** reads one line from a file input stream
	 * 
	 * @param fis
	 * @param isEOF
	 * @return
	 */
	public String readLine(FileInputStream fis) {
		String ret="", oneChar;
		int bytesRead=0;
		byte[] b = new byte[1];
		
		try {
			while((bytesRead = fis.read(b)) != -1) {
				oneChar = new String(b,0,1);
				if(oneChar.compareTo("\n")==0)
					break;
				if(oneChar.compareTo("\r")==0)
					break;
				
				ret += oneChar; 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (bytesRead == -1) ret = null;
		
		return ret;
	}
	
	
	/**
	 * Parses the Record Line of a WFDB .hea file.
	 * Syntax of Record Line
Record_Name[/SEG] S [FREQ[/CNTRfreq[(CNTRbase)]] [SpS [baseT [baseD]]]]

Record_Name = letters, digits and underscores (‘_’) only. String
 SEG        = number of segments [optional] , integer
S           = number of signals, a value of zero is legal, positive integer.
FREQ        = sampling frequency (in Hz) [optional],floating-point
 CNTRfreq   = counter frequency (in ticks per second) [optional]if the FREQ is present, floating-point
  CNTRbase  = base counter value [optional]if the CNTRfreq is present, floating-point
 SpS        = number of samples per signal [optional]if the FREQ is present, integer
  baseT     = base time [optional]if the SpS is present. HH:MM:SS
   baseD    = base date [optional]if the baseT is present. DD/MM/YYYY
	 * @param recordLine - the record line from a header file.
	 * @return - total signal count.
	 */
	public int parseHeaderRecordLine(String recordLine) {
		String[] sub0, sub2; // for parsing the 0th and 2nd sections of the line.
		String[] fields = recordLine.split("[ \\t\\n\\f\\r]");
		int fieldCount = fields.length;

		if(fieldCount >=2)
		{
			sub0 = fields[0].split("/");
			recordName = sub0[0];
			if(sub0.length==2) segmentCount = Integer.parseInt(sub0[1]);
			signalCount = Integer.parseInt(fields[1]);
			if(fieldCount>2) {
				sub2 = fields[2].split("[/()]");
				sampleFrequency = Float.parseFloat(sub2[0]);
				if(sub2.length>=2) counterFrequency = Integer.parseInt(sub2[1]);
				if(sub2.length==3) counterBase = Integer.parseInt(sub2[2]);
			}
			if(fieldCount>3) { // "& sampleFrequency exists" is implied.
				samplesPerSignal = Integer.parseInt(fields[3]);
			}
			if(fieldCount>4) { // "& samplesPerSignal exists" is implied.
				baseTime = fields[3];
			}
			if(fieldCount>5) { // "& baseTime exists" is implied.
				baseDate = fields[3];
			}

			return signalCount;
		}else {
			return -1;
		}
	}
	
	/** parses a signal line from a header file and creates a siginfo object
	 * Syntax os a signal line:
File_name Format[xSPF][:SKEW][+bOffset] [ADCgain[(ADCbase)][/ADCunits] [ADCres [ADCzero [ADCinit [CKsum [Bsize [DESC]]]]]]

File_name = name of the file in which samples of the signal are kept, String
Format    = storage format of the signal (e.g. 8, 16,61,212), integer
 SPF      = samples per frame [optional], integer
 SKEW     = skew, (positive) number of samples of the signal that are considered to precede sample 0 [optional], integer
 bOffset  = byte offset [optional] If a signal file includes a preamble, the offset in bytes from the beginning of the signal file to sample 0, integer
ADCgain   = ADC gain (ADC units per physical unit) [optional], floating-point 
 ADCbase  = baseline (ADC units) [optional]if the ADCgain is present., integer
 ADCunits = specifies the sample value corresponding to 0 physical units, [optional]if the ADCgain is present. integer
 ADCres   = ADC resolution (bits) [optional]if the ADCgain is present. integer
  ADCzero = ADC zero [optional]if the ADCres is present. integer
   ADCinit= initial value [optional]if the ADCzero is present. integer
    CKsum = 16-bit signed checksum of all samples in the signal, [optional]if the ADCinit is present, short
     Bsize= block size [optional]if the CKsum is present, integer.
      DESC= a description of the signal may include embedded spaces  
            If missing, the WFDB library functions supply a description. [optional]if the Bsize is present, String
	 * 
	 * @param signalline
	 * @return - 
	 */
	public WFDB_siginfo parseHeaderSignalLine(String signalline) {
		WFDB_siginfo si = new WFDB_siginfo();
		
		//String[] sub2; // for parsing the 0th and 2nd sections of the line.
		String[] fields = signalline.split("[ \\t\\n\\f\\r]",9);
		int fieldCount = fields.length;
		Pattern patFormat = Pattern.compile("^\\d+");
		Pattern patSPF = Pattern.compile("x\\d+");
		Pattern patSKEW = Pattern.compile(":\\d+");
		Pattern patbOffset = Pattern.compile("\\+\\d+");
		Pattern patADCgain = Pattern.compile("^\\d+\\.?\\d+");
		Pattern patADCbase = Pattern.compile("\\(\\d+\\)");
		Pattern patADCunits = Pattern.compile("\\/\\w+");
		Matcher m; 
		//String dummy = "";
		
		if(fieldCount >=2)
		{ // first two fields are required
			// field 0
			si.fname = fields[0]; // File_Name
			
			// field 1
			m=patFormat.matcher(fields[1]);
			m.find();
			si.fmt =  Integer.parseInt(m.group()); // [Format] storage format of the signal (e.g. 8, 16,61,212)

			si.spf = 1; // Default if value is missing
			m=patSPF.matcher(fields[1]);
			if(m.find()) si.spf = Integer.parseInt(m.group());  // [xSPF] samples per frame (>1 for oversampled signals)
						
			m = patSKEW.matcher(fields[1]);
			if(m.find()) si.skew = Integer.parseInt(m.group());// [:SKEW] intersignal skew (in frames) 
			
			m = patbOffset.matcher(fields[1]);
			if(m.find()) si.start = Integer.parseInt(m.group());// [+bOffset] signal file byte offset to sample 0 
			
			// field 2 (optional data)
			if(fieldCount>=3) { // "ADCgain exists", parse out [ADCgain[(ADCbase)][/ADCunits]
				m = patADCgain.matcher(fields[2]);
				m.find();
				si.gain =  Double.parseDouble(m.group()); // [ADCgain]

				m = patADCbase.matcher(fields[2]);
				if(m.find()) si.baseline = Integer.parseInt(m.group());  // [(ADCbase)]
				
				m = patADCunits.matcher(fields[2]);
				if(m.find()) si.units = m.group();// [/ADCunits]
			}
			
			// fields 4-9 (more optional data)
			if(fieldCount>=4) { // "ADCgain exists"
				si.adcres = Integer.parseInt(fields[3]); // [ADCres]
			}
			if(fieldCount>=5) { // "& (ADCgain exists) & (ADCres exists)" is implied.
				si.adczero = Integer.parseInt(fields[4]); // [ADCzero]
			}
			if(fieldCount>=6) { // "& adczero exists" is implied.
				si.initval = Integer.parseInt(fields[5]); // [ADCinit] - If this field is missing, a value equal to the ADC zero is assumed.
			}else {
				si.initval = Integer.parseInt(fields[5]);
			}
   			if(fieldCount>=7) { // "& ADCinit exists" is implied.
				si.cksum = Integer.parseInt(fields[6]); // [CKsum]
			}
			if(fieldCount>=8) { // "& cksum exists" is implied.
				si.bsize = Integer.parseInt(fields[7]); // [Bsize]
			}
			if(fieldCount == 9) { // "& bsize exists" is implied.
				si.desc = fields[8]; // [DESC], all the remaining text, including any more white spaces.
			}
		}else {
			return null;
		}
		return si;
	}
	
	
	
	/** reads a vector of samples, including at least one sample from each open input signal. 
	 * If all signals are sampled at the same frequency, only one sample is read from each signal. 
	 * Otherwise, signals sampled at multiples of the frame frequency are represented 
	 * by two or more consecutive elements of the returned vector. 
	 * For example, if the frame frequency is 125 Hz, signal 0 is sampled at 500 Hz, 
	 * and the remaining 3 signals are sampled at 125 Hz each, then the returned vector has 
	 * 7 valid components: the first 4 are samples of signal 0, and the remaining 3 are 
	 * samples of signals 1, 2, and 3.
	 * 
	 * @param vector - integer array for sample values
	 * @return
	 */
	int getframe(int[] vector)
	{
		int stat;
/*
		if (dsbuf) {	// signals must be deskewed 
			int c, i, j, s;

			//First, obtain the samples needed. 
			if (dsbi < 0) {	// dsbuf contents are invalid -- refill dsbuf
				for (dsbi = i = 0; i < dsblen; dsbi = i += framelen)
					stat = getskewedframe(dsbuf + dsbi);
				dsbi = 0;
			}
			else {		// replace oldest frame in dsbuf only 
				stat = getskewedframe(dsbuf + dsbi);
				if ((dsbi += framelen) >= dsblen) dsbi = 0;
			}
			// Assemble the deskewed frame from the data in dsbuf. 
			for (j = s = 0; s < nisig; s++) {
				if ((i = j + dsbi + isd[s]->skew*framelen) >= dsblen) i -= dsblen;
				for (c = 0; c < isd[s]->info.spf; c++)
					vector[j++] = dsbuf[i++];
				
			}
		}else {		// no deskewing necessary
		*/ 
			stat = getskewedframe(vector);
		//}
		//if (need_sigmap && stat > 0) stat = sigmap(vector);
		istime++;
		return (stat);
	}
	
	/** reads a sample from each input signal without resampling
	 * 
	 * @param vector - integer array for sample values
	 * @return vector[i] contains the next sample from signal i.
	 */
	public int rgetvec(int[] vector) {
		//int tp;
		//int s; // signal number
		int stat=0;

		if (ispfmax < 2)	// all signals at the same frequency 
			return (getframe(vector));

		/*
		if (gvmode != WFDB_HIGHRES) {// return one sample per frame, decimating
									 //(by averaging) if necessary 
			int c;
			long v;

			stat = getframe(tvector);
			for (s = 0, tp = tvector; s < nvsig; s++) {
				int sf = vsd[s]->info.spf;

				for (c = v = 0; c < sf; c++)
					v += *tp++;
				*vector++ = v/sf;
			}
		}
		else {			// return ispfmax samples per frame, using
						// zero-order interpolation if necessary 
			if (gvc >= ispfmax) {
				stat = getframe(tvector);
				gvc = 0;
			}
			for (s = 0, tp = tvector; s < nvsig; s++) {
				int sf = vsd[s]->info.spf;

				*vector++ = tp[(sf*gvc)/ispfmax];
				tp += sf;
			}
			gvc++;
		}
				*/

		return (stat);
	}
	
	/** reads a (possibly resampled) sample from each input signal
	 *  into an array of integers. 
	 * (The length of the array must be no less than the number of input signals, 
	 * as obtained from isigopen or wfdbinit.) 
	 * 
	 * @param vector - integer array of sample values
	 * @return - vector[i] contains the next sample from signal i.
	 */	 
	public int getvec(int[] vector) //, WFDB_siginfo[] siarray)
	{
		// int i;

		//if (ifreq == 0.0 || ifreq == sfreq)	/* no resampling necessary */
			return (rgetvec(vector));

		// Resample the input. 
			/*
		if (rgvtime > mnticks) {
			rgvtime -= mnticks;
			gvtime  -= mnticks;
		}
		while (gvtime > rgvtime) {
			for (i = 0; i < nisig; i++)
				gv0[i] = gv1[i];
			rgvstat = rgetvec(gv1);
			rgvtime += nticks;
		}
		for (i = 0; i < nisig; i++) {
			vector[i] = gv0[i] + (gvtime%nticks)*(gv1[i]-gv0[i])/nticks;
			gv0[i] = gv1[i];
		}
		gvtime += mticks;
		return (rgvstat);
		*/
	}
	
	public int getAduGain() {
		return gain;
	}
}
