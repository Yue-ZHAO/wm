wm
==

This is my fork of the Wikipedia Miner Toolkit: http://sourceforge.net/projects/wikipedia-miner/ written by David Milne.

Why a fork: (i) to transform it into a mvn project (instead of using the ant build tool), and, (ii) to keep my documentation of how to do things in one place (official wikipedia-miner wiki is often down).


Build JAR
=========

The repo can be cloned with the following command:

$ git clone git://github.com/chauff/wm.git

Before the project can be build with Maven, the pom.xml file needs to be adapted - a few of the dependent jars cannot be found in public Maven repositories and the link has to be hardcoded in pom.xml (the jars can be found in the `lib` folder):
+ jsc.jar
+ servlet-api.jar
+ weka-wrapper-1.0.jar

Then, to build the project, run:

```
mvn clean package
```

and a jar including all dependencies (*fatjar*) is generated in the folder `target`.



Processing a Wikipedia Dump (Hadoop-based)
==========================================
Before the toolkit can be used, a database needs to be build from a Wikipedia dump. To be able to create the database, information needs to be extracted from the dump - this pre-processing step is based on Hadoop jobs.

The original description on the project's sourceforge page can be found [here](http://sourceforge.net/apps/mediawiki/wikipedia-miner/index.php?title=Extraction).

Steps:

+ Download a Wikipedia dump (usually ending in pages-articles.xml) and copy it to a folder (e.g. `input`) on HDFS
+ Upload `configs/languages.xml` to the same folder (should work for English out-of-the-box)
+ Upload `models/en-sent.bin` to the same folder (for English Wikipedia; other languages require different sentence detection models)
+ Create an output folder (e.g. `output`) on HDFS
+ Run (`en` is here the language code for English): 

```
hadoop jar WikipediaMiner-0.0.1-SNAPSHOT-fatjar.jar org.wikipedia.miner.extraction.DumpExtractor \
	input/enwiki-xxxx-pages-articles.xml \
	input/languages.xml \
	en \
	input/en-sent.bin output
```

+ Download the folder `output/final` from HDFS (extracted summaries)


Building the Database
=====================
To build the database, needed are the original Wikipedia dump file (...pages-articles.xml) and the extracted summaries created in the previous step. 







