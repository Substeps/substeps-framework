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
package com.technophobia.substeps.glossary;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author ian
 */
public class HTMLSubstepsPublisher extends FileBasedGlossaryPublisher implements GlossaryPublisher {

    @Override
    public String getDefaultFileName() {
        return "stepimplementations.html";
    }

    /**
     * @param sectionSorted
     */
    @Override
    public String buildFileContents(final Map<String, Collection<StepDescriptor>> sectionSorted) {
        final StringBuilder buf = new StringBuilder();

        buf.append("<html><head></head><body> <table border=\"1\">\n<tr><th>Keyword</th> <th>Example</th> <th>Description</th></tr>\n");

        // buf.append(String.format(TRAC_TABLE_FORMAT, "'''Keyword'''",
        // "'''Example'''", "'''Description'''"))
        // .append("\n");

        final Set<Entry<String, Collection<StepDescriptor>>> entrySet = sectionSorted.entrySet();

        for (final Entry<String, Collection<StepDescriptor>> e : entrySet) {
            buf.append(String.format(TABLE_ROW_SECTION_FORMAT, e.getKey())).append("\n");

            buildStepTagRows(buf, e.getValue());
        }

        buf.append("</table></body></html>");
        return buf.toString();
    }


    private void buildStepTagRows(final StringBuilder buf, final Collection<StepDescriptor> infos) {


        for (final StepDescriptor info : infos) {

            System.out.println("info non escaped: " + info.getExpression() + "\n\tescaped:\n"
                    + StringEscapeUtils.escapeHtml(info.getExpression()));

            buf.append(
                    String.format(TABLE_ROW_FORMAT,
                            StringEscapeUtils.escapeHtml(info.getExpression()), info.getExample(),
                            StringEscapeUtils.escapeHtml(info.getDescription()))).append("\n");

        }
    }

    private static final String TABLE_ROW_SECTION_FORMAT = "<tr><td colspan=\"3\"><strong>%s</strong></td></tr>";

    private static final String TABLE_ROW_FORMAT = "<tr><td>%s</td><td>%s</td><td>%s</td></tr>";

}
