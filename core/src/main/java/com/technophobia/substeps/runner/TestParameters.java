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

import com.technophobia.substeps.helper.AssertHelper;
import com.technophobia.substeps.model.FeatureFile;
import com.technophobia.substeps.model.Scenario;
import com.technophobia.substeps.model.Syntax;
import com.technophobia.substeps.runner.syntax.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ian
 */
public class TestParameters {
    private final Logger log = LoggerFactory.getLogger(TestParameters.class);

    private final TagManager tagManager;
    private final Syntax syntax;
    private final String featureFile;
    private List<FeatureFile> featureFileList = null;
    private boolean failParseErrorsImmediately = true;
    private final String scenarioName;


    public TestParameters(final TagManager tagManager, final Syntax syntax, final String featureFile) {
        this(tagManager, syntax, featureFile, null);
    }


    public TestParameters(final TagManager tagManager, final Syntax syntax, final String featureFile,
                          final String scenarioName) {
        this.tagManager = tagManager;
        this.syntax = syntax;
        this.featureFile = featureFile;
        this.scenarioName = scenarioName;
    }

    public TestParameters(final TagManager tagManager, final Syntax syntax, final List<FeatureFile> featureFileList) {
        this.tagManager = tagManager;
        this.syntax = syntax;
        this.featureFileList = featureFileList;
        featureFile = null;
        scenarioName = null;
    }


    public void init() {
        init(true);
    }


    public void init(final boolean failOnNoFeatures) {
        final Collection<File> featureFiles = FileUtils.getFiles(new File(featureFile), "feature");

        final FeatureFileParser fp2 = new FeatureFileParser();
        for (final File f : featureFiles) {
            final FeatureFile fFile = fp2.loadFeatureFile(f);
            if (featureFileList == null) {
                featureFileList = new ArrayList<FeatureFile>();
            }
            if (fFile != null) {
                featureFileList.add(fFile);
            }
        }

        final File f = new File(".");
        log.debug("Current dir is: " + f.getAbsolutePath());

        if (failOnNoFeatures) {
            AssertHelper.assertNotNull("No Feature files found!", featureFileList);
            AssertHelper.assertFalse("No Feature files found!", featureFileList.isEmpty());
        } else if (featureFileList == null) {
            featureFileList = Collections.emptyList();
        }

        Collections.sort(featureFileList, new FeatureFileComparator());
    }


    /**
     * @return list of Feature files
     */
    public List<FeatureFile> getFeatureFileList() {
        return featureFileList;
    }


    public boolean isRunnable(final Scenario scenario) {

        return (this.scenarioName != null && this.scenarioName.equals(scenario.getDescription())) ||
                (this.scenarioName == null && tagManager.acceptTaggedScenario(scenario.getTags()));

    }


    public boolean isRunnable(final FeatureFile feature) {
        // a feature is runnable if any of the child scenarios are tagged
        // feature level tags are added to all children

        boolean runnable = false;
        for (final Scenario sc : feature.getScenarios()) {
            runnable = isRunnable(sc);
            if (runnable) {
                break;
            }
        }

        return runnable;
    }


    public Syntax getSyntax() {
        return syntax;
    }


    public boolean isFailParseErrorsImmediately() {
        return failParseErrorsImmediately;
    }


    /**
     * @param failParseErrorsImmediately the failParseErrorsImmediately to set
     */
    public void setFailParseErrorsImmediately(final boolean failParseErrorsImmediately) {
        this.failParseErrorsImmediately = failParseErrorsImmediately;
    }
}
