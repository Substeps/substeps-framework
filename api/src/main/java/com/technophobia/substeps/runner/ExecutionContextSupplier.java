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

import com.technophobia.substeps.model.Scope;

/**
 * Refactored out of the webdriver-substeps project Provides a wrapper around
 * the ExecutionContext allowing a supplier to be created with a given scope and
 * allowing the T being supplied to be replaced with another T, typically in
 * setup or tear down scenarios and a T that holds state across test scenarios
 * such as a database connection, results etc
 * 
 * Typical usage:
 * 
 * <pre>
 * private static final MutableSupplier&lt;MyContext&gt; myContextSupplier = new ExecutionContextSupplier&lt;MyContext&gt;(
 *             Scope.SCENARIO, "my_context_key");
 *             
 *    // in test setup
 *    myContextSupplier.set(new MyContext());
 *    
 *    // to use the context
 *    myContextSupplier.get()
 * </pre>
 */
public class ExecutionContextSupplier<T> implements MutableSupplier<T> {

    private final String key;
    private final Scope scope;


    /**
     * Construct the supplier with the given scope and key
     * 
     * @param scope
     *            the scope in which instances of &lt;T&gt; will be cached
     * @param key
     *            the key under which the &lt;T&gt; will be cached
     */
    public ExecutionContextSupplier(final Scope scope, final String key) {
        this.scope = scope;
        this.key = key;
    }


    /**
     * retrieve the cached instance of the &lt;T&gt;
     */
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) ExecutionContext.get(this.scope, this.key);
    }


    /**
     * sets the &lt;T&gt; in the desired scope
     */
    public void set(final T t) {
        ExecutionContext.put(this.scope, this.key, t);
    }

}
