/*
 *    TopicDetector.java
 *    Copyright (C) 2007 David Milne, d.n.milne@gmail.com
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */


package org.wikipedia.miner.annotation;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.wikipedia.miner.model.*;
import org.wikipedia.miner.model.Page.PageType;
import org.wikipedia.miner.util.*;
import org.wikipedia.miner.annotation.preprocessing.*;

/**
 * This class detects topics that occur in plain text, using Disambiguator to resolve ambiguous terms and phrases. 
 * Many of the detected topics will be rubbish (extracted from unhelpful terms, such as <em>and</em> or <em>the</em>, so you will probably want to use either a LinkDetector or 
 * some simple heuristics to weed out the least useful ones (see Topic for the features that are available for separating important topics from less helpful ones). 
 * <p>
 * This also doesn't resolve collisions (e.g. "united states" collides with "states of america" in "united states of america"). 
 * The DocumentTagger has methods to resolve these.
 * 
 *  @author David Milne 
 */
public class TopicDetector {
	
	private Wikipedia wikipedia ;
	//private SentenceSplitter ss; 
	private Disambiguator disambiguator ;
	//private HashSet<String> stopwords ;
	
	
	private boolean strictDisambiguation ;
	private boolean allowDisambiguations ;
	
	private int maxTopicsForRelatedness = 25 ;
	
	
	
	
	/**
	 * Initializes a new topic detector.
	 * 
	 * @param wikipedia an initialized instance of Wikipedia
	 * @param disambiguator a trained 
	 * @param stopwordFile an optional (may be null) file containing 
	 * @param strictDisambiguation
	 * @param allowDisambiguations 
	 * @throws IOException 
	 */
	public TopicDetector(Wikipedia wikipedia, Disambiguator disambiguator, boolean strictDisambiguation, boolean allowDisambiguations) throws IOException {
		this.wikipedia = wikipedia ;
		this.disambiguator = disambiguator ;
		
		this.strictDisambiguation = strictDisambiguation ;
		this.allowDisambiguations = allowDisambiguations ;
		
		/*
		this.stopwords = new HashSet<String>() ;
		if (stopwordFile != null) {
			BufferedReader input = new BufferedReader(new FileReader(stopwordFile)) ;
			String line ;
			while ((line=input.readLine()) != null) 
				stopwords.add(line.trim()) ;			
		}*/
		
		//TODO:Check caching 
		/*
		if (!wikipedia.getEnvironment().isGeneralityCached()) 
			System.err.println("TopicDetector | Warning: generality has not been cached, so this will run significantly slower than it needs to.") ;
		*/	
			
	}
	
	/**
	 * Gathers a collection of topics from the given document. 
	 * 
	 * @param doc a document that has been preprocessed so that markup (html, mediawiki, etc) is safely ignored.
	 * @param rc a cache in which relatedness measures will be saved so they aren't repeatedly calculated. This may be null.  
	 * @return a vector of topics that were mined from the document.
	 * @throws Exception
	 */
	public Vector<Topic> getTopics(PreprocessedDocument doc, RelatednessCache rc) throws Exception {
		
		if (rc == null)
			rc = new RelatednessCache(disambiguator.getArticleComparer()) ;
		

		//Vector<String> sentences = ss.getSentences(doc.getPreprocessedText(), SentenceSplitter.MULTIPLE_NEWLINES) ;
		Vector<TopicReference> references = getReferences(doc.getPreprocessedText()) ;
		
		Collection<Topic> temp = getTopics(references, doc.getContextText(), doc.getOriginalText().length(), rc).values() ;
		calculateRelatedness(temp, rc) ;

		Vector<Topic> topics = new Vector<Topic>() ;
		for (Topic t:temp) {
			if (!doc.isTopicBanned(t.getId())) 
					topics.add(t) ;
		}
		
		return topics ;
	}
	
	/**
	 * Gathers a collection of topics from the given document. 
	 * 
	 * @param text text to mine topics from. This must be plain text, without any form of markup. 
	 * @param rc a cache in which relatedness measures will be saved so they aren't repeatedly calculated. This may be null. 
	 * @return a collection of topics that were mined from the document.
	 * @throws Exception
	 */
	public Collection<Topic> getTopics(String text, RelatednessCache rc) throws Exception {
		
		if (rc == null)
			rc = new RelatednessCache(disambiguator.getArticleComparer()) ;
			

		//Vector<String> sentences = ss.getSentences(text, SentenceSplitter.MULTIPLE_NEWLINES) ;
		Vector<TopicReference> references = getReferences(text) ;
		
		HashMap<Integer,Topic> topicsById = getTopics(references, "", text.length(), rc) ;

		Collection<Topic> topics = topicsById.values() ;
		calculateRelatedness(topics, rc) ;
		
		return topics ;
	}
	
	private void calculateRelatedness(Collection<Topic> topics, RelatednessCache cache) throws Exception{
		
		TreeSet<Article> weightedTopics = new TreeSet<Article>() ;
		
		for (Topic t:topics) {
			if (t.getType() != PageType.article)
				continue ;
			
			Article art = (Article)wikipedia.getPageById(t.getId()) ;
			
			art.setWeight(t.getAverageLinkProbability() * t.getOccurances()) ;
			weightedTopics.add(art) ;
		}
		
		for (Topic topic: topics) {
			
			double totalWeight = 0 ;
			double totalWeightedRelatedness = 0 ;
			
			int count = 0 ;
			
			for (Article art: weightedTopics) {
				if (count++ > maxTopicsForRelatedness)
					break ;
				
				double weightedRelatedness = art.getWeight() * cache.getRelatedness(topic, art) ;
				
				totalWeight = totalWeight + art.getWeight();
				totalWeightedRelatedness = totalWeightedRelatedness + weightedRelatedness;
				
			}
			
			topic.setRelatednessToOtherTopics((float)(totalWeightedRelatedness/totalWeight)) ;
		}
	}
	
	
	
	
	private Vector<TopicReference> getReferences(String text) {

		Vector<TopicReference> references = new Vector<TopicReference>() ;
		//int sentenceStart = 0 ;

		//ProgressDisplayer pd = new ProgressDisplayer(" - gathering candidate links", sentences.length, 0.1) ;

		//for (String sentence:sentences) {
			String s = "$ " + text + " $" ;
			//pd.update() ;

			Pattern p = Pattern.compile("[\\s\\{\\}\\(\\)\"\'\\.\\,\\;\\:\\-\\_]") ;  //would just match all non-word chars, but we don't want to match utf chars
			Matcher m = p.matcher(s) ;

			Vector<Integer> matchIndexes = new Vector<Integer>() ;

			while (m.find()) 
				matchIndexes.add(m.start()) ;

			for (int i=0 ; i<matchIndexes.size() ; i++) {

				int startIndex = matchIndexes.elementAt(i) + 1 ;
				
				if (Character.isWhitespace(s.charAt(startIndex))) 
					continue ;

				for (int j=Math.min(i + disambiguator.getMaxLabelLength(), matchIndexes.size()-1) ; j > i ; j--) {
					int currIndex = matchIndexes.elementAt(j) ;	
					String ngram = s.substring(startIndex, currIndex) ;

					if (! (ngram.length()==1 && s.substring(startIndex-1, startIndex).equals("'"))&& !ngram.trim().equals("") && !wikipedia.getConfig().isStopword(ngram)) {
						
						//TODO: test if we need escapes here
						Label label = new Label(wikipedia.getEnvironment(), ngram, disambiguator.getTextProcessor()) ;

						if (label.exists() && label.getLinkProbability() >= disambiguator.getMinLinkProbability()) {
							Position pos = new Position(startIndex-2, currIndex-2) ;
							TopicReference ref = new TopicReference(label, pos) ;
							references.add(ref) ;
							
							//System.out.println(" - ref: " + ngram + label.getLinkProbability()) ;
						}
					}
				}
			}
			//sentenceStart = sentenceStart + sentence.length() ;
		//}
		return references ;
	}
	
	private HashMap<Integer,Topic> getTopics(Vector<TopicReference> references, String contextText, int docLength, RelatednessCache cache) throws Exception{
		HashMap<Integer,Topic> chosenTopics = new HashMap<Integer,Topic>() ;
	
		// get context articles from unambiguous Labels
		Vector<Label> unambigLabels = new Vector<Label>() ;
		for (TopicReference ref:references) {
			Label label = ref.getLabel() ;
			
			Label.Sense[] senses = label.getSenses() ;
			if (senses.length > 0) {				
				if (senses.length == 1 || senses[0].getPriorProbability() > 1-disambiguator.getMinSenseProbability())
					unambigLabels.add(label) ;	
			}		
		}
		
		//get context articles from additional context text
		//Vector<String> contextSentences = ss.getSentences(, SentenceSplitter.MULTIPLE_NEWLINES) ; 
		for (TopicReference ref:getReferences(contextText)){
			Label label = ref.getLabel() ;
			Label.Sense[] senses = label.getSenses() ;
			if (senses.length > 0) {
				if (senses.length == 1 || senses[0].getPriorProbability() > 1-disambiguator.getMinSenseProbability()) {
					unambigLabels.add(label) ;	
				}
			}
		}
		
		Context context ;
		if (cache == null)
			context = new Context(unambigLabels, new RelatednessCache(disambiguator.getArticleComparer()), disambiguator.getMaxContextSize()) ;
		
		else 
			context = new Context(unambigLabels, cache, disambiguator.getMaxContextSize()) ;	
		unambigLabels = null ;

		//now disambiguate all references
		//unambig references are still processed here, because we need to calculate relatedness to context anyway.
		
		// build a cache of valid senses for each phrase, since the same phrase may occur more than once, but will always be disambiguated the same way
		HashMap<String, ArrayList<CachedSense>> disambigCache = new HashMap<String, ArrayList<CachedSense>>() ;

		for (TopicReference ref:references) {
			//System.out.println("disambiguating ref: " + ref.getLabel().getText()) ;

			ArrayList<CachedSense> validSenses = disambigCache.get(ref.getLabel().getText()) ;

			if (validSenses == null) {
				// we havent seen this label in this document before
				validSenses = new ArrayList<CachedSense>() ;

				for (Label.Sense sense: ref.getLabel().getSenses()) {
					
					if (sense.getPriorProbability() < disambiguator.getMinSenseProbability()) break ;
					
					if (!allowDisambiguations && sense.getType() == PageType.disambiguation)
						continue ;

					double relatedness = context.getRelatednessTo(sense) ;
					double commonness = sense.getPriorProbability() ;

					double disambigProb = disambiguator.getProbabilityOfSense(commonness, relatedness, context) ;

					//System.out.println(" - sense " + sense + ", " + disambigProb) ;
					
					if (disambigProb > 0.1) {
						// there is at least a chance that this is a valid sense for the link (there may be more than one)
						
						CachedSense vs = new CachedSense(sense.getId(), commonness, relatedness, disambigProb) ;
						validSenses.add(vs) ;
					}
				}
				Collections.sort(validSenses) ;
				
				
				disambigCache.put(ref.getLabel().getText(), validSenses) ;
			}

			if (strictDisambiguation) {
				//just get top sense
				if (!validSenses.isEmpty()) {
					CachedSense sense = validSenses.get(0) ;
					Topic topic = chosenTopics.get(sense.id) ;
	
					if (topic == null) {
						// we havent seen this topic before
						topic = new Topic(wikipedia, sense.id, sense.relatedness, docLength) ;
						chosenTopics.put(sense.id, topic) ;
					}
					topic.addReference(ref, sense.disambigConfidence) ;
				}
			} else {
				//get all senses
				for (CachedSense sense: validSenses) {
					Topic topic = chosenTopics.get(sense.id) ;

					if (topic == null) {
						// we haven't seen this topic before
						topic = new Topic(wikipedia, sense.id, sense.relatedness, docLength) ;
						chosenTopics.put(sense.id, topic) ;
					}
					topic.addReference(ref, sense.disambigConfidence) ;
				}
			}
		}
		
		
		return chosenTopics ;
	}
	

	
	

	private class CachedSense implements Comparable<CachedSense>{
		
		int id ;
		double commonness ;
		double relatedness ;
		double disambigConfidence ;

		/**
		 * Initializes a new CachedSense
		 * 
		 * @param id the id of the article that represents this sense
		 * @param commonness the prior probability of this sense given a source ngram (label)
		 * @param relatedness the relatedness of this sense to the surrounding unambiguous topics
		 * @param disambigConfidence the probability that this sense is valid, as defined by the disambiguator.
		 */
		public CachedSense(int id, double commonness, double relatedness, double disambigConfidence) {
			this.id = id ;
			this.commonness = commonness ;
			this.relatedness = relatedness ;
			this.disambigConfidence = disambigConfidence ;			
		}
		
		public int compareTo(CachedSense sense) {
			return -1 * Double.valueOf(disambigConfidence).compareTo(Double.valueOf(sense.disambigConfidence)) ;
		}
	}
}
