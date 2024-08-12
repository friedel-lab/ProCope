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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import procope.data.ProteinSet;
import procope.data.networks.CombinationRules.WeightMergePolicy;
import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationExperiment;
import procope.tools.BooleanExpression;
import procope.tools.ProCopeException;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;


/**
 * Represents a network of binary protein interactions. For each edge an edge
 * weight as well as edge annotations as key/value pairs can be stored. This
 * class is one of the central objects in this library and provides high-
 * performance accession and manipulation methods. Below you find some basic
 * information and examples for these networks:
 * 
 * <p><font size="+1"><b>Edges in the network</b></font>
 * <p>There are two possibilities on how to create an edge between two proteins
 * in the network. (1) You can assign a numeric weight to the edge using {@link 
 * #setEdge(int, int, float)}. This weight is used by all scoring functions
 * delivered with this library. (2) Alternativly you can assign a set of 
 * key/value pairs to an edge which allows you to store virtually any kind of
 * information for an edge.
 * <p><b>Notes:</b>
 * <ul>
 * <li>Both setting a weight or an annotation will cause an edge to be created.
 * </li>
 * <li>Using edge weights is much faster than using annotations. The data 
 * structures have been optimized for the storing of float values as weights.
 * </li>
 * </ul>
 * 
 * <p><font size="+1"><b>Undirected and directed networks</b></font>
 * <p>By default networks are undirected, but can can create directed networks 
 * using the constructor {@link #ProteinNetwork(boolean)}. For undirected 
 * networks the edges {@code (a,b)} and {@code (b,a)} are identical and only
 * stored once.  
 * <p>The directedness of a network affects different functionalities, e.g. 
 * neighbor detection or network searching. Further information are provided 
 * with the documentations of the functions.
 * 
 * <p><font size="+1"><b>Iterating over networks</b></font>
 * <p>There are different ways of iterating over all edges of a network.
 * Basically the different methods provide different convenience/efficency
 * tradeoffs. For the examples below we assume that there is an existing
 * network object called {@code net}.
 * <ol>
 * <li>The most convenient and Java-like way to iterate over a network is the 
 * use of {@link #iterator()} function:
 * <pre>  for (NetworkEdge edge : net) {
 *       // do something with 'edge'
 *  }</pre>
 * <b>Note:</b> This method is very easy to use but for each edge a 
 * {@link NetworkEdge} object has to be created which might lead to efficiency 
 * problems for large networks.<p> 
 * </li>
 * <li>
 * A more efficient way to iterate over a network is the usage of 
 * {@link #getEdgesArray()}. It returns an array which alternatingly contains
 * both partners of each edge: 
 * <pre>  int[] edges = net.getEdgesArray();
 *  for (int i=0; i&lt;edges.length; i+=2) {
 *      int protein1 = edges[i];
 *      int protein2 = edges[i+1];
 *      // protein1 and protein2 have an edge
 *  }</pre>
 * 
 * </ol>
 * 
 * @author Jan Krumsiek
 *
 */

//implements container for pairwise protein interactions
public class ProteinNetwork implements ProteinSet, Iterable<NetworkEdge> {

	private SparseMatrixFloat interactionMatrix;
	private SparseMatrixHashmap annotationMatrix;
	private PartnerList partnerList;

	private boolean directed = false;
	private boolean iterateTwice=false;
	private int highestID = 0;

	private Set<Integer> nodes;
	private Set<String> annotationKeys;

	int edges=0;
	
	/**
	 * Creates an empty undirected network.
	 */
	public ProteinNetwork() {
		this(false);
	}

	/**
	 * Creates an empty network.
	 * 
	 * @param directed specifies if this will be a directed or an undirected network
	 */
	public ProteinNetwork(boolean directed) {
		this.interactionMatrix = new SparseMatrixFloat(!directed);
		this.directed = directed;
		this.annotationMatrix = new SparseMatrixHashmap(!directed);
		this.partnerList = new PartnerList();

		nodes = new HashSet<Integer>();
		annotationKeys = new HashSet<String>();
	}

	/**
	 * Checks whether there is an edge in the network between two given proteins.
	 * @param prot1 first protein
	 * @param prot2 second protein
	 * @return {@code true} if there is an edge between the two proteins
	 */
	public boolean hasEdge(int prot1, int prot2) {
		return partnerList.get(prot1, prot2);
	}

	/**
	 * Returns the weight of an edge between two given proteins. If there
	 * is no weighted edge between the two proteins this function will return
	 * {@code Float.NaN}
	 * 
	 * @param prot1 first protein
	 * @param prot2 second protein
	 * @return the weight of the edge or {@code Float.NaN} if no weighted edge
	 *         exists between those proteins in the network 
	 */
	public float getEdge(int prot1, int prot2) {
		return interactionMatrix.get(prot1, prot2) ;
	}

	/**
	 * Sets a weighted edge between two given proteins. The edge weight
	 * can be an arbitrary float value.
	 * 
	 * @param prot1 first protein
	 * @param prot2 second protein
	 * @param weight weight to assign to the edge
	 * @throws ProCopeException if {@code weight == Float.NaN}
	 */
	public void setEdge(int prot1, int prot2, float weight) throws ProCopeException {
		
		if (weight!=weight)
			throw new ProCopeException("Cannot set an edge to NaN");
		
		partnerList.set(prot1, prot2);
		if (Float.isNaN(interactionMatrix.set(prot1, prot2, weight)) && annotationMatrix.getAll(prot1,prot2) == null) {
			
			// new edge
			edges++;
		}

		addToNodeList(prot1, prot2);
		
	}
	
	/**
	 * Takes an existing {@link NetworkEdge} object and inserts the edge
	 * into this network.
	 * 
	 * @param edge network edge to be inserted
	 */
	public void setFullEdge(NetworkEdge edge) {
		float weight = edge.getWeight();
		if (weight == weight) // not NaN
			setEdge(edge.getSource(), edge.getTarget(), weight);
		Map<String, Object> annotations = edge.getAnnotations();
		if (annotations.size() > 0)
			setEdgeAnnotations(edge.getSource(), edge.getTarget(), annotations);
	}

	/**
	 * keeps track of all nodes in the network and the highest internal ID
	 */
	private void addToNodeList(int prot1, int prot2) {
		if (prot1 > highestID) highestID = prot1;
		if (prot2 > highestID) highestID = prot2;
		nodes.add(prot1);
		nodes.add(prot2);
	}

	/**
	 * Sets an edge between two given proteins to a standard weight of 1.0.
	 * 
	 * @param prot1 first protein
	 * @param prot2 second protein
	 */
	public void setEdge(int prot1, int prot2) {
		setEdge(prot1,prot2,1.0f);
	}

	/**
	 * Labels an edge between two proteins with a given key/value pair. If
	 * there already is an annotation with this key it will be overwritten.
	 * <p><b>Note:</b> An edge does not have to be created using setEdge in 
	 * order to add an annotation. Adding an annotation will also create a 
	 * new edge in the network.
	 * 
	 * @param prot1 first protein
	 * @param prot2 second protein
	 * @param key key of the annotation
	 * @param value value of the annotation
	 * @throws ProCopeException if {@code value} is not an 
	 *         {@link Integer}, {@link Float}, {@link String} or 
	 *         {@link java.lang.List}
	 */
	public void setEdgeAnnotation(int prot1, int prot2, String key, Object value)
			throws ProCopeException{
		// verify type
		Tools.verifyAnnotationType(value);
		partnerList.set(prot1, prot2);
		if (annotationMatrix.add(prot1, prot2, key, value)) {
			if (Float.isNaN(interactionMatrix.get(prot1, prot2)))
				// new edge
				edges++;
		}

		addToNodeList(prot1, prot2);
		annotationKeys.add(key);
	}
	
	/**
	 * Labels an edge in the network with a given set of annotations.
	 * <p><b>Note:</b> An edge does not have to be created using setEdge in 
	 * order to add an annotation. Adding an annotation will also create a 
	 * new edge in the network.
	 * 
	 * @param prot1 first protein
	 * @param prot2 second protein
	 * @param annotations map of key=>value pairs to add to the edge,
	 *        already existing annotations with identical keys will
	 *        be overwritten
	 * @throws ProCopeException if one or more of the values is 
	 *         not an {@link Integer}, {@link Float}, {@link String} or 
	 *         {@link java.lang.List}
	 */
	public void setEdgeAnnotations(int prot1, int prot2,
			Map<String, Object> annotations) throws ProCopeException {
		partnerList.set(prot1, prot2);
		boolean oneNew=false;
		for (String key : annotations.keySet()) {
			Object value = annotations.get(key);
			Tools.verifyAnnotationType(value);
			oneNew |= annotationMatrix.add(prot1, prot2, key, value);
			annotationKeys.add(key);
		}
		
		if (oneNew) {
			if (Float.isNaN(interactionMatrix.get(prot1, prot2)))
				// new edge
				edges++;
		}

		addToNodeList(prot1, prot2);
	}

	/**
	 * Delete all annotations associated with an edge
	 */
	private void clearAnnotations(int i, int j) {
		annotationMatrix.getAll(i, j).clear();
	}

	/**
	 * Internal annotation setter, no type checks
	 */
	private void setAnnotations(int i, int j, Map<String, Object> annotations) {
		if (annotations != null) {
			for (String key : annotations.keySet()) {
				setEdgeAnnotation(i, j, key, annotations.get(key));
				annotationKeys.add(key);
			}
		}
	}

	/**
	 * Retrieves an annotation from a given edge.
	 * 
	 * @param prot1 first protein
	 * @param prot2 second protein
	 * @param key the key for which the value will be read
	 * @return the value belonging to this key (will be of type 
	 *         {@link Integer}, {@link Float}, {@link String} or 
	 *         {@link java.lang.List} or {@code null} if there is no value 
	 *         associated with this key
	 */
	public Object getEdgeAnnotation(int prot1, int prot2, String key) {
		return annotationMatrix.get(prot1, prot2, key);
	}

	/**
	 * Deletes an edge from the network along with all of its annotations.
	 * 
	 * @param prot1 first protein
	 * @param prot2 second protein
	 * @return {@code true} if the edge existed and was deleted, {@code false}
	 *         if no such edge exists in the network
	 */
	public boolean deleteEdge(int prot1, int prot2) {
		boolean deletedSomething=false;
		deletedSomething |= annotationMatrix.delete(prot1, prot2);
		deletedSomething |= interactionMatrix.delete(prot1, prot2);
		
		if (deletedSomething)
			edges--;
		
		// for directed networks we need to check if maybe the other edge is still there
		boolean deleteCompleteInteraction = false;
		if (directed) {
			if (Float.isNaN(getEdge(prot2,prot1)) && getEdgeAnnotations(prot2, prot1).size() == 0) {
				deleteCompleteInteraction = true;
			}				
		} else
			deleteCompleteInteraction = true;
		
		if (deleteCompleteInteraction)
			partnerList.delete(prot1, prot2);
		
		return deletedSomething;

	}

	/**
	 * Returns an array of proteins which contains all neighbors in the network
	 * of a given protein. For directed networks, this will contain neighbors
	 * which are sources as well as neighbors which are targets of directed 
	 * edges.
	 * 
	 * @param protein protein for which the neighbors will be retrieved
	 * @return array of neighbor proteins
	 */
	public int[] getNeighborArray(int protein) { 
		return partnerList.getPartners(protein);
	}
	
	/**
	 * Basically the same as {@link #getNeighbors(int)}, but returns
	 * the IDs of all neighbors with descending weights. That is, the most
	 * similar neighbor (with the highest score) will be the first one in the
	 * result list
	 * @param protein of which the sorted neighbor list will be returned
	 */
	public List<NetworkEdge>  getNearestNeighbors(int protein) {
		List<NetworkEdge> neighbors = getNeighbors(protein);
		Collections.sort(neighbors);
		return neighbors;
	}

	/**
	 * Returns an array containing all edges of the network. <b>Note:</b>
	 * This method is intendend for high-performance iteration over the
	 * all edges of a network. If this array has length {@code n} there are
	 * {@code n/2} edges in the network. The array alternatingly contains the
	 * first and second protein of each edge. 
	 * <p>See above for examples on {@link ProteinNetwork iterating over a 
	 * network}
	 * 
	 * @return array with edges in this network consisting of the first and 
	 *         second proteins of each edge alternatingly  
	 */
	public int[] getEdgesArray() {

		int[] result = new int[getEdgeCount()*2];
		int index=0;

		// need to differentiate directed and undirected networks
		// iterate over all interacting pairs
		for (int first : partnerList.getFirstPartners()) {
			for (int partner : partnerList.getPartners(first)) {
				// add to list (but just once!)
				if ((!directed && (first <= partner))) {
					result[index++] = first;
					result[index++] = partner;
				}
				else if (directed) {
					float score = interactionMatrix.get(first, partner);
					Map<String, Object> anno = annotationMatrix.getAll(first, partner);
					if (score == score || anno != null) { // NaN check
						result[index++] = first;
						result[index++] = partner;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Returns all annotations associated with a given edge.
	 * 
	 * @param prot1 first protein
	 * @param prot2 second protein
	 * @return a {@link java.util.Map} of key/value pairs associated with this
	 *         edge, all values are of type {@link Integer}, {@link Float}, 
	 *         {@link String} or {@link java.lang.List} 
	 */
	public Map<String, Object> getEdgeAnnotations(int prot1, int prot2) {
		Map<String, Object> annotations = annotationMatrix.getAll(prot1, prot2);
		if (annotations == null)
			// return empty map
			return Tools.EMPTY_ANNOTATION_MAP;
		else
			return annotations;
	}

	/**
	 * Returns all incident edges for a given protein in the network. For 
	 * undirected networks this query protein will always be the <i>source</i> 
	 * of the returned edges which means that {@code edge.getSource() == 
	 * protein}. For directed networks of course {@code edge.getSource()} will 
	 * always be the actual source of the directed edge.
	 * 
	 * @param protein protein to retrieve neighbors for
	 * @return collection of {@link NetworkEdge network edges} incident with
	 *         the given protein
	 */
	public List<NetworkEdge> getNeighbors(int protein) {
		return getNeighbors(protein, false, false); // fromProtein parameters doesn't matter here
	}

	/**
	 * Returns all incident edges of a given direction from a directed network.
	 * For {@code fromProtein==true} it will only return edges where {@code 
	 * protein} is the source protein, for {@code fromProtein==false} only 
	 * those edges where {@code protein} is the target will be returned.
	 * <p>The function should not be called for undirected networks and will
	 * output a warning on {@code stderr} if you do so.
	 * 
	 * @param protein protein for which neighbors will be retrieved from the 
	 *        network
	 * @param fromProtein get directed edges where {@code protein} is the 
	 *        source ({@code fromProtein==true}) or the target ({@code
	 *        fromProtein==false})
	 * @return list of incident edges according to the settings
	 */
	public Collection<NetworkEdge> getDirectedNeighbors(int protein, boolean fromProtein) {
		// for an undirected networks this function has no effect
		if (!directed) {
			System.err.println("getDirectedNeighbors is not intended for undirected networks! Behaving like getNeighbors!");
			return getNeighbors(protein, false, false); // fromProtein parameters doesn't matter here
		} else 
			return getNeighbors(protein, fromProtein, true); // onlyOne doesn't matter here 
	}

	/**
	 * Function used by getNeighbors, getDirectedNeighbors and iterator() to enumerate neighbor proteins
	 */
	private List<NetworkEdge> getNeighbors(int protein, boolean fromProtein, boolean onlyOneDirection) {

		ArrayList<NetworkEdge> result = new ArrayList<NetworkEdge>();
		if (!directed) {
			// undirected case
			// iterate over all partners
			for (int partner : partnerList.getPartners(protein)) { 
				if (!onlyOneDirection || protein <= partner) {
					result.add(new NetworkEdge(protein, partner,
							interactionMatrix.get(protein, partner),
							getEdgeAnnotations(protein, partner)) );
				}
			}
		} else {
			// directed case, we need to add both directions
			for (int partner : partnerList.getPartners(protein)) {
				if (fromProtein || !onlyOneDirection) {
					// first direction, get and add if there is something
					float score1 = interactionMatrix.get(protein, partner);
					Map<String, Object> anno1 = getEdgeAnnotations(protein, partner);
					if (score1 == score1 || anno1.size() > 0) { // NaN check
						result.add(new NetworkEdge(protein, partner, score1, anno1));
					}
				}

				if (!fromProtein || !onlyOneDirection) {
					// second direction, get and add if there is something
					float score2 = interactionMatrix.get(partner, protein);
					Map<String, Object> anno2 = getEdgeAnnotations(partner, protein);
					if (score2==score2 || anno2.size() > 0) { // NaN check
						result.add(new NetworkEdge(partner, protein, score2,	anno2));
					}
				}

			}
		}
		return result;
	}

	/**
	 * Returns the set of proteins which are contained as nodes in this network
	 */
	public Set<Integer> getProteins() {
		return nodes;
	}

	/**
	 * Returns the number of edges in this network. Note that for directed
	 * networks the edges (a,b) and (b,a) will both increase this count.
	 * 
	 * @return number of edges in the network
	 */
	public int getEdgeCount() {
		return edges;
	}
	
	/**
	 * Returns the number of nodes in this network. This value is equivalent
	 * to {@code getProteins().size()}.
	 * 
	 * @return number of nodes in the network
	 */
	public int getNodeCount() {
		return nodes.size();
	}

	
	/**
	 * Returns a set of all distinct annotation keys used in the network.
	 * 
	 * @return set of distinct annotation keys used in the network
	 */
	public Set<String> getAnnotationKeys() {
		return annotationKeys;
	}
	
	/**
	 * Returns a new network object which contains only those edges where one or both
	 * adjacent proteins are contained in a given set of proteins. 
	 * 
	 * @param proteins set of proteins to which this network will be restricted
	 * @param fullCoverage if {@code true} then both proteins of an edge have 
	 *                     to be in the restriction set, if {@code false} then
	 *                     one protein is sufficient
	 * @return network restricted to the given set of proteins
	 */
	public ProteinNetwork restrictToProteins(ProteinSet proteins, boolean fullCoverage) {
		return restrictToProteins(proteins.getProteins(), fullCoverage);
	}

	/**
	 * Returns a new network object which contains only those edges where one or both
	 * adjacent proteins are contained in a given set of proteins. The {@code
	 * proteinIDs} set should be a quickly searchable {@link Set} 
	 * implementation like {@link HashSet}.
	 * 
	 * @param proteinIDs set of proteins to which this network will be restricted
 	 * @param fullCoverage if {@code true} then both proteins of an edge have 
 	 *                     to be in the restriction set, if {@code false} then
	 *                     one protein is sufficient
	 * @return network restricted to the given set of proteins
	 */
	public ProteinNetwork restrictToProteins(Set<Integer> proteinIDs, boolean fullCoverage) {
		// create empty network
		ProteinNetwork newNet = new ProteinNetwork(this.directed);
		// iterate over all interactions
		int[] edges = this.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			// both partners contained in protein space?
			if ( (fullCoverage && proteinIDs.contains(edges[i]) && proteinIDs.contains(edges[i+1])) || 
				(!fullCoverage && (proteinIDs.contains(edges[i]) || proteinIDs.contains(edges[i+1])) )  ){
				// add edge and annotations
				float score = getEdge(edges[i], edges[i+1]);
				if (score == score) // NaN check
					newNet.setEdge(edges[i], edges[i+1], score);
				newNet.setAnnotations(edges[i], edges[i+1], annotationMatrix.getAll(edges[i], edges[i+1]));
			}
		}

		return newNet;
	}


	/**
	 * Returns if the network is a directed network
	 * 
	 * @return {@code true} if this is a directed network, {@code false}
	 *         otherweise
	 */
	public boolean isDirected() {
		return directed;
	}
	
	/**
	 * Filters the network using a given boolean expression. All edges
	 * whose weight and annotations fullfil the expression will be contained
	 * in the resulting network. <b>Note:</b> The edge of a weight can be
	 * addressed using the variable name {@code @weight} in the expression.
	 * 
	 * @param expression boolean expression used for edge evaluation
	 * @return filtered network according to the boolean expression
	 * @see   procope.tools.BooleanExpression
	 */
	public ProteinNetwork getFilteredNetwork(BooleanExpression expression) {

		// create new network with same directed-property as current one
		ProteinNetwork newNet = new ProteinNetwork(directed);

		// iterate over network
		int[] edges = this.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			Map<String, Object> original = getEdgeAnnotations(edges[i], edges[i+1]);
			Map<String, Object> annotations;
			// add score if existing
			float score = getEdge(edges[i], edges[i+1]);
			if (score == score) { // not NaN
				annotations = new HashMap<String, Object>(original);
				annotations.put("@weight", score);
			} else
				annotations = original;
			
			if (expression.evaluate(annotations)) {
				if (annotations.size() > 0) 
					newNet.setAnnotations(edges[i], edges[i+1], original);
				if (score==score) // NaN check
					newNet.setEdge(edges[i], edges[i+1], score);
			}
		}

		return newNet;
	}

	/**
	 * Get list of proteins in this network. Equal to {@link #getProteins()}
	 * @return set of proteins in the network
	 */
	public Set<Integer> getNodes() {
		return nodes;
	}


	/**
	 * For undirected networks this function determines the iterator behaviour.
	 * When iterating over such a network using {@link #iterator()} this value
	 * sets if each undirected edge will appear just once, or if it will 
	 * appear twice in each iteration with both neighbors acting as source one
	 * time and as target the second time.
	 * <p>If iterating <u>once</u> the protein with the smaller internal ID 
	 * will be the source of the edge.
	 * <p><b>Note:</b> This setting has no effect on directed networks and will
	 * output a warning on {@code stderr} if called on such a network.
	 * <p>By default this value is set to {@code false}.
	 * 
	 * 
	 * @param iterateTwice
	 */
	public void setIterateEdgesTwice(boolean iterateTwice) {
		if (directed) System.err.println("Warning: iterate twice has no effect on directed networks!");
		this.iterateTwice = iterateTwice;
	}

	/**
	 * Returns an iterator over all edges of the network. Be sure to check out
	 * the {@link #setIterateEdgesTwice} setting and the {@link ProteinNetwork 
	 * iterating over a network} section above.
	 */
	public Iterator<NetworkEdge> iterator() {
		return new Iterator<NetworkEdge>() {

			Iterator<Integer> partnerIterator;
			private Collection<NetworkEdge> nextList;
			private Iterator<NetworkEdge> nextListIterator;

			{
				// intialisation of this anonymous inner class
				// get iterator for first partners
				partnerIterator = partnerList.getFirstPartners().iterator();
				prepareNextList();
			}

			private void prepareNextList() {
				// find the next non-empty list
				nextList = null;
				while (nextList == null || nextList.size() == 0) {
					if (!partnerIterator.hasNext()) {
						nextList = null;
						return;
					}
					nextList = getNeighbors(partnerIterator.next(), true, directed || !iterateTwice);
					nextListIterator = nextList.iterator();
				}
			}

			public boolean hasNext() {
				return nextList != null ;
			}

			public NetworkEdge next() {
				NetworkEdge toReturn = nextListIterator.next();
				if (!nextListIterator.hasNext())
					prepareNextList();
				return toReturn;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Checks whether two networks are equal regardings their edge weights
	 * 
	 * @param compare other network for comparison
	 * @return {@code true} if and only if both networks have the same edges
	 *         and all of these edges have the same weight
	 */
	public boolean equalScores(ProteinNetwork compare) {
		// different number of edges => not equal
		if (this.getEdgeCount() != compare.getEdgeCount())
			return false;
		// iterate over all edges
		int[] edges = this.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			if (this.getEdge(edges[i], edges[i+1]) != compare.getEdge(edges[i], edges[i+1]))
				return false;
		}

		// everything was equal
		return true;
	}
	
	/**
	 * Returns true if and only if obj is also of type ProteinNetwork, both
	 * network have the same directedness and all edge weights and annotations
	 * of the networks are identical
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ProteinNetwork)) return false;
		ProteinNetwork otherNet = (ProteinNetwork)obj;
		
		// check for directedness
		if (this.isDirected() != otherNet.isDirected())
			return false;
		// check for edge count
		if (this.getEdgeCount() != otherNet.getEdgeCount())
			return false;
		// check edges
		int[] edges = this.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			// 1. check score
			float score = this.getEdge(edges[i], edges[i+1]);
			if (score != score) {
				// score is NaN, must also be NaN in the other network
				if (!Float.isNaN(otherNet.getEdge(edges[i], edges[i+1]))) {
					System.out.println("score1 failed");
					return false;
				}
				
			} else {
				// score is not NaN, must be equal
				if (score != otherNet.getEdge(edges[i], edges[i+1])) {
					System.out.println(ProteinManager.getLabel(edges[i]) + ", " + ProteinManager.getLabel(edges[i+1]));
					System.out.println("thisnet:  " + score);
					System.out.println("othernet: " + otherNet.getEdge(edges[i], edges[i+1]));
					
					System.out.println("score2 failed");
					return false;
				}
			}
			// 2. check annotations
			Map<String, Object> annos = this.getEdgeAnnotations(edges[i], edges[i+1]);
			Map<String, Object> otherAnnos = otherNet.getEdgeAnnotations(edges[i], edges[i+1]);
			if (!annos.equals(otherAnnos)) {
				System.out.println("annos failed");
				return false;
			}
		}
		
		// everything worked
		return true;
	}

	/**
	 * Performs a depth-first search one the network. Check out  
	 * {@link NetworkSearchCallback} for more information.
	 * 
	 * @param start the protein node where to start the search
	 * @param callback callback object to which all nodes passed in the search
	 *        are reported
	 */
	public void depthFirstSearch(int start, NetworkSearchCallback callback) {
		networkSearch(start, callback, new WaitStack<Integer>());
	}
	
	/**
	 * Performs a breadth-first search one the network. Check out  
	 * {@link NetworkSearchCallback} for more information. 
	 * 
	 * @param start the protein node where to start the search
	 * @param callback callback object to which all nodes passed in the search
	 *        are reported
	 */
	public void breadthFirstSearch(int start, NetworkSearchCallback callback) {
		networkSearch(start, callback, new WaitQueue<Integer>());
	}

	/**
	 * perform depth-first or breadth-frist search (depends on input structure, stack or queue)
	 */
	private void networkSearch(int start, NetworkSearchCallback callback, WaitList<Integer> list) {
		boolean[] visited = new boolean[highestID+1]; 
		// add first one and mark
		list.put(start);
		visited[start] = true;
		// do the search
		while (!list.isEmpty()) {
			int protein = list.next();
			System.out.println("Passing: " + protein);
			// callback and check if we go on
			if (!callback.reportNode(protein))
				break;
			// get current partners
			if (!directed) {
				// easy => simple get array of partners
				int[] partners = getNeighborArray(protein);
				for (int i=0; i<partners.length; i++) {
					// only unvisited partners
					if (!visited[partners[i]]) {
						// visit them 
						list.put(partners[i]);
						visited[partners[i]] = true;
					}
				}
			} else {
				// now we need to get directed information
				for (NetworkEdge edge : getDirectedNeighbors(protein, true)) {
					int partner = edge.getTarget();
					// only unvisited partners
					if (!visited[partner]) {
						// visit them 
						list.put(partner);
						visited[partner] = true;
					}
				}
			}
		}
	}

	/**
	 * Combines two network using a given {@link CombinationRules combination rules}.
	 * If one network is directed and the other one undirected the result will be undirected
	 * <p>For more information check out the {@link CombinationRules} API docs.
	 * 
	 * @param other the network this one to be combined with
	 * @param rules combination rules
	 * @return the combined network
	 */
	public ProteinNetwork combineWith(ProteinNetwork other, CombinationRules rules) {

		int highestID = Math.max(this.highestID, other.highestID);
		
		// if direction of network differs => assume undirected
		boolean directed;
		if (this.isDirected() != other.isDirected()) 
			directed = false;
		else
			directed = this.isDirected();

		ProteinNetwork newNet = new ProteinNetwork(directed);

		// get some stuff from the rules
		boolean merge=rules.getCombinationType() == CombinationRules.CombinationType.MERGE;
		WeightMergePolicy scoreMerge = rules.getWeightMergePolicy();
		String scoreAnnoKey1 = rules.getWeightMergeKey1();
		String scoreAnnoKey2 = rules.getWeightMergeKey2();
		String nodeMergeDelimiter = rules.getNodeMergeSeparator();

		Set<Integer> proteins1 = new HashSet<Integer>(this.getProteins());
		Set<Integer> proteins2 = other.getProteins();
		Set<Integer> newProteins=null;

		int[] mappedToNew=null; 
		
		// first step, determine nodes involved in resulting network
		if (rules.getMapping() == null) {
			// no mapping given, use internal IDs

			// trivial mapping
			mappedToNew = new int[highestID+1];
			for (int i=0; i<mappedToNew.length; i++)
				mappedToNew[i] = i;


			if (!merge) {
				// calculate intersection of involved proteins
				proteins1.retainAll(proteins2);
				newProteins = proteins1;
			} else {
				// merge involved proteins
				proteins1.addAll(proteins2);
				newProteins = proteins1;
			}

		} else {
			mappedToNew = new int[highestID+1];

			// do combination with mapping!
			ProteinNetwork mapping = rules.getMapping();
			HashSet<Integer> mergedNodes = new HashSet<Integer>();
			HashSet<Integer> oldMappedNodes = new HashSet<Integer>();
			Set<Integer> mappingProteins = mapping.getProteins();
			// determine protein overlap wrt to mapping
			for (int i=0; i<mappedToNew.length; i++)
				mappedToNew[i] = i;

			for (int protein : proteins1) {
				if (mappedToNew[protein] == protein && mappingProteins.contains(protein)) {
					// we need to gather all proteins which are involved in this component of the graph
					Set<Integer> involved = new HashSet<Integer>();
					involved.add(protein);
					// add neighbors
					int[] arrNeighbors = mapping.getNeighborArray(protein);
					for (int i=0; i<arrNeighbors.length; i++) {
						if (proteins2.contains(arrNeighbors[i])) {
							involved.add(arrNeighbors[i]);
							// add neighbor's neighbors
							int[] nNeighbors = mapping.getNeighborArray(arrNeighbors[i]);
							for (int j=0; j<nNeighbors.length; j++) {
								if (proteins1.contains(nNeighbors[j]))
									involved.add(nNeighbors[j]);
							}
						}
					}
					// got all involved proteins, create new label from that
					String newIdentifier = "";
					int count=0;
					for (int inv : involved) {
						newIdentifier += ProteinManager.getLabel(inv); 
						if (count < involved.size()-1) newIdentifier += nodeMergeDelimiter;
						count++;
					}
					// create new "protein" 
					int newID = ProteinManager.getInternalID(newIdentifier); 
					// set mappings to this new ID, also merge annotations
					Map<String, Object> newAnnotations = new HashMap<String, Object>();
					for (int inv : involved) {
						mappedToNew[inv] = newID;
						Map<String, Object> oldAnnotations = ProteinManager.getAnnotations(inv);
						if (oldAnnotations.size() > 0)
							newAnnotations = mergeAnnotations(oldAnnotations, newAnnotations);
					}
					// if there were any annotations => store them now
					ProteinManager.addAnnotations(newID, newAnnotations);

				}	
			}
			// now create set of proteins in new network according to merge policy
			if (merge) {
				// merging, add new ones, old ones and remove those old ones which have been mapped
				newProteins = new HashSet<Integer>();
				newProteins.addAll(proteins1);
				newProteins.addAll(proteins2);
				newProteins.addAll(mergedNodes);
				newProteins.removeAll(oldMappedNodes);

			} else {
				// intersecting, only use newly merged nodes
				newProteins = mergedNodes;
				//	System.out.println(mergedNodes);
			}

		}

		// iterate over all edges of first network
		int[] edges = this.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			int protein1 = edges[i];
			int protein2 = edges[i+1];
			// contained in new set?
			if (merge || (newProteins.contains(mappedToNew[protein1]) && newProteins.contains(mappedToNew[protein2])) ) {
				// add combined edge to new network
				setCombinedEdge(this,newNet,mappedToNew[protein1],mappedToNew[protein2], 
						this.getEdge(protein1, protein2), this.getEdgeAnnotations(protein1, protein2), scoreMerge, scoreAnnoKey1);
			}
		}
		// do the same thing for the other network
		edges = other.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			int protein1 = edges[i];
			int protein2 = edges[i+1];

			// contained in new set?
			if (merge || (newProteins.contains(protein1) && newProteins.contains(protein2)) ) {
				// add combined edge to new network
				setCombinedEdge(other,newNet,mappedToNew[protein1],mappedToNew[protein2], 
						other.getEdge(protein1, protein2), other.getEdgeAnnotations(protein1, protein2), scoreMerge, scoreAnnoKey2);
			}
		}
		

		return newNet;

	}

	/**
	 * add an edge to the new network, do combination steps if that edge is already there 
	 */
	private void setCombinedEdge(ProteinNetwork from, ProteinNetwork newNet, 
			int protein1, int protein2, float score, Map<String, Object> annotations, WeightMergePolicy scoreMerge, String scoreAnnoKey) {

		// get score and annotations 
		float existingScore = newNet.getEdge(protein1, protein2);
		Map<String, Object> existingAnnotations = newNet.getEdgeAnnotations(protein1, protein2);

		// 1. combine scores according to policy, if only one score given => use that one
		// set score or combine if already existing
		float setScore=Float.NaN;
		if (score == score) { // NaN check
			if (scoreMerge == WeightMergePolicy.AVERAGE) {
				if (existingScore == existingScore) // NaN check 
					setScore = (score + existingScore) / 2;
				else
					setScore = score;
			} else if (scoreMerge == WeightMergePolicy.ADD) { 
				if (existingScore == existingScore) // NaN check 
					setScore = score + existingScore;
				else
					setScore = score;
			} else {
				// annotate
				if (annotations.size()==0) annotations = new HashMap<String, Object>();
				annotations.put(scoreAnnoKey, score);
			}
		}

		// 2. now combine annotations, overlapping ones need to be handled!
		Map<String, Object> setAnnotations = null;
		if (annotations.size() > 0 || existingAnnotations.size() > 0) {
			setAnnotations = mergeAnnotations(annotations, existingAnnotations);
		}

		// set new edge
		if (!Float.isNaN(setScore)) 
			newNet.setEdge(protein1, protein2, setScore);
		if (setAnnotations != null) {
			if (existingAnnotations.size() > 0)
				newNet.clearAnnotations(protein1, protein2);
			for (String key : setAnnotations.keySet())
				newNet.setEdgeAnnotation(protein1, protein2, key, setAnnotations.get(key));
		}

	}
	
	/**
	 * Merge two given annotation maps. Equal annotations are just copied once (of course),
	 * multiple values with the same key are merged into a list.
	 */
	private Map<String, Object> mergeAnnotations (Map<String, Object> anno1, Map<String, Object> anno2) {
		Set<String> keys = anno1.keySet();
		Set<String> existingKeys = anno2.keySet();
		Map<String,Object> result = new HashMap<String,Object>();
		// iterate over first annotations
		if (keys.size() > 0) {
			for (String key : keys) {
				// both edges have this annotation?
				if (existingKeys.contains(key)) {
					// merge annotations.. if they are equal: copy, if they are different => add as list
					Object value1 = anno1.get(key);
					Object value2 = anno2.get(key);
					if (value1.equals(value2)) {
						// equal values, just copy
						result.put(key, value1);
					} else {
						// different values, add as list, lists will be merged
						ArrayList<Object> list = new ArrayList<Object>();
						if (value1 instanceof List)	
							list.addAll((List<?>)value1);
						else						
							list.add(value1);
						if (value2 instanceof List)	
							list.addAll((List<?>)value2);
						else						
							list.add(value2);
						result.put(key, list);
					}
				} else
					// simply copy annotation
					result.put(key, anno1.get(key));
			}
		}
		
		// now copy remaining annotations from second edge
		if (anno2.size() > 0) {
			for (String key : existingKeys) {
				if (!keys.contains(key)) 
					result.put(key, anno2.get(key));

			}
		}
		
		return result;
	}

	/**
	 * Returns a network containing only edges with a weight greater or equal 
	 * than a given cutoff value. Missing edge weights (edges which only have
	 * an annotation) are treated as zero.
	 * 
	 * @param cutOff the cutoff value
	 * @return network only containing edges with a weight above the cutoff 
	 */
	public ProteinNetwork getCutOffNetwork(float cutOff) {
		return getCutOffNetwork(cutOff, true);
	}

	/**
	 * Returns a network containing only edges with a weight above or below a
	 * given cutoff value. <i>Above</i> means greater or equal than where as 
	 * <i>below</i> means less or equal than. Missing edge weights (edges which
	 * only have an annotation) are treated as zero.
	 * 
	 * @param cutOff the cutoff value
	 * @param cutBelow {@code true} to cut weights below the threshold, {@code
	 *        false} to cut weights above the threshold.
	 * @return  network only containing edges with a weight above or below the 
	 *          cutoff 
	 */
	public ProteinNetwork getCutOffNetwork(float cutOff, boolean cutBelow) {

		ProteinNetwork newNet = new ProteinNetwork(isDirected());
		// iterate over edges
		int[] edges = this.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			float score = interactionMatrix.get(edges[i], edges[i+1]);
			if ((cutBelow && score >= cutOff) || (!cutBelow && score <= cutOff)) {
				// add edge and annotations
				newNet.setEdge(edges[i], edges[i+1], score);
				newNet.setAnnotations(edges[i], edges[i+1], annotationMatrix.getAll(edges[i], edges[i+1]));
			}
		}

		return newNet;
	}

	/**
	 * Create a copy of the network.
	 * @return copy of the network
	 */
	public ProteinNetwork copy() {
		return createCopy(directed);
	}

	/**
	 * Create an explicitly undirected copy of the network. That means
	 * each edge of an directed network will be turned into an undirected
	 * edge in the resulting network. <b>Note:</b> If the directed networks
	 * contains edges in both directions, e.g. (a,b) and (b,a) the second
	 * edge will overwrite the first one regarding the weight and the 
	 * annotations.
	 * <p>For undirected networks this function is the same as {@link #copy()}
	 * 
	 * @return undirected copy of the network
	 */
	public ProteinNetwork undirectedCopy() {
		return createCopy(false);
	}
	
	/**
	 * creates a directed or an undirected copy of a network
	 */
	private ProteinNetwork createCopy(boolean directed) {
		ProteinNetwork result = new ProteinNetwork(directed);
		// iterate over all edges
		int[] edges = getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			// get values
			float score = getEdge(edges[i], edges[i+1]);
			Map<String, Object> annotations = getEdgeAnnotations(edges[i], edges[i+1]);
			// transfer them
			if (score==score) // NaN check
				result.setEdge(edges[i], edges[i+1], score);
			if (annotations.size() > 0) 
				result.setAnnotations(edges[i], edges[i+1], annotations);
		}
		return result;
	}
	
	/**
	 * Multiplies all existing edge weights of the network with a given value.
	 * 
	 * @param factor multiplication factor
	 */
	public void scalarMultiplication(float factor) {
		// iterate over edges
		int[] edges = this.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			float score = interactionMatrix.get(edges[i], edges[i+1]);
			setEdge(edges[i], edges[i+1], score * factor);
		}
	}

	/**
	 * Randomizes a network by rewiring. For each rewiring step, two edges 
	 * {@code (a,b)} and {@code (c,d)} are selected such that {@code a != b 
	 * != c != d}. These 4 nodes are rewired to create two new edges {@code 
	 * (a,d)} and {@code (c,b)}.
	 * <p>This function will do 10 times as many rewirings as there are edges
	 * in the network.
	 * 
	 * @return rewired network
	 */
	public ProteinNetwork randomizeByRewiring() {
		return randomizeByRewiring(this.getEdgeCount()*10);
	}
		
	/**
	 * Randomizes a network by rewiring. For each rewiring step, two edges 
	 * {@code (a,b)} and {@code (c,d)} are selected such that {@code a != b 
	 * != c != d}. These 4 nodes are rewired to create two new edges {@code 
	 * (a,d)} and {@code (c,b)}.
	 *
	 * @param rewirings number of rewirings which will be performed
	 * @return rewired network
	 */
	public ProteinNetwork randomizeByRewiring(int rewirings) {
		ProteinNetwork newNet = copy();
		Random random = Tools.random;
		
		// get all edges
		int[] edges = newNet.getEdgesArray();
		int numEdges = newNet.getEdgeCount();
		int rewiringsDone=0;
		while (rewiringsDone < rewirings) {
			// select two edges randomly
			int edge1 = random.nextInt(numEdges);
			int edge2 = random.nextInt(numEdges);
			// check that we did not get the same edge twice
			if (edge1 != edge2) {
				// get involved protein
				int protein11,protein12,protein21,protein22;
				if (random.nextFloat() <= 0.5f) {
					protein11 = edges[edge1*2];
					protein12 = edges[edge1*2+1];
				} else {
					protein12 = edges[edge1*2];
					protein11 = edges[edge1*2+1];
				}
				if (random.nextFloat() <= 0.5f) {
					protein21 = edges[edge2*2];
					protein22 = edges[edge2*2+1];
				} else {
					protein22 = edges[edge2*2];
					protein21 = edges[edge2*2+1];
				}
				
				// no overlap!
				if (protein11 != protein21 && protein11 != protein22
						&& protein12 != protein21 && protein12 != protein22) {
					// ensure that the new edges do not already exist
					if (!newNet.hasEdge(protein11, protein22) && !newNet.hasEdge(protein21, protein12)) { 
						// get edge contents
						Map<String, Object> annotations1 = newNet.getEdgeAnnotations(protein11, protein12);
						float score1 = newNet.getEdge(protein11, protein12);
						Map<String, Object> annotations2 = newNet.getEdgeAnnotations(protein21, protein22);
						float score2 = newNet.getEdge(protein21, protein22);
						// delete old edges
						newNet.deleteEdge(protein11, protein12);
						newNet.deleteEdge(protein21, protein22);
						// set new edges
						if (annotations1.size() > 0)
							newNet.setAnnotations(protein11, protein22, annotations1);
						if (score1==score1) // NaN check
							newNet.setEdge(protein11, protein22, score1);
						if (annotations2.size() > 0)
							newNet.setAnnotations(protein21, protein12, annotations2);
						if (score2==score2) // NaN check
							newNet.setEdge(protein21, protein12, score2);
						// set array values
						edges[edge1*2] = protein11;
						edges[edge1*2+1] = protein22;
						edges[edge2*2] = protein12;
						edges[edge2*2+1] = protein21;
						
						rewiringsDone++;
					}
				}
			}
		}
		
		return newNet;
	}
	
	/**
	 * Creates a purification data object from a directed network. The source
	 * proteins of each edge will be treated as the bait protein where as the
	 * targt of each directed edge is a prey.
	 * <p>If {@code poolBaits==true} then one {@link PurificationExperiment} 
	 * for each source protein will be created. With {@code poolBaits==false}
	 * each single edge will be treated as one purification experiment. 
	 * <b>Note:</b> {@code poolBaits==false} might cause very large and memory-
	 * intense {@link PurificationData} object
	 * 
	 * @param poolBaits if {@code true} one {@link PurificationExperiment} is
	 *        created per bait, otherwise each edge is treated as a single
	 *        experiment.
	 * @return {@link PurificationExperiment} object derived from the network
	 * @throws ProCopeException if the network is undirected
	 */
	public PurificationData derivePurificationData(boolean poolBaits) throws ProCopeException {
		// javadoc: beachtet keine annotation edges
		if (!directed)
			throw new ProCopeException("Can only derive purification data from directed networks.");
		
		PurificationData result = new PurificationData();
		
		if (!poolBaits) {
			// do not pool baits, every edge is a single purification experiment
			int[] edges = getEdgesArray();
			for (int i=0; i<edges.length; i+=2) {
				PurificationExperiment exp = new PurificationExperiment(edges[i]);
				exp.addPrey(edges[i+1]);
				result.addExperiment(exp);
			}
		} else {
			// pool baits, all outgoing edges of one protein are treated as one purification experiment
			Set<Integer> sources = interactionMatrix.getFirstPartners();
			for (int source : sources) {
				PurificationExperiment exp = new PurificationExperiment(source);
				for (int prey : interactionMatrix.getPartners(source))
					exp.addPrey(prey);
				result.addExperiment(exp);
			}
		}
		
		return result;
	}
	
	/**
	 * Helper interface used for network searching, capsulates functionality of Stack and Queue
	 */
	private interface WaitList<E>{
		public E next();
		public boolean isEmpty();
		public void put(E item);
	}
	
	/**
	 * Helper class simple wrapping a Queue with the WaitList interface
	 */
	private class WaitQueue<E> implements WaitList<E>{
		private Queue<E> queue;
		public WaitQueue() {
			this.queue = new LinkedBlockingQueue<E>();
		}
		public boolean isEmpty() {
			return queue.isEmpty();
		}
		public E next() {
			return queue.poll();
		}
		public void put(E item) {
			queue.add(item);
		}
	}
	
	/**
	 * Helper class simple wrapping a Stack with the WaitList interface
	 */
	private class WaitStack<E> implements WaitList<E>{
		private Stack<E> stack;
		public WaitStack() {
			this.stack = new Stack<E>();
		}
		public boolean isEmpty() {
			return stack.isEmpty();
		}
		public E next() {
			return stack.pop();
		}
		public void put(E item) {
			stack.push(item);
		}
	}


}

