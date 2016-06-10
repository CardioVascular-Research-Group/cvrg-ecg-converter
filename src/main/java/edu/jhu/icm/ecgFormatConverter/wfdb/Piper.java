package edu.jhu.icm.ecgFormatConverter.wfdb;
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
* @author Andre Vilardo, Chris Jurado
*/
import java.io.IOException;
import java.io.InputStream;

public class Piper implements Runnable{

    private java.io.InputStream input;
    private java.io.OutputStream output;

    public Piper(java.io.InputStream input, java.io.OutputStream output) {
        this.input = input;
        this.output = output;
    }

    public void run() {
        try {
            // Create 512 bytes buffer
            byte[] b = new byte[512];
            int read = 1;
            // As long as data is read; -1 means EOF
            while (read > -1) {
                // Read bytes into buffer
                read = input.read(b, 0, b.length);
                //System.out.println("read: " + new String(b));
                if (read > -1) {
                    // Write bytes to output
                    output.write(b, 0, read);
                }
            }
        } catch (IOException e) {
        	e.printStackTrace();
            // Something happened while reading or writing streams; pipe is broken
            throw new RuntimeException("Broken pipe", e);
        } finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
    }
    
    public static InputStream pipe(java.lang.Process... proc){
        // Start Piper between all processes
        java.lang.Process p1;
        java.lang.Process p2;
        for (int i = 0; i < proc.length; i++) {
            p1 = proc[i];
            // If there's one more process
            if (i + 1 < proc.length) {
                p2 = proc[i + 1];
                // Start piper
                new Thread(new Piper(p1.getInputStream(), p2.getOutputStream())).start();
            }
        }
        java.lang.Process last = proc[proc.length - 1];
        // Wait for last process in chain; may throw InterruptedException
        try {
			last.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        // Return its InputStream
        return last.getInputStream();
    }
}