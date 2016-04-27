/*
 *    Copyright Technophobia Ltd 2012
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
package org.substeps.runner;

import com.technophobia.substeps.model.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public enum CoreSubstepsPropertiesConfiguration implements CoreSubstepsConfiguration {

    INSTANCE ; // uninstantiable

    private final Logger LOG = LoggerFactory.getLogger(CoreSubstepsPropertiesConfiguration.class);

    private final int stepDepthForDescription;

    private final boolean logUncalledAndUnusedStepImpls;


    private CoreSubstepsPropertiesConfiguration() {

        final URL defaultURL = getClass().getResource("/default-core-substeps.properties");

        if (defaultURL == null){
            throw new IllegalStateException("Unable to find default core properties");
        }

        Configuration.INSTANCE.addDefaultProperties(defaultURL, "default-core-substeps");

        stepDepthForDescription = Configuration.INSTANCE.getInt("step.depth.description");

        logUncalledAndUnusedStepImpls = Configuration.INSTANCE.getBoolean("log.unused.uncalled");

        LOG.info("Using core properties:\n" + Configuration.INSTANCE.getConfigurationInfo());
    }


    public int getStepDepthForDescription() {
        return stepDepthForDescription;
    }

    public boolean isLogUncalledAndUnusedStepImpls() {
        return logUncalledAndUnusedStepImpls;
    }
}
