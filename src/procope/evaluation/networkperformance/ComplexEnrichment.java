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
package procope.evaluation.networkperformance;

import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;
import procope.methods.scores.ComplexScoreCalculator;

/**
 * Contains one static method to calculate the complex enrichment score of
 * a network with respect to a complex set. 
 * 
 * @author Jan Krumsiek
 */
public class ComplexEnrichment {
	
	/**
	 * Calculates the complex enrichment of a given network with respect to
	 * a given complex set. The complex enrichment score is the quotient of
	 * the average complex score of the reference complex set and a randomized 
	 * copy of that reference set. The higher this value the more high-scoring
	 * edges the network contains within the complexes. Note that multiple calls 
	 * of this function will cause slighty different results. 
	 * 
	 * @param network the network for which to calculate to complex enrichment
	 * @param reference reference complex set
	 * @param numrand number of randomizations, the average of all runs will be taken;
	 *                this parameter is used to minimize variation in the results
	 * @param useWeightedScores calculate weighted complex set average score? 
	 * @return the complex enrichment of that score with respect to the given complex set
	 *         or 0 if no inner-complex edge has a nonzero value
	 */
	public static float calculateComplexEnrichment(ProteinNetwork network, ComplexSet reference, 
			int numrand, boolean useWeightedScores) {
		// get average complex score for this set
		float avgScore = ComplexScoreCalculator.averageComplexSetScore(network, reference, useWeightedScores);
		// get randomized set
		float totalAvg=0;
		for (int i=0; i<numrand; i++) {
			ComplexSet randomized = reference.randomizeByExchanging();
			float avgRandScore = ComplexScoreCalculator.averageComplexSetScore(network, randomized, useWeightedScores);
			totalAvg += avgRandScore;
		}
		// return enrichment factor
		if (totalAvg == 0)
			totalAvg= Float.MIN_VALUE;
		return avgScore / (totalAvg/(float)numrand);
		
	}

}
