<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->

[![Build Status](https://travis-ci.org/apache/datasketches-pig.svg?branch=master)](https://travis-ci.org/apache/datasketches-pig)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.datasketches/datasketches-pig/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.datasketches/datasketches-pig)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/apache/datasketches-pig.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/apache/datasketches-pig/context:java)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/apache/datasketches-pig.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/apache/datasketches-pig/alerts/)
[![Coverage Status](https://coveralls.io/repos/github/apache/datasketches-pig/badge.svg?branch=master)](https://coveralls.io/github/apache/datasketches-pig?branch=master)

=================

# DataSketches Java UDF/UDAF Adaptors for Apache Pig 

Please visit the main [DataSketches website](https://datasketches.apache.org) for more information. 

If you are interested in making contributions to this site please see our [Community](https://datasketches.apache.org/docs/Community/) page for how to contact us.

### Hadoop Pig UDFs 
See relevant sections under the different sketch types in Java Core Documentation.

## Build Instructions
__NOTE:__ This component accesses resource files for testing. As a result, the directory elements of the full absolute path of the target installation directory must qualify as Java identifiers. In other words, the directory elements must not have any space characters (or non-Java identifier characters) in any of the path elements. This is required by the Oracle Java Specification in order to ensure location-independent access to resources: [See Oracle Location-Independent Access to Resources](https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html)

### JDK8 is required to compile
This DataSketches component is pure Java and you must compile using JDK 8.

### Recommended Build Tool
This DataSketches component is structured as a Maven project and Maven is the recommended Build Tool.

There are two types of tests: normal unit tests and tests run by the strict profile.  

To run normal unit tests:

    $ mvn clean test

To run the strict profile tests:

    $ mvn clean test -P strict

To install jars built from the downloaded source:

    $ mvn clean install -DskipTests=true

This will create the following jars:

* datasketches-pig-X.Y.Z.jar The compiled main class files.
* datasketches-pig-X.Y.Z-tests.jar The compiled test class files.
* datasketches-pig-X.Y.Z-sources.jar The main source files.
* datasketches-pig-X.Y.Z-test-sources.jar The test source files
* datasketches-pig-X.Y.Z-javadoc.jar  The compressed Javadocs.

### Dependencies

#### Run-time
This has the following top-level dependencies:

* org.apache.datasketches : datasketches-java
* org.apache.pig : pig
* org.apache.hadoop : hadoop-common
* org.apache.commons : commons-math3

#### Testing
See the pom.xml file for test dependencies.

