package nl.tudelft.ewi.wis.test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.wikipedia.miner.comparison.ArticleComparer;
import org.wikipedia.miner.model.Article;
import org.wikipedia.miner.model.Label;
import org.wikipedia.miner.model.Wikipedia;

/*
 * test of the generated berkeleydb
 * 
 * code taken from: http://sourceforge.net/apps/mediawiki/wikipedia-miner/index.php?title=Building
 */

public class DatabaseTest {

	public static void main(String args[]) {
		try {
			
			if(args.length!=2) {
				System.err.println("Error: expect wikipedia-template.xml path as only argument!");
				System.exit(1);
			}
			
			Wikipedia w = new Wikipedia(new File(args[0]), false);

			String concept = args[1];
			Label lblConcept = w.getLabel(concept, null);
			System.out.println("\nSenses for "+concept+":");
			for (Label.Sense sense : lblConcept.getSenses())
				System.out.println("\t -" + sense.getTitle());

			Article artConcept = lblConcept.getSenses()[0];
			System.out.println(artConcept.getSentenceMarkup(0));

			System.out.println("\nSynonyms: ");
			for (Article.Label synConcept : artConcept.getLabels())
				System.out.println("\t -" + synConcept.getText());

			System.out.println("\n10 Translations: ");
			int counter = 0;
			TreeMap<String, String> trans = artConcept.getTranslations();
			for (String e : trans.keySet()) {
				System.out.println("\t -" + trans.get(e) + " (" + e + ")");
				counter++;
				if(counter>=10) {
					break;
				}
			}

			Article[] relatedTopics = artConcept.getLinksOut();
			ArticleComparer comparer = new ArticleComparer(w);
			for (Article rt : relatedTopics)
				rt.setWeight(comparer.getRelatedness(artConcept, rt));
			Arrays.sort(relatedTopics);

			System.out.println("\n10 Related Topics: ");
			counter=0;
			for (Article rt : relatedTopics) {
				System.out.println("\t -" + rt.getTitle());
				counter++;
				if(counter>=10) {
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
