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
package procope.methods.clustering;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;
import procope.tools.Tools;






/**
 * Calculates the quality of a clustering using a given scores network 
 * according to the method proposed in the PhD of Stijn van Dongen 
 * (for a reference see: {@link MarkovClusterer}).
 * 
 * <p>Basically the performance of a clusterings gets higher the more
 * high-scoring edges there are within the clusters.  Efficiency values
 * are between 0.0 and 1.0.
 * <p> Note that it might not make very much sense to evaluate a clustering 
 * with a scores network it was <u>not</u> derived from.
 * 
 * @author Jan Krumsiek
 */

public class MCLEfficiencyCalculator {


	/**
	 * Calculate performance of the clustering using a given complex network.
	 * Uses a value of 2.0 for the <i>r</i> parameter.
	 * 
	 * @param network scores network used for evaluation
	 * @param clustering clustering to be evaluated
	 * @return efficiency value between 0.0 and 1.0
	 */
	public static double calculateEfficiency(ProteinNetwork network, ComplexSet clustering) {
		return calculateEfficiency(network, clustering, 2.0f);
	}
	
	/**
	 * Calculate performance of the clustering using a given complex network.
	 * Requires a value for the <i>r</i> parameter. If you have no idea what
	 * that parameter is just call 
	 * {@link #calculateEfficiency(ProteinNetwork, ComplexSet)}.
	 * 
	 * @param network scores network used for evaluation
	 * @param clustering clustering to be evaluated
	 * @param value for the <i>r</i> parameter
	 * @return efficiency value between 0.0 and 1.0
	 */
	private static double calculateEfficiency(ProteinNetwork network, ComplexSet clustering, float r) {
		
		Set<Integer> proteins = network.getProteins();
		int maxID = Tools.findMax(proteins);
		
		// get a mapping of each protein to its cluster(s)
		HashMap<Integer, Complex> clustersForProteins = new HashMap<Integer, Complex>();
		for (Complex complex : clustering) {
			for (int protein : complex)
				clustersForProteins.put(protein, complex);
		}
		
		// sum up scores for all proteins
		double sum=0;
		for (int protein : proteins) {
			float cov = calculateCoverage(protein, network, clustersForProteins.get(protein), r, maxID);
			sum += cov; 
		}
		
		sum /= (float)proteins.size();

		return sum;
	}

	private static float calculateCoverage(int protein, ProteinNetwork network,
			Complex complex, float r, int maxID) {
		
		// get network partners for this protein as boolean array for efficiency
		float[] partnerScores = new float[maxID+1];
		Arrays.fill(partnerScores, Float.NaN);
		for (int partner : network.getNeighborArray(protein)) {
			float score = network.getEdge(protein, partner);
			if (score == score) // NaN check
				partnerScores[partner] = score; 
		}
		
		int sizeP = complex.size();
		int sizeP_and_S = complex.size();
		float sum=0;
		// iterate over all other proteins 
		for (int other : network.getNeighborArray(protein)) {
				sum += partnerScores[other];
				// also contained in complex?
				if (!complex.contains(other))
					sizeP_and_S++;
				
		}
		// if the protein has no self-edge: add another value to P u S
		
		// calculate relative frequencies
		float[] pi = new float[maxID+1];
		for (int other : network.getNeighborArray(protein)) {
			pi[other] = partnerScores[other] / sum;
		}
		
		// calculate rest whatever that is
		float pi_sum_div = 0;
		float ctr = ctr(pi, r);
		
		for (int i = 1; i < maxID+1; i++) {
			if (complex.contains(i)) {
				pi_sum_div += pi[i];
			} else {
				pi_sum_div -= pi[i];
			}
		}
		// ...
		
		float result = 1 - (sizeP - (pi_sum_div) / ctr) / sizeP_and_S;
		
		return result;
	}

	private static float ctr(float[] pi, float r) {
		float sum = 0;
		float value;
		for (int i = 0; i < pi.length; i++) {
			value = (float) Math.pow(pi[i], r);
			sum += value;

		}
		sum = (float) Math.pow(sum, 1 / (r - 1));
		
		return sum;
	}

}
