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
package com.technophobia.substeps.runner;

import com.technophobia.substeps.execution.node.IExecutionNode;

/**
 * @author imoore
 */
public interface IExecutionListener {

    /**
     * @param rootNode the node that was been executed
     * @param cause    the Throwable thrown while executing the node
     */
    void onNodeFailed(IExecutionNode rootNode, Throwable cause);

    /**
     * @param node the node that has is going to be executed
     */
    void onNodeStarted(IExecutionNode node);

    /**
     * @param node the node that has finished been executed
     */
    void onNodeFinished(IExecutionNode node);

    /**
     * @param node the node that has been ignored
     */
    void onNodeIgnored(IExecutionNode node);

}