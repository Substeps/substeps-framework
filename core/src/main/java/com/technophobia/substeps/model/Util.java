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
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ian
 */
public final class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    private static final boolean substituteParameters = Configuration.INSTANCE.getConfig().getBoolean("parameter.substitution.enabled");
    private static final String startDelimiter = Configuration.INSTANCE.getConfig().getString("parameter.substitution.start");
    private static final String endDelimiter = Configuration.INSTANCE.getConfig().getString("parameter.substitution.end");

    private static final boolean normalizeValues = Configuration.INSTANCE.getConfig().getBoolean("parameter.substitution.normalizeValue");
    private static final String normalizeFrom = Configuration.INSTANCE.getConfig().getString("parameter.substitution.normalize.from");
    private static final String normalizeTo = Configuration.INSTANCE.getConfig().getString("parameter.substitution.normalize.to");



    private Util() {
        // no op
    }


    public static String substituteValues(String src) {


        if (src != null && substituteParameters && src.startsWith(startDelimiter)){
            String key = StringUtils.stripStart(StringUtils.stripEnd(src, endDelimiter), startDelimiter);
            String substitute = Configuration.INSTANCE.getString(key);
            if (substitute == null){
                throw new SubstepsRuntimeException("Failed to resolve property " + src + " to be substituted ");
            }
            String normalizedValue = substitute;

            if (normalizeValues) {
                // This part will support the conversion of properties files containing accented characters
                try {
                    normalizedValue = new String(substitute.getBytes(normalizeFrom), normalizeTo);
                } catch (UnsupportedEncodingException e) {
                    log.error("error substituting accented characters", e);
                }
            }

            return normalizedValue;
        }
        return src;
    }


    // TODO - these two methods are both used - used to be one, but now it's two
    // - could they be combined ??
    public static String[] getArgs(final String patternString, final String sourceString, final String[] keywordPrecedence, Config cfg) {

        log.debug("Util getArgs String[] with pattern: " + patternString + " and sourceStr: "
                + sourceString);

        String[] rtn = null;

        ArrayList<String> argsList = null;

        String patternCopy = new String(patternString);
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
                final String arg = substituteValues(matcher.group(i));

                if (arg != null) {
                    if (argsList == null) {
                        argsList = new ArrayList<String>();
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
                                       final Class<?>[] parameterTypes, final Class<? extends Converter<?>>[] converterTypes) {

        log.debug("Util getArgs List<Object> with pattern: " + patternString + " and sourceStr: "
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
                        argsList = new ArrayList<Object>();
                    }
                    String substituted = substituteValues(arg);
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
