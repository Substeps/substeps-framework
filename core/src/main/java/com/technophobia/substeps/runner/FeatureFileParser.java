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

import com.google.common.base.Strings;
import com.technophobia.substeps.helper.AssertHelper;
import com.technophobia.substeps.model.Background;
import com.technophobia.substeps.model.FeatureFile;
import com.technophobia.substeps.model.Scenario;
import com.technophobia.substeps.model.Step;
import com.technophobia.substeps.parser.FileContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ian
 */
public class FeatureFileParser {

    private final Logger log = LoggerFactory.getLogger(FeatureFileParser.class);

    private static Map<String, Directive> directiveMap = new HashMap<String, Directive>();

    private static final Pattern DIRECTIVE_PATTERN = Pattern.compile("([\\w ]*):");

    public FeatureFile loadFeatureFile(final File featureFile) {
        // IM - this is a little clumsy, feature file created, passed around and
        // if invalid, discarded..

        // rest our current set of lines

        AssertHelper.assertTrue("Feature file: " + featureFile.getAbsolutePath() + " does not exist!", featureFile.exists());

        FileContents currentFileContents = FileContents.fromFile(featureFile);

        return getFeatureFile(currentFileContents);

    }

    public FeatureFile getFeatureFile(FileContents fileContents) {
        final FeatureFile ff = new FeatureFile();
        ff.setSourceFile(fileContents.getFile());

        final String deCommented = stripCommentsAndBlankLines(fileContents.getLines());

        chunkUpFeatureFile(deCommented, ff, fileContents);

        if (parseFeatureDescription(ff)) {
            // now we're in chunks, time to process each scenario..
            if (ff.getScenarios() != null) {

                for (final Scenario sc : ff.getScenarios()) {
                    buildScenario(sc, fileContents);

                }

                cascadeTags(ff);

                return ff;
            } else {
                this.log.debug("discarding feature " + fileContents.getFile().getName() + "as no scenarios");
                return null;
            }
        } else {
            this.log.debug("discarding feature " + fileContents.getFile().getName() + "as no feature description");
            return null;
        }
    }

    private static String getFirstLinePattern(final String element) {

        final StringBuilder buf = new StringBuilder();
        final String[] lines = element.split("\n");
        // add a wildcard to allow # comments on the end of the line and
        // also tab / space formatting

        buf.append("(").append(Pattern.quote(lines[0])).append(")");
        return buf.toString();
    }

    /**
     * @param ff
     */
    private static void cascadeTags(final FeatureFile ff) {
        // add any feature level tags to all scenario children

        if (ff != null && ff.getTags() != null && !ff.getTags().isEmpty()) {
            for (final Scenario sc : ff.getScenarios()) {
                if (sc.getTags() == null) {
                    sc.setTags(ff.getTags());
                } else {
                    sc.getTags().addAll(ff.getTags());
                }
            }
        }
    }

    /**
     * @param ff
     */
    private static boolean parseFeatureDescription(final FeatureFile ff) {
        boolean valid = true;
        final String raw = ff.getRawText();

        if (Strings.isNullOrEmpty(raw)) {
            valid = false;
        } else {
            final String[] lines = raw.split("\n");
            final StringBuilder description = new StringBuilder();

            for (int i = 0; i < lines.length; i++) {
                final String line = lines[i];
                if (i == 0) {
                    // first line, description is everything after the :
                    final int idx = line.indexOf(':');
                    ff.setName(line.substring(idx + 1).trim());
                } else {
                    if (description.length() > 0) {
                        description.append("\n");
                    }
                    description.append(line);
                }
            }
        }
        return valid;
    }

    /**
     * @param sc
     */
    private void buildScenario(final Scenario sc, FileContents fileContents) {

        final String raw = sc.getRawText();

        final String[] lines = raw.split("\n");

        boolean collectExamples = false;

        int lastOffset = sc.getSourceStartOffset();

        sc.setSourceStartLineNumber(fileContents.getSourceLineNumberForOffset(lastOffset));

        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];

            // need to find the line number using an offset. move the offset as
            // we progress through the lines, that way we can take into account
            // duplicates

            final int lineNumber = fileContents.getSourceLineNumber(line, lastOffset);

            lastOffset = fileContents.getEndOfLineOffset(lineNumber);

            if (i == 0) {
                // first line, description is everything after the :
                final int idx = line.indexOf(':');
                sc.setDescription(line.substring(idx + 1).trim());
                sc.setScenarioLineNumber(lineNumber);
            } else if (line.startsWith(Directive.EXAMPLES.val)) {
                collectExamples = true;
            } else {
                if (line.startsWith("|")) {

                    if (collectExamples) {
                        // we're now onto the examples
                        parseExamples(lineNumber, line, sc);
                    } else {
                        // this is an inline table
                        final Step last = sc.getSteps().get(sc.getSteps().size() - 1);
                        final String[] data = line.split("\\|");
                        last.addTableData(data);
                    }

                } else {
                    sc.addStep(new Step(line, fileContents.getFile(), lineNumber, fileContents
                            .getSourceStartOffsetForLineIndex(lineNumber)));
                }
            }
        }
    }

    /**
     * @param fileContents
     * @param ff
     */
    private void chunkUpFeatureFile(final String fileContents, final FeatureFile ff, FileContents currentFileContents) {
        // get the feature name / description
        // split the feature file up

        final String topLevelFeatureElements[] = fileContents
                .split("(?=Tags:)|(?=Feature:)|(?=Background:)|(?=Scenario:)|(?=Scenario Outline:)");

        Set<String> currentTags = null;

        if (topLevelFeatureElements != null) {
            String currentBackground = null;

            for (final String element : topLevelFeatureElements) {

                if (!Strings.isNullOrEmpty(element)) {

                    this.log.trace("topLevelElement:\n" + element);

                    // grab the identifer

                    final Matcher m = DIRECTIVE_PATTERN.matcher(element);
                    if (m.lookingAt()) {
                        final Directive directive = directiveMap.get(m.group(1));

                        switch (directive) {
                            case TAGS: {
                                if (currentTags == null) {
                                    currentTags = new HashSet<String>();
                                }
                                processTags(currentTags, element);
                                break;
                            }
                            case FEATURE: {
                                ff.setRawText(element);
                                if (currentTags != null) {
                                    ff.setTags(currentTags);
                                }
                                currentTags = null;
                                currentBackground = null;
                                break;
                            }
                            case BACKGROUND: {
                                // stash
                                currentBackground = element;
                                break;
                            }
                            case SCENARIO:
                            case SCENARIO_OUTLINE: {

                                final String firstLinePattern = getFirstLinePattern(element);

                                final Pattern finderPattern = Pattern.compile(firstLinePattern);

                                final Matcher matcher = finderPattern
                                        .matcher(currentFileContents.getFullContent());
                                int start = -1;

                                if (matcher.find()) {
                                    start = matcher.start(0);
                                    // start offsets of this elem into the
                                    // original file
                                }

                                processScenarioDirective(ff, currentTags, currentBackground, element,
                                        directive == Directive.SCENARIO_OUTLINE, start, currentFileContents);

                                currentTags = null;
                                break;
                            }
                            default: {
                                this.log.error("unknown directive");
                                break;
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * @param ff
     * @param currentTags
     * @param currentBackground
     * @param sc
     * @param outline
     * @return
     */
    private void processScenarioDirective(final FeatureFile ff, final Set<String> currentTags,
                                          final String currentBackground, final String sc, final boolean outline, final int start, FileContents currentFileContents) {
        final Scenario scenario = new Scenario();

        scenario.setRawText(sc);
        scenario.setTags(currentTags);
        scenario.setOutline(outline);
        scenario.setSourceStartOffset(start);

        ff.addScenario(scenario);

        if (currentBackground != null) {

            final int backgroundLineNumberIdx = backgroundLineNumber(currentFileContents);

            scenario.setBackground(new Background(backgroundLineNumberIdx, currentBackground, ff.getSourceFile()));

        }
    }

    private int backgroundLineNumber(FileContents currentFileContents) {
        return Math.max(currentFileContents.getFirstLineNumberStartingWith("Background:"), 0);
    }

    /**
     * @param currentTags
     * @param raw
     */
    private static void processTags(final Set<String> currentTags, final String raw) {
        // break up the tags - TODO - this is where we will need to evaluate any
        // boolean logic of tag expressions

        final String postDirective = raw.substring(raw.indexOf(':') + 1);

        final String[] split = postDirective.split("\\s");
        for (final String s : split) {
            final String trimmed = s.trim();
            if (trimmed.length() > 0) {
                currentTags.add(s.trim());
            }
        }
    }

    public static String stripComments(final String line) {
        String trimmed = null;
        if (line != null) {

            final int idx = line.trim().indexOf("#");
            if (idx >= 0) {
                // is the # inside matched quotes

                boolean doTrim = false;

                if (idx == 0) {
                    // first char
                    doTrim = true;
                }

                final String[] splitByQuotes = line.split("\"[^\"]*\"|'[^']*'");
                // this will find parts of the string not in quotes
                for (final String split : splitByQuotes) {
                    if (split.contains("#") ) {
                        // hash exists not in a matching pair of quotes
                        doTrim = true;
                        break;
                    }
                }

                if (doTrim) {
                    trimmed = line.trim().substring(0, idx).trim();
                } else {
                    trimmed = line.trim();
                }
            } else {
                trimmed = line.trim();
            }
        }
        return trimmed;
    }

    /**
     * @param lines
     * @return
     */
    private String stripCommentsAndBlankLines(final List<String> lines) {

        final StringBuilder buf = new StringBuilder();

        for (final String s : lines) {

            final String trimmed = stripComments(s);

            if (!Strings.isNullOrEmpty(trimmed)) {
                // up for inclusion
                buf.append(trimmed);
                buf.append("\n");
            }
        }

        return buf.toString();
    }

    /**
     * @param trimmed
     */
    private static void parseExamples(final int lineNumber, final String trimmed, final Scenario sc) {
        final String[] split = trimmed.split("\\|");

        if (sc.getExampleParameters() == null) {
            sc.addExampleKeys(split);
            sc.setExampleKeysLineNumber(lineNumber);
        } else {
            sc.addExampleValues(lineNumber, split);
        }

    }

    private enum Directive {
        // @formatter:off
        TAGS("Tags"), FEATURE("Feature"), BACKGROUND("Background"), SCENARIO("Scenario"), SCENARIO_OUTLINE(
                "Scenario Outline"), EXAMPLES("Examples");

        // @formatter:on

        Directive(final String val) {
            this.val = val;
        }

        private final String val;

    }

    static {
        for (final Directive d : Directive.values()) {
            directiveMap.put(d.val, d);
        }
    }

}
