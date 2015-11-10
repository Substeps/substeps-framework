/**
 * Copyright Ian Moore 2014 
 */
package com.technophobia.substeps.execution.node;




/**
 * A wrapper around ExecutionNode that enables different equality checks.  
 * Used to build the call hierarchy for checking that all code is called..
 * @author ian
 *
 */
public class ExecutionNodeUsage {

    IExecutionNode theNode;
    
    public ExecutionNodeUsage(final IExecutionNode theNode){
        this.theNode = theNode;
    }
    
    @Override
    public String toString(){
        return "u: " + theNode.getDescription();
    }

    public String getDescription(){
        return theNode.getDescription();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((theNode.getLine() == null) ? 0 : theNode.getLine().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExecutionNodeUsage other = (ExecutionNodeUsage) obj;
        if (theNode.getLine() == null) {
            if (other.theNode.getLine() != null) {
                return false;
            }
        } else if (!theNode.getLine().equals(other.theNode.getLine())) {
            return false;
        }
        return true;
    }
    
    
}
