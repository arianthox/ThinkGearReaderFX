# ThinkGearReaderFX
===========================================

The purpose of this project is to provide a UI for ThinkGearReader in order to Analyze the BrainWaves Spectrum


Technologies
------------

+ java
+ javafx
+ Spring
+ docker
+ gradle

Prerequisites
--------------


How To Compile
--------------

The service can be compiled with:

```
gradle clean build
```


How To Run
--------------

The service can be executed with:

```
gradle bootRun
```


Successful compilation conditions
--------------
This project uses pmd, findbugs, jacoco to guaranty the quality of the code.

In addition there is a jacoco task that is attached to the build lifecycle that prevents the successful compilation of the project if there is no enought unit test code coverage.

The current minimun coverage percentage is: 80 %