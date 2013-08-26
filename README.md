wm
==

This is my fork of the [Wikipedia Miner Toolkit](http://sourceforge.net/projects/wikipedia-miner/) written by David Milne.

Why a fork: (i) to transform it into a mvn project (instead of using the ant build tool), (ii) to keep my documentation of how to do things in one place (official wikipedia-miner wiki is often down), and (iii) to fix small errors.

Significant changes:
+ Using the included weka-wrapper jar led to several errors; instead including the (more up-to-date) source provided by David Milne [here](http://code.google.com/p/weka-wrapper/) solved these problems.


Build JAR
=========

The repo can be cloned with the following command:

```
$ git clone git://github.com/chauff/wm.git
```

Before the project can be build with Maven, the pom.xml file needs to be adapted - a few of the dependent jars cannot be found in public Maven repositories and the link has to be hardcoded in pom.xml (the jars can be found in the `lib` folder):
+ jsc.jar
+ servlet-api.jar

Then, to build the project, run:

```
mvn clean package
```

and a jar including all dependencies (*fatjar*) is generated in the folder `target`.



Processing a Wikipedia Dump 
===========================
Before the toolkit can be used, a database needs to be build from a Wikipedia dump. To be able to create the database, information needs to be extracted from the dump - this pre-processing step is based on Hadoop jobs.

The original description on the project's sourceforge page can be found [here](http://sourceforge.net/apps/mediawiki/wikipedia-miner/index.php?title=Extraction).

Steps:

+ Download a Wikipedia dump (usually ending in pages-articles.xml) and copy it to a folder (e.g. `input`) on HDFS
+ Upload `configs/languages.xml` to the same HDFS folder (should work for English out-of-the-box)
+ Upload `models/en-sent.bin` to the same HDFS folder (for English Wikipedia; other languages require different sentence detection models)
+ Create an output folder (e.g. `output`) on HDFS
+ Run (`en` is here the language code for English): 

```
hadoop jar WikipediaMiner-0.0.1-SNAPSHOT-fatjar.jar org.wikipedia.miner.extraction.DumpExtractor \
	input/enwiki-xxxx-pages-articles.xml \
	input/languages.xml \
	en \
	input/en-sent.bin \
 	output
```

+ Download the contents of the HDFS folder `output/final` to the local file system (all subsequent steps are not based on Hadoop jobs).


Building the Database
=====================

To build the database, copy the original Wikipedia dump file (...pages-articles.xml) into the local folder from the previous step.
Make the necessary changes to the file `configs/wikipedia-template.xml`: at least change `<langCode>` (`en` for English), `<dataDirectory>` (the local folder from the previous step), and `<databaseDirectory>` (folder where the database will be stored). All other parameters are optional, though changing them will likely yield better performance.

Run:

```
java -cp WikipediaMiner-XX-SNAPSHOT-fatjar.jar org.wikipedia.miner.util.EnvironmentBuilder \
	/path/to/wikipedia-template.xml
```


Testing the Database
====================
To test if the created database works, run (program will take a while to run):

```
java -cp WikipediaMiner-XX-SNAPSHOT-fatjar.jar nl.tudelft.ewi.wis.test.DatabaseTest \ 		
	/path/to/wikipedia-template.xml Cat
```

The program reads