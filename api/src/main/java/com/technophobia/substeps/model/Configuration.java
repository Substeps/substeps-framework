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

import com.typesafe.config.Config;
import org.substeps.runner.NewSubstepsExecutionConfig;

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
