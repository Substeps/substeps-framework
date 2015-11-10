Substeps Runner - Release Notes
===============================

- A list of changes per release 

1.1.1
-----
- BUG: Exception thrown during statup would prevent spawned processes from shutting down.  Changed the sequence of when the shutdown hook is registered.
- JAVA_HOME now used when running in forked mode rather than relying on the path.

1.1.0
-----
- The maven plugin has been renamed to substeps-maven-plugin in keeping with standard conventions
- By default the plugin now spawns a new instance of the JVM in which the tests are executed, communication is via JMX
- Maven site now generated
- Output from child process uses System.out rather than Maven logger
 

1.0.0
-----
- changes to support new reporting changes and the 1.0.0 release of substeps-core

0.0.6
-----
- BUG: Non Fatal failures were still causing the build to fail
 
0.0.5
-----
- Changes as a result of core changes to Notifications.
- BUG: failures in @BeforeAllFeatures not failing the maven build
- Doc changes clarified use of tags
- Report builder no longer essential
