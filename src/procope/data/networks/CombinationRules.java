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
package procope.data.networks;


/**
 * A set of combination rules defines how the combination of two networks is
 * accomplished. Here is a list of all settings and what effect they have on
 * the combination procedure:
 * <ul>
 *   <li><b>Combination type:</b>
 *     <ul>
 *       <li><i>Merge:</i> The resulting network consists of the merged set of nodes of both input networks.</li> 
 *       <li><i>Intersect:</i> The resulting network will only contain the intersection of both input node sets.</li>
 *     </ul>
 *   </li>
 *   <li><b>Weight merge policy:</b>
 *     <ul>
 *       <li><i>Average</i> (default): Weights for equal edges will be averaged in the new network.</li>
 *       <li><i>Add:</i> Edge weights will be added in the new network.</li> 
 *       <li><i>Annotate weights:</i> All edge weights will be stored as annotations, real edge weights are set to NaN.
 *       One annotation key for each network has to be provided for those annotations.</li>
 *     </ul>
 *   </li>
 *   <li><b>Mapping:</b> You can specify an undirected network which contains a mapping between
 *       protein identifiers. Mapped proteins will be merged into identical nodes in the resulting network.</li>
 *   <li><b>Node merge separator:</b> (only relevant when using a mapping) The labels of the newly create nodes 
 *       are constructed from the identifiers of the original nodes. The separating string can be customized using 
 *       this option. Default string: " / " (without quotes)
 * </ul>
 * 
 * @see ProteinNetwork#combineWith(ProteinNetwork, CombinationRules)
 */
public class CombinationRules {
	
	private CombinationType type;
	private ProteinNetwork mapping=null;
	private WeightMergePolicy weightMerge = WeightMergePolicy.AVERAGE;
	private String weightMergeKey1=null;
	private String weightMergeKey2=null;
	private String nodeMergeSeparator = " / ";
	
	/**
	 * Creates a combination rule object using the given {@link CombinationType}
	 * @param type combination type to be used
	 */
	public CombinationRules(CombinationType type) {
		this.type = type;
	}
	
	/**
	 * Sets a mapping, mapped nodes will be merged into single nodes in the
	 * resulting network. <b>Note:</b> If this is not a bipartite graph the
	 * merging behaviour is undefined.
	 */
	public void setMapping(ProteinNetwork mapping) {
		// muss undirected sein
		if (mapping.isDirected())
			throw new IllegalArgumentException("Mapping must be an undirected graph.");
		
		
		this.mapping = mapping;
	}
	
	/**
	 * Sets how the weights of identical edges are merged in the resulting network.
	 * 
	 * @param weightMerge weight merge policy for the combination
	 */
	public void setWeightMergePolicy(WeightMergePolicy weightMerge) {
		if (weightMerge == WeightMergePolicy.ANNOTATE_WEIGHTS)
			throw new IllegalArgumentException("When using the weight annotation merge policy you must call setWeightMergePolicy(WeightMergePolicy, String, String) and provide two keys for the new weights.");
		this.weightMerge  = weightMerge;
	}
	
	/**
	 * Sets how the weights of identical edges are merged in the resulting network.
	 * 
	 * @param weightMerge weight merge policy for the combination
	 * @param key1 annotation key used for the weights from the first network
	 * @param key2 annotation key used for the weights from the second network
	 */
	public void setWeightMergePolicy(WeightMergePolicy weightMerge, String key1, String key2) {
		this.weightMerge  = weightMerge;
		this.weightMergeKey1 = key1;
		this.weightMergeKey2 = key2;
	}
	
	/**
	 * Returns the first weight merge key.
	 * 
	 * @return first weight merge key
	 * @see #setWeightMergePolicy(procope.data.networks.CombinationRules.WeightMergePolicy, String, String)
	 */
	public String getWeightMergeKey1() {
		return weightMergeKey1;
	}
	
	/**
	 * Returns the second weight merge key.
	 * 
	 * @return second weight merge key
	 * @see #setWeightMergePolicy(procope.data.networks.CombinationRules.WeightMergePolicy, String, String)
	 */
	public String getWeightMergeKey2() {
		return weightMergeKey2;
	}

	/**
	 * Defines how the nodes of both source networks are combined into
	 * a new set of nodes
	 */
	public enum CombinationType {
		/**
		 * Merge the two sets of input nodes (calculate union)
		 */
		MERGE,
		/**
		 * Only use intersection of the two sets of input nodes
		 */
		INTERSECT
	}
	
	/**
	 * Defines how the weights of identical edges are combined for the 
	 * resulting network
	 */
	public enum WeightMergePolicy {
		/**
		 * Calculate averages of original weights
		 */
		AVERAGE, 
		/**
		 * Calculate sum of original weights
		 */
		ADD, 
		/**
		 * Store weights as annotations, you have to set an annotation key for 
		 * each of the input networks.
		 * @see CombinationRules#setWeightMergePolicy(procope.data.networks.CombinationRules.WeightMergePolicy, String, String)
		 */
		ANNOTATE_WEIGHTS
	}
	
	/**
	 * Returns the combination type of this rules set.
	 * 
	 * @return conbination type of this rules set
	 */
	public CombinationType getCombinationType() {
		return type;
	}
	
	/**
	 * Returns the mapping network of this rules set.
	 * 
	 * @return mapping network of this rules set or {@code null} if no mapping
	 *         network is set
	 */
	public ProteinNetwork getMapping() {
		return mapping;
	}
	
	/**
	 * Returns the weight merge policy of this rules set.
	 * 
	 * @return weight merge policy of this rules set.
	 */
	public WeightMergePolicy getWeightMergePolicy() {
		return weightMerge;
	}
	
	/**
	 * Defines how the labels of merged nodes are separated. Only relevant
	 * if you are merging two networks with a 
	 * {@link #setMapping(ProteinNetwork) mapping}.
	 * 
	 * @param separator separator string which will be put between the source
	 *                  protein labels
	 */
	public void setNodeMergeSeparator(String separator) {
		this.nodeMergeSeparator = separator;
	}
	
	/**
	 * Returns the node label merge separator
	 * 
	 * @return node label merge separator
	 */
	public String getNodeMergeSeparator() {
		return this.nodeMergeSeparator;
	}
}
