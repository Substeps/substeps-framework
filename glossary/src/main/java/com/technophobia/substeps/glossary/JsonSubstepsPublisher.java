package com.technophobia.substeps.glossary;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * Contributed to substeps by Andrew Lee
 * 
 * A publisher which produces a json representation of the glossary.
 * 
 * The resultant json can be used by <a
 * href="https://github.com/plasma147/api-viewer/">Api Viewer</a>
 * 
 * @author Andrew Lee
 */
public class JsonSubstepsPublisher implements GlossaryPublisher {

    /**
     * @parameter default-value = stepimplementations.json
     */
    private File outputFile;
    private Gson gson = new GsonBuilder().create();
    private Comparator<StepDescriptor> expressionComparator = new Comparator<StepDescriptor>() {

        public int compare(final StepDescriptor s1, final StepDescriptor s2) {
            return s1.getExpression().compareTo(s2.getExpression());
        }
    };

    public void publish(final List<StepImplementationsDescriptor> stepimplementationDescriptors) {
        TreeMultimap<String, StepDescriptor> sections = TreeMultimap.create(Ordering.natural(), expressionComparator);

        for (final StepImplementationsDescriptor descriptor : stepimplementationDescriptors) {
            for (final StepDescriptor step : descriptor.getExpressions()) {
                sections.put(getSection(step), step);
            }
        }

        writeToFile(gson.toJson(sections.asMap()));
    }

    private String getSection(StepDescriptor stepTag) {
        boolean noTag = stepTag.getSection() == null || stepTag.getSection().isEmpty();
        return noTag ? "Miscellaneous" : stepTag.getSection();
    }

    private void writeToFile(final String html) {
        try {
            outputFile.delete();

            if (outputFile.createNewFile()) {
                Files.write(html, outputFile, Charset.defaultCharset());
            } else {
                throw new IOException("Couldn't create file: " + outputFile.getAbsolutePath());
            }

        } catch (final IOException e) {
            throw new RuntimeException("Problem writing out file: " + outputFile.getAbsolutePath(), e);
        }
    }

}