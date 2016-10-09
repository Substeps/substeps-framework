package org.substeps.report;

import com.google.common.io.Files;
import com.google.gson.*;
import com.technophobia.substeps.model.Step;
import com.technophobia.substeps.model.StepImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.substeps.runner.CoreSubstepsPropertiesConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ian on 26/04/16.
 */
public class ReportingUtil {

    private final File outputDir;

    public ReportingUtil(final File outputDir){
        this.outputDir = outputDir;
    }

    private static Logger log = LoggerFactory.getLogger(ReportingUtil.class);


    public static class ClassSerializer implements JsonSerializer<Class> {
        public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }

    public static class MethodSerializer implements JsonSerializer<Method> {
        public JsonElement serialize(Method src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }

    private static class UncalledStepDef {

        private String line, source;
        private int lineNumber;

        public UncalledStepDef(Step step){
            this.line = step.getLine();
            this.source = step.getSource().getName();
            this.lineNumber = step.getSourceLineNumber();
        }

        public String getLine() {
            return line;
        }

        public String getSource() {
            return source;
        }

        public int getLineNumber() {
            return lineNumber;
        }
    }


    public static Gson gson() {

        GsonBuilder gson = new GsonBuilder();

        gson.registerTypeAdapter(Class.class, new ClassSerializer());
        gson.registerTypeAdapter(Method.class, new MethodSerializer());

        return gson.create();
    }

    public void writeUncalledStepDefs(List<Step> uncalledSubstepDefs) {

        final StringBuilder buf = new StringBuilder();

        List<UncalledStepDef> uncalled = new ArrayList<>();
        for (Step parent : uncalledSubstepDefs) {

            uncalled.add(new UncalledStepDef(parent));

            if (CoreSubstepsPropertiesConfiguration.INSTANCE.isLogUncalledAndUnusedStepImpls()) {
                buf.append("\t")
                        .append(parent.getLine())
                        .append(" @ ")
                        .append(parent.getSource().getName())
                        .append(":")
                        .append(parent.getSourceLineNumber())
                        .append("\n");
            }
        }

        if (CoreSubstepsPropertiesConfiguration.INSTANCE.isLogUncalledAndUnusedStepImpls()) {

            if (buf.length() > 0) {
                log.warn("** Substep definitions not called in current substep execution scope...\n\n" + buf.toString());
            }
        }

        String json = "var uncalledStepDefs=" + gson().toJson(uncalled) ;

        File out = new File(outputDir, "uncalled.stepdefs.js");

        log.info("writing uncalledStepDefs to " + out.getAbsolutePath());

        write(json, out);
    }


    public void writeUncalledStepImpls(List<StepImplementation> uncalledStepImplementations){


        if (!uncalledStepImplementations.isEmpty()) {

            if (CoreSubstepsPropertiesConfiguration.INSTANCE.isLogUncalledAndUnusedStepImpls()) {

                final StringBuilder buf = new StringBuilder();
                buf.append("** Uncalled Step implementations in scope, this is suspect if these implementations are in your projects domain:\n\n");
                for (final StepImplementation s : uncalledStepImplementations) {
                    buf.append(s.getMethod()).append("\n");
                }
                buf.append("\n");
                log.info(buf.toString());
            }
        }


        String json = "var uncalledStepImplementations=" + gson().toJson(uncalledStepImplementations);

        File out = new File(outputDir, "uncalled.stepimpls.js");

        log.info("writing uncalledStepImplementations to " + out.getAbsolutePath());

        write(json, out);
    }


    private void write(String json, File out){

        if (out.exists()){
            out.delete();
        }

        try {
            Files.write(json, out, Charset.forName("UTF-8"));
        } catch (IOException e) {

            log.error("IOException writing " + out.getName(), e);

        }

    }
}
