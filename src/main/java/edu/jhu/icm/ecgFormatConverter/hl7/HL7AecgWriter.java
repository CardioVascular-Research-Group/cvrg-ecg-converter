package edu.jhu.icm.ecgFormatConverter.hl7;
/*
Copyright 2015 Johns Hopkins University Institute for Computational Medicine

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
* @author cyang, Chris Jurado
*/
import hl7OrgV3.AnnotatedECGDocument;
import hl7OrgV3.GLISTPQ;
import hl7OrgV3.PORTMT020001AnnotatedECG;
import hl7OrgV3.PORTMT020001Component5;
import hl7OrgV3.PORTMT020001Component8;
import hl7OrgV3.PORTMT020001Component9;
import hl7OrgV3.PORTMT020001Sequence;
import hl7OrgV3.PORTMT020001SequenceSet;
import hl7OrgV3.SLISTPQ;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlSimpleList;

import edu.jhu.cvrg.converter.exceptions.ECGConverterException;
import edu.jhu.icm.ecgFormatConverter.ECGFileData;
import edu.jhu.icm.ecgFormatConverter.ECGFileWriter;

public class HL7AecgWriter extends ECGFileWriter{
	
	 private static final String TEMPLATE_FILENAME = "TemplateExample1.xml";
	 public final static String[] LEADNAME = {"MDC_ECG_LEAD_X", "MDC_ECG_LEAD_Y", "MDC_ECG_LEAD_Z"};
	 private static XmlObject xmlObject;
	 private static ByteArrayOutputStream outputStream;

	@Override
	public File writeToFile(String outputPath, String recordName, ECGFileData ecgFile) {
        AnnotatedECGDocument aecgdoc;
        File file = null;
		aecgdoc = createTemplate(true);
		aecgdoc = createAecgDoc(ecgFile.data, ecgFile.samplingRate, aecgdoc);
		file = saveAecgXML(aecgdoc, outputPath + recordName + ".xml"); // XML output filename (HL7)

        return file;
	}

	@Override
	public byte[] writeToByteArray(String recordName, ECGFileData ecgFile) {
        AnnotatedECGDocument aecgdoc = null;
		aecgdoc = createTemplate(true);
		aecgdoc = createAecgDoc(ecgFile.data, ecgFile.samplingRate, aecgdoc);

        return saveAecgXML(aecgdoc);
	}
	
    private static File saveAecgXML(XmlObject aecgdoc, String fileName){
    	File file = new File(fileName);
        try {
			aecgdoc.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return file;
    }
    
    private static byte[] saveAecgXML(XmlObject aecgdoc){
    	
    	xmlObject = aecgdoc;
		outputStream = new ByteArrayOutputStream();
		new Thread(new Runnable() {
			public void run(){
				try {
					xmlObject.save(outputStream);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		try {
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputStream.toByteArray();
    }

	private static AnnotatedECGDocument createAecgDoc(int[][] data,
			float samplingRate, AnnotatedECGDocument aecgdoc) {
		PORTMT020001SequenceSet rdtSeqSet = getSequenceSet(aecgdoc);

		int size = 0;
		while (4 < (size = rdtSeqSet.sizeOfComponentArray())) {
			rdtSeqSet.removeComponent(size - 1);
		}
		PORTMT020001Component9[] c9s = rdtSeqSet.getComponentArray();
		int leadIndex = -1;

		try {
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
		} catch (ECGConverterException e) {
			e.printStackTrace();
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

    private static AnnotatedECGDocument createTemplate(boolean isResouce){

        AnnotatedECGDocument aecgdoc = null;
        try {
            if (isResouce) {
                InputStream ins = HL7AecgWriter.class.getResourceAsStream(File.separator + TEMPLATE_FILENAME);
                aecgdoc = AnnotatedECGDocument.Factory.parse(ins);
            } else {
                File xmlFile = new File(TEMPLATE_FILENAME);
                aecgdoc = AnnotatedECGDocument.Factory.parse(xmlFile);
            }
        } catch (XmlException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return aecgdoc;
    }
}