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
package com.technophobia.substeps.execution;

import com.technophobia.substeps.execution.node.*;

/**
 * Interface of visitor pattern for traversing the tree of ExecutionNodes, extend the AbstractExecutionNodeVisitor
 * to then just implement the interesting nodes
 *
 * @param <RETURN_TYPE> the type returned by the visitor implementation
 */
public interface ExecutionNodeVisitor<RETURN_TYPE> {


    /**
     * Visit the root node
     *
     * @param rootNode the root node
     * @return the return type
     */
    RETURN_TYPE visit(RootNode rootNode);

    /**
     * Visit a feature node
     *
     * @param featureNode the feature node
     * @return the return type
     */
    RETURN_TYPE visit(FeatureNode featureNode);

    /**
     * Visit a basic scenario node
     *
     * @param basicScenarioNode the basic scenario node
     * @return the return type
     */
    RETURN_TYPE visit(BasicScenarioNode basicScenarioNode);

    /**
     * Visit an outline scenario node.
     *
     * @param outlineNode the outline node
     * @return the return type
     */
    RETURN_TYPE visit(OutlineScenarioNode outlineNode);

    /**
     * Visit an outline scenario row node
     *
     * @param outlineScenarioRowNode the outline scenario row node
     * @return the return type
     */
    RETURN_TYPE visit(OutlineScenarioRowNode outlineScenarioRowNode);

    /**
     * Visit a substep node
     *
     * @param substepNode the substep node
     * @return the return type
     */
    RETURN_TYPE visit(SubstepNode substepNode);

    /**
     * Visit a step implementation node
     *
     * @param stepImplementationNode the step implementation node
     * @return the return type
     */
    RETURN_TYPE visit(StepImplementationNode stepImplementationNode);

}
