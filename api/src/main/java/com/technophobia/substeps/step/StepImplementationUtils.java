/*
 *  Copyright Technophobia Ltd 2013
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
package com.technophobia.substeps.step;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for helper methods for various methods used in a number of
 * step implementations
 *
 * @author imoore
 */
public class StepImplementationUtils {

    /**
     * Converts a comma separated list of attributes into a map with their
     * corresponding values. Double quoted values have the quotes removed. A
     * typical use case is a step that takes a series of attributes contained
     * within angle brackets eg [COLOUR="red", name="bob", age=26]
     *
     * @param attributes the comma separated attributes
     * @return the atrribue values mapped by their key
     */
    public static Map<String, String> convertToMap(final String attributes) {
        Map<String, String> attributeMap = null;

        // split the attributes up, will be received as a comma separated list
        // of name value pairs
        final String[] nvps = attributes.split(",");

        if (nvps != null) {
            for (final String nvp : nvps) {
                final String[] split = nvp.split("=");
                if (split != null && split.length == 2) {
                    if (attributeMap == null) {
                        attributeMap = new HashMap<String, String>();
                    }
                    attributeMap.put(split[0], split[1].replaceAll("\"", ""));
                }
            }
        }

        return attributeMap;
    }

}
