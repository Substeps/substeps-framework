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
package com.technophobia.substeps.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import com.technophobia.substeps.model.FeatureFile;
import com.technophobia.substeps.model.Scenario;
import com.technophobia.substeps.model.Step;
import com.technophobia.substeps.runner.FeatureFileComparator;
import com.technophobia.substeps.runner.FeatureFileParser;
import com.technophobia.substeps.runner.syntax.FileUtils;


/**
 * @author ian
 *
 */
public class SubstepsToHTML
{
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// TODO
		// args featureFile=xxxxx substeps=xxxxx
		
		final String featureFile = "/home/ian/TPWork/CapitaBSA_Portal/fp17-contracts-acceptance-tests/src/test/resources/new_bdd_features/TM29/bugfixes.feature";
		final String substepsFile = "";
		
		final SubstepsToHTML converter = new SubstepsToHTML();
		
		final String html = 
		converter.toHTML(featureFile);
		
		System.out.println(html);
	}

	
	
	/**
	 * @param featureFile
	 * @return
	 */
	private String toHTML(final String featureFile)
	{
		final List<FeatureFile> features = loadFeatures(featureFile);
		
		final StringBuilder buf = new StringBuilder();
		
		if (features != null){
			
			for (final FeatureFile ff : features){
				
				buf.append("File: " + ff.getSourceFile().getName())
				.append("\n\n");
				toHTML(ff, buf);
				
			}
			
		}
		return buf.toString();
	}



	/**
	 * @param ff
	 * @param buf 
	 * @return
	 */
	private String toHTML(final FeatureFile ff, final StringBuilder buf)
	{
		appendTags(ff.getTags(), buf);

		appendKeyword("Red", "Feature", ff.getName(), buf);
		
		if (ff.getDescription() != null){
			buf.append(ff.getDescription());
		}
		
		for (final Scenario scenario : ff.getScenarios()){
			
			toHTML(scenario, buf);
		}
		
		return ff.getRawText();
	}

	


	/**
	 * @param scenario
	 * @param buf
	 */
	private void toHTML(final Scenario scenario, final StringBuilder buf)
	{
		appendTags(scenario.getTags(), buf);
		
		
		
		if (scenario.isOutline()){

			appendKeyword("Blue", "Scenario Outline", scenario.getDescription(), buf);
		}
		else {
			appendKeyword("Blue", "Scenario", scenario.getDescription(), buf);
		}
				
		buf.append("<div style=\"position:relative;left:5em\">");
		for (final Step step : scenario.getSteps()){
			
			buf.append(StringEscapeUtils.escapeHtml(step.getLine()))
			.append("<br/>")
			.append("\n");
		}
		buf.append("</div>");
		buf.append("<br/><br/>\n");
		// TODO
		//scenario.getExampleParameters();
	}
	
	private void appendTags(final Set<String> tags, final StringBuilder buf){
		
		if (tags != null){
			
			appendKeyword("Green", "Tags", null, buf);
			
			for (final String tag : tags){
				buf.append(tag)
				.append("<br/>\n");
			}
			
			buf.append("<br/>\n");
		}
	}	

	
	private void appendKeyword(final String colour, final String keyword, final String description, final StringBuilder buf){
		buf.append("<span style=\"color:")
		.append(colour)
		.append(";\">")
		.append(keyword)
		.append(":</span> ");
		
		if (description != null){
			buf.append(description);
//			.append("<br/>");			
		}
		buf.append("<br/>\n");
	}
	

	private List<FeatureFile> loadFeatures(final String featureFile ){
	
		List<FeatureFile> featureFileList = null;
		
        final List<File> featureFiles = FileUtils.getFiles(new File(featureFile), ".feature");

        final FeatureFileParser fp2 = new FeatureFileParser();
        for (final File f : featureFiles) {
            final FeatureFile fFile = fp2.loadFeatureFile(f);
            if (featureFileList == null) {
                featureFileList = new ArrayList<FeatureFile>();
            }
            if (fFile != null) {
                featureFileList.add(fFile);
            }
        }

        Collections.sort(featureFileList, new FeatureFileComparator());
        
        return featureFileList;
	}
}
