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
package com.technophobia.substeps.model.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;

public class SubstepsException extends RuntimeException {

    private static final long serialVersionUID = 4647698987295633906L;

    public SubstepsException() {
        super();
    }

    public SubstepsException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SubstepsException(final String message) {
        super(message);
    }

    public SubstepsException(final Throwable cause) {
        super(cause);
    }

    @Override
    public StackTraceElement[] getStackTrace() {

        StackTraceElement[] original = super.getStackTrace();
        int i = 0;
        for (; i < original.length; i++){
            if (original[i].getClassName().contains("substeps")){
                //
            }
            else {
                break;
            }
        }

        return Arrays.copyOf(original, i + 1);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        StackTraceElement[] elems = getStackTrace();
        setStackTrace(elems);
        super.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        StackTraceElement[] elems = getStackTrace();
        setStackTrace(elems);

        super.printStackTrace(s);
    }
}
