substeps-glossary
=================

Release Notes
=============

1.1.1
-----
* Version number bump

This project will build an xml descriptor of the step implementations for inclusion within step implementation libraries, or if used in a project that uses step implementations,  the plugin can generate an HTML report for all of the step implementations in use on that project, both in libraries and bespoke.

Usage:
```xml
    <profile>
        <id>generate-glossary</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <build>
            <plugins>

                <plugin>
                    <groupId>com.technophobia.substeps</groupId>
                    <artifactId>substeps-glossary-builder</artifactId>
                    <version>0.0.1-SNAPSHOT</version>

                    <executions>
                        <execution>
                            <id>Build SubSteps Glossary</id>
                            <phase>process-classes</phase>
                            <goals>
                                <goal>generate-docs</goal>
                            </goals>
                        </execution>
                    </executions>

                    <configuration>
                        <stepImplementationClassNames>
				<!-- the list of step implementation classes you wish to document, these classes must reside in the source of the project or in a depedency of this pom -->	
                            <param>com.technophobia.webdriver.substeps.impl.AssertionWebDriverSubStepImplementations</param>
                            <param>com.technophobia.webdriver.substeps.impl.ActionWebDriverSubStepImplementations</param>
                            <param>com.technophobia.webdriver.substeps.impl.FinderWebDriverSubStepImplementations</param>
                            <param>com.technophobia.webdriver.substeps.impl.FormWebDriverSubStepImplementations</param>
                            <param>com.technophobia.webdriver.substeps.impl.StartupWebDriverSubStepImplementations</param>
                            <param>com.technophobia.webdriver.substeps.impl.TableSubStepImplementations</param>
                        </stepImplementationClassNames>

                        <glossaryPublisher
                            implementation="com.technophobia.substeps.glossary.HTMLSubstepsPublisher">
                            <outputFile>./docs/stepimplementations.html</outputFile>
                        </glossaryPublisher>

                    </configuration>


                    <dependencies>
			<!-- Other dependencies are used from the surrounding pom -->

                        <dependency>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-log4j12</artifactId>
                            <version>1.6.4</version>
                        </dependency>

                    </dependencies>

                </plugin>
            </plugins>
        </build>
    </profile>
```   


Substeps documentation can be found [here](http://substeps.technophobia.com/ "Substeps documentation").  

There is also a [Substeps Google group](http://groups.google.com/group/substeps?hl=en-GB "Substeps Google group") if you have any queries and where new releases will ne announced.
