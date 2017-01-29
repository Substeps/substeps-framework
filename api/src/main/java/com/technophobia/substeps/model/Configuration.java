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
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * @author ian
 */
public enum Configuration {

    INSTANCE;

    private final Logger logger;

    private final Config config;

    Configuration() {
        logger = LoggerFactory.getLogger(Configuration.class);
        final String resourceBundleName = resourceBundleName();


        config = ConfigFactory.load(resourceBundleName);

    }

    public Config getConfig(){
        return config;
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



    public String getConfigurationInfo() {

        ConfigRenderOptions options =
                ConfigRenderOptions.defaults().setComments(false).setFormatted(true).setJson(false).setOriginComments(false);

        return config.withoutPath("java")
                .withoutPath("sun").withoutPath("awt").withoutPath("idea").withoutPath("line.separator").withoutPath("os").withoutPath("path.separator").root().render(options);
    }


    private String resourceBundleName() {

        String useProps = System.getProperty("substeps.use.dot.properties");
        String ext;
        if (useProps != null && Boolean.parseBoolean(useProps)){
            logger.info("Using legacy properties for configuration, use .conf for greater functionality");
            ext = ".properties";
        }
        else {
            ext = ".conf";
        }

        String resourceBundle = System.getProperty("environment", "localhost") + ext;

        System.out.println("loading config from resource bundle: " + resourceBundle);
        return  resourceBundle;
    }


    public String getString(final String key) {
        return config.getString(key);
    }


    public int getInt(final String key) {
        return config.getInt(key);
    }


    public long getLong(final String key) {
        return config.getLong(key);
    }


    public boolean getBoolean(final String key) {
        return config.getBoolean(key);
    }
}
