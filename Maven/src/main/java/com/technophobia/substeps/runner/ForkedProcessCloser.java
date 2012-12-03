package com.technophobia.substeps.runner;

import org.apache.maven.plugin.logging.Log;

public class ForkedProcessCloser implements Runnable {

    private SubstepsJMXClient client;
    private Process forkedJvm;
    private Thread thread;
    private Log log;

    private static final long GRACEFULL_SHUTDOWN_TIMEOUT_MILLIS = 2 * 1000;

    public static ForkedProcessCloser addHook(SubstepsJMXClient client, Process forkedJvm, Log log) {

        ForkedProcessCloser hook = new ForkedProcessCloser(client, forkedJvm, log);
        Runtime.getRuntime().addShutdownHook(hook.getThread());
        return hook;
    }

    public void notifyShutdownSuccessful() {

        Runtime.getRuntime().removeShutdownHook(getThread());
    }

    private ForkedProcessCloser(SubstepsJMXClient client, Process forkedJvm, Log log) {

        this.client = client;
        this.forkedJvm = forkedJvm;
        this.log = log;
        this.thread = new Thread(this);
    }

    private Thread getThread() {

        return this.thread;
    }

    @Override
    public void run() {

        log.warn("Substeps forked process shutdown hook triggered, process may not have completed cleanly");

        performShutdown();
    }

    public void performShutdown() {

        if (client != null) {

            GracefullShutdownRunner gracefullShutdownRunner = new GracefullShutdownRunner();
            Thread gracefullShutdownThread = new Thread(gracefullShutdownRunner);
            gracefullShutdownThread.run();

            try {

                gracefullShutdownThread.join(GRACEFULL_SHUTDOWN_TIMEOUT_MILLIS);

            } catch (InterruptedException e) {

                log.error("Interrupted waiting for graceful shutdown");
            }

            if (!gracefullShutdownRunner.hasShutdown()) {

                log.info("Graceful shutdown failed, it will now be forcibly terminated");

                this.forkedJvm.destroy();

                log.info("The remote process has been terminated");
            }
        }
    }

    private class GracefullShutdownRunner implements Runnable {

        private boolean shutdown = false;

        @Override
        public void run() {

            ForkedProcessCloser.this.log.info("Attempting to shutdown remote substep server via jmx");

            try {

                if (ForkedProcessCloser.this.client.shutdown()) {
                    ForkedProcessCloser.this.forkedJvm.waitFor();
                    ForkedProcessCloser.this.log.info("Server process exited");
                    shutdown = true;
                }
            } catch (Exception e) {

                ForkedProcessCloser.this.log.info("Failed to shutdown the server gracefully");
            }

        }

        public boolean hasShutdown() {

            return shutdown;

        }
    }

}
