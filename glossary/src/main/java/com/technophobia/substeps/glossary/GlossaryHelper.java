package com.technophobia.substeps.glossary;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ian on 22/02/16.
 */
public class GlossaryHelper {

    private static final Logger log = LoggerFactory.getLogger(GlossaryHelper.class);


    public static Map<String, List<StepDescriptor>> sortStepDescriptions(List<StepImplementationsDescriptor> stepimplementationDescriptors) {

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
        return sectionSorted;
    }


    public static void writeOutputFile(String content, File outputFile) {
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
