/*
 * Created on Apr 5, 2006
 *
 */
package edu.jhu.icm.ecgFormatConverter.hl7;

import hl7OrgV3.AnnotatedECGDocument;
import hl7OrgV3.PORTMT020001AnnotatedECG;
import hl7OrgV3.PORTMT020001Component5;
import hl7OrgV3.PORTMT020001Component8;
import hl7OrgV3.PORTMT020001Component9;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.xmlbeans.XmlException;

/**
 * Reads aEcg xml file; creates png image
 * 
 * @author cyang
 */
@Deprecated
public class Reader {
    static Logger logger = Logger.getLogger(Reader.class.getName());

    private PORTMT020001Component9[] c9s;

    private AnnotatedECGDocument aecgdoc;
    
    /**
     * Constructor
     * 
     * @param fileName
     *            xmlfile to read
     */
    public Reader(String fileName) {
        File xmlFile = new File(fileName);
        try {
            this.aecgdoc = AnnotatedECGDocument.Factory.parse(xmlFile);
        } catch (XmlException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        retrieve();
    }

    /**
     * Constructor
     * 
     * @param ins
     *            xml file inputstream
     */
    public Reader(InputStream ins) {
        try {
            this.aecgdoc = AnnotatedECGDocument.Factory.parse(ins);
        } catch (XmlException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        retrieve();
    }
    /**
     * Constructor
     * @param aecgdoc
     */
    public Reader(AnnotatedECGDocument aecgdoc){
        this.aecgdoc = aecgdoc;
        retrieve();
    }
    /**
     * retrieve sequence data
     */
    private void retrieve() {

        String idRoot = this.aecgdoc.getAnnotatedECG().getId().getRoot();
        //logger.debug("<id root=" + idRoot + ">");

        PORTMT020001AnnotatedECG aecg = aecgdoc.getAnnotatedECG();
//        System.out.println(aecgdoc.toString());
        PORTMT020001Component5[] components = aecg.getComponentArray();

        PORTMT020001Component5 component = components[0];
        String mf = component.getSeries().getAuthor().getSeriesAuthor()
                .getManufacturedSeriesDevice().getManufacturerModelName()
                .newCursor().getTextValue();//.toString();

       // logger.debug("manufactorModelName " + mf);

        PORTMT020001Component8 c8 = component.getSeries().getComponentArray()[0];

        this.c9s = c8.getSequenceSet().getComponentArray();

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
    /**
     * @return Returns the c9s.
     */
    public PORTMT020001Component9[] getC9s() {
        return c9s;
    }
}
