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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.jmx.SubstepsServerMBean;

/**
 * @author ian
 * 
 */
public class SubstepsJMXClient implements SubstepsRunner {

    Logger log = LoggerFactory.getLogger(SubstepsJMXClient.class);
    private SubstepsServerMBean mbean;

    private JMXConnector cntor = null;

    public void init(final int portNumber) throws MojoExecutionException {

        final String url = "service:jmx:rmi:///jndi/rmi://:" + portNumber + "/jmxrmi";

        // The address of the connector server

        try {
            final JMXServiceURL serviceURL = new JMXServiceURL(url);

            final Map<String, ?> environment = null;

            // Create the JMXCconnectorServer
            this.cntor = JMXConnectorFactory.connect(serviceURL, environment);

            // Obtain a "stub" for the remote MBeanServer
            final MBeanServerConnection mbsc = this.cntor.getMBeanServerConnection();

            final ObjectName objectName = new ObjectName(SubstepsServerMBean.SUBSTEPS_JMX_MBEAN_NAME);
            this.mbean = MBeanServerInvocationHandler.newProxyInstance(mbsc, objectName, SubstepsServerMBean.class,
                    false);

        } catch (final IOException e) {

            throw new MojoExecutionException("Failed to connect to substeps server", e);

        } catch (final NullPointerException e) {

            throw new MojoExecutionException("Failed to connect to substeps server", e);
        } catch (final MalformedObjectNameException e) {

            throw new MojoExecutionException("Failed to connect to substeps server", e);
        }
    }

    public RootNode prepareExecutionConfig(final SubstepsExecutionConfig cfg) {

        return this.mbean.prepareExecutionConfig(cfg);

    }

    public List<SubstepExecutionFailure> getFailures() {

        return this.mbean.getFailures();
    }

    public RootNode run() {

        return this.mbean.run();
    }

    public void addNotifier(final IExecutionListener listener) {

        this.mbean.addNotifier(listener);
    }

    public boolean shutdown() {

        boolean successfulShutdown = false;
        try {

            this.mbean.shutdown();
            successfulShutdown = true;

        } catch (final RuntimeException re) {

            this.log.debug("Unable to connect to server to shutdown, it may have already closed");

        }
        return successfulShutdown;
    }

}
