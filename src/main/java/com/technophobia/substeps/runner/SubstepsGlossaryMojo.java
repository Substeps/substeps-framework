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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javadoc.Main;
import com.sun.tools.javadoc.Messager;
import com.technophobia.substeps.execution.ExecutionNode;
import com.technophobia.substeps.glossary.ClassStepTags;
import com.technophobia.substeps.glossary.CustomDoclet;
import com.thoughtworks.xstream.XStream;

/**
 * 
 * @goal generate-docs
 * @requiresDependencyResolution test
 * @phase generate-resources
 * 
 * @configurator include-project-dependencies
 */
public class SubstepsGlossaryMojo extends AbstractMojo {

    public static void printRed(final String msg) {

        // TODO
        System.out.println(msg);
    }

    private static final String XML_FILE_NAME = "classStepTags.xml";

    
    private final Logger log = LoggerFactory.getLogger(SubstepsGlossaryMojo.class);

    /**
     * Location of the file.
     * 
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;

//    /**
//     * @parameter default-value="${project.build.directory}"
//     */
//    private File outputDir;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private Properties systemProperties;

    /**
     * @parameter
     * @required
     */
    private String[] stepImplementationClassNames;

    
    /**
     * @parameter default-value="${plugin.classRealm}"
     * @required
     */
    private ClassRealm containerRealm;
    
    /**
     * @parameter
     */
    private final GlossaryPublisher glossaryPublisher = null;

    
    /**
	 * 
	 */
    private List<Class<?>> stepImplementationClasses;
    
//    /**
//     * @parameter
//     */
//    private final List<ExecutionConfig> executionConfigs = null;

    private final List<ExecutionNode> failedNodes = null;
    private final List<ExecutionNode> nonFatalFailedNodes = null;

    
    protected class LogWriter extends Writer {

        Level level;

        public LogWriter(final Level level) {
          this.level = level;
        }

        @Override
		public void write(final char[] chars, final int offset, final int length) throws IOException {
          final String s = new String(Arrays.copyOf(chars, length));
          if (!s.equals("\n"))
		{
        	  System.out.println(level.toString() + " - " + s);
//			logger.log(level, s);
		}
        }

        @Override
		public void flush() throws IOException {
        }

        @Override
		public void close() throws IOException {
        }
      }

      public class PublicMessager extends Messager {

        public PublicMessager(final Context context, final String s) {
          super(context, s);
        }

        public PublicMessager(final Context context, final String s, final PrintWriter printWriter,
            final PrintWriter printWriter1, final PrintWriter printWriter2) {
          super(context, s, printWriter, printWriter1, printWriter2);
        }
      }

      
      // TODO - this is copied from ExecutionConfig..
      
      private List<Class<?>> getClassesFromConfig(final String[] config) {
          List<Class<?>> stepImplementationClassList = null;
          for (final String className : config) {
              if (stepImplementationClassList == null) {
                  stepImplementationClassList = new ArrayList<Class<?>>();
              }
              Class<?> implClass;
              try {
                  implClass = Class.forName(className);
                  stepImplementationClassList.add(implClass);

              } catch (final ClassNotFoundException e) {
                  // TODO - fail
                  e.printStackTrace();
              }
          }
          return stepImplementationClassList;
      }
      
      
      private List<ClassStepTags> runJavaDoclet(final String classToDocument){

    	  final List<ClassStepTags> classStepTagList = new ArrayList<ClassStepTags>();

    	  this.project.getBasedir();
    	  
    	  
    	  
    	  String sourceRoot = null;
    	  // what's the path to this class ?
    	  String path = resolveClassToPath(classToDocument, this.project.getBuild().getSourceDirectory());
    	  if (path != null){
    		  // file is in the source path
    		  sourceRoot = this.project.getBuild().getSourceDirectory();
    	  }
    	  else {
    		  path = resolveClassToPath(classToDocument, this.project.getBuild().getTestSourceDirectory());
    		  if (path != null){
    			  // file is in the test tree
    			  sourceRoot = this.project.getBuild().getTestSourceDirectory();
    		  }
    	  }
    	  
    	  if (sourceRoot == null || path == null){
    		  
    		  log.error("unabled to locate source file");
    		  // TODO exception ?
    	  }
    	  else{

        	  CustomDoclet.setExpressionList(classStepTagList);
        	  
        	  
            	final String[] args = {"-doclet",
      			"com.technophobia.substeps.glossary.CustomDoclet",
//      			"-docletpath",
//      			"/home/ian/projects/webdriverbdd-utils/target/classes", // path to this jar ?
      			"-sourcepath",
      			sourceRoot, // "./src/main/java",	// path to the step impls / classpath ?
      			path //      			javadocStr //"/home/ian/projects/github/substeps-webdriver/src/main/java/com/technophobia/webdriver/substeps/impl/AssertionWebDriverSubStepImplementations.java"
            		};  // list of step impls to have a butcher sat 
          		// "/home/ian/projects/github/substeps-webdriver/src/main/java/com/technophobia/webdriver/substeps/impl/AssertionWebDriverSubStepImplementations.java"
      	
            	
            		Main.execute(args);

    	  }
      		
      		return classStepTagList ;
      }
      
    /**
	 * @param classToDocument = fqn of a class, dotted syntax
	 * @param isSourcePath
	 * @return
	 */
	private String resolveClassToPath(final String classToDocument, final String dir)
	{
		String fullpath = null;
		log.debug("resolving class to path: " + classToDocument + " in " + dir);
		
		final String filepath = dir + File.separator + classToDocument.replace('.', File.separator.charAt(0)) + ".java";
		
		log.debug("looking for file: " + filepath);
		
		final File f = new File(filepath);
		
		if (f.exists()){
			fullpath = f.getAbsolutePath();
		}
		return fullpath;
	}


	public void execute() throws MojoExecutionException, MojoFailureException {

//    	printDependencies();
    	
    	// TODO - have a look at the classes in the impl, locate the class
    	// if it's in a jar, open it have a look at the xml file
    	
    	// could build up a list of steptags from this method
    	// need some switch to work out wether to write out the file or not
    	
    	// CustomDoclet will need a static list which we can clear out

    	final HashSet<String> loadedClasses = new HashSet<String>();
    	
    	final List<ClassStepTags> classStepTags = new ArrayList<ClassStepTags>();
    	
    	// TODO - could look across all the classes in the source and test directories, javadoc the lot, custom
    	// doclet will just not produce anythin....
    	
    	for (final String classToDocument: stepImplementationClassNames){
    		
    		log.debug("documenting: " + classToDocument);
    		
    		// have we loaded info for this class already ?
    		if (!loadedClasses.contains(classToDocument)){
    		
	    		// where is this class ?
	    		final JarFile jarFileForClass = getJarFileForClass(classToDocument);
	    		if (jarFileForClass != null){
	    			
	    			log.debug("loading info from jar");
	    			
	    			// look for the stepTags.xml file in the jar, load up from there
	    			loadStepTagsFromJar(jarFileForClass, classStepTags, loadedClasses);
	    		}
	    		else {
	    			log.debug("loading step info from paths");
		    		// if it's in the project, run the javadoc and collect the details
	    			
	    			// TODOx establish the full path to the file
	    			// TODOx pass into the method the path
	    			
	    			classStepTags.addAll(runJavaDoclet(classToDocument));

	    		}
    		}
    	}
    	
    	
    	if (!classStepTags.isEmpty()){
    		
    		if (glossaryPublisher != null){
    			
    			glossaryPublisher.publish(classStepTags);
    			
    		}
    		else {
    			
    			saveXMLFile(classStepTags);
    		}
    	}
    	else {
    		log.error("no results to write out");
    	}
    	

    	
    	
    	//        final MojoNotifier notifier = new MojoNotifier();
//        final ReportData data = new ReportData();
//
//        Assert.assertNotNull("executionConfigs cannot be null", executionConfigs);
//        Assert.assertFalse("executionConfigs can't be empty", executionConfigs.isEmpty());
//
//        for (final ExecutionConfig executionConfig : executionConfigs) {
        	
        	// build up the javadoc
        	
        	// see http://www.docjar.com/html/api/com/sun/tools/javadoc/Main.java.html
        	
    	// or use JavadocTool ??
    	//http://www.docjar.com/html/api/com/sun/tools/javadoc/JavadocTool.java.html
    	

    	
// http://javasourcecode.org/html/open-source/jdk/jdk-6u23/com/sun/tools/javadoc/JavadocTool.java.html    	
    	
    	
//    	printDependencies();
    	
    	
    	
//        	final Context context = new Context();
//        	// what do I put in here ?
//        	
//        	context.put(JavaFileManager.class, arg1)
//        	
//        	 final Options compOpts = Options.instance(context);
//        	    compOpts.put("-sourcepath", "src/main/java");
//  
//        	    new PublicMessager(context, "test", 
//        	    		new PrintWriter(new LogWriter(Level.SEVERE), true),
//        	    		new PrintWriter(new LogWriter(Level.WARNING), true),
//        	    		new PrintWriter(new LogWriter(Level.FINE), true));
//
//        	    final JavadocTool javadocTool = JavadocTool.make0(context);
//        	    
//        	    final ListBuffer<String> javaNames = new ListBuffer<String>();
//        	    javaNames.append("com.technophobia.webdriver.substeps.impl");
//        	    
//        	    final ListBuffer<String[]> options = new ListBuffer<String[]>();
//        	    final ListBuffer<String> packageNames = new ListBuffer<String>();
//        	    final ListBuffer<String> excludedPackages = new ListBuffer<String>();

        	    
        	    
        	    /*
        	     * javadocTool.getRootDocImpl arguments: 
												(String doclocale,
  118                                         String encoding,
  119                                         ModifierFilter filter,
  120                                         List<String> javaNames,
  121                                         List<String[]> options,
  122                                         boolean breakiterator,
  123                                         List<String> subPackages,
  124                                         List<String> excludedPackages,
  125                                         boolean docClasses,
  126                                         boolean legacyDoclet,
  127                         boolean quiet)
        	     */
        	    
        	    // see line 142 of javadoctool http://www.docjar.com/html/api/com/sun/tools/javadoc/JavadocTool.java.html
        	    // different StandardJavaFileManager as mentioned here:
        	    //http://stackoverflow.com/questions/3299233/generating-javadoc-from-string-stream-etc	
        	    
//        	    try
//				{
//					final RootDocImpl rootDocImpl = javadocTool.getRootDocImpl("en", "",
//					    new ModifierFilter(ModifierFilter.ALL_ACCESS), javaNames.toList(),
//					    options.toList(), false, packageNames.toList(),
//					    excludedPackages.toList(), true, false, false);
//					
//					if (rootDocImpl != null){
//						System.out.println("root doc info ! null");
//						
//						System.out.println("rootDocImpl.getRawCommentText:\n" + rootDocImpl.getRawCommentText());
//						
//						
//						CustomDoclet.start(rootDocImpl);
//						
//					}
//					else {
//						
//						System.out.println("root doc impl is null");
//					}
//					
//				}
//				catch (final IOException e)
//				{
//					e.printStackTrace();
//				}

        	    
        	
        	
        	/*
        	 * // force the use of Javadoc's class reader
   96               JavadocClassReader.preRegister(context);
   97   
   98               // force the use of Javadoc's own enter phase
   99               JavadocEnter.preRegister(context);
  100   
  101               // force the use of Javadoc's own member enter phase
  102               JavadocMemberEnter.preRegister(context);
  103   
  104               // force the use of Javadoc's own xtodo phase
  105               JavadocTodo.preRegister(context);
  106   
  107               // force the use of Messager as a Log
  108               messager = Messager.instance0(context);
        	 */
        	
/*	
this is from portal - have to unpack the source jars...

rm -rf stepImplSrc
mkdir stepImplSrc
cd stepImplSrc
$JAVA_HOME/bin/jar xf $M2_REPO/uk/co/itmoore/webdriverbdd/$WEBDRIVER_BDD_VERSION/webdriverbdd-$WEBDRIVER_BDD_VERSION-sources.jar uk/co/itmoore/substep/impl/BaseWebdriverSubStepImplementations.java

cd ..

export BOOT_CLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar:$JAVA_DOC_CP

java -classpath $BOOT_CLASSPATH com.sun.tools.javadoc.Main -doclet uk.co.itmoore.bdd.util.CustomDoclet -docletpath $BOOT_CLASSPATH:$M2_REPO/uk/co/itmoore/webdriverbdd-util/0.0.1-SNAPSHOT/webdriverbdd-util-0.0.1-SNAPSHOT.jar -sourcepath ./stepImplSrc/*:./src/test/java/* ./stepImplSrc/uk/co/itmoore/substep/impl/BaseWebdriverSubStepImplementations.java ./src/test/java/uk/gov/nhsbsa/dcss/bddrunner/NHSStepImplementations.java


 */
        	
        	

    }
    
    
    /**
     * @param classStepTags 
	 * 
	 */
	private void saveXMLFile(final List<ClassStepTags> classStepTags)
	{
    	// got them all now serialize
		final XStream xstream = new XStream();
		final File output = new File(outputDirectory, XML_FILE_NAME);
		
		if (!outputDirectory.exists()){
			if (!outputDirectory.mkdirs()){
				
				throw new IllegalStateException("unable to create output directory");
			}
		}
		
		try
		{
			Files.write(xstream.toXML(classStepTags), output, Charset.forName("UTF-8"));
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
	}


	/**
	 * @param jarFileForClass
	 * @param classStepTags
	 * @param loadedClasses
	 */
	private void loadStepTagsFromJar(final JarFile jarFileForClass, final List<ClassStepTags> classStepTags,
			final Set<String> loadedClasses)
	{
		final ZipEntry entry = jarFileForClass.getEntry(XML_FILE_NAME);
	
		if (entry != null){
			
			try
			{
				final InputStream is = jarFileForClass.getInputStream(entry);
				
				final XStream xstream = new XStream();
				final List<ClassStepTags> classStepTagList = (List<ClassStepTags>)xstream.fromXML(is);
				
				classStepTags.addAll(classStepTagList);
				
				for (final ClassStepTags cst : classStepTagList){
					loadedClasses.add(cst.getClassName());
				}
			}
			catch (final IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			log.error("couldn't locate file in jar: " + XML_FILE_NAME);
		}
	}


	private String convertClassNameToPath(final String className){
//		log.debug("running replacement on className: " + className);
		return  className.replace('.', '/') + ".class";
    }
    
    private JarFile getJarFileForClass(final String className){

    	JarFile jarFile = null;
    	final Set<Artifact> artifacts = project.getArtifacts();
    	if (artifacts != null)
    	{
    		for (final Artifact a : artifacts)
    		{
    			// does this jar contain this class?
    			try
				{
					final JarFile tempJarFile = new JarFile(a.getFile());
					
					final JarEntry jarEntry = tempJarFile.getJarEntry(convertClassNameToPath(className));
					
					if (jarEntry != null){
						jarFile = tempJarFile;
						break;
					}
				}
				catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	return jarFile;
    }
    
	/**
	 * 
	 */
	public void printDependencies()
	{
		// TODO this lists out the deps of this project, fine - but we need the source in order to be able to build the javadoc
    	
    	// explicit reference of the artifacts / jars we'll look for source jars? 
    	// create an xml file that we bundle in the jar for reference ?
    	// that could be an easier way to do it?
    	
    	// artifacts returned depends on phase when executed
    	final Set<Artifact> artifacts = project.getArtifacts();
    	if (artifacts != null)
    	{
    		for (final Artifact a : artifacts)
    		{
    			System.out.println("Artifact: " + a.getGroupId() + 
    					" : " + a.getArtifactId() 
    					+ " : " + a.getVersion() + " file path: " + a.getFile().getAbsolutePath());
    			
    		}
    	}
    	
    	if (containerRealm != null)
    	{
    		final URL[] constituents = containerRealm.getConstituents();
    		
    		System.out.println("container realm constituents: ");
    		for (final URL url : constituents)
    		{
    			System.out.println( url.toString());
    		}
    	}
    	    	
    	try
		{
			 final List testClasspathElements = project.getTestClasspathElements();
			
			 
			 if (testClasspathElements != null)
			 {
				 System.out.println("test class path elements");
				 
				 for (int i = 0; i < testClasspathElements.size(); i++)
				 {
					 System.out.println(testClasspathElements.get(i));
				 }
			 }
			
		}
		catch (final DependencyResolutionRequiredException e)
		{
			System.out.println("DependencyResolutionRequiredException: " + e);
			e.printStackTrace();
		}
	}



  




}
