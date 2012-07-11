Using Substeps Runner
=====================

Plugin Configuration
--------------------

- Below is a fragment of a maven pom file to configure the susbteps-runner plugin.
- An SLF4J logging implementation is required as a dependency, the plugin adds all of the test dependencies of the project 
  to it's own set of dependencies to avoid duplication in the pom.

.. code-block:: xml
   
     <plugin>
         <groupId>com.technophobia.substeps</groupId>
         <artifactId>substeps-runner</artifactId>
         <version>0.0.4</version>
 
         <executions>
             <execution>
                 <id>SubSteps Test</id>
                 <phase>integration-test</phase>
                 <goals>
                     <goal>run-features</goal>
                 </goals>
             </execution>
         </executions>
         <configuration>
            <executionConfigs>
                
                <!-- multiple execution configurations - details below -->

             <executionConfigs>     
         </configuration>
        <dependencies>

            <!-- NB. The plugin uses all test dependencies defined in this project, 
                as it's own so there is no need to list separately. The exception is an slf4j 
                logging implementation, which is required before other dependencies have 
                been added. This logger is included as an example, it can be replaced with 
                another slf4j logger of your choice. -->

            <dependency>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-log4j12</artifactId>
                <version>1.6.4</version>
            </dependency>

        </dependencies>


         
Execution Configuration
-----------------------        

- The Substeps runner plugin can execute the tests specified by a number of configurations.
- This can be useful in order to break up test suites into smaller chunks, use different configurations etc.

Below is a table of the execution configuration parameters and their meaning.

+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| Field                        | Description                                                                                                                                                                                            | |
+==============================+========================================================================================================================================================================================================+=+
| description                  | A descriptive name for the configuration, this is used in the test execution report.                                                                                                                   | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| featureFile                  | path to the feature file, or directory containing the feature files                                                                                                                                    | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| subStepsFileName             | path to directory of substep files, or a single substep file                                                                                                                                           | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| stepImplementationClassNames | List of classes containing step implementations                                                                                                                                                        | |
|                              | eg <param>com.technophobia.substeps.StepImplmentations<param>                                                                                                                                          | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| initialisationClass          | Ordered list of classes containing setup and tear down methods                                                                                                                                         | |
|                              | eg <param>com.technophobia.substeps.MySetup<param>                                                                                                                                                     | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| tags                         | <**optional**> If the feature or scenario has this tag, then it will be                                                                                                                                | |
|                              | included, otherwise it won't                                                                                                                                                                           | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| nonFatalTags                 | <**optional**> If a scenario (and therefore a feature) that has this tag fails to pass, then the build will not fail.  This is useful for scenarios                                                    | |
|                              | where tests are written and are included in a CI build in advance of completed functionality, this allows the build and therefore maven releases to succeed.  Over the course of                       | |
|                              | a project this list should be reduced as confidence in the delivery grows.                                                                                                                             | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| fastFailParseErrors          | <**optional**> if true any parse errors will fail the build immediately, rather than attempting to execute as much as possible and fail those tests that can't be parsed                               | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| strict                       | <**optional**> defaults to true, if false, Substeps will use the nonStrictKeywordPrecedence to look for alternate expressions if an exact match can't be found.  Useful for porting Cucumber features. | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+
| nonStrictKeywordPrecedence   | <**optional**> required if strict is false.  An parameter list of keywords to use if an exact match can't be found.                                                                                    | |
|                              | eg. <param>Given</param>                                                                                                                                                                               | |
|                              | <param>When</param> ...                                                                                                                                                                                | |
|                              | Then if a step was defined in a feature or substep as "When I login", but implemented as "Given I login", the feature would parse correctly.                                                           | |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-+

Example Pom fragment

.. code-block:: xml
   <executionConfig>
   
     <description>Self Test Features</description> 
         
     <featureFile>${basedir}/target/test-classes/features</featureFile> 
     
     <subStepsFileName>${basedir}/target/test-classes/substeps</subStepsFileName> 
   
     <stepImplementationClassNames>
         <param>com.technophobia.webdriver.substeps.impl.BaseWebdriverSubStepImplementations</param>
         <param>com.technophobia.webdriver.substeps.example.ExampleCustomWebdriverStepImplementations</param>
     </stepImplementationClassNames>
   
     <initialisationClass>
         <param>com.technophobia.webdriver.substeps.runner.DefaultExecutionSetupTearDown</param>
         <param>com.technophobia.webdriver.substeps.example.ExampleSetupAndTearDown</param>
     </initialisationClass>
      
      <!-- optional attributes -->
   
     <tags>@all</tags>  
     
     <nonFatalTags>@new_phase</nonFatalTags>
      
     <fastFailParseErrors>false</fastFailParseErrors> 
   
     <strict>false</strict>
      
     <nonStrictKeywordPrecedence>
         <param>Given</param>
         <param>When</param>
         <param>Then</param>
         <param>And</param>
     </nonStrictKeywordPrecedence>


   </executionConfig>
         


Reporting
---------
- The Susbteps runner plugin can create an HTML test execution report with the following configuration at plugin level.
- Results from multiple execution configurations are combined.
- alter the outputDirectory to write the html report elsewhere.  
- Typically we use a 'post-build' task to move the HTML report elsewhere and create a permanent link. (In Jenkins this can be achieved with the HTMLPublisher plugin).    

.. code-block:: xml
      <configuration>
         ...
         <executionReportBuilder implementation="com.technophobia.substeps.report.DefaultExecutionReportBuilder">
           <outputDirectory>${project.build.directory}</outputDirectory>
         </executionReportBuilder>
      
      </configuration>

- Alternative report builders can be used provided that they implement ``com.technophobia.substeps.report.ExecutionReportBuilder``  



         
         
.. Note::
    Check `Maven Central <http://search.maven.org/#search|ga|1|com.technophobia.substeps>`_ for the latest versions of the plugin.
