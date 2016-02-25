package com.technophobia.substeps.glossary;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by ian on 25/02/16.
 */
public abstract class FileBasedGlossaryPublisher implements GlossaryPublisher {

    private static final Logger log = LoggerFactory.getLogger(FileBasedGlossaryPublisher.class);

    /**
     * @parameter
     */
    private File outputFile;


    public void publish(final List<StepImplementationsDescriptor> stepimplementationDescriptors) {

        final Map<String, Collection<StepDescriptor>> sectionSorted = sortStepDescriptions(stepimplementationDescriptors);

        final String output = buildFileContents(sectionSorted);

        writeOutputFile(output, outputFile != null ? outputFile : new File(getDefaultFileName()));
    }

    public abstract String buildFileContents(final Map<String, Collection<StepDescriptor>> sectionSorted);

    public abstract String getDefaultFileName();



    private Comparator<StepDescriptor> expressionComparator = (s1, s2) -> s1.getExpression().compareTo(s2.getExpression());

    private String getSection(StepDescriptor stepTag) {
        boolean noTag = stepTag.getSection() == null || stepTag.getSection().isEmpty();
        return noTag ? "Miscellaneous" : stepTag.getSection();
    }




    private Map<String, Collection<StepDescriptor>> sortStepDescriptions(List<StepImplementationsDescriptor> stepimplementationDescriptors) {

        TreeMultimap<String, StepDescriptor> sections = TreeMultimap.create(Ordering.natural(), expressionComparator);

        for (final StepImplementationsDescriptor descriptor : stepimplementationDescriptors) {
            for (final StepDescriptor step : descriptor.getExpressions()) {
                sections.put(getSection(step), step);
            }
        }

//        final Map<String, List<StepDescriptor>> sectionSorted = new TreeMap<String, List<StepDescriptor>>();
//
//        for (final StepImplementationsDescriptor descriptor : stepimplementationDescriptors) {
//
//            for (final StepDescriptor stepTag : descriptor.getExpressions()) {
//
//                String section = stepTag.getSection();
//                if (section == null || section.isEmpty()) {
//                    section = "Miscellaneous";
//                }
//
//                List<StepDescriptor> subList = sectionSorted.get(section);
//
//                if (subList == null) {
//                    subList = new ArrayList<StepDescriptor>();
//                    sectionSorted.put(section, subList);
//                }
//                subList.add(stepTag);
//            }
//        }
        return sections.asMap();
    }


    private void writeOutputFile(String content, File outputFile) {
        outputFile.delete();

        // write out
        try {
            if (outputFile.createNewFile()) {
                Files.write(content, outputFile, Charset.defaultCharset());
            } else {

                log.error("unable to create new file: " +outputFile.getAbsolutePath() );

            }
        } catch (final IOException e) {
            log.error("IOException writing file", e);
        }
    }
}
