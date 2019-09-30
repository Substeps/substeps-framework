package org.substeps.glossary

import java.io.{File, StringWriter}
import java.lang.reflect.Method
import java.util
import java.util.Locale

import javax.tools.StandardLocation

import com.sun.source.doctree._
import com.sun.source.util.{DocTrees, TreePath}
import com.technophobia.substeps.glossary.{StepDescriptor, StepImplementationsDescriptor}
import com.technophobia.substeps.model.SubSteps
import javax.lang.model.SourceVersion
import javax.lang.model.element.{Element, ElementKind, TypeElement}
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic.Kind
import javax.tools.{DiagnosticCollector, DocumentationTool, JavaFileObject, ToolProvider}
import jdk.javadoc.doclet.{Doclet, DocletEnvironment, Reporter}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable


object SubstepsDocletResults {

  // use as a container for results
  private val buf = mutable.Buffer.empty[StepImplementationsDescriptor]

  def addStepImplementationsDescriptor(sid : StepImplementationsDescriptor) = {
    buf += sid
  }

  def reset() = buf.clear()

  def getStepImplementationsDescriptors() = {
    buf.asJava
  }
}

object DocletWrapper {

  val log = LoggerFactory.getLogger("org.substeps.glossary.DocletWrapper")


  def invoke(sourceRoot: String, classToDocument : String, classpathList : util.List[String]) : util.List[StepImplementationsDescriptor] = {

    invoke(sourceRoot, classToDocument, classpathList.asScala.mkString("" + File.pathSeparatorChar))
  }


  def invoke(sourceRoot: String, classToDocument : String, classpath : String) = {

    log.debug(s"invoking substeps glossary doclet srcRoor: $sourceRoot class: $classToDocument")

    val compiler = ToolProvider.getSystemJavaCompiler
    val diagnosticListener = new DiagnosticCollector[JavaFileObject]
    val fm = compiler.getStandardFileManager(diagnosticListener, null, null)

    val jfo = fm.getJavaFileObjects(classToDocument)

    SubstepsDocletResults.reset()

    val systemDocumentationTool : DocumentationTool = ToolProvider.getSystemDocumentationTool()

    val args = List ("-sourcepath", sourceRoot,
      "-classpath", classpath)

    val esw = new StringWriter()


    val task : DocumentationTool.DocumentationTask = systemDocumentationTool.getTask(esw, fm, diagnosticListener,
      classOf[SubstepsDoclet], args.toSeq.asJava, jfo)

    task.call()

    val diagnostics = diagnosticListener.getDiagnostics().asScala

    log.debug("diagnostics")
    diagnostics.foreach( d => {

      val srcName = Option(d.getSource).map(d2 => d2.getName).getOrElse("unknown")

      log.debug(  s"${d.getKind} : line ${d.getLineNumber} source: ${srcName} msg: ${d.getMessage(Locale.ENGLISH)}")
    })

    val warnings = esw.toString()
    if (!warnings.isEmpty()) {
        log.info("SubstepsDoclet warnings:\n" + warnings);
    }


    SubstepsDocletResults.getStepImplementationsDescriptors()


  }

}

class SubstepsDoclet extends Doclet {

//  https://docs.oracle.com/en/java/javase/11/docs/api/jdk.javadoc/jdk/javadoc/doclet/package-summary.html

  private val log: Logger = LoggerFactory.getLogger("org.substeps.glossary.SubstepsDoclet")

  /**
    * some pure scala versions : https://groups.google.com/forum/#!topic/scala-user/stwdJ1sz_rQ
    *
    *
    */


  var reporter: Reporter = null

  override def getName: String = "Substeps Glossary Doclet"

  override def getSupportedOptions: util.Set[_ <: Doclet.Option] = Set.empty.asJava

  override def getSupportedSourceVersion: SourceVersion = {
    // support the latest release
    SourceVersion.latest()
  }

  override def init(locale: Locale, reporter: Reporter): Unit = {
    this.reporter = reporter
    this.reporter.print(Kind.NOTE, "SubstepsDoclet using locale: " + locale)
  }


  def printElement(trees: DocTrees , e: Element) : Unit = {


     val docCommentTree: DocCommentTree = trees.getDocCommentTree(e.asInstanceOf[TreePath])
    if (docCommentTree != null) {
      log.trace("Element (" + e.getKind() + ": "
        + e + ") has the following comments:")

      log.trace("Entire body: " + docCommentTree.getBody)
      log.trace("Block tags: " + docCommentTree.getBlockTags())
    }
  }

  def getMethod(methodName: String, methods: List[Method], expectedNumberOfParams: Int) = {

    val candidateMethods =
      methods.filter(m => {
        m.getName == methodName && m.getParameterCount == expectedNumberOfParams
      })

      if (candidateMethods.isEmpty || candidateMethods.size > 1) {
          throw new IllegalStateException("Unable to match method name and parameter count to underlying class")
      }

    candidateMethods(0)
  }

  override def run(docletEnvironment: DocletEnvironment): Boolean = {

    log.debug("Substeps doclet running!")

    val docTrees = docletEnvironment.getDocTrees

    val classTypesIn: mutable.Set[TypeElement] = ElementFilter.typesIn(docletEnvironment.getIncludedElements).asScala.filter(te => te.getKind == ElementKind.CLASS)

    classTypesIn.foreach(t => {
            log.trace("type kind " + t.getKind + ":" + t)

      val classLoader = docletEnvironment.getJavaFileManager.getClassLoader(StandardLocation.CLASS_PATH)
      val implClass = classLoader.loadClass(t.getQualifiedName.toString)
      val implMethods = implClass.getMethods()

      val sid = new StepImplementationsDescriptor(t.toString)
      SubstepsDocletResults.addStepImplementationsDescriptor(sid)

      val substepMethodElements = t.getEnclosedElements.asScala.filter(e =>
        e.getKind == ElementKind.METHOD && Option(e.getAnnotation(classOf[SubSteps.Step])).isDefined)

      substepMethodElements.foreach(substepMethodElement => {

        log.trace("method element simple name: " + substepMethodElement.getSimpleName)

        val sd = new StepDescriptor()
        sid.addStepTags(sd)

        val substepAnnotation = substepMethodElement.getAnnotation(classOf[SubSteps.Step])

        sd.setRegex(substepAnnotation.value())

        // is this a substeps annotated method ?

        val methodDocCommentTree = docTrees.getDocCommentTree(substepMethodElement)

        log.trace("methodDocCommentTree fullbody:")

        if (Option(methodDocCommentTree).isDefined) {

          val textElems = methodDocCommentTree.getFullBody.asScala.filter (comment => {

            log.trace(s"kind: ${comment.getKind} tostr: ${comment.toString}")

            comment.getKind match {
              case DocTree.Kind.TEXT | DocTree.Kind.START_ELEMENT | DocTree.Kind.END_ELEMENT | DocTree.Kind.ENTITY => true
              case _ => false
            }
          })

          sd.setDescription(textElems.mkString(" ").replaceAll("\n", ""))

          val fullBody = methodDocCommentTree.getFullBody.asScala

          fullBody.foreach(dt => {
            log.trace("doc tree kind: " + dt.getKind + " : " + dt.toString)

          })

          val blockTags = methodDocCommentTree.getBlockTags.asScala.map(bt => bt.asInstanceOf[BlockTagTree])

          val exampleOption = blockTags.find(tag => tag.getKind == DocTree.Kind.UNKNOWN_BLOCK_TAG && tag.getTagName == "org.substeps.step.example")
          val sectionOption = blockTags.find(tag => tag.getKind == DocTree.Kind.UNKNOWN_BLOCK_TAG && tag.getTagName == "org.substeps.step.section")

          val parameterNames = blockTags.filter(tag => tag.getKind == DocTree.Kind.PARAM).map(t => t.asInstanceOf[ParamTree].getName.toString)

          val underlyingMethod = getMethod(substepMethodElement.getSimpleName.toString, implMethods.toList, parameterNames.size)

          val realParams = underlyingMethod.getParameters

          val (realParamNames, paramTypes) = realParams.toList.map(p => (p.getName, p.getType.getSimpleName)).unzip

          var line = substepAnnotation.value()

          parameterNames.foreach(pn => {
            line = line.replaceFirst("\\([^\\)]*\\)", "<" + pn + ">")
          })

          line = line.replaceAll("\\?", "").replaceAll("\\\\", "")

          sd.setExpression(line)

          if (!parameterNames.isEmpty) sd.setParameterNames(parameterNames.toArray)
          if (!paramTypes.isEmpty) sd.setParameterClassNames(paramTypes.toArray)

          exampleOption.map(tag => sd.setExample(tag.asInstanceOf[UnknownBlockTagTree].getContent.asScala(0).asInstanceOf[TextTree].getBody.replaceAll("\n", "")))
          sectionOption.map(tag => sd.setSection(tag.asInstanceOf[UnknownBlockTagTree].getContent.asScala(0).asInstanceOf[TextTree].getBody))

          blockTags.foreach(bt => {
            log.trace("block tag kind: " + bt.getKind + " tagName: " + bt.asInstanceOf[BlockTagTree].getTagName)
          })

          log.trace("block tags: " + methodDocCommentTree.getBlockTags)
        }
      })
    })
    true
  }
}
