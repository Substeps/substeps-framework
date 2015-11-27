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
package com.technophobia.substeps.execution.node;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.technophobia.substeps.execution.ExecutionNodeVisitor;

public class StepImplementationNode extends ExecutionNode implements StepNode {

    private static final long serialVersionUID = 1L;

    private final transient Class<?> targetClass;
    private final transient Method targetMethod;

    private final Set<String> tags;

    // FIXME: RB I'd prefer this to be final, it's like this because of the builder.
    private transient Object[] methodArgs;


    public StepImplementationNode(final Class<?> targetClass, final Method targetMethod, final Set<String> tags,
        final int depth) {
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.setDepth(depth);
        this.tags = tags;
    }


    public void appendMethodInfo(final StringBuilder buf) {
        appendMethodInfo(null, buf);
    }


    public void appendMethodInfo(final String prefix, final StringBuilder buf) {
        if (this.targetClass != null && this.targetMethod != null) {

            if (prefix != null) {
                buf.append(prefix);
            }

            buf.append(targetClass.getSimpleName()).append(".").append(targetMethod.getName()).append("(");

            if (this.getMethodArgs() != null) {
                boolean commaRequired = false;
                for (final Object arg : this.getMethodArgs()) {
                    if (commaRequired) {
                        buf.append(", ");
                    }

                    boolean quotes = false;
                    if (arg instanceof String) {
                        quotes = true;
                        buf.append("\"");
                    }
                    buf.append(arg.toString());
                    if (quotes) {
                        buf.append("\"");
                    }
                    commaRequired = true;
                }
            }

            buf.append(")").append("\n");
        }
    }


    public Object[] getMethodArgs() {
        return methodArgs;
    }


    public void setMethodArgs(final Object[] methodArgs) {
        this.methodArgs = methodArgs;
    }


    public Class<?> getTargetClass() {

        return targetClass;
    }


    public Method getTargetMethod() {

        return targetMethod;
    }


    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(final ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }


    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(final ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return Collections.singletonList(executionNodeVisitor.visit(this));
    }


    @Override
    public String getDescription() {

        return getLine();
    }


    public Set<String> getTags() {
        return tags;
    }


    @Override
    public String toDebugString() {
        return super.toDebugString() + " impl: "
            + this.targetMethod.getDeclaringClass().getSimpleName() + "." + this.targetMethod.getName();
    }
}
