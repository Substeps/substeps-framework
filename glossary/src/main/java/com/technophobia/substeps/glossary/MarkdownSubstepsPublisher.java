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

import com.google.common.io.Files;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author ian
 * 
 */
public class MarkdownSubstepsPublisher implements GlossaryPublisher {

    /**
     * @parameter default-value = stepimplementations.md
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

        final String md = buildMarkdown(sectionSorted);

        outputFile.delete();

        // write out
        try {
            if (outputFile.createNewFile()) {
                Files.write(md, outputFile, Charset.defaultCharset());
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
    private String buildMarkdown(final Map<String, List<StepDescriptor>> sectionSorted) {
        final StringBuilder buf = new StringBuilder();

//        buf.append("<html><head></head><body> <table border=\"1\">\n<tr><th>Keyword</th> <th>Example</th> <th>Description</th></tr>\n");

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
                            StringEscapeUtils.escapeHtml(info.getExpression()), info.getExample().replaceAll("\n", " "),
                            StringEscapeUtils.escapeHtml(info.getDescription()).replaceAll("\n", " "))).append("\n");

        }
    }

    private static final String TABLE_ROW_SECTION_FORMAT = "%s\n" +
            "==========\n" +
            "| **Keyword**  | **Example**  | **Description** |\n" +
            "| :------------ |:---------------| :-----|";

    private static final String TABLE_ROW_FORMAT = "| %s | %s | %s |";

}
