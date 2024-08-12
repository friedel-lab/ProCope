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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import procope.data.ProteinSet;
import procope.data.networks.ProteinNetwork;
import procope.methods.scores.ComplexScoreCalculator;
import procope.tools.ProCopeException;
import procope.tools.Tools;


/**
 * A complex set is a list of complexes. This class contains various methods
 * which work on this complex list, e.g. methods for manipulating, selecting 
 * and randomizing complexes.
 * 
 * <p>ComplexSet is one of the major classes of this software library and is used
 * in many different classes and methods.
 * 
 * @author Jan Krumsiek
 * @see procope.data.complexes.Complex
 * @see procope.data.complexes.ComplexSetReader
 * @see procope.data.complexes.ComplexSetWriter
 */

public class ComplexSet implements Iterable<Complex>, ProteinSet {

	private ArrayList<Complex> complexes;
	private boolean proteinsAltered = false;
	
	// proteins
	private Set<Integer> proteins;
	private int numberOfMembers=0;
	private int highestProteinID;
	
	private static final float EPSILON = 0.001f; 

	
	/**
	 * Creates an empty complex set
	 */
	public ComplexSet() {
		// initialize
		this.complexes = new ArrayList<Complex>();
		collectProteins();
	}
	

	/**
	 * Creates a complex set from a given list of lists of internal IDs.
	 * Each of this lists of internal IDs represents one complex. 
	 * 
	 * <p>The constructor will create copies of the collections, the
	 * original objects will not be altered.
	 * 
	 * @param newComplexes List of lists of internal IDs
	 */
	public ComplexSet(Collection<? extends Collection<Integer>> newComplexes) {
		this.complexes = new ArrayList<Complex>();
		for (Collection<Integer> complex : newComplexes)
			this.complexes.add(new Complex(complex));
		collectProteins();
	}
	
	/**
	 * Get the set of proteins involved in this complex set, the
	 * highest internal ID of any protein in this set and the total
	 * number of proteins 
	 */
	private void collectProteins() {
		proteins = new HashSet<Integer>();
		highestProteinID = -1;
		numberOfMembers = 0;
		// iterate over complex set and collect proteins
		for (Complex complex : complexes) {
			for (int prot : complex) {
				proteins.add(prot);
				if (prot > highestProteinID)
					highestProteinID = prot;
			}
			numberOfMembers += complex.size();
		}
		proteinsAltered = false;
	}
	
	
	/**
	 * Returns the list of complexes backing this complex set. <b>Note:</b>
	 * A reference to the <u>original</u> backing list will be returned, changes to
	 * this object will also affect the complex set.
	 * 
	 * @return The list of complexes contained in this set
	 */
	public List<Complex> getComplexes() {
		return complexes;
	}
	
	/**
	 * Returns the number of complexes in this set
	 * 
	 * @return number of complexes in this set
	 */
	public int getComplexCount() {
		return complexes.size();
	}
	
	/**
	 * Returns the number of proteins in this set as the sum of the single 
	 * complex sizes. <b>Note:</b> For complex sets where proteins are 
	 * contained in multiple complexes, this value will be larger than
	 * {@code getProteins().size()}.
	 * 
	 * @return number of proteins in this complex set
	 */
	public int getProteinCount() {
		if (proteinsAltered) collectProteins();
		return numberOfMembers;
	}
	
	/**
	 * Returns a randomized copy of the complex set. Randomization is achieved
	 * using a random permutation of the proteins. That means, for instance, that
	 * all occurences of Protein1 will be replaced by Protein4, all occurences
	 * of Protein2 will be replaced by Protein3 and so on. The sizes of the
	 * complexes will be preserved.
	 *
	 * @return a randomized copy of the complex set
	 */
	public ComplexSet randomizeByRemapping() {
		
		Set<Integer> locProteins = getProteins();
		Random random = Tools.random;
		
		// create mapping
		int[] mapping = new int[highestProteinID+1];
		ArrayList<Integer> vecProteins = new ArrayList<Integer>(locProteins);
		// iterate over proteins 
		for (int protein : locProteins) {
			// get another protein this one will be mapped to
			int randomIndex =  random.nextInt(vecProteins.size());
			mapping[protein] = vecProteins.get(randomIndex);
			vecProteins.remove(randomIndex);
		}

		// now shuffle
		ArrayList<ArrayList<Integer>> shuffled = new ArrayList<ArrayList<Integer>>();

		for (Complex complex : complexes) {
			ArrayList<Integer> newvec = new ArrayList<Integer>();
			for (Integer protein : complex) {
				newvec.add(mapping[protein]);
			}
			shuffled.add(newvec);
		}

		return new ComplexSet(shuffled);
	}
	
	
	/**
	 * Returns a randomized copy of the complex set. Randomization is achieved
	 * by randomly exchanging proteins between complexes. The size of the 
	 * complexes as well as the number of complexes a protein is contained
	 * in will be preserved.
	 * 
	 * @return a randomized copy of the complex set
	 */
	public ComplexSet randomizeByExchanging() {
		
		ComplexSet shuffled = this.copy();
		Random random = Tools.random;
		
		final int iterations = getProteinCount()*10;
		final int numComplexes = getComplexCount();
		
		for (int i=0; i<iterations; i++) {
			// select two random complexes
			Complex complex1 = shuffled.complexes.get(random.nextInt(numComplexes));
			Complex complex2 = shuffled.complexes.get(random.nextInt(numComplexes));
			// select two random proteins
			int protIndex1 = (int)(random.nextInt(complex1.size()));
			int protIndex2 = (int)(random.nextInt(complex2.size()));
			// get the proteins
			int protein1 = complex1.getMember(protIndex1);
			int protein2 = complex2.getMember(protIndex2);
			// check if neither is contained in the other complex
			if (!complex1.contains(protein2) && !complex2.contains(protein1)) {
				// exchange them
				complex1.removeProtein(protein1);
				complex1.addProtein(protein2);
				complex2.removeProtein(protein2);
				complex2.addProtein(protein1);
			}
		}
		
		return shuffled;
		
	}

	/**
	 * Creates a copy of this complex set
	 * 
	 * @return a copy of the complex set
	 */
	public ComplexSet copy() {
		ComplexSet theCopy = new ComplexSet();
		for (Complex complex : this) {
			theCopy.addComplex(complex.copy());
		}
		return theCopy;
	}

	/**
	 * Returns an iterator over the Complex objects in this set
	 */
	public Iterator<Complex> iterator() {
		return complexes.iterator();
	}

	/**
	 * Returns the complex at a given index in the complex set list.
	 * 
	 * @param index index of the complex which will be retrieved
	 * @return Complex at the given index
	 * @throws IndexOutOfBoundsException if the complex index is invalid for this complex set
	 */
	public Complex getComplex(int index) throws IndexOutOfBoundsException {
		try {
			return complexes.get(index);
		} catch (ArrayIndexOutOfBoundsException e){
			throw new IndexOutOfBoundsException("Invalid complex index: " + index);
		}
	}

	/**
	 * Returns the set of proteins involved in this complex set.
	 */
	public Set<Integer> getProteins() {
		if (proteinsAltered) collectProteins();
		return proteins;
	}	
	
	/**
	 * Adds a complex to the complex set.
	 * 
	 * @param toAdd Complex to be added
	 * @throws ProCopeException if {@code toAdd} is an empty complex
	 */
	public void addComplex(Complex toAdd) throws ProCopeException {
		if (toAdd == null)
			throw new ProCopeException("Cannot add null.");
		if (toAdd.size() == 0)
			throw new ProCopeException("Cannot add empty complex.");
		proteinsAltered = true;
		complexes.add(toAdd);
	}
	
	
	/**
	 * Removes the complex at the specified index from the complex set.
	 * 
	 * @param toRemove index of complex to be removed from the set
	 * @throws IndexOutOfBoundsException if the complex index is invalid for this complex set
	 */
	public void removeComplex(int toRemove) throws IndexOutOfBoundsException {
		proteinsAltered = true;
		complexes.remove(toRemove);
	}
	
	/**
	 * Removes a given complex from the complex set. A complex {@code o} from the set will
	 * be removed if and only if {@code toRemove.equals(o)}, i.e. if the complexes are equal.
	 * 
	 * @param toRemove Complex to be removed
	 * @return {@code true} if a complex was removed from the set, {@code false} otherwise
	 */
	public boolean removeComplex(Complex toRemove) {
		proteinsAltered = true;
		return complexes.remove(toRemove);
	}
	
	/**
	 * Removes singletons from the complex set. Singletons are complexes
	 * containing only one protein. <b>Note:</b> This method will alter
	 * the complex set object directly and return the removed singletons
	 * as a new complex set object.
	 * 
	 * @return removed singletons
	 */
	public ComplexSet removeSingletons() {
		return removeComplexesBySize(2, true);
	}
	
	/**
	 * Removes all complexes smaller or larger than a given threshold from the 
	 * complex set. <b>Note:</b> This method will alter the complex set object 
	 * directly and return the removed complexes as a new complex set object.
	 * 
	 * @param cutoffSize size threshold
	 * @param below If this is {@code true}, all complexes with size < 
	 * {@code cutoffSize} will be removed. If {@code below} is {@code false},
	 * all complexes having > {@code cutoffSize} proteins will be removed.  
	 * @return set of removed complexes
	 */
	public ComplexSet removeComplexesBySize(int cutoffSize, boolean below) {
		ArrayList<Complex> toRemove = new ArrayList<Complex>();
		for (Complex complex : complexes) {
			if ((below && complex.size() < cutoffSize) || (!below && complex.size() > cutoffSize))
				toRemove.add(complex);
		}
		complexes.removeAll(toRemove);
		
		ComplexSet result = new ComplexSet();
		result.addComplexes(toRemove);
		
		proteinsAltered = true;
		return result;
	}
	
	/**
	 * Adds a list of complexes to the set 
	 * 
	 * @param toadd list of complexes to add
	 */
	public void addComplexes(Collection<Complex> toadd) {
		complexes.addAll(toadd);
		proteinsAltered = true;
	}
	
	/**
	 * Returns a string representation of this complex set constructed by
	 * a list of string representations of the contained {@link Complex} 
	 * objects
	 */
	@Override
	public String toString() {
		return complexes.toString();
	}
	
	/**
	 * Checks if two complex sets are equal.
	 * Returns {@code true} if and only if (1) the specified object is also a
	 * complex set, (2) both sets have the same number of complexes and (3) 
	 * each complex {@code c1} in this set has a complex {@code c2} in the 
	 * other set such that {@code c1.equals(c2) == true}.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null) return false;
		if (obj == this) return true;
		// only compare against other complex sets
		if (!(obj instanceof ComplexSet))
			return false;
		
		ArrayList<Complex> other = ((ComplexSet)obj).complexes;
		// first check sizes 
		if (other.size() != complexes.size())
			return false;
		
		return other.containsAll(complexes);
	}
	
	/**
	 * Checks if a specified complex is contained in this set. That means
	 * there exists a complex {@code c1} in this set such that 
	 * {@code c1.equals(complex) == true} 
	 * @param complex Complex object which will be searched in the set
	 * @return {@code true} if the complex is contained in the set, 
	 *         {@code false} otherwise
	 */
	public boolean contains(Complex complex) {
		return complexes.contains(complex);
	}
	
	/**
	 * Creates a network which contains a fully connected subgraph for each
	 * complex. A fully connected subgraph is a subgraph where all nodes are
	 * connected to each other. All edges get a weight of 1.0.
	 * 
	 * @return network containing a fully connected subgraph for each complex
	 */
	public ProteinNetwork getComplexInducedNetwork() {
		
		ProteinNetwork result = new ProteinNetwork(false);
		// iterate over all complexes
		for (Complex complex : complexes) {
			// iterate over all pairwise proteins
			Integer[] proteins = complex.getComplex().toArray(new Integer[0]);
			for (int i=0; i<proteins.length; i++) {
				for (int j=i+1; j<proteins.length; j++) {
					// set the edge
					result.setEdge(proteins[i], proteins[j]);
				}
			}
		}
		
		return result;
		
	}

	/**
	 * Decomposes a complex set with respect to a given scores network. All
	 * complexes are treated as subgraphs, the edge weights within these graphs
	 * are taken from the scores network. All edges below the given cutoff will
	 * be deleted and the smaller components resulting from eventual 
	 * decompositions will be treated as single complexes.
	 * 
	 * <p>The result will contain at least the number of complexes as the 
	 * original complex set.
	 * 
	 * @param scores scores network to be used for decomposition
	 * @param cutoff value below which edges will be deleted from the complex graphs
	 * @return decomposed complex  
	 */
	public ComplexSet decompose(ProteinNetwork scores, float cutoff) {
		
		if (scores.isDirected())
			throw new ProCopeException("Cannot use directed networks for decomposition.");
		
		ComplexSet result = new ComplexSet();
		
		// iterate over complexes
		for (Complex complex : complexes) {
			// create DFS array
			ArrayList<Integer> newComplex = null;
			int numVisited=0;
			boolean[] visited = new boolean[complex.size()];
			Stack<Integer> stack = new Stack<Integer>();
			// add first node
			stack.push(0);
			visited[0] = true;
			numVisited++;
			newComplex = new ArrayList<Integer>();
			newComplex.add(complex.getMember(0));
			// repeat full complex was visited
			while (numVisited < complex.size()) {
				int current = stack.pop();
				int prot = complex.getMember(current);
				// add neighbors
				for (int neighbor : scores.getNeighborArray(prot)) {
					// neighbor is in the complex?
					int neighborIndex = complex.indexOf(neighbor);
					if (neighborIndex >= 0) {
						// only check unvisited nodes
						if (!visited[neighborIndex]) {
							// sufficient edge?
							if (scores.getEdge(prot, neighbor) >= cutoff) {
								// visit this one next
								stack.push(neighborIndex);
								visited[neighborIndex] = true;
								numVisited++;
								newComplex.add(neighbor);
							}
						}
					}
					
				}
				
				// check if stack is empty and we're not done yet
				if (stack.isEmpty() && numVisited < complex.size()) {
					// this component is finished
					
					// restart with next unvisited node
					for (int i=0; i<complex.size(); i++) {
						if (!visited[i]) {
							// add to list of complexes, than reinitialize list
							result.addComplex(new Complex(newComplex));
							newComplex = new ArrayList<Integer>();
							// now start with this unvisited node
							stack.push(i);
							visited[i] = true;
							numVisited++;
							newComplex.add(complex.getMember(i));
							break;
						}
					}
				}
			}
			
			// add last one
			result.addComplex(new Complex(newComplex));
			
		}
		
		return result;
		
	}

	/**
	 * Returns a new complex set containing only those complexes whose proteins
	 * are completely or partially contained in a given set of proteins.
	 * 
	 * @param proteins set of proteins for restriction
	 * @param fullCoverage if this value is {@code true}: all proteins of a 
	 *        complex have to be contained in the reference set in order to be 
	 *        returned. if {@code fullCoverage==false} only one protein of a 
	 *        complex has to be contained in the protein set. 
	 *         
	 * @return Complex set restricted to the given protein space
	 */
	public ComplexSet restrictToProteinSpace(ProteinSet proteins, boolean fullCoverage) {
		return restrictToProteins(proteins.getProteins(), fullCoverage);
	}
		
	/**
	 * Returns a new complex set containing only those complexes whose proteins
	 * are completely or partially contained in a given set of proteins.
	 * 
	 * @param proteins set of proteins for restriction
	 * @param fullCoverage if this value is {@code true}: all proteins of a 
	 *        complex have to be contained in the reference set in order to be 
	 *        returned. if {@code fullCoverage==false} only one protein of a 
	 *        complex has to be contained in the protein set. 
	 *         
	 * @return Complex set restricted to the given protein space
	 */
	public ComplexSet restrictToProteins(Set<Integer> proteins, boolean fullCoverage) {
		
		ComplexSet result = new ComplexSet();
		
		// convert set to boolean array
		int maxID = Tools.findMax(proteins);
		boolean[] inSet = new boolean[maxID+1];
		for (int protein : proteins)
			inSet[protein] = true;
		
		// iterate over complexes
		for (Complex complex : complexes) {
			boolean complexAccepted = fullCoverage; // for full coverage: check if it is NOT accepted, for not full coverage: vice versa
			// iterate over protein
			for (int protein : complex) {
				if (!fullCoverage && (protein <= maxID && inSet[protein]) ) {
					// contained, and one is enough
					complexAccepted=true;
					break;
					
				} else if (fullCoverage && (protein >maxID || !inSet[protein])) {
					// not contained and all were needed
					complexAccepted = false;
					break;
				}
			}
			// now add
			if (complexAccepted)
				result.addComplex(complex.copy());
		}
		return result;
		
	}
	
	/**
	 * Removes all complexes from the complex set whose average edge score
	 * between all proteins of the complex regarding a given scores network 
	 * is below the cutoff.
	 * 
	 * @param scores scores network
	 * @param cutoff cutoff
	 * @return the complexes which were removed from the set
	 */
	public ComplexSet removeComplexesByScore(ProteinNetwork scores, float cutoff) {
		return removeComplexesByScore(scores, cutoff, false);
	}
	
	/**
	 * Removes all complexes from the complex set whose average edge score
	 * between all proteins of the complex regarding a given scores network 
	 * is below the cutoff.
	 * 
	 * @param scores scores network
	 * @param cutoff cutoff
	 * @param ignoreMissing {@code true}: scores of edges which do not exist
	 *                      in the score network are not considered for average
	 *                      calculation; {@code false}: missing scores ar 
	 *                      treated as 0
	 * @return the complexes which were removed from the set
	 */
	public ComplexSet removeComplexesByScore(ProteinNetwork scores, float cutoff, boolean ignoreMissing) {
		ArrayList<Complex> toRemove = new ArrayList<Complex>();
		for (Complex complex : complexes) {
			float score = ComplexScoreCalculator.averageComplexScore(scores, complex);
			if (score < cutoff)
				toRemove.add(complex);
		}
		complexes.removeAll(toRemove);
		proteinsAltered = true;

		ComplexSet result = new ComplexSet();
		result.addComplexes(toRemove);
		return result;
	}
	
	/**
	 * Sorts the list of complexes by their size.
	 * 
	 * @param ascendingly if {@code true} the smallest complex will be the 
	 *                    first complex in the list after sorting, if 
	 *                    {@code false} it will be the  largest complex 
	 */
	public void sortBySize(final boolean ascendingly) {
		Collections.sort(complexes, new Comparator<Complex>(){
			public int compare(Complex o1, Complex o2) {
				if (ascendingly)
					return (int)Math.signum(o1.size()-o2.size());
				else
					return (int)Math.signum(o2.size()-o1.size());
			}
		});
	}
	
	/**
	 * Calculates shared proteins with respect to a given interaction network
	 * as proposed by Pu et al., 2007 (Pubmed: 17370254). Parameters proposed 
	 * in the paper: a=1.5, b=-0.5.
	 * <p>A protein is added to a given complex if it interacts with a minimum
	 * fraction of a*S**b proteins of that complex (where ** is the power 
	 * function and S is the size of the acceptor complex). 
	 * 
	 * @param interactions protein interaction network to be used
	 * @param a a parameter
	 * @param b b parameter
	 * @return complex set containing shared proteins
	 */
	public ComplexSet calculateSharedProteinsPu(ProteinNetwork interactions, float a, float b) {
		ComplexSet result = copy();

		Set<Integer> proteins = getProteins();
		
		// iterate over all complexes
		int complexIndex = 0;
		for (Complex complex : complexes) {
			// gather all proteins which have an edge to this complex
			HashSet<Integer> done = new HashSet<Integer>();
			for (int protein : complex) {
				int[] arr = interactions.getNeighborArray(protein);
				for (int i=0; i<arr.length; i++) {
 					int candidate = arr[i];
 					if (!complex.contains(candidate) && !done.contains(candidate) && proteins.contains(candidate)) {
 						// determine the number if interaction partners in the complex
 						int numInteractions=0;
 						for (Integer complexMember : complex) {
 							float score = interactions.getEdge(complexMember, candidate);
 							if (score == score) // NaN check
 								numInteractions++;
 						}
 						// check if protein fulfills formula for this complex
 						if (numInteractions >= a*Math.pow(complex.size(), b)) {
 							result.getComplex(complexIndex).addProtein(candidate);
 						}
 						done.add(candidate);
 					}
 				}
			}
			complexIndex++;
		}
		
		return result;
	}
	
	

	/**
	 * Calculates the complex set with added shared proteins using a given
	 * scores network. The <i>lambda</i> parameter describes the scores
	 * theshold for the newly created complex. For example, if <i>lambda = 0.95</i>
	 * (the recommended value) the complex set with shared proteins is
	 * calculated whose average complex score is about 95% of the original
	 * complex set. Check out the documentation for more details.
	 * 
	 * @param scores scores network to be used for evaluation
	 * @param lambda lambda parameter (see description of this method)
	 * 
	 * @return the complex set containing added shared proteins
	 */
	public ComplexSet calculateSharedProteinsBootstrap(ProteinNetwork scores, float lambda) {
		float minGamma = 0;
		float maxGamma = 5;

		float originalAverage = ComplexScoreCalculator.averageComplexSetScore(scores, this, true);
		float tolerance = lambda * originalAverage;
		float currentAverage = Float.NaN;
		float lastAverage = Float.NaN;

		ComplexSet current =  null;
		do {
			lastAverage = currentAverage;
			float curGamma = (minGamma + maxGamma) / 2f;
			current = calculateSharedProteinsSet(scores, this,	lambda, curGamma);
			currentAverage = ComplexScoreCalculator.averageComplexSetScore(scores, current, true);
			
			// adapt borders
			if (currentAverage <= tolerance)
				// score too low, decrease gamma
				maxGamma = curGamma;
			else
				minGamma = curGamma;

		} while (!(currentAverage >= tolerance && currentAverage-tolerance<EPSILON) && currentAverage != lastAverage);

		return current;
	}

	
	/**
	 * Returns the complex set with added shared proteins for a complex
	 * wrt to a given scores network and the alpha and gamma parameter
	 */
	private static ComplexSet calculateSharedProteinsSet(ProteinNetwork scores,
			ComplexSet complexes, float alpha, float gamma) {

		ComplexSet result = complexes.copy();

		// calculate average complex scores once
		float[] avgComplexScores = new float[complexes.getComplexCount()];
		int complexIndex = 0;
		for (Complex complex : complexes) {
			avgComplexScores[complexIndex] = ComplexScoreCalculator.averageComplexScore(scores, complex);
			complexIndex++;
		}
		
		Set<Integer> proteins = complexes.getProteins();
		
		// iterate over all complexes
		complexIndex = 0;
		for (Complex complex : complexes) {
			// gather all proteins which have an edge to this complex
			HashSet<Integer> done = new HashSet<Integer>();
			for (int protein : complex) {
				int[] arr = scores.getNeighborArray(protein);
				for (int i=0; i<arr.length; i++) {
 					int candidate = arr[i];
 					if (!complex.contains(candidate) && !done.contains(candidate) && proteins.contains(candidate)) {
 						// calculate average score of protein to current complex
 						float avgScore = 0;
 						for (Integer complexMember : complex) {
 							float score = scores.getEdge(complexMember, candidate);
 							if (score == score) // NaN check
 								avgScore += score;
 						}
 						avgScore /= (float) complex.size();
 						// check if protein fulfills formula for this complex
 						if (avgScore > alpha * avgComplexScores[complexIndex] * (Math.pow(complex.size(), -gamma) / Math.pow(2,	-gamma))) {
 							result.getComplex(complexIndex).addProtein(candidate);
 						}
 						done.add(candidate);
 					}
 				}
			}
			complexIndex++;
		}
		
		return result;

	}
	
}
