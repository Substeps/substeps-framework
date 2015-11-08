substeps-api [![Build Status](https://travis-ci.org/G2G3Digital/substeps-api.svg)](https://travis-ci.org/G2G3Digital/substeps-api)
============

Welcome to the substeps-api project!

Substeps documentation can be found [here](http://substeps.technophobia.com/ "Substeps documentation").  

There is also a [Substeps Google group](http://groups.google.com/group/substeps?hl=en-GB "Substeps Google group") if you have any queries and where new releases will ne announced.
=============

1.1.3
-----
* Exposed a mechanism to allow users of substeps to specifiy custom execution listeners including renaming INotifier to IExecutionListener and renaming the notify_xxx methods to on_xxx.
* Support for building of the caller hierarchy in the core
* Addition of api in Configuration to enable config properties to be added in code without needing an enum and properties file.
* Tidied up ExecutionNode and storage of any exceptions thrown.  Wrapper around exception to make RMI calls easier in restricted environments.  
* MBean interface to additionally return byte[] representations of results.

1.1.2
-----
* version number bump in line with other substeps libraries

1.1.1
-----
* Addition of release notes!
* Move of ExecutionContextSupplier, MutableSupplier plus other refactorings
