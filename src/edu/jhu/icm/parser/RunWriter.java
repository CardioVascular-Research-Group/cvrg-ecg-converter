/*
 * Created on April 14, 2006
 *
 */
package edu.jhu.icm.parser;

import hl7OrgV3.AnnotatedECGDocument;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


/**
 * RunWriter create a process to run Writer.main(). Therefore this class depends
 * on Writer class
 * 
 * @author cyang
 */
public class RunWriter {

    private static Logger logger = Logger.getLogger(RunWriter.class.getName());

    private String rdtFileName, xmlFileName;

    private static final Class writerClass = Writer.class;

    private static final Class aecgClass = AnnotatedECGDocument.class;

    private String buildClasspath() {
        String classpath = LocalDir.getDir(aecgClass);
        logger.debug(classpath);
        File dir = new File(classpath);
        String ret = "";
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();

            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.getName().endsWith(".jar")) {

                    ret += f.getAbsolutePath() + File.pathSeparator;

                }

            }
            // assume writerclass and its siblings that it might use are in a
            // directory
            // is it right?
            String writerpath = LocalDir.getDir(writerClass);
            ret += writerpath;

            logger.debug(ret);
        }
        return ret;
    }

    public RunWriter(String rdtFileName, String xmlFileName) {
        this.rdtFileName = rdtFileName;
        this.xmlFileName = xmlFileName;
    }

    public String getJava() {
        return "C:\\j2sdk1.4.2_06\\bin\\java.exe";
    }

    public void run() {
        String classpath = buildClasspath();
        if (classpath.length() == 0) {
            //bark
            return;
        }
        String[] cmdArray = new String[] { getJava(),"-Xms600m",  "-Xmx1024m", "-cp", classpath,
                writerClass.getName(), this.rdtFileName, this.xmlFileName };
        if (logger.isDebugEnabled()) {
            String total = "";
            for (int i = 0; i < cmdArray.length; i++) {
                total += cmdArray[i] + " ";
            }
            logger.debug(total);
        }

        Runtime runtime = Runtime.getRuntime();
        try {
            Process p = runtime.exec(cmdArray);

            new OutputEater(p.getInputStream(), true).start();
            new OutputEater(p.getErrorStream(), false).start();
            System.out.println("process exited with value " + p.waitFor());
           
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e1) {
            logger.error(e1.getMessage());
        }

    }

    /**
     * test program
     * 
     * @param args
     */
    public static void main(String[] args) {
        initLogger();
        RunWriter rp = new RunWriter("jhu264.rdt", "generated.xml");
        rp.run();
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
     * inner class to digest outputstreams from process
     * 
     * @author cyang
     */
    class OutputEater extends Thread {
        private boolean _isOutput;

        private InputStream _is;

        OutputEater(InputStream is, boolean isOutput) {
            _is = is;
            this._isOutput = isOutput;

        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(_is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (this._isOutput) {
                        System.out.println(line);

                    } else {
                        System.err.println(line);
                        // should be commented out

                    }
                }
            } catch (IOException ex) {
            }
        }
    }
}
