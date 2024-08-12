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

import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;

/**
 * Wrapper for {@link HierarchicalClusteringTrees} which implements the
 * {@link Clusterer} interface. Here you directly have to specify the threshold
 * for the minimum similarity needed for two clusters in order to be merged.
 * 
 * @author Jan Krumsiek
 */
public class HierarchicalClusterer implements Clusterer {
	
	private HierarchicalLinkage linkage;
	private float threshold;

	/**
	 * Creates a hierarchical clusterer with given the given linkage and
	 * cutoff threshold.
	 * 
	 * @param linkage linkage method
	 * @param threshold cutoff threshold, minimum similarity needed between
	 *                  two clusters in order for them to be merged
	 */
	public HierarchicalClusterer(HierarchicalLinkage linkage, float threshold) {
		this.linkage = linkage;
		this.threshold = threshold;
	}

	/**
	 * Performs hierarchical agglomerative clustering on given similarity network.
	 * Missing edges get an implicit weight of zero.
	 */
	public ComplexSet cluster(ProteinNetwork net) {
		HierarchicalTreeNode tree = HierarchicalClusteringTrees.clusterSimilarities(net, linkage);
		return tree.extractClustering(threshold);
	}

}
