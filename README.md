wm
==

This is my fork of the [Wikipedia Miner Toolkit](http://sourceforge.net/projects/wikipedia-miner/) written by David Milne.

Why a fork: (i) to transform it into a maven project (instead of using the ant build tool), (ii) to keep essential documentation of how to do things in one place (the official wikipedia-miner wiki is often down), and (iii) to fix small errors.

Significant changes:
+ Using the included weka-wrapper jar led to several errors; instead including the more up-to-date source provided by David Milne [here](http://code.google.com/p/weka-wrapper/) solved these problems.


Build JAR
=========

The repository can be cloned with the following command:

```
$ git clone git://github.com/chauff/wm.git
```

Before the project can be build with Maven, the `pom.xml` file needs to be adapted - two of the dependent jars could not be found in public Maven repositories and their local paths have to be hardcoded in `pom.xml` (the respective jars can be found in the `lib` folder):

+ jsc.jar
+ servlet-api.jar

Once that is done, to build the project, run:

```
mvn clean package
```

and a jar including all dependencies (*fatjar*) is generated in the folder `target`.



Processing a Wikipedia Dump 
===========================
Before the toolkit can be used, a database needs to be build from a Wikipedia dump. To be able to create the database, information needs to be extracted from the dump - this pre-processing step is based on Hadoop jobs.

The original description on the project's sourceforge page can be found [here](http://sourceforge.net/apps/mediawiki/wikipedia-miner/index.php?title=Extraction).

Steps:

+ Download a Wikipedia dump (usually ending in `pages-articles.xml`) and copy it to a folder (e.g. `input`) on HDFS
+ Upload `configs/languages.xml` to the same HDFS folder (should work for English out-of-the-box)
+ Upload `models/en-sent.bin` to the same HDFS folder (for English Wikipedia; other languages require different sentence detection models)
+ Create an output folder (e.g. `output`) on HDFS
+ Run: 

```
hadoop jar WikipediaMiner-0.0.1-SNAPSHOT-fatjar.jar org.wikipedia.miner.extraction.DumpExtractor \
	input/enwiki-xxxx-pages-articles.xml \
	input/languages.xml \
	en \
	input/en-sent.bin \
 	output
```
(here `en` is here the language code for English)
+ Download the contents of the HDFS folder `output/final` to the local file system (all subsequent steps are not based on Hadoop jobs).


Building the Database
=====================

To build the database, copy the original Wikipedia dump file (`pages-articles.xml`) into the local folder from the previous step.
Make the necessary changes to the configuration file `configs/wikipedia-template.xml`: at least change `<langCode>` (`en` for English), `<dataDirectory>` (the local folder from the previous step), and `<databaseDirectory>` (folder where the database will be stored). 

All other configuration parameters are optional, though changing them will likely result in an improved performance - the models for the last five parameters for the English language are included in the `models` directory (just to be clear: not my models, this is the original code/data from David Milne) and the path to these files can just be added.

Then run:

```
java -cp WikipediaMiner-XX-SNAPSHOT-fatjar.jar org.wikipedia.miner.util.EnvironmentBuilder \
	/path/to/wikipedia-template.xml
```

to build the database. This process will take a while.


Testing the Database
====================
To test if the created database works, be patient and run:

```
java -cp WikipediaMiner-XX-SNAPSHOT-fatjar.jar nl.tudelft.ewi.wis.test.DatabaseTest \ 		
	/path/to/wikipedia-template.xml Cat
```

where the second parameter (here: `Cat`) can be any Wikipedia concept. The program prints out translations, synonyms, related concepts, etc.
If no error occurs in the process, the database was built correctly.


Line-by-Line Annotation
=======================
This program annotates the text of a file, one line at a time. 

The format of the file is expected to be: `[id] [text]` (per line), e.g.

```
1 The domestic cat is a small, usually furry, domesticated, and carnivorous mammal. 
2 Paleo-indians migrated from Asia to what is now the United States mainland around 12,000 years ago.
3 The United States is a developed country and has the world's largest national economy, with an estimated 2013 GDP of $16.2 trillion –22% of global GDP at purchasing-power parity, as of 2011.
```
The `[id]` does not need to be numeric, but a whitespace is required after the id (no tab!).
The minimum sense probability can be set through `wikipedia-template.xml`.

Then, run:

```
java -cp WikipediaMiner-XX-SNAPSHOT-fatjar.jar nl.tudelft.ewi.wis.util.LineByLineAnnotation \ 		
	/path/to/wikipedia-template.xml \
	/path/to/input-file \
	/path/to/output-file
```

The output should look like this:

```
1 The [[Cat|domestic cat]] is a small, usually furry, [[Domestication|domesticated]], and [[Carnivore|carnivorous]] [[mammal]]. 
2 [[Paleo-indians]] [[Settlement of the Americas|migrated from Asia]] to what is now the [[United States]] mainland around 12,000 years ago.
3 The [[United States]] is a [[developed country]] and has the world's largest [[Economy|national economy]], with an estimated 2013 [[Gross domestic product|GDP]] of $16.2 [[Orders of magnitude (numbers)|trillion]] –22% of global [[Gross domestic product|GDP]] at [[Purchasing power parity|purchasing-power parity]], as of 2011.
```

(output generated with `<minSenseProbability>0.1</minSenseProbability>`).