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
package com.technophobia.substeps.report;

import com.technophobia.substeps.execution.node.IExecutionNode;
import com.technophobia.substeps.execution.node.RootNode;

import java.io.File;

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

/**
 * @author ian
 */
public abstract class ExecutionReportBuilder {

    private static final String DEFAULT_EXECUTION_REPORT_BUILDER_CLASSNAME = "com.technophobia.substeps.report.DefaultExecutionReportBuilder";

    public static ExecutionReportBuilder createExecutionReportBuilder(String executionReportBuilderClassName) {

        try {

            return (ExecutionReportBuilder) Class.forName(executionReportBuilderClassName).newInstance();
        } catch (InstantiationException e) {

            throw new UnableToLoadExectuionReportBuilder(executionReportBuilderClassName, e);
        } catch (ClassNotFoundException e) {

            throw new UnableToLoadExectuionReportBuilder(executionReportBuilderClassName, e);
        } catch (IllegalAccessException e) {

            throw new UnableToLoadExectuionReportBuilder(executionReportBuilderClassName, e);
        }
    }

    public static ExecutionReportBuilder createDefaultExecutionReportBuilder() {

        return createExecutionReportBuilder(DEFAULT_EXECUTION_REPORT_BUILDER_CLASSNAME);
    }

    public abstract void addRootExecutionNode(final RootNode node);

    public abstract void buildReport();

    public static void buildDescriptionString(final String prefix, final IExecutionNode node, final StringBuilder buf) {

        if (prefix != null) {
            buf.append(prefix);
        }

        buf.append(node.getDescription());
    }

    public abstract void setOutputDirectory(File file);

}