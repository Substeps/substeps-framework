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
package com.technophobia.substeps.glossary;

import java.util.ArrayList;
import java.util.List;

import com.technophobia.substeps.model.StepImplementation;
import com.technophobia.substeps.model.Syntax;
import com.technophobia.substeps.runner.syntax.SyntaxBuilder;


/**
 * TODO
 * 
 * @author imoore
 * 
 */
public class TracWikiMarkupGenerator
{

	public static void main(final String[] args)
	{

	}

	public Syntax loadSyntax()
	{
		// load up the substeps, parse and generate some trac wiki markeup

		final List<Class<?>> stepImpls = new ArrayList<Class<?>>();
//		stepImpls.add(BaseWebdriverSubStepImplementations.class);

		return SyntaxBuilder.buildSyntax( stepImpls, null);

	}

	public String generatetracWikiMarkup(final Syntax syntax)
	{
		final List<StepImplementation> stepImplementations = syntax.getStepImplementations();

		for (final StepImplementation si : stepImplementations)
		{

		}

		return "";
	}

	private String toTracWikiMarkup(final StepImplementation si)
	{
		return "";
	}

}
