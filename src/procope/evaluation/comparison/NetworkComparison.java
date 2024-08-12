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
package procope.evaluation.comparison;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import procope.data.networks.ProteinNetwork;
import procope.tools.math.CorrelationCoefficient;


/**
 * Contains methods for comparing two {@link ProteinNetwork protein networks}.
 * <p>This class is not instantiatable.
 * 
 * @author Jan Krumsiek
 */

public class NetworkComparison {
	
	// avoid instantiation
	private NetworkComparison() {
	}
	

	/**
	 * Compares the weights of all edges of two networks. Each value pair
	 * in the result set stands for the two weights of the same edge in both
	 * networks. If a edge does not exist in one of the networks, the value
	 * 0 will be assumed.
	 * 
	 * @param scores1 network 1
	 * @param scores2 network 2
	 * @param excludeZeros do not include values where an edge does not exist
	 *                     in the other network or has a weight of 0
	 * @return list of edge weight pairs
	 * @see CorrelationCoefficient
	 */
	public static List<Point> weightsOverlap(ProteinNetwork scores1, ProteinNetwork scores2, boolean excludeZeros) {
		// javadoc: non-existing gewertet als 0

		List<Point> result = new ArrayList<Point>();
		
		// get combined set of proteins
		Set<Integer> allProteins = new HashSet<Integer>();
		allProteins.addAll(scores1.getProteins());
		allProteins.addAll(scores2.getProteins());
		
		// iterate over all pairwise proteins
		Integer[] arrProts = allProteins.toArray(new Integer[0]);
		for (int i=0; i<arrProts.length; i++) {
			for (int j=i+1; j<arrProts.length; j++) {
				// get scores from both networks
				float score1 = scores1.getEdge(arrProts[i], arrProts[j]);
				float score2 = scores2.getEdge(arrProts[i], arrProts[j]);
				// treat non-existing (NaN) as zero
				if (score1 != score1) score1 = 0;
				if (score2 != score2) score2 = 0;
				// add point
				if (excludeZeros) {
					if (score1 != 0 && score2 != 0)
						result.add(new Point(score1,score2));
				} else {
					if (score1 != 0 || score2 != 0)
						result.add(new Point(score1,score2));
				}
			}
		}
		
		return result;
	}

}
