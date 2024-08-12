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
package procope.methods.scores;

import java.util.List;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;


/**
 * Contains static methods to calculate scores of complexes using a given 
 * scores network. The score of a single complex is calculated by averaging
 * the weight of all protein-protein edges within a complex. For a complex
 * containing <i>n</i> proteins this means <i>n*(n-1)/2</i> edges are read
 * from the network and averaged. Missing edges in the network are generally
 * treated as having a weight of zero.
 * <p>This class is not instantiatable.
 * 
 * @author Jan Krumsiek
 */

public class ComplexScoreCalculator {

	// avoid instantiation
	private ComplexScoreCalculator() {
	}
	
	/**
	 * Calculates the score of a complex using a given scores network. The score
	 * is calculated by averaging the weight of all inner-complex edges
	 * (<i>n*(n-1)/2</i> edges for a complex of <i>n</i> proteins).
	 * Missing scores in the network implicitly get a weight of zero.
	 * 
	 * @param scoreNetwork scores network to be used
	 * @param complex complex for which the score will be calculated
	 * @return score of that complex with respect to the given network
	 */
	public static float averageComplexScore(ProteinNetwork scoreNetwork, Complex complex) {
		return averageComplexScore(scoreNetwork, complex, false);
	}
	
	/**
	 * Calculates the score of a complex using a given scores calculator. The score
	 * is calculated by averaging the weight of all inner-complex edges
	 * (<i>n*(n-1)/2</i> edges for a complex of <i>n</i> proteins).
	 * Missing scores in the network implicitly get a weight of zero.
	 * 
	 * @param scoreCalc scores calculator to be used
	 * @param complex complex for which the score will be calculated
	 * @return score of that complex with respect to the given network
	 */
	public static float averageComplexScore(ScoresCalculator scoreCalc, Complex complex) {
		return averageComplexScore(scoreCalc, complex, false);
	}
	
	/**
	 * Calculates the score of a complex using a given scores network. The score
	 * is calculated by averaging the weight of all inner-complex edges
	 * (<i>n*(n-1)/2</i> edges for a complex of <i>n</i> proteins).
	 * 
	 * @param scoreNetwork scores network to be used
	 * @param complex complex for which the score will be calculated
	 * @param ignoreMissingScores If {@code true} then missing scores in the 
	 *          network will not get an implicit weight of zero, but will be
	 *          completely ignored in the calculation. <b>Note:</b> When
	 *          missing scores are ignored the method will return NaN if none
	 *          of the edges in the complex has a weight in the network.
	 * @return score of that complex with respect to the given network
	 */
	public static float averageComplexScore(ProteinNetwork scoreNetwork, 
			Complex complex, boolean ignoreMissingScores) {
	
		if (scoreNetwork.isDirected())
			System.err.println("Warning: Calculating complex scores " +
					"on directed networks may yield unexpected results.");
		
		// get vector backing the complex, convert to array
		List<Integer> vec = complex.getComplex();
		Integer[] arr = vec.toArray(new Integer[0]);
		final int prots = arr.length;
		
		// iterate over all pairwise members
		float total=0;
		int count=0;
		for (int i=0; i<prots; i++) {
			for (int j=i+1; j<prots; j++) {
				float score = scoreNetwork.getEdge(arr[i], arr[j]);
				// handle NaN, if not ignored => increase count
				if (score != score) { // NaN check
					if (!ignoreMissingScores) count++;
				} else {
					total += score;
					count++;
				}
			}
		}
		
		return total/(float)count;
	}
	
	/**
	 * Calculates the score of a complex using a given scores calculator. The score
	 * is calculated by averaging the weight of all inner-complex edges
	 * (<i>n*(n-1)/2</i> edges for a complex of <i>n</i> proteins).
	 * 
	 * @param scoreCalc scores calculator to be used
	 * @param complex complex for which the score will be calculated
	 * @param ignoreMissingScores If {@code true} then missing scores in the 
	 *          network will not get an implicit weight of zero, but will be
	 *          completely ignored in the calculation. <b>Note:</b> When
	 *          missing scores are ignored the method will return NaN if none
	 *          of the edges in the complex has a weight in the network.
	 * @return score of that complex with respect to the given network
	 */
	public static float averageComplexScore(ScoresCalculator scoreCalc, 
			Complex complex, boolean ignoreMissingScores) {
		
		// get vector backing the complex, convert to array
		List<Integer> vec = complex.getComplex();
		Integer[] arr = vec.toArray(new Integer[0]);
		final int prots = arr.length;
		
		// iterate over all pairwise members
		float total=0;
		int count=0;
		for (int i=0; i<prots; i++) {
			for (int j=i+1; j<prots; j++) {
				float score = scoreCalc.getScore(arr[i], arr[j]);
				// handle NaN, if not ignored => increase count
				if (score==0 || score != score) {  
					if (!ignoreMissingScores) count++;
				} else {
					total += score;
					count++;
				}
				
			}
		}
		return total/(float)count;
	}
	
	/**
	 * Calculates the average complex score of a given complex set.
	 * 
	 * @param scoreNetwork scores network to be used
	 * @param complexSet complex set for which the average is calculated
	 * @param weighted weight scores by complex size?
	 * @return average complex score for the given complex set
	 */
	public static float averageComplexSetScore(ProteinNetwork scoreNetwork, 
			ComplexSet complexSet, boolean weighted) {
		return averageComplexSetScore(scoreNetwork, complexSet, weighted, false);
	}
	
	/**
	 * Calculates the average complex score of a given complex set.
	 * 
	 * @param scoreCalc scores calculator to be used
	 * @param complexSet complex set for which the average is calculated
	 * @param weighted weight scores by complex size?
	 * @return average complex score for the given complex set
	 */
	public static float averageComplexSetScore(ScoresCalculator scoreCalc, 
			ComplexSet complexSet, boolean weighted) {
		return averageComplexSetScore(scoreCalc, complexSet, weighted, false);
	}
	
	/**
	 * Calculates the average complex score of a given complex set.
	 * 
	 * @param scoreNetwork scores calculator to be used
	 * @param complexSet complex set for which the average is calculated
	 * @param weighted weight scores by complex size?
	 * @param ignoreMissingScores Ignore missing scores in the complex score
	 *        calculation? NaNs will be treated as zero. (see also:
	 *        {@link #averageComplexScore(ProteinNetwork, Complex, boolean)})
	 * @return average complex score for the given complex set
	 */
	public static float averageComplexSetScore(ProteinNetwork scoreNetwork, 
			ComplexSet complexSet, boolean weighted, boolean ignoreMissingScores) {
		
		float total=0, count=0;
		// iterate over complexes
		for (Complex complex : complexSet) {
			// calc average score for that complex
			float avg = averageComplexScore(scoreNetwork, complex, ignoreMissingScores);
			if (avg != avg) avg = 0;  // NaN check
			// increase weighted or unweighted counter
			if (weighted) {
				// weighted
				total += avg * (float)complex.size();
				count += complex.size();
			} else {
				// unweighted
				total += avg;
				count += 1f;
				
			}
		}
		
		// return average
		return total / count;
	}
	
	/**
	 * Calculates the average complex score of a given complex set.
	 * 
	 * @param scoreCalc scores calculator to be used
	 * @param complexSet complex set for which the average is calculated
	 * @param weighted weight scores by complex size?
	 * @param ignoreMissingScores Ignore missing scores in the complex score
	 *        calculation? NaNs will be treated as zero. (see also:
	 *        {@link #averageComplexScore(ProteinNetwork, Complex, boolean)})
	 * @return average complex score for the given complex set
	 */
	public static float averageComplexSetScore(ScoresCalculator scoreCalc, 
			ComplexSet complexSet, boolean weighted, boolean ignoreMissingScores) {
		
		float total=0, count=0;
		// iterate over complexes
		for (Complex complex : complexSet) {
			// calc average score for that complex
			float avg = averageComplexScore(scoreCalc, complex, ignoreMissingScores);
			if (avg != avg) avg = 0; // NaN check
			if (!ignoreMissingScores || avg > 0) {
				// increase weighted or unweighted counter
				if (weighted) {
					// weighted
					total += avg * (float)complex.size();
					count += complex.size();
				} else {
					// unweighted
					total += avg;
					count += 1f;
				}
			}
		}
		
		// return average
		return total / count;
	}
	
}
