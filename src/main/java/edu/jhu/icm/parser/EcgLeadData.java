/*
 * Created on Apr 6, 2006
 *
 */

package edu.jhu.icm.parser;

import hl7OrgV3.GLISTPQ;
import hl7OrgV3.GLISTTS;
import hl7OrgV3.PORTMT020001Component9;
import hl7OrgV3.PORTMT020001Sequence;
import hl7OrgV3.PQ;
import hl7OrgV3.SLISTPQ;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

//import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlSimpleList;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;

/**
 * Represents the time series from all leads.
 * 
 * @author cyang
 *  
 */
public class EcgLeadData {

//    static Logger logger = Logger.getLogger(EcgLeadData.class.getName());

    private BigDecimal[] leadOriginValue, leadScaleValue;

    public double getLeadScaleValue(int lead) {
		return leadScaleValue[lead].doubleValue();
	}

 	private String[] leadOriginUnit, leadScaleUnit;

    public String getLeadScaleUnit(int lead) {
		return leadScaleUnit[lead];
	}

	private String[] leadName;

    private List[] leadDigits;

    //private List time;
    private String timeStart;

    private BigDecimal timeIncrement;

    public double getTimeIncrement() {
		return timeIncrement.doubleValue();
	}

	private String timeUnit;

    public String getTimeUnit() {
		return timeUnit;
	}

	private List timeSeries;

    // jfreechart
    private int numberOfLeads;

    private int numberOfPoints;

    public int getNumberOfPoints() {
		return numberOfPoints;
	}

	// 1 based index
    private int pageNumber = 1;

    private int pageSize = 3000;

    private int pageCount;

    public void pageForward(int step) {
        pageNumber += step;
        if (pageNumber > pageCount) {
            pageNumber = pageCount;
        }
    }

    public void setPageWindow(double width) {
        if (width != 0) {
            this.pageSize = (int) Math.round(width
                    / this.timeIncrement.doubleValue());
            this.calcPageCount();
        }
    }

    public double getPageWindow() {
        return this.timeIncrement.doubleValue() * this.pageSize;
    }

    public void pageBackward(int step) {
        pageNumber -= step;
        if (pageNumber < 1) {
            pageNumber = 1;
        }
    }

    private void calcPageCount() {
        if (pageSize != 0) {
            double tmp = Math.ceil(((double) this.numberOfPoints)
                    / this.pageSize);
            this.pageCount = (int) tmp;
//            logger.debug("page count is " + this.pageCount);
        }
    }

    /**
     * test run
     * 
     * @param args
     */
//    public static void main(String[] args) {
//        Reader.initLogger();
//        Reader r = new Reader("example.xml");
//        EcgLeadData ds = new EcgLeadData(r.getC9s());
//        ds.pageForward(2);
//        ds.writeToFile("total.png");
//    }

    /**
     * write sequence to outputstream.
     * 
     * @param os
     *            outputstream object
     */
    public void writeToStream(OutputStream os) {
        saveChartAsPng(DrawEcg.combinedPlot(this), os);
    }


    /**
     * write sequence data to file
     * 
     * @param fileName
     *            file name
     */
    public void writeToFile(String fileName) {

        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(new File(fileName));
            saveChartAsPng(DrawEcg.combinedPlot(this), fos);
            fos.close();

        } catch (FileNotFoundException e) {

//            logger.error(e.getMessage());
        } catch (IOException e) {
//            logger.error(e.getMessage());
        }

    }
    
    /**
     * save chart as png to an outputstream
     * 
     * @param chart
     *            JFreeChart object
     * @param os
     *            destination outputStream
     */
    private void saveChartAsPng(JFreeChart chart, OutputStream os) {

        try {
            int height = this.numberOfLeads * 200;
            ChartUtilities.writeChartAsPNG(os, chart, 1200, height);
        } catch (FileNotFoundException e) {
//            logger.error(e.getMessage());
        } catch (IOException e) {
//            logger.error(e.getMessage());
        }
    }

    /**
     * Constructor
     * 
     * @param c9s
     *            array of data
     */
    public EcgLeadData(PORTMT020001Component9[] c9s) {
        if (c9s == null) {
//            logger.error("c9s is null");
            return;
        }
//        logger.debug("number of data series: " + c9s.length);
        this.numberOfLeads = c9s.length - 1;
        this.leadOriginUnit = new String[numberOfLeads];
        this.leadOriginValue = new BigDecimal[numberOfLeads];
        this.leadScaleUnit = new String[numberOfLeads];
        this.leadScaleValue = new BigDecimal[numberOfLeads];
        this.leadName = new String[numberOfLeads];
        this.leadDigits = new XmlSimpleList[numberOfLeads];
        int leadIndex = -1;
        boolean isSet = false;
        for (int i = 0; i < c9s.length; i++) {
            PORTMT020001Sequence sequence = c9s[i].getSequence();
            String code = sequence.getCode().getCode();
//            logger.debug(code);
            XmlObject value = sequence.getValue();
            if (code.equals(Constants.codeTA)) {
                if (value instanceof GLISTTS) {
                    GLISTTS g = (GLISTTS) value;
                    this.timeStart = g.getHead().getValue();
//                    logger.debug("time starts at " + timeStart);
                    // get increment
                    this.timeIncrement = (BigDecimal) g.getIncrement()
                            .getValue();
                    this.timeUnit = g.getIncrement().getUnit();

//                    logger.debug(this.timeIncrement.toString() + this.timeUnit);
                } else {
                    // throw exception?
                }
            } else if (code.equals(Constants.codeRA)) {
                if (value instanceof GLISTPQ) {
                    GLISTPQ g = (GLISTPQ) value;
                    this.timeStart = g.getHead().getValue().toString();
//                    logger.debug("time starts at " + timeStart);
                    // get increment
                    this.timeIncrement = (BigDecimal) g.getIncrement()
                            .getValue();
                    this.timeUnit = g.getIncrement().getUnit();

//                    logger.debug(this.timeIncrement.toString() + this.timeUnit);
                } else {
                    // throw exception?
                }
            } else {
                leadIndex++;
                this.leadName[leadIndex] = code;
                if (value instanceof SLISTPQ) {
                    SLISTPQ s = (SLISTPQ) value;
                    PQ origin = s.getOrigin();
                    this.leadOriginValue[leadIndex] = (BigDecimal) origin
                            .getValue();
                    this.leadOriginUnit[leadIndex] = origin.getUnit();
//                    logger.debug(code + ": orgin at "
//                            + leadOriginValue[leadIndex]
//                            + leadOriginUnit[leadIndex]);

                    PQ scale = s.getScale();
                    this.leadScaleValue[leadIndex] = (BigDecimal) scale
                            .getValue();
                    this.leadScaleUnit[leadIndex] = scale.getUnit();
//                    logger.debug(code + ": scale is "
//                            + leadScaleValue[leadIndex]
//                            + leadScaleUnit[leadIndex]);
                    // digits
                    List digits = s.getDigits();

                    //logger.debug(s.getDigits().getClass().getName());
                    this.leadDigits[leadIndex] = digits;
                    if (!isSet) {
                        this.numberOfPoints = digits.size();
                        isSet = true;
                    }

                }
            }
        }
        this.calcPageCount();

    }

    public XYDataset[] getPagedXYDatasets() {
        return getAllXYDatasets((pageNumber - 1) * pageSize, pageSize);
    }

    private XYDataset[] getAllXYDatasets(int offset, int count) {
        XYDataset[] ret = new PagedEcgXYDataset[this.numberOfLeads];
        for (int i = 0; i < this.numberOfLeads; i++) {
            PagedEcgXYDataset ecgDataset = new PagedEcgXYDataset(i);
            ecgDataset.setOffset(offset);
            ecgDataset.setReadingSize(count);
            ret[i] = ecgDataset;
//            logger.debug(i + ": offset: " + ecgDataset.getOffset()
//                    + "; readingSize is " + ecgDataset.getReadingSize());
            if (i==0) totalRead += ecgDataset.getReadingSize();
        }
        return ret;
    }
    public int totalRead=0;
    
    public XYDataset getOneXYDataset(int leadIndex) {
        if (leadIndex < 0 || leadIndex >= this.numberOfLeads) {
//            logger.error("the leadIndex is out of bounds");
            return null;
        } else {
//            logger.debug("is called");
            PagedEcgXYDataset ecgDataset = new PagedEcgXYDataset(leadIndex);
            return ecgDataset;
        }
    }

    /**
     * represents one page of data from one lead
     * 
     * @author cyang Apr 10, 2006
     */
    public class PagedEcgXYDataset implements XYDataset {
        public int whichLead;

        private int readingSize = 5000;

        private int offset = 0;

        /**
         * @return Returns the readingSize.
         */
        public int getReadingSize() {
            return readingSize;
        }

        /**
         * @param readingSize
         *            The readingSize to set.
         */
        public void setReadingSize(int count) {
            if (count > 0)
                this.readingSize = count;
//            else
//                logger
//                        .error("Error: trying to set readingSize to negative number "
//                                + count);
        }

        /**
         * @return Returns the offset.
         */
        public int getOffset() {
            return offset;
        }

        /**
         * @param offset
         *            The offset to set.
         */
        public void setOffset(int offset) {
            if (offset >= 0 && offset < numberOfPoints) {
                this.offset = offset;
            } else {
//                logger.error("Error: trying to set an invalid offset value, "
//                        + offset + "; numberofpoints " + numberOfPoints);
            }
        }

        /**
         * constructor
         * 
         * @param whichLead
         *            zero based?
         */
        private PagedEcgXYDataset(int whichLead) {
            this.whichLead = whichLead;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.general.Dataset#addChangeListener(org.jfree.data.general.DatasetChangeListener)
         */
        public void addChangeListener(DatasetChangeListener arg0) {
//            logger.debug("addChangeListener not implemented ");
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.general.Dataset#removeChangeListener(org.jfree.data.general.DatasetChangeListener)
         */
        public void removeChangeListener(DatasetChangeListener arg0) {
//            logger.debug("removeChangeListener not implemented. ");

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.general.Dataset#getGroup()
         */
        public DatasetGroup getGroup() {
//            logger.debug("getGroup not implemented. ");
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.general.Dataset#setGroup(org.jfree.data.general.DatasetGroup)
         */
        public void setGroup(DatasetGroup arg0) {
//            logger.debug("setGroup not implemented. ");

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.xy.XYDataset#getDomainOrder()
         */
        public DomainOrder getDomainOrder() {
//            logger.debug("getDomainOrder not implemented. ");
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.xy.XYDataset#getItemCount(int)
         */
        public int getItemCount(int series) {
            if (readingSize + this.offset < numberOfPoints)
                return readingSize; //numberOfPoints;
            else
                return numberOfPoints - offset;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.xy.XYDataset#getX(int, int)
         */
        public Number getX(int series, int item) {

            return new Double(getXValue(series, item));
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.xy.XYDataset#getXValue(int, int)
         */
        public double getXValue(int series, int item) {
            double inc = timeIncrement.doubleValue();
            double ret = inc * (this.offset + item);

            return ret;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.xy.XYDataset#getY(int, int)
         */
        public Number getY(int series, int item) {
            return (Number) leadDigits[this.whichLead].get(this.offset + item);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.xy.XYDataset#getYValue(int, int)
         */
        public double getYValue(int series, int item) {
            BigInteger tmp = (BigInteger) leadDigits[this.whichLead]
                    .get(this.offset + item);
            return tmp.doubleValue();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.general.SeriesDataset#getSeriesCount()
         */
        public int getSeriesCount() {

            return 1; // this.numberOfLeads;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.general.SeriesDataset#getSeriesKey(int)
         */
        public Comparable getSeriesKey(int series) {
            return leadName[whichLead];
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.jfree.data.general.SeriesDataset#indexOf(java.lang.Comparable)
         */
        public int indexOf(Comparable seriesKey) {
            if (leadName[this.whichLead].equals(seriesKey)) {
                return 0;
            } else {
//                logger.fatal("can not find the index for seriesKey "
//                        + seriesKey);

                return -1;
            }
        }

    }

    /**
     * @return Returns the leadName.
     */
    public String[] getLeadName() {
        return leadName;
    }

    /**
     * @return Returns the pageNumber.
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * @param pageNumber - 1 based index of data pages.
     *            The pageNumber to set.
     */
    public void setPageNumber(int pageNumber) {
        if (pageNumber >= 1 && pageNumber <= this.pageCount) {
            this.pageNumber = pageNumber;
        }

    }

    /**
     * @return Returns the pageCount.
     */
    public int getPageCount() {
        return pageCount;
    }
}
