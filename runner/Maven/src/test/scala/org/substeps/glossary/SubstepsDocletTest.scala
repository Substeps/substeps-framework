package org.substeps.glossary

import java.net.URI

import javax.tools.{DocumentationTool, JavaFileObject, SimpleJavaFileObject, ToolProvider}
import org.scalatest.{FlatSpec, FunSuite, Matchers}

import collection.JavaConverters._

class SubstepsDocletTest extends FlatSpec with Matchers {

//  "substeps custom doclet" must "work for a real step impl" in {
//
//    val projectDir = "/home/ian/projects/github/substeps-webdriver"
//    val m2 = "/home/ian/.m2/repository"
//
//    val classpath =
//      s"$projectDir/target/test-classes:$projectDir/target/classes:$m2/com/google/guava/guava/28.1-jre/guava-28.1-jre.jar:$m2/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:$m2/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:$m2/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:$m2/org/checkerframework/checker-qual/2.8.1/checker-qual-2.8.1.jar:$m2/com/google/errorprone/error_prone_annotations/2.3.2/error_prone_annotations-2.3.2.jar:$m2/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:$m2/org/codehaus/mojo/animal-sniffer-annotations/1.18/animal-sniffer-annotations-1.18.jar:/home/ian/projects/github/substeps-framework/api/target/classes:/home/ian/projects/github/substeps-framework/core/target/classes:$m2/com/google/code/gson/gson/2.8.2/gson-2.8.2.jar:$m2/commons-io/commons-io/2.6/commons-io-2.6.jar:$m2/joda-time/joda-time/2.9.1/joda-time-2.9.1.jar:$m2/org/json4s/json4s-native_2.12/3.6.6/json4s-native_2.12-3.6.6.jar:$m2/org/json4s/json4s-core_2.12/3.6.6/json4s-core_2.12-3.6.6.jar:$m2/org/json4s/json4s-ast_2.12/3.6.6/json4s-ast_2.12-3.6.6.jar:$m2/org/json4s/json4s-scalap_2.12/3.6.6/json4s-scalap_2.12-3.6.6.jar:$m2/com/thoughtworks/paranamer/paranamer/2.8/paranamer-2.8.jar:$m2/org/apache/commons/commons-jexl3/3.1/commons-jexl3-3.1.jar:/home/ian/projects/github/substeps-framework/runner/Junit/target/classes:/home/ian/projects/github/substeps-framework/runner/Common/target/classes:$m2/org/seleniumhq/selenium/htmlunit-driver/2.35.1/htmlunit-driver-2.35.1.jar:$m2/org/seleniumhq/selenium/selenium-api/3.141.59/selenium-api-3.141.59.jar:$m2/net/sourceforge/htmlunit/htmlunit/2.35.0/htmlunit-2.35.0.jar:$m2/xalan/xalan/2.7.2/xalan-2.7.2.jar:$m2/xalan/serializer/2.7.2/serializer-2.7.2.jar:$m2/org/apache/commons/commons-text/1.6/commons-text-1.6.jar:$m2/org/apache/httpcomponents/httpmime/4.5.8/httpmime-4.5.8.jar:$m2/net/sourceforge/htmlunit/htmlunit-core-js/2.35.0/htmlunit-core-js-2.35.0.jar:$m2/net/sourceforge/htmlunit/neko-htmlunit/2.35.0/neko-htmlunit-2.35.0.jar:$m2/xerces/xercesImpl/2.12.0/xercesImpl-2.12.0.jar:$m2/xml-apis/xml-apis/1.4.01/xml-apis-1.4.01.jar:$m2/net/sourceforge/htmlunit/htmlunit-cssparser/1.4.0/htmlunit-cssparser-1.4.0.jar:$m2/commons-net/commons-net/3.6/commons-net-3.6.jar:$m2/org/seleniumhq/selenium/selenium-support/3.141.59/selenium-support-3.141.59.jar:$m2/org/seleniumhq/selenium/selenium-remote-driver/3.141.59/selenium-remote-driver-3.141.59.jar:$m2/net/bytebuddy/byte-buddy/1.8.15/byte-buddy-1.8.15.jar:$m2/org/apache/commons/commons-exec/1.3/commons-exec-1.3.jar:$m2/com/squareup/okhttp3/okhttp/3.11.0/okhttp-3.11.0.jar:$m2/com/squareup/okio/okio/1.14.0/okio-1.14.0.jar:$m2/org/mockito/mockito-core/3.0.0/mockito-core-3.0.0.jar:$m2/net/bytebuddy/byte-buddy-agent/1.9.10/byte-buddy-agent-1.9.10.jar:$m2/org/objenesis/objenesis/2.6/objenesis-2.6.jar:$m2/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar:$m2/org/slf4j/slf4j-log4j12/1.7.26/slf4j-log4j12-1.7.26.jar:$m2/log4j/log4j/1.2.17/log4j-1.2.17.jar:$m2/junit/junit/4.12/junit-4.12.jar:$m2/org/hamcrest/hamcrest-core/2.1/hamcrest-core-2.1.jar:$m2/org/hamcrest/hamcrest/2.1/hamcrest-2.1.jar:$m2/org/hamcrest/hamcrest-library/2.1/hamcrest-library-2.1.jar:$m2/org/seleniumhq/selenium/selenium-java/3.141.59/selenium-java-3.141.59.jar:$m2/org/seleniumhq/selenium/selenium-chrome-driver/3.141.59/selenium-chrome-driver-3.141.59.jar:$m2/org/seleniumhq/selenium/selenium-edge-driver/3.141.59/selenium-edge-driver-3.141.59.jar:$m2/org/seleniumhq/selenium/selenium-firefox-driver/3.141.59/selenium-firefox-driver-3.141.59.jar:$m2/org/seleniumhq/selenium/selenium-ie-driver/3.141.59/selenium-ie-driver-3.141.59.jar:$m2/org/seleniumhq/selenium/selenium-opera-driver/3.141.59/selenium-opera-driver-3.141.59.jar:$m2/org/seleniumhq/selenium/selenium-safari-driver/3.141.59/selenium-safari-driver-3.141.59.jar:$m2/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar:$m2/org/eclipse/jetty/websocket/websocket-client/9.4.0.v20161208/websocket-client-9.4.0.v20161208.jar:$m2/org/eclipse/jetty/jetty-util/9.4.0.v20161208/jetty-util-9.4.0.v20161208.jar:$m2/org/eclipse/jetty/jetty-io/9.4.0.v20161208/jetty-io-9.4.0.v20161208.jar:$m2/org/eclipse/jetty/jetty-client/9.4.0.v20161208/jetty-client-9.4.0.v20161208.jar:$m2/org/eclipse/jetty/jetty-http/9.4.0.v20161208/jetty-http-9.4.0.v20161208.jar:$m2/org/eclipse/jetty/websocket/websocket-common/9.4.0.v20161208/websocket-common-9.4.0.v20161208.jar:$m2/org/eclipse/jetty/websocket/websocket-api/9.4.0.v20161208/websocket-api-9.4.0.v20161208.jar:$m2/org/apache/httpcomponents/httpclient/4.5.2/httpclient-4.5.2.jar:$m2/org/apache/httpcomponents/httpcore/4.4.4/httpcore-4.4.4.jar:$m2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:$m2/commons-codec/commons-codec/1.9/commons-codec-1.9.jar:$m2/com/typesafe/config/1.3.1/config-1.3.1.jar:$m2/io/github/bonigarcia/webdrivermanager/3.6.1/webdrivermanager-3.6.1.jar:$m2/org/rauschig/jarchivelib/1.0.0/jarchivelib-1.0.0.jar:$m2/org/apache/commons/commons-compress/1.18/commons-compress-1.18.jar:$m2/org/jsoup/jsoup/1.11.3/jsoup-1.11.3.jar:$m2/org/scala-lang/scala-library/2.12.8/scala-library-2.12.8.jar:$m2/org/scala-lang/scala-reflect/2.12.8/scala-reflect-2.12.8.jar:$m2/org/scalatest/scalatest_2.12/3.0.8/scalatest_2.12-3.0.8.jar:$m2/org/scalactic/scalactic_2.12/3.0.8/scalactic_2.12-3.0.8.jar:$m2/org/scala-lang/modules/scala-xml_2.12/1.2.0/scala-xml_2.12-1.2.0.jar:$m2/org/scalamock/scalamock_2.12/4.4.0/scalamock_2.12-4.4.0.jar"
//
//    val results =
//    DocletWrapper.invoke(s"$projectDir/src/main/java",
//      s"$projectDir/src/main/java/com/technophobia/webdriver/substeps/impl/ActionWebDriverSubStepImplementations.java",
//      classpath)
//
//    results.asScala should not be empty
//  }

  "substeps custom doclet" must "parse correct methods and docs" in  {

    val systemDocumentationTool : DocumentationTool = ToolProvider.getSystemDocumentationTool()

    val args = List (      "-sourcepath",
      "./src/test/java",

    )
    

    import javax.tools.DiagnosticCollector
    import javax.tools.JavaCompiler
    import javax.tools.JavaFileObject
    import javax.tools.StandardJavaFileManager
    import javax.tools.ToolProvider

    val compiler = ToolProvider.getSystemJavaCompiler
    val diagnostics = new DiagnosticCollector[JavaFileObject]
    val fm = compiler.getStandardFileManager(diagnostics, null, null)

    val jfo = fm.getJavaFileObjects("src/test/java/org/substeps/glossary/FakeSubsteps.java")

    SubstepsDocletResults.reset()

    val task : DocumentationTool.DocumentationTask = systemDocumentationTool.getTask(null, null, null,
      classOf[SubstepsDoclet], args.asJava, jfo)

        task.call()


    val results = SubstepsDocletResults.getStepImplementationsDescriptors().asScala

    results.size should be (1)

    val sid = results(0)

    sid.getClassName should be ("org.substeps.glossary.FakeSubsteps")

    val expressions = sid.getExpressions.asScala

    expressions.size should be (5)

    println("stop")
    val expr1 = expressions.find(sd => Option(sd.getRegex).isDefined && sd.getRegex.startsWith("FindByIdTimeout"))

    expr1 shouldBe defined
    expr1.get.getExample should be ("FindByIdTimeout username timeout = 15 secs")
    expr1.get.getDescription should be ("Find an element by it's ID with the specified timeout")
    expr1.get.getParameterNames.toList.size should be (2)

    println("paramnames: " + expr1.get.getParameterNames.toList.mkString(","))
  }

}
