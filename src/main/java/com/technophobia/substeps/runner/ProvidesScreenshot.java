package com.technophobia.substeps.runner;

/**
 * Implementing this interface indicates that a screenshot can be taken when
 * running steps in the given class.
 * 
 * @author rbarefield
 * 
 */
public interface ProvidesScreenshot {

    /**
     * 
     * @return the screenshot as a byte array
     */
    byte[] getScreenshotBytes();

}
