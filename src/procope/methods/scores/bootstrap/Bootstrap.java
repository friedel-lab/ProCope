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
package procope.methods.scores.bootstrap;

import java.util.ArrayList;
import java.util.Collection;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;


/**
 * Contains helper methods for the calculation of protein interaction scores 
 * using the <i>Bootstrap approach</i> according to
 * 
 * <p>Caroline C. Friedel, Jan Krumsiek, Ralf Zimmer<br/>
 * Bootstrapping the Interactome: Unsupervised Identification of Protein Complexes in Yeast.<br/>
 * <i>RECOMB 2008</i>, LNBI 4955, pp. 3-16. 
 * <p>This class is not instantiatable.
 */
public class Bootstrap {

	// avoid instantiation
	private Bootstrap() {
	}
	
		
	/**
	 * Extracts all clusterings at a given index of the clustering lists.
	 * 
	 * @param clusteringLists set of clusterings
	 * @param optimalIndex index in each clustering list to be extracted 
	 * @return set of clusterings with the given inflation coefficient
	 */
	public static ArrayList<ComplexSet> extractOptimalClusterings(
			ArrayList<BootstrapClusterings> clusteringLists, int optimalIndex) {
		
		ArrayList<ComplexSet> result = new ArrayList<ComplexSet>();
		
		// iterate over clustering lists
		for (BootstrapClusterings clusterings : clusteringLists) {
			result.add(clusterings.getClustering(optimalIndex).getClustering());
		}
		
		return result;
		
	}
	
	/**
	 * Creates the bootstrap network for a given set of clusterings. The weights
	 * of the edges in this network represent the frequency of cooccurence of two
	 * proteins in the given set of clusterings. For instance, if two proteins
	 * appear in the same complex in 60% of the clusterings, their edge gets a
	 * weight of 0.6
	 * 
	 * @param clusterings set of clusterings for which the boostrap network is calculated
	 * @return bootstrap network for the given clusterings
	 */
	public static ProteinNetwork createBootstrapNetwork(Collection<ComplexSet> clusterings) {
		
		ProteinNetwork btnet = new ProteinNetwork(false);

		// iterate over all complex sets
		for (ComplexSet set : clusterings) {
			// for each complex set we need a helper network so each protein pair is only counted once
			ProteinNetwork setEdges = new ProteinNetwork(false);
			// iterate over all complexes
			for (Complex complex : set) {
				// iterate over all pairwise edges
				Integer[] proteins = complex.getComplex().toArray(
						new Integer[0]);
				for (int i = 0; i < proteins.length; i++) {
					for (int j = i + 1; j < proteins.length; j++) {
						// only count this edge if we havent counted it yet for this complex set
						if (Float.isNaN(setEdges.getEdge(proteins[i], proteins[j]))) {
							// increase edge
							float score = btnet.getEdge(proteins[i], proteins[j]);
							if (score != score) // NaN check
								score = 1;
							else
								score++;
							btnet.setEdge(proteins[i], proteins[j], score);
							// mark edge for this complexset
							setEdges.setEdge(proteins[i], proteins[j]);
						}
					}
				}
			}
		}

		// normalize
		float numSets = clusterings.size();
		int[] edges = btnet.getEdgesArray();
		for (int i = 0; i < edges.length; i += 2) {
			btnet.setEdge(edges[i], edges[i + 1], btnet.getEdge(edges[i], edges[i + 1]) / numSets);
		}

		return btnet;
	}



	/**
	 * Finds the index of the clusterings with the best average efficiency in
	 * the set. Note that all clustering lists must contain the same number
	 * of clusterings produced by the same parameters.
	 * <p>Note: The clustering objects do not have to contain actual clusterings,
	 * inflation coefficient and efficiency are enough. See also:
	 * {@link BootstrapClusterings#BootstrapClusterings(String, boolean)}
	 *  
	 * @param clusteringsOnlyEff clusterings list from which the best inflation 
	 *                           coeffient is determined
	 * @return index of clusterings with best average efficiency
	 */
	public static int findBestIndex(ArrayList<BootstrapClusterings> clusteringsOnlyEff) {
		
//		System.out.println("#"+ clusteringsOnlyEff.size());
//		System.out.println("@" + clusteringsOnlyEff.get(0).getClusterings().size());
		
		// iterate over all clusterings
		int count=clusteringsOnlyEff.iterator().next().getClusterings().size();
		float[] totalEffs = new float[count];
		for (BootstrapClusterings clusterings : clusteringsOnlyEff) {
			// do the counting
			int index=0;
			for (BootstrapClustering clustering : clusterings) {
				float efficiency = clustering.getEfficiency();
				totalEffs[index] += efficiency;
				index++;
			}
		}
		
		// determine best inflation
		float max=Float.NEGATIVE_INFINITY;
		int maxIndex=-1;
		for (int i=0; i<count; i++) {
			if (totalEffs[i] > max) {
				max = totalEffs[i];
				maxIndex=i;
			}
		}
		
		return maxIndex;
	}
	
}
