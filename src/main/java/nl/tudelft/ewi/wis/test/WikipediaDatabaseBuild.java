package nl.tudelft.ewi.wis.test;

import java.io.File;

import org.wikipedia.miner.model.Wikipedia;

public class WikipediaDatabaseBuild {

	public static void main(String args[]) {
		try {
			Wikipedia w = new Wikipedia(new File("/local/wikipediaDumps/20120802/wikipedia-template.xml"), false) ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
