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
package procope.methods.interologs;

import procope.data.networks.ProteinNetwork;
import procope.methods.interologs.blast.BlastHits;

/**
 * Calculates interologs from a given set of binary interactions and a
 * (homology) mapping between proteins. The basic assumption is that
 * homologs of interacting proteins have a high probability of being
 * interaction partners as well. These homology-derived potential interactors
 * are then called interologs.
 * <p>This class is not instantiatable.
 * <p>Some ideas are adapted from
 * <p>Yu, et. al<br/>
 * Annotation transfer between genomes: protein-protein interologs and protein-DNA regulogs.<br/>
 * <i>Genome Res.</i>, 2004, 14, 1107-1118<br/>
 * Pubmed: 15173116
 * 
 * @author Jan Krumsiek
 */
public class InterologsCalculator {

	// avoid instantiation
	private InterologsCalculator() {
	}
	
	/**
	 * Calculates interologs of an interaction network using a given protein mapping 
	 * (which should contain homology information like bidirectional best hits).
	 * <p>The resulting network will have the directedness of the original 
	 * interaction network.
	 * <p>The mapping network can be a directed network but will be used in an 
	 * undirected manner. 
	 * 
	 * @param toTransfer interaction network for which interologs will be calculated
	 * @param mapping the protein mapping to be used, can be 
	 * {@link BlastHits#getBidirectionalBestHits(BlastHits, procope.methods.interologs.blast.BlastBBHConstraints) BBHs}  
	 * for instance
	 * @param scorer scorer for the interolog edges or {@code null} to let all 
	 *               transferred edges have a weight of 1.0
	 * @return the transferred interolog network
	 */
	public static ProteinNetwork calculateInterologs(ProteinNetwork toTransfer,
			ProteinNetwork mapping, InterologScorer scorer) {

		ProteinNetwork newNet = new ProteinNetwork(toTransfer.isDirected()); 
		
		// iterate over network
		int[] edges = toTransfer.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			int protein1 = edges[i];
			int protein2 = edges[i+1];
			// get BBHs for both proteins
			int[] bbhs1 = mapping.getNeighborArray(protein1);
			int[] bbhs2 = mapping.getNeighborArray(protein2);
			                   
			if (bbhs1.length > 0 && bbhs2.length > 0) {
				// there might be more than one BBH per source protein => transfer everything
				for (int bbh1 : bbhs1) {
					for (int bbh2 : bbhs2) {
						// get protein ids of the BBHs
						float score = (scorer != null) ? 
								scorer.getInterologScore(protein1, bbh1, protein2, bbh2) : 1.0f;

						// store interaction (if larger than old value)
						float oldValue = newNet.getEdge(bbh1, bbh2);
						
						if (oldValue != oldValue || score > oldValue) // NaN check
							newNet.setEdge(bbh1, bbh2, score);
					}
				}
			}
		}

		return newNet;
		
	}
}
