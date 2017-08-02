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

import com.typesafe.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.substeps.runner.NewSubstepsExecutionConfig;

import java.net.URL;

/**
 * @author ian
 */
public enum Configuration {

    INSTANCE;

    public Config getSubstepsConfig(){
        return NewSubstepsExecutionConfig.threadLocalConfig().getConfig("org.substeps.config");
    }

    public Config getConfig(){
        return NewSubstepsExecutionConfig.threadLocalConfig();
    }


    /**
     * Implementors of substep libraries should call this with default
     * properties for their library
     *
     * @param url  to a properties file containing default values
     * @param name to name of the properties file that is being added
     */
    @Deprecated
    public void addDefaultProperties(final URL url, final String name) {
        throw new IllegalArgumentException("method no longer supported, rename default substep library properties to reference.conf and they will be loaded by Typesafe config");
    }


    public String getString(final String key) {
        return getConfig().getString(key);
    }


    public int getInt(final String key) {
        return getConfig().getInt(key);
    }


    public long getLong(final String key) {
        return getConfig().getLong(key);
    }


    public boolean getBoolean(final String key) {
        return getConfig().getBoolean(key);
    }
}
