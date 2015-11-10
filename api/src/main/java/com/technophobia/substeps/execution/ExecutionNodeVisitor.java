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

import com.technophobia.substeps.execution.node.BasicScenarioNode;
import com.technophobia.substeps.execution.node.FeatureNode;
import com.technophobia.substeps.execution.node.OutlineScenarioNode;
import com.technophobia.substeps.execution.node.OutlineScenarioRowNode;
import com.technophobia.substeps.execution.node.RootNode;
import com.technophobia.substeps.execution.node.StepImplementationNode;
import com.technophobia.substeps.execution.node.SubstepNode;

public interface ExecutionNodeVisitor<RETURN_TYPE> {

    RETURN_TYPE visit(RootNode rootNode);

    RETURN_TYPE visit(FeatureNode featureNode);

    RETURN_TYPE visit(BasicScenarioNode basicScenarioNode);

    RETURN_TYPE visit(OutlineScenarioNode outlineNode);

    RETURN_TYPE visit(OutlineScenarioRowNode outlineScenarioRowNode);

    RETURN_TYPE visit(SubstepNode substepNode);

    RETURN_TYPE visit(StepImplementationNode stepImplementationNode);

}
