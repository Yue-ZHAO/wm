package nl.tudelft.ewi.wis.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import org.wikipedia.miner.annotation.Disambiguator;
import org.wikipedia.miner.annotation.Topic;
import org.wikipedia.miner.annotation.TopicDetector;
import org.wikipedia.miner.annotation.preprocessing.HtmlPreprocessor;
import org.wikipedia.miner.annotation.preprocessing.PreprocessedDocument;
import org.wikipedia.miner.annotation.tagging.DocumentTagger;
import org.wikipedia.miner.annotation.tagging.DocumentTagger.RepeatMode;
import org.wikipedia.miner.annotation.tagging.WikiTagger;
import org.wikipedia.miner.annotation.weighting.LinkDetector;
import org.wikipedia.miner.model.Wikipedia;

/**
 * 
 * @author claudiahauff
 * 
 * The code takes as input (1) wikipedia-template.xml file, (2) input file, and (3) output file.
 * The input file is expected to be in the format (per line): [id] [text], e.g. tweetid tweet-text
 * The output is [id] [marked up text] (per line).
 * 
 * The annotation is conducted line by line.
 *
 */

public class LineByLineAnnotation {

	public static void main(String args[]) {
		try {

			if (args.length != 3) {
				System.err
						.println("Error: expecting three arguments - [wikipedia-template.xml path] [input file with format <tweetid> <text>] [output file]");
				System.exit(1);
			}
			
			//safety check, to not accidentally overwrite important files.
			if(new File(args[2]).exists()==true) {
				System.err.println("Error: output file "+args[2]+" already exists, aborting.");
				System.exit(1);
			}
			
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(args[2]));
			Wikipedia w = new Wikipedia(new File(args[0]), false);
			float minProb = w.getConfig().getMinSenseProbability();
			System.err.println("Minimum sense probability is set to "+minProb);
			
			Disambiguator d = new Disambiguator(w);
			d.loadClassifier(w.getConfig().getTopicDisambiguationModel());

			TopicDetector td = new TopicDetector(w, d, false, false);

			LinkDetector ld = new LinkDetector(w);
			ld.loadClassifier(w.getConfig().getLinkDetectionModel());

			DocumentTagger dt = new WikiTagger();

			HtmlPreprocessor preproc = new HtmlPreprocessor();
			BufferedReader br = new BufferedReader(new FileReader(args[1]));
			String line;
			while ((line = br.readLine()) != null) {
				int spaceIndex = line.indexOf(' ');
				
				if (spaceIndex < 0) {
					System.err.println("Error: cannot process line [" + line
							+ "], expecting a whitespace after the id");
					continue;
				}

				String id = line.substring(0, spaceIndex);
				String text = line.substring(spaceIndex + 1);
				
				System.err.println("Wikifying [" + text +"]");

				PreprocessedDocument pd = preproc.preprocess(text);

				// stops wikifier from detecting "Space (punctuation)" ;
				pd.banTopic(143856);

				ArrayList<Topic> allTopics = ld.getWeightedTopics(td.getTopics(
						pd, null));
				ArrayList<Topic> bestTopics = new ArrayList<Topic>();
				ArrayList<Topic> detectedTopics = new ArrayList<Topic>();
				for (Topic t : allTopics) {
					if (t.getWeight() >= minProb)
						bestTopics.add(t);

					detectedTopics.add(t);
				}

				String taggedText = dt.tag(pd, bestTopics, RepeatMode.ALL);

				System.err.println("\t => " + taggedText);
				bw.write(id + " " + taggedText);
				bw.newLine();
			}
			bw.close();
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
