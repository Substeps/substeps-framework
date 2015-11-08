substeps-runner [![Build Status](https://travis-ci.org/G2G3Digital/substeps-runner.svg)](https://travis-ci.org/G2G3Digital/substeps-runner)
===============

Runners to execute substeps, currently includes an ANT runner, a Maven plugin and a Junit runner.  

Substeps documentation can be found [here](http://substeps.technophobia.com/ "Substeps documentation").  

There is also a [Substeps Google group](http://groups.google.com/group/substeps?hl=en-GB "Substeps Google group") if you have any queries and where new releases will ne announced.

Release Notes
=============
1.1.3
-----
* Changes to support ExecutionListener refactoring in core and api projects
* bug with final modifier set on config class preventing it from bring set
* configurable description depth parameter

1.1.2
-----
* version number bump in line with other substeps libraries

1.1.1
-----
* A 'Catch all' in the Maven runner to handle hidden exceptions in spawned VMs 
