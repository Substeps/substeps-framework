package com.technophobia.substeps.glossary;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

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
public class JsonSubstepsPublisher extends FileBasedGlossaryPublisher implements GlossaryPublisher {


    @Override
    public String getDefaultFileName(){
        return "stepimplementations.json";
    }
//    /**
//     * @parameter default-value =
//     */
//    private File outputFile;
//    public void publish(final List<StepImplementationsDescriptor> stepimplementationDescriptors) {
//        TreeMultimap<String, StepDescriptor> sections = TreeMultimap.create(Ordering.natural(), expressionComparator);
//
//        for (final StepImplementationsDescriptor descriptor : stepimplementationDescriptors) {
//            for (final StepDescriptor step : descriptor.getExpressions()) {
//                sections.put(getSection(step), step);
//            }
//        }
//
//        NavigableMap<String, Collection<StepDescriptor>> stringCollectionNavigableMap = sections.asMap();
//
//        writeToFile(gson.toJson(sections.asMap()));
//    }

//    private String getSection(StepDescriptor stepTag) {
//        boolean noTag = stepTag.getSection() == null || stepTag.getSection().isEmpty();
//        return noTag ? "Miscellaneous" : stepTag.getSection();
//    }

    @Override
    public String buildFileContents(final Map<String, Collection<StepDescriptor>> sectionSorted){
        Gson gson = new GsonBuilder().create();
        return gson.toJson(sectionSorted);
    }

//    private void writeToFile(final String html) {
//        try {
//            outputFile.delete();
//
//            if (outputFile.createNewFile()) {
//                Files.write(html, outputFile, Charset.defaultCharset());
//            } else {
//                throw new IOException("Couldn't create file: " + outputFile.getAbsolutePath());
//            }
//
//        } catch (final IOException e) {
//            throw new RuntimeException("Problem writing out file: " + outputFile.getAbsolutePath(), e);
//        }
//    }

}