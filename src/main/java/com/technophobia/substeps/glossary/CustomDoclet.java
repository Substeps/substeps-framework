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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.technophobia.substeps.model.SubSteps.Step;

/**
 * TODO
 * 
 * @author imoore
 * 
 */
public class CustomDoclet extends Doclet
{
	private static final String TRAC_TABLE_FORMAT = "|| %s || %s || %s ||";
	private static final String TRAC_TABLE_INFO_FORMAT = "|| {{{ %s }}} ||{{{ %s }}} ||{{{ %s }}} ||";

//	public static File outputDirectory;
	private static List<ClassStepTags> classStepTagsList;
	
	public static void setExpressionList(final List<ClassStepTags> expressionList){
		classStepTagsList = expressionList;
	}
	
	public static List<ClassStepTags> getExpressions(){
		return classStepTagsList;
	}
	
	public static class SubStepInfo
	{
		public String expression = "";
		public String example = "";
		public String description = "";
		public String section = null;
		
		
		public String toTracString()
		{
			return String.format(TRAC_TABLE_INFO_FORMAT, expression, example, description);
		}

		public String toDebugString(final String delimitter)
		{
			final StringBuilder buf = new StringBuilder();

			buf.append(expression).append(delimitter);

			if (example != null)
			{
				buf.append(example);
			}
			buf.append(delimitter);
			if (description != null)
			{
				buf.append(description);
			}
			buf.append(delimitter);

			return buf.toString();

		}
	}

	public static boolean start(final RootDoc root)
	{
		// final Throwable t = new Throwable();
		// t.fillInStackTrace();
		//
		// t.printStackTrace();

//		System.out.println("\n\n\n\n ************************ WAHOO **********************\n\n\n");

		final ClassDoc[] classes = root.classes();


//		System.out.println("cp: " + System.getProperty("java.class.path"));
		
		
		for (final ClassDoc cd : classes)
		{
//			System.out.println("typeName: " + cd.typeName() + " name: " + cd.qualifiedTypeName()); // BaseWebdriverSubStepImplementations

			final ClassStepTags classStepTags = new ClassStepTags(cd.qualifiedName());
			
			classStepTagsList.add(classStepTags);
			
			Class<?> implClass = null;
			Method[] implMethods = null;
			try
			{

//				final Class predicate = root.getClass().getClassLoader().loadClass("com.google.common.base.Predicate");
				implClass = root.getClass().getClassLoader().loadClass(cd.qualifiedTypeName());
				implMethods = implClass.getMethods();
				
//				ClassDoc classDoc = root.classNamed(cd.qualifiedTypeName());
			}
			catch (final ClassNotFoundException e)
			{
				e.printStackTrace();
			}

			final MethodDoc[] methods = cd.methods();

			for (final MethodDoc md : methods)
			{
				 System.out.println("\t method " + md.name());

				final Method underlyingMethod = getMethod(implMethods, md);
				
				if (underlyingMethod != null)
				{
					final Step annotation = underlyingMethod.getAnnotation(Step.class);
	
					if (annotation != null)
					
	//				final AnnotationDesc[] annotations = md.annotations();
	//				for (final AnnotationDesc ad : annotations)
					{
	//					 System.out.println("\t\t" + ad.toString());
						 
	
	//					final AnnotationTypeDoc annotationType = ad.annotationType();
	//					ClassDocImpl cdi = (ClassDocImpl)ad.annotationType();
						
	//					System.out.println("cdi type val " + cdi.type.stringValue());
	//					
	//					final AnnotationTypeDoc annotationType = cdi.asAnnotationTypeDoc();
	//					if (annotationType.typeName().equals(Step.class.getName()))
	//					{
							// we're interested
							final StepTags expression = new StepTags();
	
							classStepTags.addStepTags(expression);
	
							expression.setDescription(md.commentText().replaceAll("\n", " "));

//							System.out.println("\t" + md.name() + "\t\t" + md.commentText() + "\n\t\t\traw: "
//									+ md.getRawCommentText());
	
							expression.setExample(getSingleJavadocTagValue(md, "example"));
							expression.setSection(getSingleJavadocTagValue(md, "section"));
//							expression.example = getSingleJavadocTagValue(md, "example");
//							expression.section = getSingleJavadocTagValue(md, "section");
	
	
	
							String line = annotation.value();
	
//							System.out.println("original line: " + line);
							// incremental replace
	
							final Parameter[] parameters = md.parameters();
							if (parameters != null && parameters.length > 0)
							{
								for (final Parameter p : parameters)
								{
									// replace any captures with <variable name>
	
									line = line.replaceFirst("\\([^\\)]*\\)", "<" + p.name() + ">");
	
									p.typeName();
								}
							}
							line = line.replaceAll("\\?", "");
							line = line.replaceAll("\\\\", "");
							expression.setExpression(line);
//							expression.expression = line;
	
							System.out.println("tweaked line: " + line);
	
	//					}
					}
				}
			}
		}

//		System.out.println("\n\n\n\n\n\n\n");

		// serialize out the xml
		
		
		System.out.println("done");
		
//		System.out.println(buildSubStepInfoTable(expressions));

		
		
		return true;
	}

	/**
	 * @param expressions
	 */
	private static String buildSubStepInfoTable(final List<SubStepInfo> expressions)
	{
		// sort into the various headings

		final Map<String, List<SubStepInfo>> sectionSorted = new TreeMap<String, List<SubStepInfo>>();
		for (final SubStepInfo info : expressions)
		{
			if (info.section == null)
			{
				info.section = "Miscellaneous";
			}
			List<SubStepInfo> subList = sectionSorted.get(info.section);

			if (subList == null)
			{
				subList = new ArrayList<SubStepInfo>();
				sectionSorted.put(info.section, subList);
			}
			subList.add(info);
		}

		// go through the headings

		final StringBuilder buf = new StringBuilder();

		buf.append(String.format(TRAC_TABLE_FORMAT, "'''Keyword'''", "'''Example'''", "'''Description'''"))
				.append("\n");

		final Set<Entry<String, List<SubStepInfo>>> entrySet = sectionSorted.entrySet();

		for (final Entry<String, List<SubStepInfo>> e : entrySet)
		{
			buf.append(String.format(TRAC_TABLE_FORMAT, "'''" + e.getKey() + "'''", "", "")).append("\n");

			buf.append(tracPrintSubStepInfoList(e.getValue()));
		}
		return buf.toString();
	}

	private static String tracPrintSubStepInfoList(final List<SubStepInfo> infos)
	{
		final StringBuilder buf = new StringBuilder();

		Collections.sort(infos, new Comparator<SubStepInfo>()
		{

			public int compare(final SubStepInfo s1, final SubStepInfo s2)
			{
				return s1.expression.compareTo(s2.expression);
			}
		});

		for (final SubStepInfo info : infos)
		{
			buf.append(info.toTracString()).append("\n");
		}
		return buf.toString();
	}

	/**
	 * @param md
	 * @param expression
	 */
	private static String getSingleJavadocTagValue(final MethodDoc md, final String tagName)
	{
		String rtn = null;
		final Tag[] tags = md.tags(tagName);
		if (tags != null && tags.length > 0)
		{
			rtn = tags[0].text().replace("@" + tagName, "");
			rtn.replaceAll("\n", " ");
		}

		return rtn != null ? rtn : "";
	}

	/**
	 * @param implClass
	 * @param implMethods
	 * @param md
	 * @return
	 */
	private static Method getMethod(final Method[] implMethods, final MethodDoc md)
	{
		Method rtn = null;
		
		int desiredNumberOfParams = 0;

		final Parameter[] parameters = md.parameters();
		if (parameters != null)
		{
			desiredNumberOfParams = parameters.length;
		}

		final List<Method> candidateMethods = new ArrayList<Method>();

		// try and match by name
		for (final Method m : implMethods)
		{
			if (m.getName().equals(md.name()))
			{
				if (m.getParameterTypes().length == desiredNumberOfParams)
				{
					candidateMethods.add(m);
				}
			}
		}

		if (candidateMethods.size() > 1)
		{
			throw new IllegalStateException("need to impl parameter type matching");
		}

		if (!candidateMethods.isEmpty())
		{
			rtn = candidateMethods.get(0);
//			throw new IllegalStateException("failed to match a method");
		}

		return rtn;
	}

}
