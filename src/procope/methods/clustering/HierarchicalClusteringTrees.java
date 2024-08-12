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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import procope.data.networks.ProteinNetwork;
import procope.tools.ProCopeException;
import procope.tools.Tools;


/**
 * Implements hierarchical agglomerative clustering in O(n<sup>2</sup>) time
 * and space complexity. It utilizes a nearest neighbor chain method as 
 * described in
 * 
 * <p>Murthag, F.<br/>
 * Complexities of Hierarchic Clustering Algorithms: State of the Art<br/>
 * <i>Computational Statistics Quarterly</i>, 1984, 1, 101-113<br/>
 * 
 * <p>Supports single linkage, complete linkage, UPGMA and WPGMA clustering
 * <p><b>Note:</b> As there are seems to be a confusion in the literature about
 * UPGMA and WPGMA we adapt the notation of the paper above. UPGMA calculates
 * the average of two clusters as the average similarty between all members.
 * For WPGMA the similarity of a merged cluster to any other cluster
 * is the arithmetic mean of the two merged clusters to that other cluster.
 * 
 * <p><font size="+1">Trees</font>
 * <p>This class does not directly compute a clustering but a tree which
 * represents the order of agglomerations and the similarities at which
 * the subclusters were merged. You have to apply a cutoff value on that
 * tree afterwards to cut the tree and get clusters.
 * 
 * <p>Note: This class contains only static members and is not instantiatable.
 * 
 * @author Jan Krumsiek
 *
 */

public class HierarchicalClusteringTrees {

	// avoid instantiation
	private HierarchicalClusteringTrees() {
	}
	
	/**
	 * Performs agglomerative hierarchical clustering on a given network using
	 * a given linkage method. Missing edges get an implicit weight of zero.
	 * 
	 * @param net similarity network to be clustered
	 * @param linkage {@link HierarchicalLinkage linkage} to be used
	 * @return the resulting clustering tree
	 */
	public static HierarchicalTreeNode clusterSimilarities(ProteinNetwork net, HierarchicalLinkage linkage) {
		return clusterSimilarities(net, linkage, net.getProteins());
	}
	
	/**
	 * Performs agglomerative hierarchical clustering on a given network using
	 * a given linkage method. Missing edges get an implicit weight of zero.
	 * 
	 * @param net similarity network to be clustered
	 * @param linkage {@link HierarchicalLinkage linkage} to be used
	 * @param proteins only cluster this given set of proteins
	 * @return the resulting clustering tree
	 */
	public static HierarchicalTreeNode clusterSimilarities(ProteinNetwork net, HierarchicalLinkage linkage, Set<Integer> proteins) {
		
		// verify directedness of network
		if (net.isDirected()) 
			throw new ProCopeException("Clustering can only be done on undirected graph");

		// convert set to list && get back mapping
		List<Integer> protList = new ArrayList<Integer>(proteins);
		Collections.sort(protList);
		int[] mapBack = HierarchicalClusteringTrees.getBackMapping(protList);
		
		// create matrix
		float[][] simMatrix = generateMatrix(net, protList, mapBack);
		
		// do clustering
		HierarchicalTreeNode root = clusterSimilarities(simMatrix, linkage);
		
		mapTree(root, protList);
		
		return root;
		
	}
	
	/**
	 * Performs the clustering using NN chains
	 */
	@SuppressWarnings("unchecked")
	private static HierarchicalTreeNode clusterSimilarities(float[][] sims, HierarchicalLinkage linkage) {
		
		final int numElements = sims.length;
		int numClusters = numElements;
		
		// generate initial clusters
		ArrayList<Integer>[] arrClusters = new ArrayList[numElements];
		HierarchicalTreeNode[] arrTree = new HierarchicalTreeNode[numElements]; 
		for (int i=0; i<numElements; i++) {
			ArrayList<Integer> newcluster = new ArrayList<Integer>();
			newcluster.add(i);
		//	clusters.add(newcluster);
			arrClusters[i] = newcluster;
			arrTree[i] = new HierarchicalTreeNode(i);
		}
		
		// generate NNchain objects
		int curChainIndex=0;
		int[] nnChain = new int[numElements];
		
		// select arbitrary object, store in chain
		nnChain[0] = 0;
		int last=0;
		
		while (numClusters > 1) {
			
			// get nearest neighbor
			int NN=-1;
			float maxSim = Float.NEGATIVE_INFINITY;
			for (int i=0; i<arrClusters.length; i++) {
				if (arrClusters[i] != null && i!=last) {
					// calculate similarity according to formula
					if (sims[last][i] > maxSim) {
						maxSim = sims[last][i];
						NN = i;
					}
				}
			}
			
			// RNN?
			if (curChainIndex>0 && NN == nnChain[curChainIndex-1]) {
				int index1 = nnChain[curChainIndex-1];
				int index2 = nnChain[curChainIndex];
				int size1 = arrClusters[index1].size();
				int size2 = arrClusters[index2].size();
				// merge clusters
				ArrayList<Integer> merged = arrClusters[index1];
				merged.addAll(arrClusters[index2]);
				// remove old references, get new int value and add reference
				arrClusters[index1] = merged;
				arrClusters[index2] = null;
				// now merge trees
				HierarchicalTreeNode newNode = new HierarchicalTreeNode(-1, maxSim);
				newNode.addChild(arrTree[index1]);
				newNode.addChild(arrTree[index2]);
				arrTree[index1] = newNode;
				arrTree[index2] = null;

				// now update scores in matrix according to linkage
				if (linkage == HierarchicalLinkage.UPGMA) {
					for (int i=0; i<numElements; i++) {
						if (arrClusters[i] != null && i!=index1 && i!=index2) {
							// new score = avg of old scores
							sims[index1][i] = sims[i][index1] =  
								((float)size1 / (float)(size1+size2)) * sims[index1][i] +
								((float)size2 / (float)(size1+size2)) * sims[index2][i];
							//
						}
					}
				} else if (linkage == HierarchicalLinkage.SINGLE_LINK) {
					for (int i=0; i<numElements; i++) {
						if (arrClusters[i] != null && i!=index1 && i!=index2) {
							// new score = max of old scores
							sims[index1][i] = sims[i][index1] =  
								max(sims[index1][i], sims[index2][i]);
						}
					}
				} else if (linkage == HierarchicalLinkage.WPGMA) {
					for (int i=0; i<numElements; i++) {
						if (arrClusters[i] != null && i!=index1 && i!=index2) {
							// new score = 50% of each old score
							sims[index1][i] = sims[i][index1] =  
								sims[index1][i] * 0.5f + sims[index2][i] * 0.5f;
						}
					}
				} else if (linkage == HierarchicalLinkage.COMPLETE_LINK) {
					for (int i=0; i<numElements; i++) {
						if (arrClusters[i] != null && i!=index1 && i!=index2) {
							// new score = min of old scores
							sims[index1][i] = sims[i][index1] =  
								min(sims[index1][i], sims[index2][i]);
						}
					}
				}
				
				if (curChainIndex > 1) {
					// there is something left => step backward in the chain  
					curChainIndex-=2;
					last = nnChain[curChainIndex];
				} else {
					// need to start a new chain
					int nextChain=0;
					while (arrClusters[nextChain] == null)
						nextChain++;
					nnChain[0] = nextChain;
					last = nextChain;
					curChainIndex = 0;
				}
					
				numClusters--; 
			
			} else {
				// elongate chain
				curChainIndex++;
				nnChain[curChainIndex] = NN;
				last = NN;
			}
			
		}
		
		return arrTree[last];
	}
	

	/**
	 * minimum of two float values
	 */
	private static float min(float f, float g) {
		return (f < g) ? f : g;
	}

	/**
	 * maximum of two float values
	 */
	private static float max(float f, float g) {
		return (f > g) ? f : g;
	}

	/**
	 * get mapping from the given list of proteins to a
	 * continuously increasing list of integers
	 */
	private static int[] getBackMapping(List<Integer> proteins) {
		// map back
		int[] mapBack = new int[Tools.findMax(proteins)+1];
		Arrays.fill(mapBack, -1);
		for (int i=0; i<proteins.size(); i++) 
			mapBack[proteins.get(i)] = i;
		return mapBack;
	}

	private static void mapTree(HierarchicalTreeNode root, List<Integer> mapping) {
		// parse tree
		Stack<HierarchicalTreeNode> nodes = new Stack<HierarchicalTreeNode>();
		nodes.push(root);
		
		while (!nodes.empty()) {
			HierarchicalTreeNode current = nodes.pop();
			// add children
			if (current.children.size() > 0) {
				for (HierarchicalTreeNode child : current.children)
					nodes.push(child);
			} else {
				// leaf, change label
				current.label = mapping.get(current.label);
			}
		} 
		
	}
	
	/**
	 * generates a 2D similarity matrix from a given scores network
	 */
	private static float[][] generateMatrix(ProteinNetwork network, List<Integer> proteins, int[] mapBack) {
	
		// only allow undirected networks
		if (network.isDirected())
			throw new UnsupportedOperationException("Cannot generate networks of directed networks");
		
		// create boolean matrix for proteins
		int maxVal = Tools.findMax(proteins);
		boolean[] inList = new boolean[maxVal+1];
		for (int protein : proteins)
			inList[protein] = true;
		
		// generate empty matrix
		float[][] matrix = new float[proteins.size()][proteins.size()];
		
		// iterate over all edges
		int[] edges = network.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			// verify that both proteins are in the restricted protein set
			if (!(edges[i] > maxVal) && inList[edges[i]] && !(edges[i+1] > maxVal) && inList[edges[i+1]]) {
				float score = network.getEdge(edges[i], edges[i+1]);
				if (score != score) // check for NaN
					score = 0;
				// write to matrix
				matrix[mapBack[edges[i]]][mapBack[edges[i+1]]] = matrix[mapBack[edges[i+1]]][mapBack[edges[i]]] = score; 
			}
		}
		
		return matrix;
	}
	
}
