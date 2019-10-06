package com.technophobia.substeps.glossary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collection;
import java.util.Map;

/**
 * Contributed to substeps by Andrew Lee
 * <br>
 * A publisher which produces a json representation of the glossary.
 * <br>
 * The resultant json can be used by <a
 * href="https://github.com/plasma147/api-viewer/">Api Viewer</a>
 *
 * @author Andrew Lee
 */
public class JsonSubstepsPublisher extends FileBasedGlossaryPublisher implements GlossaryPublisher {


    @Override
    public String getDefaultFileName() {
        return "stepimplementations.json";
    }

    @Override
    public String buildFileContents(final Map<String, Collection<StepDescriptor>> sectionSorted) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(sectionSorted);
    }

}