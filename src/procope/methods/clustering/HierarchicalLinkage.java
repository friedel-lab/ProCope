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

/**
 * Linkage methods for hierarchical agglomerative clustering
 * 
 * @author Jan Krumsiek
 */
public enum HierarchicalLinkage {
	/**
	 * Single linkage clustering, the similarity of two clusters is defined
	 * as the maximum similarty between any two members of the clusters
	 */
	SINGLE_LINK, 

	/**
	 * Complete linkage clustering, the similarity of two clusters is defined
	 * as the minimum similarity between any two members of the clusters
	 */
	COMPLETE_LINK, 
	
	/**
	 * Weighted pair group method with averaging, the similarity of a merged
	 * cluster {@code (a,b)} to a cluster {@code c} is defined as the 
	 * arithmetic mean of {@code a} and {@code b} to {@code c}.
	 */
	WPGMA, 
	
	/**
	 * Unweighted pair group method with averaging, the similarity of two
	 * clusters is defined as the average similarity between all members
	 * of both clusters
	 */
	UPGMA
}
