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
package com.technophobia.substeps.runner.builder;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.node.FeatureNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.model.FeatureFile;
import com.technophobia.substeps.runner.ExecutionConfigWrapper;
import com.technophobia.substeps.runner.TestParameters;
import com.typesafe.config.Config;
import org.substeps.runner.NewSubstepsExecutionConfig;

import java.util.List;

/**
 * @author ian
 */
public class ExecutionNodeTreeBuilder {

    private final TestParameters parameters;
    private final FeatureNodeBuilder featureNodeBuilder;
//    private final ExecutionConfigWrapper configWrapper;
    private final Config config;

//    public ExecutionNodeTreeBuilder(final TestParameters parameters, ExecutionConfigWrapper configWrapper) {
//        this.parameters = parameters;
//        this.featureNodeBuilder = new FeatureNodeBuilder(parameters);
////        this.configWrapper = configWrapper;
//        this.config = null;
//    }

    public ExecutionNodeTreeBuilder(final TestParameters parameters, Config config) {
        this.parameters = parameters;
        this.featureNodeBuilder = new FeatureNodeBuilder(parameters);
//        this.configWrapper = null;
        this.config = config;
    }


    public RootNode buildExecutionNodeTree(String description) {

        List<FeatureNode> features = Lists.newArrayListWithExpectedSize(parameters.getFeatureFileList().size());

        for (final FeatureFile featureFile : parameters.getFeatureFileList()) {

            FeatureNode featureNode = featureNodeBuilder.build(featureFile);
            if (featureNode != null) {

                features.add(featureNode);
            }
        }
        String env = System.getProperty("environment", "localhost");


//        return new RootNode(description, features, env, configWrapper.getExecutionConfig().getTags(), configWrapper.getExecutionConfig().getNonFatalTags());
        return new RootNode(description, features, env, NewSubstepsExecutionConfig.getTags(config), NewSubstepsExecutionConfig.getNonFatalTags(config));

    }

}
