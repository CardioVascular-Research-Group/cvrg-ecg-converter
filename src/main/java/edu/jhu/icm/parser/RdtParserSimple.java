/*
 * Created on Mar 10, 2006
 */
package edu.jhu.icm.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;

/**
 * The parsing of RDT file is enlighted by and derived from the loadrdt.m file
 * by Alois Schloegl at a.schloegl@ieee.org.
 * 
 * Limitation: It does not handle the calibration files as we do not have them
 * at hand. Thus the class is a simplified version and handles just RDT files.
 * 
 * @author cyang
 */
public class RdtParserSimple {
    static Logger logger = Logger.getLogger(RdtParserSimple.class.getName());

    private String fileName;
       
    private InputStream rdtFis;
    
    private long fileSize;

    private short channels, samplingRate;

    private int counts;

    private int[][] data;

    private static final ByteOrder BYTEORDER = ByteOrder.LITTLE_ENDIAN;

    private static final int HEADERBYTES = 4;

    private static final int SHORTBYTES = 2;

    /**
     * constructor
     * 
     * @param filename
     *            a rdt file to parse
     */
    public RdtParserSimple(String filename) {
        this.fileName = filename;
        File rdtFile = new File(this.fileName);
        if (!rdtFile.exists()) {
           logger.error(this.fileName + " does not exist.");
        }
        this.fileSize = rdtFile.length();
        if (fileSize > Integer.MAX_VALUE) {
            logger.error("file size exceeding maximum int value.");

        }       
        try {
            this.rdtFis = new FileInputStream(rdtFile);
        } catch (FileNotFoundException e) {
            this.rdtFis = null;
            logger.error(e.getMessage());
        }
    }
    public RdtParserSimple(InputStream ins, long size){
        this.rdtFis = ins;
        this.fileSize = size;
    }
    /**
     * parse rdt file
     * 
     * @return true if succeeds
     */
    public boolean parse() {

        BufferedInputStream rdtBis = new BufferedInputStream(rdtFis);

        byte[] header = new byte[HEADERBYTES];
        try {
            int result = rdtBis.read(header);
            if (result != HEADERBYTES) {
                logger.error("error occured while reading header.");
                return false;
            }
            ByteBuffer bbHead = ByteBuffer.wrap(header);
            bbHead.order(BYTEORDER);
            this.channels = bbHead.getShort();
            this.samplingRate = bbHead.getShort();
        } catch (IOException e) {

            logger.error(e.getMessage());

            try {
                rdtBis.close();
            } catch (IOException e1) {
            }
            return false;
        }
        final int REALBUFFERSIZE = (int) fileSize - HEADERBYTES;
        if (REALBUFFERSIZE % (channels * SHORTBYTES) != 0) {
            logger.error("rdt file is not aligned: channels " + channels+" ; shortbytes "+ SHORTBYTES + " ; REALBUFFERSIZE "+REALBUFFERSIZE);
            return false;
        }

        this.counts = REALBUFFERSIZE / (channels * SHORTBYTES);
        this.data = new int[channels][counts];

        logger.debug("count is " + this.counts);

        byte[] body = new byte[REALBUFFERSIZE];
        boolean ret = false;
        try {

            int length = rdtBis.read(body);
            if (length != REALBUFFERSIZE) {
                logger.error("error while reading data into buffer");
                try {
                    rdtBis.close();
                    rdtFis.close();
                } catch (IOException e2) {
                }
                return false;
            }

            ByteBuffer bbBody = ByteBuffer.wrap(body);
            bbBody.order(BYTEORDER);
            for (int index = 0; index < this.counts; index++) {
                for (int channel = 0; channel < this.channels; channel++) {
                    short value = bbBody.getShort();
                    if (logger.isDebugEnabled() && index < 3)
                        logger.debug("channel " + channel + " : " + value);
                    this.data[channel][index] = value;
                }
            }
            ret = true;
        } catch (IOException e1) {

        } finally {
            try {
                rdtBis.close();
                rdtFis.close();
            } catch (IOException e2) {
            }
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

    public void viewHeader() {
        System.out.println("# of channels is " + this.channels
                + "; sampling rate is " + this.samplingRate);
    }

    /**
     * @return Returns the channels.
     */
    public short getChannels() {
        return channels;
    }

    /**
     * @return Returns the counts.
     */
    public int getCounts() {
        return counts;
    }

    /**
     * @return Returns the data.
     */
    public int[][] getData() {
        return data;
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return Returns the samplingRate.
     */
    public short getSamplingRate() {
        return samplingRate;
    }
}
