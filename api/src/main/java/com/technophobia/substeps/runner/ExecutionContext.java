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
package com.technophobia.substeps.runner;

import com.google.common.collect.Sets;
import com.technophobia.substeps.model.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class holding state a various scopes for the duration of execution,
 * state will be cleared at appropriate points through execution
 *
 * @author imoore
 */
public final class ExecutionContext {

    private static final Logger log = LoggerFactory.getLogger(ExecutionContext.class);

    private static final ThreadLocal<ExecutionContext> executionContextThreadLocal = new ThreadLocal<ExecutionContext>() {
        @Override
        protected ExecutionContext initialValue() {
            return new ExecutionContext();
        }

        ;
    };

    private Map<Scope, Map<String, Object>> scopedData = null;

    private ExecutionContext() {
        scopedData = new EnumMap<>(Scope.class);
    }


    public static void put(final Scope scope, final String key, final Object value) {
        executionContextThreadLocal.get().putInternal(scope, key, value);
    }

    public static Object get(final Scope scope, final String key) {
        return executionContextThreadLocal.get().getInternal(scope, key);
    }

    private void putInternal(final Scope scope, final String key, final Object value) {
        // NB. this is not currently synchronised

        Map<String, Object> map = scopedData.get(scope);
        if (map == null) {
            map = new HashMap<String, Object>();
            scopedData.put(scope, map);
        }
        map.put(key, value);
    }

    private Object getInternal(final Scope scope, final String key) {
        Object rtn = null;
        final Map<String, Object> map = scopedData.get(scope);
        if (map != null) {
            rtn = map.get(key);
        }
        return rtn;
    }
    public static void clear(final Scope scope, String key) {
        ExecutionContext ec = executionContextThreadLocal.get();
        Map<String, Object> map = ec.scopedData.get(scope);
        if (map != null) {
            map.remove(key);
        }

    }

    public static void clear(final Scope scope) {
        executionContextThreadLocal.get().scopedData.remove(scope);
    }

    public static Map<String, Object> flatten(){

        // return a single Map with the items in the narrowest scope remaining

        ExecutionContext ec = executionContextThreadLocal.get();

        Map newMaster = new HashMap<String, Object>();
        for (Scope scope : Scope.values()){

            Map<String, Object> scopedMap = ec.scopedData.get(scope);
            if (scopedMap != null) {
                Sets.SetView intersection = Sets.intersection(newMaster.keySet(), scopedMap.keySet());

                if (!intersection.isEmpty()) {
                    intersection.stream().forEach(s -> log.warn("existing key value " + s + " is being overwritten flattening the ExecutionContext"));
                }

                newMaster.putAll(scopedMap);
            }
        }
        return newMaster;
    }
}
