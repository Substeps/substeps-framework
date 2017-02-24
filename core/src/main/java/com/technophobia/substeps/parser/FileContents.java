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
package com.technophobia.substeps.parser;

import com.google.common.io.Files;
import com.technophobia.substeps.model.exception.SubstepsParsingException;
import com.technophobia.substeps.model.exception.SubstepsRuntimeException;
import com.technophobia.substeps.runner.FeatureFileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

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
 * Encapsulate some details about a file; a list of lines, offsets of those
 * lines, plus the original content. Shared functionality between the feature
 * file parser and substeps parser
 */
public class FileContents {

    private final List<String> lines;
    private final int[] lineStartOffsets;
    private final String fullContents ;
    private final File file;


    public FileContents(final List<String> lines, final File file ){
        this.lines = lines;
        this.file = file;

        this.lineStartOffsets = new int[this.lines.size()];

        StringBuilder buf = new StringBuilder();

        for (String l : lines){
            buf.append(l).append("\n");
        }
        this.fullContents = buf.toString();

        int lastOffset = 0;
        for (int i = 0; i < this.lines.size(); i++) {

            final String s = this.lines.get(i);

            this.lineStartOffsets[i] = this.fullContents.indexOf(s, lastOffset);
            lastOffset = this.lineStartOffsets[i] + s.length();
        }
    }


    public List<String> getLines() {
        return this.lines;
    }


    public int getFirstLineNumberStartingWith(final String text) {

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().startsWith(text)) {
                // line numbers are 1-based, lines list isn't
                return i + 1;
            }
        }
        return -1;
    }


    public int getNumberOfLines() {
        return this.lines.size();
    }


    public File getFile() {
        return this.file;
    }


    public static FileContents fromFile(final File file) {

        try {
            List<String> lines = Files.readLines(file, Charset.forName("UTF-8"));
            return new FileContents(lines, file);
        }
        catch (final IOException e) {
            throw new SubstepsRuntimeException("failed to load feature file: " + e.getMessage(), e);
        }
    }

    public int getSourceLineNumber(final String line, final int offset) {

        int lineNumber = -1;
        // find the line from the offset
        final int idx = this.fullContents.indexOf(line, offset - 1);

        if (idx != -1) {
            // what's the line number of this offset ?
            lineNumber = getSourceLineNumberForOffset(idx);
        }
        return lineNumber;
    }


    // TODO: Tidy this up - what's the correct algorithm?
    // The original version returned the 1st line number after the offset.
    // However, as we were usually hitting this method with the offset for the
    // 1st character of the line, it was usually returning the following line
    public int getSourceLineNumberForOffset(final int offset) {

        int lineNumber = 0;
        for (; lineNumber < this.lineStartOffsets.length - 1; lineNumber++) {

            if (this.lineStartOffsets[lineNumber + 1] > offset) {
                break;
            }
        }
        if (offset > lineStartOffsets[lineStartOffsets.length - 1]) {
            lineNumber = lineStartOffsets.length - 1;
        }

        // we now have line number, but it's 0-based. Need to convert to 1-based
        return lineNumber + 1;
    }


    public int getEndOfLineOffset(final int lineNumber) {

        final int normalisedLineNumber = normaliseLineNumber(lineNumber);

        int lastOffset;
        if (normalisedLineNumber + 1 < this.lineStartOffsets.length) {
            lastOffset = this.lineStartOffsets[normalisedLineNumber + 1] - 1;
        } else {
            lastOffset = this.fullContents.length();
        }
        return lastOffset;
    }


    // Line number is 1-based. When doing lookups in arrays etc, need it to be
    // 0-based.
    // This method makes this clear, rather than using undocumented +1, -1
    // operations on lineNumber
    private int normaliseLineNumber(final int lineNumber) {
        return lineNumber - 1;
    }


    public String getLineAt(final int lineNumberIdx) {

        return this.lines.get(normaliseLineNumber(lineNumberIdx));
    }


    public int getLineStartOffsetForLineIndex(final int lineNumberIdx) {

        return this.lineStartOffsets[normaliseLineNumber(lineNumberIdx)];
    }


    public int getSourceStartOffsetForLineIndex(final int lineNumberIdx) {
        final String line = getLineAt(lineNumberIdx);
        return getLineStartOffsetForLineIndex(lineNumberIdx) + firstCharacterIndex(line);
    }


    public String getFullContent() {

        return this.fullContents;
    }


    private int firstCharacterIndex(final String line) {
        int offset = 0;
        if (!line.isEmpty()) {
            while (Character.isWhitespace(line.charAt(offset))) {
                if (offset == line.length() - 1) {
                    return 0;
                }
                offset++;
            }
        }
        return offset;
    }
}
