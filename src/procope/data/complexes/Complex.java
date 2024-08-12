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
package procope.data.complexes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import procope.data.ProteinSet;
import procope.data.networks.ProteinNetwork;
import procope.tools.namemapping.ProteinManager;


/**
 * Represents a complex of proteins. This class provides various manipulation,
 * retrieval and and analysis methods.<p>
 * 
 * Proteins are represented as internal integer IDs (see also: 
 * {@link procope.tools.namemapping.ProteinManager}) 
 * 
 * @author Jan Krumsiek
 * @see procope.data.complexes.ComplexSet
 */
public class Complex implements Iterable<Integer>, ProteinSet {

	private ArrayList<Integer> complex;
	
	/**
	 * Intializes the complex with a given list of proteins. <b>Note:</b>The
	 * original Collection object is copied and will not be altered.
	 * 
	 * @param complex Collection with proteins IDs for initialization
	 */
	public Complex(Collection<Integer> complex) {
		this.complex = new ArrayList<Integer>(complex);
	}
	
	/**
	 * Intializes the complex with a given integer array provided as a c
	 * onvenient parameter list.
	 * 
	 * @param elements Array with proteins IDs for initialization
	 */
	public Complex(Integer ... elements) {
		this.complex = new ArrayList<Integer>();
		for (Integer protein : elements)
			complex.add(protein);
	}
	
	/**
	 * Creates an empty complex.
	 */
	public Complex() {
		this.complex = new ArrayList<Integer>();
	}
	
	/**
	 * Adds a protein to the complex.
	 * 
	 * @param protein internal ID of the protein to be added
	 */
	public void addProtein(int protein) {
		complex.add(protein);
	}
	
	/**
	 * Adds a list of proteins to the complex.
	 * 
	 * @param proteins Collection of internal IDs of proteins to be added to the complex
	 */
	public void addProteins(Collection<Integer> proteins) {
		complex.addAll(proteins);
	}
	
	/**
	 * Removes the protein at a given index from the complex. 
	 * 
	 * @param index <b>index</b> of protein to be removed (zero-based)
	 * @throws IndexOutOfBoundsException if the index is out of range for this complex
	 */
	public void removeProteinIndex(int index) throws IndexOutOfBoundsException {
		try {
			complex.remove(index);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Invalid protein index for this complex: " + index);
		}
	}


	/**
	 * Removes a given protein from the complex. Be careful not to confuse this
	 * method with {@link #removeProteinIndex(int)}.
	 * 
	 * @param protein internal protein ID of member to be removed
	 * @return {@code true} if the protein was contained in the complex and
	 *         could be removed, <tt>false</tt> otherwise 
	 */
	public boolean removeProtein(Integer protein) {
		return complex.remove((Object)protein);
	}
	

	/**
	 * Returns an iterator over the interal protein IDs in this complex.
	 * 
	 * @return iterater over internal Ids
	 */
	public Iterator<Integer> iterator() {
		return complex.iterator();
	}

	/**
	 * Returns the set of internal IDs which are involved in this complex.
	 */
	public Set<Integer> getProteins() {
		return new HashSet<Integer>(complex);
	}
	
	/**
	 * Returns the size of the complex.
	 * 
	 * @return Size of this complex, that is the number of proteins contained in the complex.
	 */
	public int size() {
		return complex.size();
	}
	
	/**
	 * Returns the protein at a given index of in the complex.
	 * 
	 * @param index index of protein to retrieve
	 * @return internal ID of protein at given index.
	 * @throws IndexOutOfBoundsException if the index is out of range for this complex
	 */
	public int getMember(int index) throws IndexOutOfBoundsException {
		try {
			return complex.get(index);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Invalid protein index for this complex: " + index);
		}
	}
	
	/**
	 * Returns the list backing this complex. <b>Attention:</b> changes to this
	 * object will also affect the complex.
	 * 
	 * @return List of internal IDs backing this complex
	 */
	public List<Integer> getComplex() {
		return complex;
	}
	
	/**
	 * Returns the string representation of this complex as the string 
	 * representation of the backing list of internal IDs
	 */
	@Override
	public String toString() {
		return complex.toString();
	}
	
	/**
	 * Returns {@code true} if and only if the specified object is also a 
	 * Complex and both complexes have identical members.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null) return false;
		if (obj == this) return true;
		// only compare against other complexes
		if (!(obj instanceof Complex))
			return false;
	
		List<Integer> other = ((Complex)obj).getComplex();
		
		// first: check sizes
		if (complex.size() != other.size())
			return false;

		return other.containsAll(complex);
	}
	
	/**
	 * Checks whether a given protein is contained in this complex.
	 * 
	 * @param protein internal ID of protein to be checked for
	 * @return {@code true} if the protein is contained in this complex, 
	 *         {@code false} otherwise 
	 */
	public boolean contains(int protein) {
		return complex.contains(protein);
	}
	
	/**
	 * Returns the index of a given protein in the complex. If there are 
	 * multiple occurences of this protein in the complex, only the first
	 * position will be returned.
	 * 
	 * @param protein internal ID of protein to be checked for
	 * @return Index of the protein or -1 if the protein is not contained in
	 *         the complex
	 */
	public int indexOf(int protein) {
		return complex.indexOf((Integer)protein);
	}
	
	/**
	 * Creates a copy of this complex
	 * 
	 * @return Exact copy of the complex
	 */
	public Complex copy() {
		return new Complex(new ArrayList<Integer>(complex));
	}
	
	/**
	 * Returns the minimal spanning tree of this complex as a network object. 
	 * The method uses Kruskal's algorithm for efficient MST calculation.
	 * <b>Note:</b> This implementation is based on similarity values, not
	 * distances. Thus the result will be the maximum-weight spanning tree
	 * of the complex graph. If the graph consists of multiple components due
	 * to missing edges, no MST can be calculated.
	 * 
	 * @param scores Scores network to be used for weighting the complex edges
	 * @return Minimal spanning tree of the complex or {@code null} if no 
	 * spanning tree exists due to missing edges.
	 */
	@SuppressWarnings("unchecked")
	public ProteinNetwork getMinimalSpanningTree(ProteinNetwork scores) {
		
		ProteinNetwork result = new ProteinNetwork();
		
		// determine all nonzero edges and insert into priority queue
		ArrayList<ComplexEdge> list = new ArrayList<ComplexEdge>();
		Integer[] arr = complex.toArray(new Integer[0]);
		for (int i=0; i<arr.length; i++) {
			for (int j=i+1; j<arr.length; j++) {
				float score = scores.getEdge(arr[i], arr[j]);
				if (score == score) { // NaN check
					list.add(new ComplexEdge(i, j, score));
				}
			}
		}
		Collections.sort(list);
		
		// initialize data structure which holds the graph components each protein belongs to
		final int proteins = complex.size();
		HashSet<Integer>[] components = new HashSet[proteins];
		for (int i=0; i<proteins; i++) {
			HashSet<Integer> set = new HashSet<Integer>();
			set.add(i);
			components[i] = set;
		}
		// iterate over sorted edges
		int numEdges = list.size();
		for (int i=0; i<numEdges; i++) {
			ComplexEdge edge = list.get(i);
			// check if both members are in different components
			if (components[edge.proteinIndex1] != components[edge.proteinIndex2]) {
				// use edge
				System.out.println("Using: " + edge);
				result.setEdge(arr[edge.proteinIndex1], arr[edge.proteinIndex2]);
				// merge components
				components[edge.proteinIndex1].addAll(components[edge.proteinIndex2]);
				HashSet<Integer> toReplace= components[edge.proteinIndex2];
				for (int member : toReplace)
					components[member] =components[edge.proteinIndex1];
			}
		}
		
		
		// there must be only one component left
		if (components[0].size() < proteins)
			return null;
		else
			return result;
		
	}
	
	/**
	 * Represents a scored edge within a complex, needed for minimal spanning tree calculation
	 */
	private class ComplexEdge implements Comparable<ComplexEdge> {
		public int proteinIndex1,proteinIndex2;
		public float score;
		public ComplexEdge(int proteinIndex1, int proteinIndex2, float score) {
			this.proteinIndex1 = proteinIndex1;
			this.proteinIndex2 = proteinIndex2;
			this.score = score;
		}
		public int compareTo(ComplexEdge o) {
			return (int)Math.signum(o.score-this.score); 
		}
		public String toString() {
			return proteinIndex1+","+proteinIndex2+":"+score;
		}
	}
	
	/**
	 * Convenience and debugging method which translates this complex into a
	 * list of protein labels
	 * 
	 * @return list of protein labels for this complex
	 * @see procope.tools.namemapping.ProteinManager
	 */
	public ArrayList<String> getComplexAsLabels() {
		ArrayList<String> result = new ArrayList<String>();
		for (int protein : complex)
			result.add(ProteinManager.getLabel(protein));
		return result;
	}

	/**
	 * Returns the intersection of both complexes. The intersection means the 
	 * list of proteins which are contained in both complexes. When working 
	 * with complexes containing certain proteins more than once, this method 
	 * will only returnthe intersecting subset of this complex, not the other. 
	 * 
	 * @param other Complex to calculate intersection with
	 * @return intersecting set of proteins
	 */
	public Complex intersection(Complex other) {
		ArrayList<Integer> dummy = new ArrayList<Integer>(complex);
		dummy.retainAll(other.complex);
		return new Complex(dummy);
	}
	
	/**
	 * Calculates the number of overlapping proteins with another complex.
	 * 
	 * @param other The other complex to which this one is compared
	 * @return number of overlapping proteins
	 */
	public int calculateOverlap(Complex other) {
		Set<Integer> overlap = new HashSet<Integer>(this.getProteins());
		overlap.retainAll(other.getProteins());
		return overlap.size();
	}
	
	/**
	 * Calculates the Jaccard index as a measure of similarity between
	 * two complexes.
	 * 
	 * @param other The other complex to which this one is compared
	 * @return Jaccard index between the two complexes
	 */
	public float calculateJaccardIndex(Complex other) {
		Set<Integer> diff = new HashSet<Integer>(this.getProteins());
		diff.retainAll(other.getProteins());
		Set<Integer> union = new HashSet<Integer>(this.getProteins());
		union.addAll(other.getProteins());
		
		return (float)diff.size()/(float)union.size();
		
	}
	
}
