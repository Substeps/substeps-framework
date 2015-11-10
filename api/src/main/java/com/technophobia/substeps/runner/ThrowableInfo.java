package com.technophobia.substeps.runner;

import java.io.Serializable;

/**
 * Created by ian on 04/09/15.
 *
 * A class to contain the info from an exception, but not the actual exception itself.
 * This is to facilitate running via JMX when the client is unable to load the exception classes via RMI, ie an IntelliJ plugin.
 */
public class ThrowableInfo implements Serializable{

    private static final long serialVersionUID = 4981517213059529046L;


    private final StackTraceElement[] stackTrace;
    private final String message;
    private final String throwableClass;
    private final String description;
    private transient Throwable throwable;


    public ThrowableInfo(Throwable t){

        stackTrace = t.getStackTrace();
        message = t.getMessage();
        throwableClass = t.getClass().getName();
        this.description = t.toString();
        this.throwable = t;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public String getMessage() {
        return message;
    }

    public String getThrowableClass() {
        return throwableClass;
    }

    public Throwable getThrowable() {
        return throwable;
    }


    public String getDescription() {
        return description;
    }

}
