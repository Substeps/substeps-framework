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

import com.google.common.base.Strings;
import com.technophobia.substeps.model.ParentStep;
import com.technophobia.substeps.model.PatternMap;
import com.technophobia.substeps.model.Step;
import com.technophobia.substeps.model.exception.DuplicatePatternException;
import com.technophobia.substeps.parser.FileContents;
import com.technophobia.substeps.runner.FeatureFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

/**
 * @author ian
 *         <br>
 *         TODO: failOnDuplicateSubsteps is used inconsistently between
 *         processDirective and parseSubStepFile. It's also being interpreted as
 *         'fail-fast' when it was intended to mean
 *         parse-and-(run|don't-run)-on-error.
 */
public class SubStepDefinitionParser {

    private final Logger log = LoggerFactory.getLogger(SubStepDefinitionParser.class);

    private ParentStep currentParentStep;

    private final PatternMap<ParentStep> parentMap = new PatternMap<ParentStep>();

    private final boolean failOnDuplicateSubsteps;

    private final SyntaxErrorReporter syntaxErrorReporter;

    private Directive currentDirective = null;

    public SubStepDefinitionParser(final SyntaxErrorReporter syntaxErrorReporter) {
        this(true, syntaxErrorReporter);
    }

    public SubStepDefinitionParser(final boolean failOnDuplicateSubsteps, final SyntaxErrorReporter syntaxErrorReporter) {
        this.failOnDuplicateSubsteps = failOnDuplicateSubsteps;
        this.syntaxErrorReporter = syntaxErrorReporter;
    }


    void parseSubStepFile(final File substepFile) {

        FileContents currentFileContents = FileContents.fromFile(substepFile);

        parseSubstepFileContents(currentFileContents);

    }

    public PatternMap<ParentStep> parseSubstepFileContents(FileContents currentFileContents) {
        for (int i = 0; i < currentFileContents.getNumberOfLines(); i++) {

            // Line numbers are 1-based in FileContents
            int idx = i + 1;
            final String line = currentFileContents.getLineAt(idx);

            processLine(idx, line, currentFileContents);
        }

        // add the last scenario in, but only if it has some steps
        if (this.currentParentStep != null) {

            if (this.currentParentStep.getSteps() != null && !this.currentParentStep.getSteps().isEmpty()) {
                try {
                    storeForPatternOrThrowException(this.currentParentStep.getParent().getPattern(),
                            this.currentParentStep);
                } catch (final DuplicatePatternException ex) {
                    this.syntaxErrorReporter.reportSubstepsError(ex);
                    if (this.failOnDuplicateSubsteps) {
                        throw ex;
                    }
                }
            } else {

                this.log.warn("Ignoring substep definition [" + this.currentParentStep.getParent().getLine()
                        + "] as it has no steps");
            }
            // we're moving on to another file, so set this to null.
            // TODO - pass this around rather than stash the state
            this.currentParentStep = null;
        }
        return this.parentMap;
    }

    public PatternMap<ParentStep> loadSubSteps(final File definitions) {

        final Collection<File> substepsFiles = FileUtils.getFiles(definitions, "substeps");

        for (final File f : substepsFiles) {
            parseSubStepFile(f);
        }

        return this.parentMap;
    }

    private void processLine(final int lineNumberIdx, final String line, final FileContents currentFileContents) {

        if (this.log.isTraceEnabled()) {
            this.log.trace("substep line[" + line + "] @ " + lineNumberIdx + ":"
                    + currentFileContents.getFile().getName());
        }

        if (line != null && line.length() > 0) {
            // does this line begin with any of annotation values that we're
            // interested in ?

            // pick out the first word
            final String trimmed = FeatureFileParser.stripComments(line.trim());
            if (trimmed != null && trimmed.length() > 0 && !trimmed.startsWith("#")) {
                processTrimmedLine(trimmed, lineNumberIdx, currentFileContents);
            }

        }
    }

    private void processTrimmedLine(final String trimmed, final int lineNumberIdx, FileContents currentFileContents) {

        // TODO convert <> into regex wildcards

        final int scolon = trimmed.indexOf(':');

        boolean lineProcessed = false;

        if (scolon > 0) {
            // is this a directive line
            final String word = trimmed.substring(0, scolon);
            final String remainder = trimmed.substring(scolon + 1);
            final Directive d = isDirective(word);
            if (d != null) {
                final String trimmedRemainder = remainder.trim();
                if (!Strings.isNullOrEmpty(trimmedRemainder)) {
                    processDirective(d, remainder, lineNumberIdx, currentFileContents);
                    lineProcessed = true;
                }
            }
        }

        if (!lineProcessed && this.currentParentStep != null) {

            final int sourceOffset = currentFileContents.getSourceStartOffsetForLineIndex(lineNumberIdx);
            // no context at the mo
            this.currentParentStep.addStep(new Step(trimmed, true, currentFileContents.getFile(),
                    lineNumberIdx, sourceOffset));

        }
    }


    private void processDirective(final Directive d, final String remainder, final int lineNumberIdx, FileContents currentFileContents) {
        this.currentDirective = d;

        switch (this.currentDirective) {

            case DEFINITION: {

                // build up a Step from the remainder

                final int sourceOffset = currentFileContents.getSourceStartOffsetForLineIndex(lineNumberIdx);

                final Step parent = new Step(remainder, true, currentFileContents.getFile(), lineNumberIdx,
                        sourceOffset);

                if (this.currentParentStep != null) {
                    final String newPattern = this.currentParentStep.getParent().getPattern();

                    try {
                        storeForPatternOrThrowException(newPattern, this.currentParentStep);
                    } catch (final DuplicatePatternException ex) {
                        this.syntaxErrorReporter.reportSubstepsError(ex);

                        if (this.failOnDuplicateSubsteps) {
                            throw ex;
                        }
                    }
                }

                this.currentParentStep = new ParentStep(parent);

                break;
            }
            default: // whatever
        }
    }

    private void storeForPatternOrThrowException(final String newPattern, final ParentStep parentStep) {

        if (!this.parentMap.containsPattern(newPattern)) {
            this.parentMap.put(newPattern, parentStep);
        } else {
            throw new DuplicatePatternException(newPattern, this.parentMap.getValueForPattern(newPattern), parentStep);
        }

    }

    private enum Directive {
        // @formatter:off
        DEFINITION("Define");

        // @formatter:on

        Directive(final String name) {
            this.name = name;
        }

        private final String name;
    }

    private static Directive isDirective(final String word) {

        Directive rtn = null;

        for (final Directive d : Directive.values()) {

            if (word.equalsIgnoreCase(d.name)) {
                rtn = d;
                break;
            }
        }
        return rtn;
    }

}
