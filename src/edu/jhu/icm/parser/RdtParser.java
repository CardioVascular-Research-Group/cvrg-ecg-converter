package edu.jhu.icm.parser;

import hl7OrgV3.AnnotatedECGDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class RdtParser {

	/* TODO JDP add in proper logging */
	public void createAECGXMLFromRDT(File rdtFile, String outputFilename) {
    InputStream rdt = null;
    try {
        rdt = new FileInputStream(rdtFile);
    } catch (IOException e) {
        //logger.error(e.getMessage());
        return;
    }
    long fileSize = rdtFile.length();
    RdtParserSimple rps = new RdtParserSimple(rdt, fileSize);//, "doesnotmatter");
    if (!rps.parse()) {
        //logger.error("rdt file parsing failed.");
    	System.out.println("rdt file parsing failed.");
        // do something
    } else {
        //logger.debug("rdt file parsed successfully.");


        //logger.debug("deleted previous rdt upload.");
        AnnotatedECGDocument aecgdoc = Writer.createTemplate(true);

        aecgdoc = Writer.createAecgDoc(rps, aecgdoc);
        
        Writer.saveAecgXML(aecgdoc, outputFilename);

        //logger.debug("saved aecgdoc object.");

       // rps.cleanUp();//
        System.gc(); //ask for gc due to memory usage from this method
        /*
        System.gc();
        Reader r = new Reader(aecgdoc);
        EcgLeadData eld = new EcgLeadData(r.getC9s());
        aecgdoc = null;
        System.gc();
        */
    }
}
}
