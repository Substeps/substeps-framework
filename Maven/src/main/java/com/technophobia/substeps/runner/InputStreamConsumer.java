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

    private final CountDownLatch processStarted;
    private final AtomicBoolean processStartedOk;

    private final InputStream stderr;
    private InputStreamReader isr = null;
    private BufferedReader br = null;


    public InputStreamConsumer(final InputStream stderr, final Log logger, final CountDownLatch processStarted,
            final AtomicBoolean processStartedOk) {
        this.logger = logger;
        this.processStarted = processStarted;
        this.processStartedOk = processStartedOk;
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
            this.isr = new InputStreamReader(this.stderr);
            this.br = new BufferedReader(this.isr);

            while ((line = this.br.readLine()) != null) {

                // NB. this is not a logger as we don't want to be able to turn
                // this off
                // If the level of logging from the child process is verbose,
                // change the logging level of the spawned process.
                System.out.println(" *\t" + line);

                if (line.contains("awaiting the shutdown notification...")) {
                    this.logger.info("mbean server process started");
                    this.processStartedOk.set(true);
                    this.processStarted.countDown();

                } else if (!this.processStartedOk.get()) {
                    this.logger.info("line received but this was not the correct line: " + line);
                }

            }
        } catch (final IOException e) {

            e.printStackTrace();
        } finally {

            if (this.processStarted.getCount() > 0) {
                this.logger
                        .info("spawned process didn't start fully, no further output, an error is assumed and the process will terminate");
                this.processStarted.countDown();
            }
        }
    }

}
