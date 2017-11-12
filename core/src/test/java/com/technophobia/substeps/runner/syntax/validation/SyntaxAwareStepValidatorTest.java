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
package com.technophobia.substeps.runner.syntax.validation;

import com.technophobia.substeps.model.FeatureFile;
import com.technophobia.substeps.model.ParentStep;
import com.technophobia.substeps.model.PatternMap;
import com.technophobia.substeps.model.Syntax;
import com.technophobia.substeps.runner.FeatureFileParser;
import com.technophobia.substeps.runner.syntax.ClassAnalyser;
import com.technophobia.substeps.runner.syntax.SubStepDefinitionParser;
import com.technophobia.substeps.runner.syntax.SyntaxBuilder;
import com.technophobia.substeps.runner.syntax.SyntaxErrorReporter;
import com.technophobia.substeps.stepimplementations.MockStepImplementations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SyntaxAwareStepValidatorTest {

    private static final String FEATURE_PATH = "./target/test-classes/features/";
    private static final String SUBSTEPS_PATH = "./target/test-classes/substeps/";

    private FeatureFileParser featureFileParser;
    private SubStepDefinitionParser substepsFileParser;
    private SyntaxErrorReporter mock;

    @Before
    public void initialise() {

        this.mock = Mockito.mock(SyntaxErrorReporter.class);
        this.featureFileParser = new FeatureFileParser();
        this.substepsFileParser = new SubStepDefinitionParser(mock);
    }


    @Test
    public void validatorReportsMissingStepsInScenario() {
        File ff = createFeatureFile("error.feature");
        final FeatureFile featureFile = this.featureFileParser.loadFeatureFile(ff);

        createStepValidatorWithSubsteps("simple.substeps", mock).validateFeatureFile(featureFile, mock);

        verify(mock).reportFeatureError(eq(ff), eq("Given step 1"), eq(6), anyInt(), any());
        verify(mock).reportFeatureError(eq(ff), eq("Given step 2"), eq(7), anyInt(), any());
    }


    @Test
    public void validatorReportsNoErrorsForFeatureWithValidSteps() {
        final FeatureFile featureFile = this.featureFileParser.loadFeatureFile(createFeatureFile("error.feature"));

        createStepValidatorWithSubsteps("error.substeps",mock).validateFeatureFile(featureFile, mock);

        verify(mock, never()).reportFeatureError(any(),any(),anyInt(),anyInt(),any());

        verify(mock, never()).reportSubstepsError(any());
        verify(mock, never()).reportStepImplError(any());
    }


    @Test
    public void validatorReportsMissingSubstepsInDefinition() {

        File substepsFile = createSubstepsFile("error.substeps").getAbsoluteFile();

        final PatternMap<ParentStep> substeps = substepsFileParser.loadSubSteps(substepsFile);

        final StepValidator stepValidator = createStepValidatorWithSubsteps("simple.substeps", mock);
        for (final ParentStep substep : substeps.values()) {
            stepValidator.validateSubstep(substep, mock);
        }

        verify(mock).reportFeatureError(eq(substepsFile), eq("SingleWord"), eq(5), eq(101), eq("Step \"SingleWord\" is not defined"));
        verify(mock).reportFeatureError(eq(substepsFile), eq("Test_Then something else has happened"), eq(6), anyInt(), any());
        verify(mock).reportFeatureError(eq(substepsFile), eq("Test_Then something has happened"), eq(9), anyInt(), any());

    }


    @Test
    public void validatorReportsNoErrorsForSubstepsWithValidSteps() {
        final PatternMap<ParentStep> substeps = this.substepsFileParser
                .loadSubSteps(createSubstepsFile("allFeatures.substeps"));

        final StepValidator stepValidator = createStepValidatorWithSubsteps("simple.substeps", mock,
                MockStepImplementations.class);
        for (final ParentStep substep : substeps.values()) {
            stepValidator.validateSubstep(substep, mock);
        }

        verify(mock, never()).reportFeatureError(any(),any(),anyInt(),anyInt(),any());
        verify(mock, never()).reportSubstepsError(any());
        verify(mock, never()).reportStepImplError(any());
    }

    private File createFeatureFile(final String name) {
        return new File(FEATURE_PATH, name);
    }

    private File createSubstepsFile(final String name) {
        return new File(SUBSTEPS_PATH, name);
    }

    private StepValidator createStepValidatorWithSubsteps(final String substepsFilename, SyntaxErrorReporter errorReporter,
                                                          final Class<?>... stepImplClasses) {
        final Syntax syntax = SyntaxBuilder.buildSyntax(Arrays.asList(stepImplClasses),
                createSubstepsFile(substepsFilename), true, new String[0], new ClassAnalyser(), true,
                errorReporter);

        return new SyntaxAwareStepValidator(syntax);
    }
}
