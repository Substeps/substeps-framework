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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

//Note: This class is populated my maven via reflection, therefore changes here must be mirrored in
//ExecutionConfig under the maven runner project
public class SubstepsExecutionConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String description;

    private String tags;

    private String nonFatalTags;

    private String featureFile;

    private String subStepsFileName;

    private String scenarioName;

    private boolean strict = true;

    private boolean fastFailParseErrors = true;

    private Properties systemProperties;

    private String[] nonStrictKeywordPrecedence;

    private String[] stepImplementationClassNames;

    private String[] initialisationClass;

    private List<Class<?>> stepImplementationClasses;

    private Class<?>[] initialisationClasses;

    private String[] executionListeners;

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(final String tags) {
        this.tags = tags;
    }

    public String getNonFatalTags() {
        return nonFatalTags;
    }

    public void setNonFatalTags(final String nonFatalTags) {
        this.nonFatalTags = nonFatalTags;
    }

    public String getFeatureFile() {
        return featureFile;
    }

    public void setFeatureFile(final String featureFile) {
        this.featureFile = featureFile;
    }

    public String getSubStepsFileName() {
        return subStepsFileName;
    }

    public void setSubStepsFileName(final String subStepsFileName) {
        this.subStepsFileName = subStepsFileName;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(final boolean strict) {
        this.strict = strict;
    }

    public boolean isFastFailParseErrors() {
        return fastFailParseErrors;
    }

    public void setFastFailParseErrors(final boolean fastFailParseErrors) {
        this.fastFailParseErrors = fastFailParseErrors;
    }

    public Properties getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(final Properties systemProperties) {
        this.systemProperties = systemProperties;
    }

    public String[] getNonStrictKeywordPrecedence() {
        return nonStrictKeywordPrecedence;
    }

    public void setNonStrictKeywordPrecedence(final String[] nonStrictKeywordPrecedence) {
        this.nonStrictKeywordPrecedence = nonStrictKeywordPrecedence;
    }

    public String[] getStepImplementationClassNames() {
        return stepImplementationClassNames;
    }

    public void setStepImplementationClassNames(final String[] stepImplementationClassNames) {
        this.stepImplementationClassNames = stepImplementationClassNames;
    }

    public String[] getInitialisationClass() {
        return initialisationClass;
    }

    public void setInitialisationClass(final String[] initialisationClass) {
        this.initialisationClass = initialisationClass;
    }

    public List<Class<?>> getStepImplementationClasses() {
        return stepImplementationClasses;
    }

    public void setStepImplementationClasses(final List<Class<?>> stepImplementationClasses) {
        this.stepImplementationClasses = stepImplementationClasses;
    }

    public Class<?>[] getInitialisationClasses() {
        return initialisationClasses;
    }

    public void setInitialisationClasses(final Class<?>[] initialisationClasses) {
        this.initialisationClasses = initialisationClasses;
    }

    public String[] getExecutionListeners() {
        return executionListeners;
    }

    public void setExecutionListeners(final String[] executionListeners) {
        this.executionListeners = executionListeners;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }


    public String printParameters() {
        return "SubstepExecutionConfig [description=" + getDescription() + ", tags=" + getTags() + ", nonFatalTags="
                + getNonFatalTags() + ", featureFile=" + getFeatureFile() + ", subStepsFileName="
                + getSubStepsFileName() + ", strict=" + isStrict() + ", fastFailParseErrors=" + isFastFailParseErrors() + ", scenarioName=" + getScenarioName()
                + ", nonStrictKeywordPrecedence=" + Arrays.toString(getNonStrictKeywordPrecedence())
                + ", stepImplementationClassNames=" + Arrays.toString(getStepImplementationClassNames())
                + ", initialisationClass=" + Arrays.toString(getInitialisationClass()) + ", stepImplementationClasses="
                + getStepImplementationClasses() + ", initialisationClasses="
                + Arrays.toString(getInitialisationClasses()) + ", executionListeners="
                + Arrays.toString(getExecutionListeners()) + "]";
    }
}