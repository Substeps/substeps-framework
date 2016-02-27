/*
 *  Copyright Technophobia Ltd 2012
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

        public void run() {

            log.info("Attempting to shutdown remote substep server via jmx");

            try {

                if (ForkedProcessCloser.this.client.shutdown()) {
                    ForkedProcessCloser.this.forkedJvm.waitFor();
                    ForkedProcessCloser.this.log.info("Server process exited");
                    shutdown = true;
                }
            } catch (Exception e) {

                log.info("Failed to shutdown the server gracefully", e);
            }

        }

        public boolean hasShutdown() {

            return shutdown;

        }
    }

}
