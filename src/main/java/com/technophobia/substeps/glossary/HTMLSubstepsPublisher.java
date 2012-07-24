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
package com.technophobia.substeps.glossary;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.common.io.Files;

/**
 * @author ian
 * 
 */
public class HTMLSubstepsPublisher implements GlossaryPublisher {

    /**
     * @parameter default-value = stepimplementations.html
     */
    private File outputFile;


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.technophobia.substeps.runner.GlossaryPublisher#publish(java.util.
     * List)
     */
    public void publish(final List<StepImplementationsDescriptor> stepimplementationDescriptors) {
        final Map<String, List<StepDescriptor>> sectionSorted = new TreeMap<String, List<StepDescriptor>>();

        for (final StepImplementationsDescriptor descriptor : stepimplementationDescriptors) {

            for (final StepDescriptor stepTag : descriptor.getExpressions()) {

                String section = stepTag.getSection();
                if (section == null || section.isEmpty()) {
                    section = "Miscellaneous";
                }

                List<StepDescriptor> subList = sectionSorted.get(section);

                if (subList == null) {
                    subList = new ArrayList<StepDescriptor>();
                    sectionSorted.put(section, subList);
                }
                subList.add(stepTag);
            }
        }

        final String html = buildHtml(sectionSorted);

        outputFile.delete();

        // write out
        try {
            if (outputFile.createNewFile()) {
                Files.write(html, outputFile, Charset.defaultCharset());
            } else {
                // TODO
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * @param sectionSorted
     */
    private String buildHtml(final Map<String, List<StepDescriptor>> sectionSorted) {
        final StringBuilder buf = new StringBuilder();

        buf.append("<html><head></head><body> <table border=\"1\">\n<tr><th>Keyword</th> <th>Example</th> <th>Description</th></tr>\n");

        // buf.append(String.format(TRAC_TABLE_FORMAT, "'''Keyword'''",
        // "'''Example'''", "'''Description'''"))
        // .append("\n");

        final Set<Entry<String, List<StepDescriptor>>> entrySet = sectionSorted.entrySet();

        for (final Entry<String, List<StepDescriptor>> e : entrySet) {
            buf.append(String.format(TABLE_ROW_SECTION_FORMAT, e.getKey())).append("\n");

            buildStepTagRows(buf, e.getValue());
        }

        buf.append("</table></body></html>");
        return buf.toString();
    }


    private void buildStepTagRows(final StringBuilder buf, final List<StepDescriptor> infos) {

        Collections.sort(infos, new Comparator<StepDescriptor>() {
            public int compare(final StepDescriptor s1, final StepDescriptor s2) {
                return s1.getExpression().compareTo(s2.getExpression());
            }
        });

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
