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

import com.technophobia.substeps.runner.SubstepsExecutionConfig;
import com.technophobia.substeps.runner.SubstepsRunner;

/**
 * @author ian
 */
public interface SubstepsServerMBean extends SubstepsRunner {

    static final String SUBSTEPS_JMX_MBEAN_NAME = "com.technopobia.substeps.jmx:type=SubstepsServerMBean";

    void shutdown();

    byte[] prepareExecutionConfigAsBytes(final SubstepsExecutionConfig theConfig);

    byte[] runAsBytes();

}
