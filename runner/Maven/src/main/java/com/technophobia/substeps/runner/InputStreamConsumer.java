/*
 *	Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps.
 *
 *    Substeps is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.technophobia.substeps.runner;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.maven.plugin.logging.Log;

class InputStreamConsumer implements Runnable {

    private final Log logger;

    private final InputStream stderr;
    private InputStreamReader isr = null;
    private BufferedReader br = null;


    public InputStreamConsumer(final InputStream stderr, final Log logger){

        this.logger = logger;
        this.stderr = stderr;
    }


    void closeQuietly(final Closeable closeable) {

        if (closeable != null) {

            try {
                closeable.close();
            } catch (final IOException e) {

                e.printStackTrace();
            }
        }
    }


    /**
     * 
     */
    public void closeStreams() {
        closeQuietly(this.br);
        closeQuietly(this.isr);
        closeQuietly(this.stderr);
    }


    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        String line = null;
        try {


            int c;
            StringBuilder buf = new StringBuilder();
            while ((c = this.stderr.read()) != -1){

                String s = String.valueOf((char) c);

                if ((char)c == '\n'){
                    line = buf.toString();

                    buf = new StringBuilder();
                    logger.info("*\t" + line);
                }
                else {
                    buf.append(s);
                }
            }

        } catch (final IOException e) {
            logger.error("error handling output streams", e);
        }
    }

}
