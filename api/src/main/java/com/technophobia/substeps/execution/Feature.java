/*
 *	Copyright Technophobia Ltd 2012
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
package com.technophobia.substeps.execution;

import java.io.Serializable;

public class Feature implements Serializable {

    private static final long serialVersionUID = 8793134696276224499L;

    private final String name;
    private final String filename;


    /**
     * @param name     feature name
     * @param filename feature file name
     */
    public Feature(final String name, final String filename) {
        this.name = name;
        this.filename = filename;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

}