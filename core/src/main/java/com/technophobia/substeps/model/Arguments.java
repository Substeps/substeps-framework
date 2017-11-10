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
package com.technophobia.substeps.model;

import com.technophobia.substeps.model.exception.SubstepsRuntimeException;
import com.technophobia.substeps.model.parameter.Converter;
import com.technophobia.substeps.model.parameter.ConverterFactory;
import com.technophobia.substeps.runner.ExecutionContext;
import com.typesafe.config.Config;
import org.apache.commons.jexl3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.substeps.runner.JSubstepsConfigKeys;
import org.substeps.runner.NewSubstepsExecutionConfig;
import org.substeps.runner.ParameterSubstitution;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ian
 */
public final class Arguments {
    private static final Logger log = LoggerFactory.getLogger(Arguments.class);

    String x = JSubstepsConfigKeys.substepsReportDir();

    private static final JexlEngine jexl = new JexlBuilder().cache(512).strict(false).silent(false).create();


    private Arguments() {
        // no op
    }

    public static Object evaluateExpression(String expressionWithDelimiters){

        ParameterSubstitution parameterSubstituionConfig = NewSubstepsExecutionConfig.getParameterSubstituionConfig();

        // TODO - check that the expression doesn't contain any of the bad words
        // or and eq ne lt gt le ge div mod not null true false new var return
        // any of those words need to be qutoed or ['  ']
        // http://commons.apache.org/proper/commons-jexl/reference/syntax.html

        // try evaluating this expression against the executionContext

        // TODO check flag to see whether we can evaluate things from the ec

        if (expressionWithDelimiters != null && parameterSubstituionConfig.substituteParameters()
                && expressionWithDelimiters.startsWith(parameterSubstituionConfig.startDelimiter())) {
            String expression = StringUtils.stripStart(StringUtils.stripEnd(expressionWithDelimiters, parameterSubstituionConfig.endDelimiter()),
                    parameterSubstituionConfig.startDelimiter());

            JexlContext context = new MapContext(ExecutionContext.flatten());

            JexlExpression e = jexl.createExpression(expression);

            return e.evaluate(context);
        }
        else {
            return expressionWithDelimiters;
        }
    }

    public static String substituteValues(String src, Config cfg) {

        ParameterSubstitution parameterSubstituionConfig = NewSubstepsExecutionConfig.getParameterSubstituionConfig();

        if (src != null && parameterSubstituionConfig.substituteParameters() &&
                src.startsWith(parameterSubstituionConfig.startDelimiter())){
            String key = StringUtils.stripStart(StringUtils.stripEnd(src, parameterSubstituionConfig.endDelimiter()),
                    parameterSubstituionConfig.startDelimiter());

            String normalizedValue = src;

            if (cfg.hasPath(key)){
                String substitute = cfg.getString(key);

                if (substitute == null){
                    throw new SubstepsRuntimeException("Failed to resolve property " + src + " to be substituted ");
                }
                normalizedValue = substitute;
                if (parameterSubstituionConfig.normalizeValues()) {
                    // This part will support the conversion of properties files containing accented characters
                    try {
                        normalizedValue = new String(substitute.getBytes(parameterSubstituionConfig.normalizeFrom()), parameterSubstituionConfig.normalizeTo());
                    } catch (UnsupportedEncodingException e) {
                        log.error("error substituting accented characters", e);
                    }
                }
            }



            return normalizedValue;
        }
        return src;
    }


    // TODO - these two methods are both used - used to be one, but now it's two
    // - could they be combined ??
    public static String[] getArgs(final String patternString, final String sourceString, final String[] keywordPrecedence, Config cfg) {

        log.debug("Arguments getArgs String[] with pattern: " + patternString + " and sourceStr: "
                + sourceString);

        String[] rtn = null;

        ArrayList<String> argsList = null;

        String patternCopy = patternString;
        if (keywordPrecedence != null && StringUtils.startsWithAny(patternString, keywordPrecedence)) {
            //
            for (String s : keywordPrecedence) {


                patternCopy = StringUtils.removeStart(patternCopy, s);
            }

            patternCopy = "(?:" + StringUtils.join(keywordPrecedence, "|") + ")" + patternCopy;
        }


        final Pattern pattern = Pattern.compile(patternCopy);
        final Matcher matcher = pattern.matcher(sourceString);

        final int groupCount = matcher.groupCount();

        // TODO - this doesn't work if we're not doing strict matching
        if (matcher.find()) {

            for (int i = 1; i <= groupCount; i++) {
                final String arg = substituteValues(matcher.group(i), cfg);

                if (arg != null) {
                    if (argsList == null) {
                        argsList = new ArrayList<>();
                    }
                    argsList.add(arg);
                }
            }
        }

        if (argsList != null) {
            rtn = argsList.toArray(new String[argsList.size()]);

            if (log.isDebugEnabled()) {

                final StringBuilder buf = new StringBuilder();
                buf.append("returning args: ");

                for (final String s : argsList) {

                    buf.append("[").append(s).append("] ");
                }

                log.debug(buf.toString());
            }

        }

        return rtn;
    }


    public static List<Object> getArgs(final String patternString, final String sourceString,
                                       final Class<?>[] parameterTypes, final Class<? extends Converter<?>>[] converterTypes, Config cfg) {

        log.debug("Arguments getArgs List<Object> with pattern: " + patternString + " and sourceStr: "
                + sourceString);

        List<Object> argsList = null;

        final Pattern pattern = Pattern.compile(patternString);
        final Matcher matcher = pattern.matcher(sourceString);

        final int groupCount = matcher.groupCount();

        int argIdx = 0;

        if (matcher.find()) {

            for (int i = 1; i <= groupCount; i++) {
                final String arg = matcher.group(i);

                if (arg != null) {
                    if (argsList == null) {
                        argsList = new ArrayList<>();
                    }
                    String substituted = substituteValues(arg, cfg);

                        argsList.add(getObjectArg(substituted, parameterTypes[argIdx], converterTypes[argIdx]));
                }
                argIdx++;
            }
        }

        return argsList;

    }


    private static Object getObjectArg(final String stringArgument, final Class<?> desiredType,
                                       final Class<? extends Converter<?>> converter) {
        return ConverterFactory.convert(stringArgument, desiredType, converter);
    }

}
