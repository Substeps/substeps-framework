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
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public enum CoreSubstepsPropertiesConfiguration implements CoreSubstepsConfiguration {

    INSTANCE ; // uninstantiable

    private transient final Logger LOG = LoggerFactory.getLogger(CoreSubstepsPropertiesConfiguration.class);

    private final int stepDepthForDescription;

    private final boolean logUncalledAndUnusedStepImpls;

    private final boolean prettyPrintReportData;

    private final String reportDataBaseDir;


    private CoreSubstepsPropertiesConfiguration() {

        Config substepsConfig = Configuration.INSTANCE.getSubstepsConfig();

        stepDepthForDescription = substepsConfig.getInt("step.depth.description");

        logUncalledAndUnusedStepImpls = substepsConfig.getBoolean("log.unused.uncalled");

        prettyPrintReportData = substepsConfig.getBoolean("report.data.pretty.print");

        reportDataBaseDir = substepsConfig.getString("report.data.base.dir");

        LOG.info("Using core properties:\n" + Configuration.INSTANCE.getConfigurationInfo());
    }

    @Override
    public int getStepDepthForDescription() {
        return stepDepthForDescription;
    }

    @Override
    public boolean isLogUncalledAndUnusedStepImpls() {
        return logUncalledAndUnusedStepImpls;
    }

    @Override
    public boolean isPrettyPrintReportData() {
        return prettyPrintReportData;
    }

    public String getReportDataBaseDir() {
        return reportDataBaseDir;
    }
}
