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

import java.io.Serializable;
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

    private boolean strict = true;

    private boolean fastFailParseErrors = true;

    private Properties systemProperties;

    private String[] nonStrictKeywordPrecedence;

    private String[] stepImplementationClassNames;

    private String[] initialisationClass;

    private List<Class<?>> stepImplementationClasses;

    private Class<?>[] initialisationClasses;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getNonFatalTags() {
        return nonFatalTags;
    }

    public void setNonFatalTags(String nonFatalTags) {
        this.nonFatalTags = nonFatalTags;
    }

    public String getFeatureFile() {
        return featureFile;
    }

    public void setFeatureFile(String featureFile) {
        this.featureFile = featureFile;
    }

    public String getSubStepsFileName() {
        return subStepsFileName;
    }

    public void setSubStepsFileName(String subStepsFileName) {
        this.subStepsFileName = subStepsFileName;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean isFastFailParseErrors() {
        return fastFailParseErrors;
    }

    public void setFastFailParseErrors(boolean fastFailParseErrors) {
        this.fastFailParseErrors = fastFailParseErrors;
    }

    public Properties getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
    }

    public String[] getNonStrictKeywordPrecedence() {
        return nonStrictKeywordPrecedence;
    }

    public void setNonStrictKeywordPrecedence(String[] nonStrictKeywordPrecedence) {
        this.nonStrictKeywordPrecedence = nonStrictKeywordPrecedence;
    }

    public String[] getStepImplementationClassNames() {
        return stepImplementationClassNames;
    }

    public void setStepImplementationClassNames(String[] stepImplementationClassNames) {
        this.stepImplementationClassNames = stepImplementationClassNames;
    }

    public String[] getInitialisationClass() {
        return initialisationClass;
    }

    public void setInitialisationClass(String[] initialisationClass) {
        this.initialisationClass = initialisationClass;
    }

    public List<Class<?>> getStepImplementationClasses() {
        return stepImplementationClasses;
    }

    public void setStepImplementationClasses(List<Class<?>> stepImplementationClasses) {
        this.stepImplementationClasses = stepImplementationClasses;
    }

    public Class<?>[] getInitialisationClasses() {
        return initialisationClasses;
    }

    public void setInitialisationClasses(Class<?>[] initialisationClasses) {
        this.initialisationClasses = initialisationClasses;
    }

}