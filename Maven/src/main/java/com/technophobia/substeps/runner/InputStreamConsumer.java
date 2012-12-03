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
    @Override
    public void run() {

        String line = null;
        try {
            this.isr = new InputStreamReader(this.stderr);
            this.br = new BufferedReader(this.isr);

            while ((line = this.br.readLine()) != null) {
                this.logger.info(" *\t" + line);

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
