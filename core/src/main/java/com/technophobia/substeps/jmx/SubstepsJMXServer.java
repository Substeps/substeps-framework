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

package com.technophobia.substeps.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;

/**
 * @author ian
 */
public class SubstepsJMXServer {

    private final Logger log = LoggerFactory.getLogger(SubstepsJMXServer.class);

    private final CountDownLatch shutdownSignal = new CountDownLatch(1);

    public static void main(final String[] args) {

        // this is the thing that will be instantiated by an external process

        // TODO check the system args for this make sure the jmx args are set

        // -Dcom.sun.management.jmxremote.port=9999
        // -Dcom.sun.management.jmxremote.authenticate=false
        // -Dcom.sun.management.jmxremote.ssl=false

        final SubstepsJMXServer server = new SubstepsJMXServer();
        server.run();
    }

    private void run() {

        log.trace("starting jmx server");

        final SubstepsServer mBeanImpl = new SubstepsServer(this.shutdownSignal);

        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        log.trace("got mbean server");

        try {

            final ObjectName name = new ObjectName(SubstepsServerMBean.SUBSTEPS_JMX_MBEAN_NAME);

            mbs.registerMBean(mBeanImpl, name);

            log.trace("bean registered");

            // TODO use notifications instead of parsing the log file

            boolean rpt = true;
            while (rpt) {
                try {

                    log.debug("awaiting the shutdown notification...");

                    this.shutdownSignal.await();
                    rpt = false;
                    this.log.debug("shutdown notification received");

                } catch (final InterruptedException e) {
                    log.error("InterruptedException", e);
                }
            }

        } catch (final MalformedObjectNameException ex) {

            this.log.error("exception starting substeps mbean server", ex);
        } catch (final InstanceAlreadyExistsException ex) {
            this.log.error("exception starting substeps mbean server", ex);
        } catch (final MBeanRegistrationException ex) {
            this.log.error("exception starting substeps mbean server", ex);
        } catch (final NotCompliantMBeanException ex) {
            this.log.error("exception starting substeps mbean server", ex);
        }

        this.log.debug("run method complete");
    }

}
