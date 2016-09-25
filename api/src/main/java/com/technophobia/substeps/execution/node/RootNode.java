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
package com.technophobia.substeps.execution.node;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;

import java.util.List;

public class RootNode extends NodeWithChildren<FeatureNode> {

    private static final long serialVersionUID = 1L;

    private final String featureSetDescription;

    private final long timestamp;

    private final String environment;
    private final String tags;
    private final String nonFatalTags;

    public RootNode(String featureSetDescription, List<FeatureNode> features, String environment, String tags, String nonFatalTags) {
        super(features);
        this.featureSetDescription = featureSetDescription;
        this.setDepth(0);
        this.timestamp = System.currentTimeMillis();
        this.environment = environment;
        this.tags = tags;
        this.nonFatalTags = nonFatalTags;
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        List<RETURN_TYPE> toReturn = Lists.newArrayList();

        toReturn.add(executionNodeVisitor.visit(this));

        for (FeatureNode feature : getChildren()) {

            toReturn.addAll(feature.accept(executionNodeVisitor));
        }

        return toReturn;
    }

    @Override
    public String getDescription() {

        return featureSetDescription;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getTags() {
        return tags;
    }

    public String getNonFatalTags() {
        return nonFatalTags;
    }


}
