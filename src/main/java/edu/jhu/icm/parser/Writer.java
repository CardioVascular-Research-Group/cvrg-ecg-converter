/*
 * Created on Apr 5, 2006/
 */
package edu.jhu.icm.parser;

import hl7OrgV3.AnnotatedECGDocument;
import hl7OrgV3.GLISTPQ;
import hl7OrgV3.PORTMT020001AnnotatedECG;
import hl7OrgV3.PORTMT020001Component5;
import hl7OrgV3.PORTMT020001Component8;
import hl7OrgV3.PORTMT020001Component9;
import hl7OrgV3.PORTMT020001Sequence;
import hl7OrgV3.PORTMT020001SequenceSet;
import hl7OrgV3.PQ;
import hl7OrgV3.SLISTPQ;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlSimpleList;

/**
 * Wrap a rdt in an aECG xml
 * Reads a RDT formatted ECG file and
 * writes the data out as an aECG xml file (HL7).
 * 
 * @author cyang
 *  
 */
public class Writer {

    private static Logger logger = Logger.getLogger(Writer.class.getName());

    private static final String FILENAME = "TemplateExample1.xml";

    public final static String[] LEADNAME = { "MDC_ECG_LEAD_X",
            "MDC_ECG_LEAD_Y", "MDC_ECG_LEAD_Z" };

    /**
     * create an aecgdoc for wrapping rdt data
     * 
     * @param isResouce
     *            if treating file as a resource
     * @return aecgdoc if succeed
     */
    public static AnnotatedECGDocument createTemplate(boolean isResouce) {

        AnnotatedECGDocument aecgdoc = null;
        try {
            if (isResouce) {
//            	File f = new File("/" + FILENAME);
//            	System.err.println(f.getAbsoluteFile());
//            	System.err.println(f.getCanonicalPath());
//            	System.err.println(f.getPath());
                InputStream ins = Writer.class.getResourceAsStream("/"
                        + FILENAME);
//                InputStream ins = new FileInputStream(FILENAME); 
                aecgdoc = AnnotatedECGDocument.Factory.parse(ins);

            } else {
                File xmlFile = new File(FILENAME);

                aecgdoc = AnnotatedECGDocument.Factory.parse(xmlFile);

            }
        } catch (XmlException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return aecgdoc;
    }

    private static PORTMT020001SequenceSet getSequenceSet(
            AnnotatedECGDocument aecgdoc) {
        if (aecgdoc == null) {
            logger.error("template is null.");
            return null;
        }

        //  validate(aecgdoc, true);
        String idRoot = aecgdoc.getAnnotatedECG().getId().getRoot();
        logger.debug("<id root=" + idRoot + ">");

        PORTMT020001AnnotatedECG aecg = aecgdoc.getAnnotatedECG();
        PORTMT020001Component5[] components = aecg.getComponentArray();

        PORTMT020001Component5 component = components[0];
        String mf = component.getSeries().getAuthor().getSeriesAuthor()
                .getManufacturedSeriesDevice().getManufacturerModelName()
                .newCursor().getTextValue();//.toString();

        logger.debug(mf);

        //TO-DO may need to delete other Component8
        PORTMT020001Component8 c8 = component.getSeries().getComponentArray()[0];

        PORTMT020001SequenceSet rdtSeqSet = c8.getSequenceSet();
        return rdtSeqSet;
    }

    /**
     * wraps rdt data in aecgdoc.
     * 
     * @param rps
     *            RdtParserSimple object
     * @param aecgdoc
     *            tempalte aecgdoc
     * @return aecgdoc
     */
    public static AnnotatedECGDocument createAecgDoc(RdtParserSimple rps,
            AnnotatedECGDocument aecgdoc) {
        PORTMT020001SequenceSet rdtSeqSet = getSequenceSet(aecgdoc);
        logger.debug("number of sequences " + rdtSeqSet.sizeOfComponentArray());

        int size = 0;
        while (4 < (size = rdtSeqSet.sizeOfComponentArray())) {
            rdtSeqSet.removeComponent(size - 1);
            logger.debug("trying to remove" + size + "; size of array is "
                    + rdtSeqSet.sizeOfComponentArray());
        }
        PORTMT020001Component9[] c9s = rdtSeqSet.getComponentArray();
        int leadIndex = -1;
        logger.debug("number of sequences " + c9s.length);
        for (int i = 0; i < c9s.length; i++) {
            PORTMT020001Sequence sequence = c9s[i].getSequence();
            String code = sequence.getCode().getCode();
            logger.debug(code);
            XmlObject value = sequence.getValue();
            if (code.equals(Constants.codeTA)) {
                logger.error("should use an template file of TIME_RELATIVE");

            } else if (code.equals(Constants.codeRA)) {
                if (value instanceof GLISTPQ) {
                    GLISTPQ g = (GLISTPQ) value;
                    BigDecimal newIncrement = new BigDecimal(1.0 / rps
                            .getSamplingRate());
                    g.getIncrement().setValue(newIncrement);
                    logger.debug(newIncrement.toString());
                } else {
                    // throw exception?
                }
            } else {
                leadIndex++;
                String leadName = LEADNAME[leadIndex];
                sequence.getCode().setCode(leadName);
                if (value instanceof SLISTPQ) {
                    SLISTPQ s = (SLISTPQ) value;
                    // value and unit of origin should be fine
                    PQ origin = s.getOrigin();
                    // not sure about the value and unit for Scale though
                    PQ scale = s.getScale();

                    // digits
                    int[] digits = rps.getData()[leadIndex];
                    List newList = new PrimitiveIntArrayList(digits);
                    s.setDigits(new XmlSimpleList(newList));

                } else {
                    logger.error("check the template file for SLIST_PQ");
                }
            }
        }
        return aecgdoc;
    }


    /**
     * wraps int[][] data in aecgdoc.
     * 
     * @param rps
     *            RdtParserSimple object
     * @param aecgdoc
     *            tempalte aecgdoc
     * @return aecgdoc
     */
    public static AnnotatedECGDocument createAecgDoc(int[][] data, float samplingRate,
            AnnotatedECGDocument aecgdoc) {
        PORTMT020001SequenceSet rdtSeqSet = getSequenceSet(aecgdoc);
        logger.debug("number of sequences " + rdtSeqSet.sizeOfComponentArray());

        int size = 0;
        while (4 < (size = rdtSeqSet.sizeOfComponentArray())) {
            rdtSeqSet.removeComponent(size - 1);
            logger.debug("trying to remove" + size + "; size of array is "
                    + rdtSeqSet.sizeOfComponentArray());
        }
        PORTMT020001Component9[] c9s = rdtSeqSet.getComponentArray();
        int leadIndex = -1;
        logger.debug("number of sequences " + c9s.length);
        for (int i = 0; i < c9s.length; i++) {
            PORTMT020001Sequence sequence = c9s[i].getSequence();
            String code = sequence.getCode().getCode();
            logger.debug(code);
            XmlObject value = sequence.getValue();
            if (code.equals(Constants.codeTA)) {
                logger.error("should use an template file of TIME_RELATIVE");

            } else if (code.equals(Constants.codeRA)) {
                if (value instanceof GLISTPQ) {
                    GLISTPQ g = (GLISTPQ) value;
                    double d = (1.0 / samplingRate);
                    BigDecimal newIncrement = new BigDecimal(d);
                    g.getIncrement().setValue(newIncrement);
                    logger.debug(newIncrement.toString());
                } else {
                    // throw exception?
                }
            } else {
                leadIndex++;
                //if (leadIndex<2) {
	                String leadName = LEADNAME[leadIndex];
	                sequence.getCode().setCode(leadName);
	                if (value instanceof SLISTPQ) {
	                    SLISTPQ s = (SLISTPQ) value;
	                    // value and unit of origin should be fine
	                    PQ origin = s.getOrigin();
	                    // not sure about the value and unit for Scale though
	                    PQ scale = s.getScale();
	
	                    // digits
	                    //int[] digits = data[leadIndex];
	                    logger.debug("data[leadIndex].length: " + data[leadIndex].length);
	                    //List newList = new PrimitiveIntArrayList(digits);
	                    logger.debug("Creating PrimitiveIntArrayList for lead index: " + leadIndex);
	                    List newList = new PrimitiveIntArrayList(data[leadIndex]);
	                    logger.debug("PrimitiveIntArrayList done, running SetDigits");
	                    s.setDigits(new XmlSimpleList(newList));
	
	                } else {
	                    logger.error("check the template file for SLIST_PQ");
	                }
                //}
            }
        }
        return aecgdoc;
    }
    
    public static void writeHL7(String fullPathOutput, int[][] data, float samplingRate) {
        initLogger();

        AnnotatedECGDocument aecgdoc = createTemplate(true);
        aecgdoc = createAecgDoc(data, samplingRate, aecgdoc);
        saveAecgXML(aecgdoc, fullPathOutput); // XML output filename (HL7)
        aecgdoc = null;
        logger.debug("done.");
    }

        
    /**
     * save aecgdoc to file
     * 
     * @param aecgdoc
     *            doc to save
     * @param fileName
     *            file name
     */
    public static void saveAecgXML(XmlObject aecgdoc, String fileName) {
        try {
            aecgdoc.save(new File(fileName));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * validate aecgdoc
     * 
     * @param aecgdoc
     *            aecg doc to validate
     * @return true if valid
     */
    public static boolean validate(AnnotatedECGDocument aecgdoc) {
        return validate(aecgdoc, true);
    }

    /**
     * validate an XmlObject
     * 
     * @param doc
     *            can be aecgdoc
     * @param verbose
     *            log errors or not
     * @return true if valid
     */
    public static boolean validate(XmlObject doc, boolean verbose) {
        if (verbose) {
            ArrayList validationErrors = new ArrayList();
            XmlOptions validationOptions = new XmlOptions();
            validationOptions.setErrorListener(validationErrors);
            boolean isValid = doc.validate(validationOptions);
            if (!isValid) {
                Iterator iter = validationErrors.iterator();
                while (iter.hasNext()) {
                    logger.error(">> " + iter.next() + "\n");
                }
                return false;
            }
            return true;
        } else {
            return doc.validate();
        }
    }

    /**
     * init logger
     */
    public static void initLogger() {
        // basic configuration
        // BasicConfigurator.configure();

        // or customized config
        Logger rootLogger = Logger.getRootLogger();
        PatternLayout layout = new PatternLayout("%r %-5p %C.%M - %m%n");
        ConsoleAppender appender = new ConsoleAppender(layout);
        rootLogger.addAppender(appender);
        //  rootLogger.setLevel(Level.ERROR);
    }
}
