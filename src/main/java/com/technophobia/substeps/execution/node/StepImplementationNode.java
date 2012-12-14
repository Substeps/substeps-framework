package com.technophobia.substeps.execution.node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.technophobia.substeps.execution.ExecutionNodeVisitor;
import com.technophobia.substeps.model.Scope;
import com.technophobia.substeps.runner.ProvidesScreenshot;
import com.technophobia.substeps.runner.SubstepExecutionFailure;


public class StepImplementationNode extends StepNode {

    private static final Logger log = LoggerFactory.getLogger(StepImplementationNode.class);
    
    private final transient Class<?> targetClass;
    private final transient Method targetMethod;
    
    //FIXME RB I'd prefer this to be final, it's like this because of the builder.
    private transient Object[] methodArgs;

    
    public StepImplementationNode(Class<?> targetClass, Method targetMethod) {

        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
    }


    public void appendMethodInfo(final StringBuilder buf) {
        appendMethodInfo(null, buf);
    }

    /**
     * @param buf
     */
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

    public void setMethodArgs(Object[] methodArgs) {
        this.methodArgs = methodArgs;
    }


    public Class<?> getTargetClass() {

        return targetClass;
    }


    public Method getTargetMethod() {
        
        return targetMethod;
    }

    @Override
    public <RETURN_TYPE> RETURN_TYPE dispatch(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return executionNodeVisitor.visit(this);
    }

    @Override
    public <RETURN_TYPE> List<RETURN_TYPE> accept(ExecutionNodeVisitor<RETURN_TYPE> executionNodeVisitor) {

        return Collections.singletonList(executionNodeVisitor.visit(this));
    }


    @Override
    public String getDescription() {

        return getLine();
    }
    
}
