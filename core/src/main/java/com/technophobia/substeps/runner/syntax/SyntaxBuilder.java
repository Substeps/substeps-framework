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
package com.technophobia.substeps.runner.syntax;

import com.technophobia.substeps.model.ParentStep;
import com.technophobia.substeps.model.PatternMap;
import com.technophobia.substeps.model.Syntax;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author imoore
 */
public final class SyntaxBuilder {
    private SyntaxBuilder() {
    }

    public static Syntax buildSyntax(final List<Class<?>> stepImplementationClasses, final File subStepsFile) {
        return buildSyntax(stepImplementationClasses, subStepsFile, true, (String[])null);
    }


    public static Syntax buildSyntax(final List<Class<?>> stepImplementationClasses, final File subStepsFile,
                                     final boolean strict, final String[] nonStrictKeywordPrecedence) {
        return buildSyntax(stepImplementationClasses, subStepsFile, strict, nonStrictKeywordPrecedence,
                new ClassAnalyser());
    }

    public static Syntax buildSyntax(final List<Class<?>> stepImplementationClasses, final File subStepsFile,
                                     final boolean strict, final List<String> nonStrictKeywordPrecedence) {

        String[] array = null;
        if (nonStrictKeywordPrecedence != null){
            array = nonStrictKeywordPrecedence.toArray(new String[nonStrictKeywordPrecedence.size()]);
        }
        return buildSyntax(stepImplementationClasses, subStepsFile, strict,array , new ClassAnalyser());
    }

    public static Syntax buildSyntax(final List<Class<?>> stepImplementationClasses, final File subStepsFile,
                                     final boolean strict, final String[] nonStrictKeywordPrecedence, final ClassAnalyser classAnalyser) {
        return buildSyntax(stepImplementationClasses, subStepsFile, strict, nonStrictKeywordPrecedence, classAnalyser,
                true);
    }


    public static Syntax buildSyntax(final List<Class<?>> stepImplementationClasses, final File subStepsFile,
                                     final boolean strict, final String[] nonStrictKeywordPrecedence, final ClassAnalyser classAnalyser,
                                     final boolean failOnDuplicateEntries) {
        return buildSyntax(stepImplementationClasses, subStepsFile, strict, nonStrictKeywordPrecedence, classAnalyser,
                failOnDuplicateEntries, new DefaultSyntaxErrorReporter());
    }


    public static Syntax buildSyntax(final List<Class<?>> stepImplementationClasses, final File subStepsFile,
                                     final boolean strict, final String[] nonStrictKeywordPrecedence, final ClassAnalyser classAnalyser,
                                     final boolean failOnDuplicateEntries, final SyntaxErrorReporter syntaxErrorReporter) {
        final Syntax syntax = buildBaseSyntax(stepImplementationClasses, classAnalyser, failOnDuplicateEntries,
                syntaxErrorReporter);

        syntax.setStrict(strict, nonStrictKeywordPrecedence);

        if (subStepsFile != null) {
            final SubStepDefinitionParser subStepParser = new SubStepDefinitionParser(failOnDuplicateEntries,
                    syntaxErrorReporter);
            syntax.setSubStepsMap(subStepParser.loadSubSteps(subStepsFile));
        }

        return syntax;
    }

    public static Syntax buildSyntax(final List<Class<?>> stepImplementationClasses, final PatternMap<ParentStep> loadedSubSteps) {

        final Syntax syntax = new Syntax(new DefaultSyntaxErrorReporter());

        syntax.setSubStepsMap(loadedSubSteps);

        final ClassAnalyser classAnalyser = new ClassAnalyser();
        for (final Class<?> implClass : stepImplementationClasses) {
            classAnalyser.analyseClass(implClass, syntax);
        }

        syntax.setStrict(true, null);

        return syntax;
    }


    private static Syntax buildBaseSyntax(final List<Class<?>> stepImplementationClasses,
                                          final ClassAnalyser classAnalyser, final boolean failOnDuplicateEntries,
                                          final SyntaxErrorReporter syntaxErrorReporter) {
        // step implementations (arranged by StepDefinition, ie the annotation)
        // +
        // sub step definitions

        final Syntax syntax = new Syntax(syntaxErrorReporter);
        syntax.setFailOnDuplicateStepImplementations(failOnDuplicateEntries);

        final List<Class<?>> implClassList;

        if (stepImplementationClasses != null) {
            implClassList = stepImplementationClasses;
        } else {
            implClassList = Collections.emptyList();
        }

        for (final Class<?> implClass : implClassList) {
            classAnalyser.analyseClass(implClass, syntax);
        }

        return syntax;
    }
}
