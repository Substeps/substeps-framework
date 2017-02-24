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
package com.technophobia.substeps.glossary;

import java.util.Arrays;

/**
 * @author ian
 */
public class StepDescriptor {
    private String expression;
    private String regex;
    private String example;
    private String section;
    private String description;
    private String[] parameterNames;
    private String[] parameterClassNames;


    /**
     * @return the example
     */
    public String getExample() {
        return example;
    }


    /**
     * @param example the example to set
     */
    public void setExample(final String example) {
        this.example = example;
    }


    /**
     * @return the section
     */
    public String getSection() {
        return section;
    }


    /**
     * @param section the section to set
     */
    public void setSection(final String section) {
        this.section = section;
    }


    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }


    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }


    /**
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }


    /**
     * @param expression the expression to set
     */
    public void setExpression(final String expression) {
        this.expression = expression;
    }


    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String[] getParameterClassNames() {
        return parameterClassNames;
    }

    public void setParameterClassNames(String[] parameterClassNames) {
        this.parameterClassNames = parameterClassNames;
    }
    public String[] getParameterNames() {
        return parameterNames;
    }

    public void setParameterNames(String[] parameterNames) {
        this.parameterNames = parameterNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepDescriptor that = (StepDescriptor) o;

        if (expression != null ? !expression.equals(that.expression) : that.expression != null) return false;
        if (regex != null ? !regex.equals(that.regex) : that.regex != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(parameterNames, that.parameterNames)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(parameterClassNames, that.parameterClassNames);
    }

    @Override
    public int hashCode() {
        int result = expression != null ? expression.hashCode() : 0;
        result = 31 * result + (regex != null ? regex.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(parameterNames);
        result = 31 * result + Arrays.hashCode(parameterClassNames);
        return result;
    }
}
