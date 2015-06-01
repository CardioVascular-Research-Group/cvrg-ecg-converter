package edu.jhu.icm.ecgFormatConverter.hl7;

import hl7OrgV3.AnnotatedECGDocument;
import hl7OrgV3.GLISTPQ;
import hl7OrgV3.PORTMT020001AnnotatedECG;
import hl7OrgV3.PORTMT020001Component5;
import hl7OrgV3.PORTMT020001Component8;
import hl7OrgV3.PORTMT020001Component9;
import hl7OrgV3.PORTMT020001Sequence;
import hl7OrgV3.PORTMT020001SequenceSet;
import hl7OrgV3.SLISTPQ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigDecimal;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlSimpleList;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFile;
import edu.jhu.icm.ecgFormatConverter.ECGFileWriter;

public class HL7AecgWriter extends ECGFileWriter{
	
	 private static final String TEMPLATE_FILENAME = "TemplateExample1.xml";
	 public final static String[] LEADNAME = {"MDC_ECG_LEAD_X", "MDC_ECG_LEAD_Y", "MDC_ECG_LEAD_Z"};
	 private static XmlObject xmlObject;
	 private static PipedOutputStream outputStream;

	@Override
	public File writeToFile(String outputPath, String recordName, ECGFile ecgFile) throws ECGConverterException, IOException {
        AnnotatedECGDocument aecgdoc = createTemplate(true);
        aecgdoc = createAecgDoc(ecgFile.data, ecgFile.samplingRate, aecgdoc);
        File file = saveAecgXML(aecgdoc, outputPath); // XML output filename (HL7)
        return file;
	}

	@Override
	public InputStream writeToInputStream(String recordName, ECGFile ecgFile) throws ECGConverterException, IOException{
        AnnotatedECGDocument aecgdoc = createTemplate(true);
        aecgdoc = createAecgDoc(ecgFile.data, ecgFile.samplingRate, aecgdoc);
        File tempFile = writeToFile("", recordName, ecgFile);
        return new FileInputStream(tempFile);
	}
	
    private static File saveAecgXML(XmlObject aecgdoc, String fileName) throws IOException {
    	File file = new File(fileName);
        aecgdoc.save(file);
        return file;
    }
    
    private static InputStream saveAecgXML(XmlObject aecgdoc) throws IOException {
    	
    	xmlObject = aecgdoc;
    	PipedInputStream inputStream = new PipedInputStream();
    	outputStream = new PipedOutputStream((PipedInputStream)inputStream);
		
		new Thread(new Runnable() {
			public void run(){
				try {
					xmlObject.save(outputStream);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		outputStream.close();
		return inputStream;
    }

    private static AnnotatedECGDocument createAecgDoc(int[][] data, float samplingRate, AnnotatedECGDocument aecgdoc)
    		throws ECGConverterException, IOException {
        PORTMT020001SequenceSet rdtSeqSet = getSequenceSet(aecgdoc);

        int size = 0;
        while (4 < (size = rdtSeqSet.sizeOfComponentArray())) {
            rdtSeqSet.removeComponent(size - 1);
        }
        PORTMT020001Component9[] c9s = rdtSeqSet.getComponentArray();
        int leadIndex = -1;

        for (int i = 0; i < c9s.length; i++) {
            PORTMT020001Sequence sequence = c9s[i].getSequence();
            String code = sequence.getCode().getCode();
       
            XmlObject value = sequence.getValue();
            if (code.equals(Constants.codeTA)) {
            	throw new ECGConverterException("check the template file for SLIST_PQ");
            } else if (code.equals(Constants.codeRA)) {
                if (value instanceof GLISTPQ) {
                    GLISTPQ g = (GLISTPQ) value;
                    double d = (1.0 / samplingRate);
                    BigDecimal newIncrement = new BigDecimal(d);
                    g.getIncrement().setValue(newIncrement);
   
                } else {
                	throw new ECGConverterException("Aecg Doc creation error.");
                }
            } else {
                leadIndex++;
	                String leadName = LEADNAME[leadIndex];
	                sequence.getCode().setCode(leadName);
	                if (value instanceof SLISTPQ) {
	                    SLISTPQ s = (SLISTPQ) value;
	                    List newList = new PrimitiveIntArrayList(data[leadIndex]);
	                    s.setDigits(new XmlSimpleList(newList));
	
	                } else {
	                	throw new ECGConverterException("check the template file for SLIST_PQ");
	                }
            }
        }
        return aecgdoc;
    }

    private static PORTMT020001SequenceSet getSequenceSet(AnnotatedECGDocument aecgdoc) {
        if (aecgdoc == null) {
            return null;
        }

        //  validate(aecgdoc, true);

        PORTMT020001AnnotatedECG aecg = aecgdoc.getAnnotatedECG();
        PORTMT020001Component5[] components = aecg.getComponentArray();

        PORTMT020001Component5 component = components[0];

        //TO-DO may need to delete other Component8
        PORTMT020001Component8 c8 = component.getSeries().getComponentArray()[0];

        PORTMT020001SequenceSet rdtSeqSet = c8.getSequenceSet();
        return rdtSeqSet;
    }

    private static AnnotatedECGDocument createTemplate(boolean isResouce) throws ECGConverterException, IOException {

        AnnotatedECGDocument aecgdoc = null;
        try {
            if (isResouce) {
                InputStream ins = Writer.class.getResourceAsStream(File.separator + TEMPLATE_FILENAME);
                aecgdoc = AnnotatedECGDocument.Factory.parse(ins);
            } else {
                File xmlFile = new File(TEMPLATE_FILENAME);
                aecgdoc = AnnotatedECGDocument.Factory.parse(xmlFile);
            }
        } catch (XmlException e) {
        	throw new ECGConverterException("AECGTemplate XML Error.");	
        } catch (IOException e) {
        	throw new ECGConverterException("AECGTemplate not found.");	
        }
        return aecgdoc;
    }
}