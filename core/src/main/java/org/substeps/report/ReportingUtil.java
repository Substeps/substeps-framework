package org.substeps.report;

import com.google.common.io.Files;
import com.google.gson.*;
import com.technophobia.substeps.model.Configuration;
import com.technophobia.substeps.model.Step;
import com.technophobia.substeps.model.StepImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.substeps.runner.JSubstepsConfigKeys;

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

    private static Logger log = LoggerFactory.getLogger(ReportingUtil.class);


    public static class ClassSerializer implements JsonSerializer<Class> {
        @Override
        public JsonElement serialize(Class src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }

    public static class MethodSerializer implements JsonSerializer<Method> {
        @Override
        public JsonElement serialize(Method src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }

    public static class UncalledStepDef {

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

    public void writeUncalledStepDefs(List<Step> uncalledSubstepDefs, File outputDir) {

        final StringBuilder buf = new StringBuilder();

        List<UncalledStepDef> uncalled = new ArrayList<>();
        for (Step parent : uncalledSubstepDefs) {

            uncalled.add(new UncalledStepDef(parent));

            if (Configuration.INSTANCE.getSubstepsConfig().getBoolean(JSubstepsConfigKeys.logUncallEdAndUnusedStepImplsKey())) {
                buf.append("\t")
                        .append(parent.getLine())
                        .append(" @ ")
                        .append(parent.getSource().getName())
                        .append(":")
                        .append(parent.getSourceLineNumber())
                        .append("\n");
            }
        }

        if (Configuration.INSTANCE.getSubstepsConfig().getBoolean(JSubstepsConfigKeys.logUncallEdAndUnusedStepImplsKey()) && buf.length() > 0) {
            log.warn("** Substep definitions not called in current substep execution scope...\n\n" + buf.toString());
        }

        File out = new File(outputDir, "uncalled.stepdefs.js");

        log.info("writing uncalledStepDefs to " + out.getAbsolutePath());

        write(gson().toJson(uncalled), out);
    }


    public void writeUncalledStepImpls(List<StepImplementation> uncalledStepImplementations, File outputDir){


        if (!uncalledStepImplementations.isEmpty() && Configuration.INSTANCE.getSubstepsConfig().getBoolean(JSubstepsConfigKeys.logUncallEdAndUnusedStepImplsKey())) {

            final StringBuilder buf = new StringBuilder();
            buf.append("** Uncalled Step implementations in scope, this is suspect if these implementations are in your projects domain:\n\n");
            for (final StepImplementation s : uncalledStepImplementations) {
                buf.append(s.getMethod()).append("\n");
            }
            buf.append("\n");
            log.info(buf.toString());

        }


        File out = new File(outputDir, "uncalled.stepimpls.js");

        log.info("writing uncalledStepImplementations to " + out.getAbsolutePath());

        write(gson().toJson(uncalledStepImplementations), out);
    }


    private static void write(String json, File out){

        if (out.exists() && !out.delete()){
            log.error("failed to delete file: " + out.getAbsolutePath());
        }


        try {
            Files.asCharSink(out, Charset.forName("UTF-8")).write(json);
        } catch (IOException e) {

            log.error("IOException writing " + out.getName(), e);

        }

    }
}
