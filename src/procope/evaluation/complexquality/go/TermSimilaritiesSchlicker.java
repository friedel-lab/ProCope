/*
 * This file is part of ProCope
 *
 * ProCope is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProCope is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ProCope.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2008, Teaching and Research Unit Bioinformatics, LMU Munich
 * http://www.bio.ifi.lmu.de/Complexes/ProCope/
 *
 */
package procope.evaluation.complexquality.go;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import procope.tools.Tools;



/**
 * Implements the calculation of similarities between two GO terms as described in
 * <p>Schlicker, A.; Domingues, F. S.; Rahnenf&uuml;hrer, J. & Lengauer, T.<br/>
 * A new measure for functional similarity of gene products based on Gene Ontology.<br/>
 * <i>BMC Bioinformatics</i>, 2006, 7, 302<br/>
 * Pubmed: 16776819
 * 
 * @author Jan Krumsiek
 */

public class TermSimilaritiesSchlicker implements TermSimilarities {
	
	private GONetwork gonet;
//	private GOTerm root;
	private TermSimilarityMeasure similarityMeasure;
	private boolean caching; 
	private HashMap<String, Float> simCache;
	private CommonAncestors commonAncestors;
	private GOAnnotations annotations;
	
	private HashMap<String, Integer> absFrequencies;	// mapping ID => absolute frequencies
	
	/**
	 * Creates a new term similarity calculator.
	 * 
	 * @param gonet the go network on which similarities will be calculated
	 * @param annotations the annotations needed for term frequency calculation
	 * @param similarityMeasure the similarity measure to be used
	 * @param caching enables or disables the caching of similarity scores to 
	 *        avoid multiple calculation of the same scores
	 */
	public TermSimilaritiesSchlicker(GONetwork gonet, GOAnnotations annotations, 
			TermSimilarityMeasure similarityMeasure, boolean caching) {
		// store values
		this.gonet = gonet;
		this.similarityMeasure = similarityMeasure;
		this.caching = caching;
		this.commonAncestors = new CommonAncestors(gonet); 
		if (caching) simCache = new HashMap<String, Float>();
		this.annotations =  annotations;
		
		// take care of probabilities
		absFrequencies = new HashMap<String, Integer>();
		calculateProbabilities();
	}
	

	/**
	 * Calculates the similarity of two given GO terms. If both terms are the
	 * root term of the respective network, this method will return 0.
	 */
	public float calculateSimilarity(String term1ID, String term2ID) {
		
		String key = Tools.min(term1ID, term2ID) + "#" + Tools.max(term1ID, term2ID); 
		
		Float cachedSim=null;
		// cached?
		if (caching && ( cachedSim = simCache.get(key) ) != null) {
			return cachedSim;
		} else {
			
			// get actual GOTerm objects
			GOTerm term1 = gonet.getTerm(term1ID);
			GOTerm term2 = gonet.getTerm(term2ID);
			
			if (term1 == null || term2 == null || (term1.prob == 1 && term2.prob == 1))
				return 0;
			
			// switch between different similarity measures
			float sim=Float.NaN;
			if (similarityMeasure == TermSimilarityMeasure.RESNIK)
				sim = resnikSimilarity(term1, term2);
			else if (similarityMeasure == TermSimilarityMeasure.LIN)
				sim = linSimilarity(term1, term2);
			else // relevance similarity
				sim = relevanceSimilarity(term1, term2);
			
			// cache?
			if (caching) {
				// generate hash key
					simCache.put(key, sim);
			}
			
			return sim;
		}
	}


	/**
	 * Calculate term similarity based on "Resnik"
	 */
	private float resnikSimilarity(GOTerm term1, GOTerm term2) {
		// calc common ancestors
		Collection<GOTerm> commonAncestors = getCommonAncestors(term1, term2);
		
		// iterate over common ancestors c and find max (-log(p(c))
		float max = Float.NEGATIVE_INFINITY;
		for (GOTerm ancestor : commonAncestors) {
			float logSim = (float)(-Math.log(ancestor.prob));
			if (logSim > max) max = logSim;
		}
		
		return max;
	}
	
	/**
	 * Calculate term similarity based on "Lin"
	 */
	private float linSimilarity(GOTerm term1, GOTerm term2)  {
		// calc common ancestors
		Collection<GOTerm> commonAncestors = getCommonAncestors(term1, term2);	// iterate over common ancestors c and find
		
		// max (2*log p(c) /  (log p(c1) + log p(c2) )   )
		float max = Float.NEGATIVE_INFINITY;
		for (GOTerm ancestor : commonAncestors) {
			
			float logSim = (float) (
				2 * Math.log(ancestor.prob) 
				/ 
				(Math.log(term1.prob) + Math.log(term2.prob))
				);
			if (logSim > max) max = logSim;
			
			if ( (Math.log(term1.prob) + Math.log(term2.prob)) == 0) {
			}
		}
		
		return max;
	}

	/**
	 * Calculate term similarity based on "Relevance"
	 */
	private float relevanceSimilarity(GOTerm term1, GOTerm term2)  {
		// calc common ancestors
		Collection<GOTerm> commonAncestors = getCommonAncestors(term1, term2);	// iterate over common ancestors c and find 
		// max (2*log p(c) /  (log p(c1) + log p(c2) )   )
		float max = Float.NEGATIVE_INFINITY;
		for (GOTerm ancestor : commonAncestors) {
			float logSim = (float) (
				2 * Math.log(ancestor.prob) 
				/ 
				(Math.log(term1.prob) + Math.log(term2.prob))
				*
				(1-ancestor.prob)
				);
                       
			if (logSim > max) max = logSim;
		}
		
		return max;
	}
	
	/**
	 * Retrieves the common ancestors of two GOTerms and returns them
	 * as a collection of GOTerm objects
	 */
	private Collection<GOTerm> getCommonAncestors(GOTerm term1, GOTerm term2) {
		Collection<String> ancestorIDs = 
			commonAncestors.getCommonAncestors(term1.ID, term2.ID);
		// iterate over string ids and get go term from map
		Collection<GOTerm> terms = new ArrayList<GOTerm>();
		for (String termID : ancestorIDs)
			terms.add(gonet.getTerm(termID));
		
		return terms;
		
	}
	
	/**
	 * The similarity to be used. Consult 
	 * {@link TermSimilaritiesSchlicker the paper} for more information.
	 */
	public enum TermSimilarityMeasure {
		/**
		 * Resnik's measure
		 */
		RESNIK, 
		/**
		 * Lin's measure
		 */
		LIN, 
		/**
		 * Relevance similarity
		 */
		RELEVANCE
	}
	
	
	/**
	 * Calculate the probabilities of all GO terms according to the annotations
	 */
	private void calculateProbabilities() {
		// do a depth-first search to determine all probabilities
		absFrequencies = new HashMap<String, Integer>();
		// get root term
		HashMap<String, GOTerm> goterms = gonet.goterms;
		GOTerm root = gonet.getRoot();

		// start recursive search with this root term
		recFrequencyCalculation(root);

		// get root frequency, then iterate over all terms and calcualate
		// probability
		float rootFreq = (float) absFrequencies.get(root.ID);

		for (String key : absFrequencies.keySet()) {
			// calculate probability as relative frequency
			float prob = (float) absFrequencies.get(key) / rootFreq;
			// assign to corresponding term
			goterms.get(key).prob = prob;


		}

	}
	
	/**
	 * Recursive function for frequency calculation
	 */
	private int recFrequencyCalculation(GOTerm currentNode) {
		
		int totalFreq  = -1;
		
		// value already calculated?
		Integer cachedFreq=null;
		if ( (cachedFreq = absFrequencies.get(currentNode.ID)) != null) {	
			// use cached value
                       
			totalFreq = cachedFreq.intValue();
		} else {
			// need to calculate it
			
			// iterate over children
			int childrenSum=0;
			for (GOTerm child : currentNode.children)
				childrenSum += recFrequencyCalculation(child);
			// calculate and add frequency for this item
			totalFreq = annotations.getAnnotationCount(currentNode.ID) + childrenSum;
			// cache this node
			absFrequencies.put(currentNode.ID, totalFreq);
		}
		
		return totalFreq; 
		
	}
	
}
