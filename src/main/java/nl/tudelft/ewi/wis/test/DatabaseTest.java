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
			Wikipedia w = new Wikipedia(new File(args[0]), false);

			Label lblDog = w.getLabel("Dog", null);
			System.out.println("Senses for Dog:");
			for (Label.Sense sense : lblDog.getSenses())
				System.out.println(" -" + sense.getTitle());

			Article artDog = lblDog.getSenses()[0];
			System.out.println(artDog.getSentenceMarkup(0));

			System.out.println("Synonyms: ");
			for (Article.Label synDog : artDog.getLabels())
				System.out.println(" -" + synDog.getText());

			System.out.println("Translations: ");
			TreeMap<String, String> trans = artDog.getTranslations();
			for (String e : trans.keySet())
				System.out.println(" -" + trans.get(e) + " (" + e + ")");

			Article[] relatedTopics = artDog.getLinksOut();
			ArticleComparer comparer = new ArticleComparer(w);
			for (Article rt : relatedTopics)
				rt.setWeight(comparer.getRelatedness(artDog, rt));
			Arrays.sort(relatedTopics);

			System.out.println("Related Topics: ");
			for (Article rt : relatedTopics)
				System.out.println(" -" + rt.getTitle());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
