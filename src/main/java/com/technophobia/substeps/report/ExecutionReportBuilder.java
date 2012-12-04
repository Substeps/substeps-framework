package com.technophobia.substeps.report;

import java.io.File;

import com.technophobia.substeps.execution.ExecutionNode;

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

/**
 * @author ian
 * 
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

    public abstract void addRootExecutionNode(final ExecutionNode node);

    public abstract void buildReport();

    public static void buildDescriptionString(final String prefix, final ExecutionNode node, final StringBuilder buf) {
        if (prefix != null) {
            buf.append(prefix);
        }

        if (node.getFeature() != null) {

            buf.append(node.getFeature().getName());

        } else if (node.getScenarioName() != null) {

            if (node.isOutlineScenario()) {
                buf.append("Scenario #: ");
            } else {
                buf.append("Scenario: ");
            }
            buf.append(node.getScenarioName());
        }

        if (node.getParent() != null && node.getParent().isOutlineScenario()) {

            buf.append(node.getRowNumber()).append(" ").append(node.getParent().getScenarioName()).append(":");
        }

        if (node.getLine() != null) {
            buf.append(node.getLine());
        }
    }

    public abstract void setOutputDirectory(File file);

}