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

import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * @author ricky
 */
public class ExecutionConfig {

    /**
     * 
     * @parameter
     */
    private String description;

    /**
     * @parameter
     */
    private String tags;

    /**
     * @parameter
     */
    private String nonFatalTags;

    /**
     * @parameter
     * @required
     */
    private String featureFile;
    /**
     * @parameter
     * @required
     */
    private String subStepsFileName;
    /**
     * 
     * @parameter default-value=true
     * @required
     */
    private boolean strict = true;

    /**
     * 
     * @parameter default-value=true
     * @required
     */
    private boolean fastFailParseErrors = true;

    /**
     * @parameter
     */
    private Properties systemProperties;

    /**
     * @parameter
     */
    private String[] nonStrictKeywordPrecedence;
    /**
     * @parameter
     * @required
     */
    private String[] stepImplementationClassNames;
    /**
     * @parameter
     */
    private String[] initialisationClass;

    public SubstepsExecutionConfig asSubstepsExecutionConfig() throws MojoExecutionException {

        try {
            SubstepsExecutionConfig executionConfig = new SubstepsExecutionConfig();

            reflectivelySetFields(executionConfig);

            return executionConfig;

        } catch (Exception exception) {

            throw new MojoExecutionException("Unable to convert " + ExecutionConfig.class.getName() + " into "
                    + SubstepsExecutionConfig.class.getName(), exception);
        }
    }

    private void reflectivelySetFields(SubstepsExecutionConfig executionConfig) throws NoSuchFieldException,
            IllegalAccessException {

        for (Field mojoField : ExecutionConfig.class.getDeclaredFields()) {

            Field executionConfigField = SubstepsExecutionConfig.class.getDeclaredField(mojoField.getName());
            executionConfigField.setAccessible(true);
            executionConfigField.set(executionConfig, mojoField.get(this));
        }
    }

    public String getDescription() {

        return this.description;
    }

}