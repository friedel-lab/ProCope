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
import java.util.Iterator;
import java.util.List;

import procope.data.complexes.ComplexSet;


/**
 * Represents a list of mappings between complexes of two complex sets.
 * Result of the mapping methods in {@link ComplexSetComparison}.
 * 
 * @author Jan Krumsiek
 * @see ComplexMapping
 */

public class ComplexMappings implements Iterable<ComplexMapping> {
	

	private List<ComplexMapping> mappings;
	private ComplexSet setA, setB;
	private List<Integer> nonMappedA, nonMappedB;

	/**
	 * Creates a new set of complex mappings.
	 * 
	 * @param setA first complex set
	 * @param setB second complex set
	 * @param mappings list of mappings
	 */
	public ComplexMappings(ComplexSet setA, ComplexSet setB, 
			List<ComplexMapping> mappings) {
		this.mappings = mappings;
		this.setA = setA;
		this.setB = setB;
		
		// calculate the non-mapped complexes
		int complexesA = setA.getComplexCount();
		int complexesB = setB.getComplexCount();
		byte[] mappedA = new byte[complexesA];
		byte[] mappedB = new byte[complexesB];
		for (ComplexMapping mapping : mappings) {
			mappedA[mapping.complexInA] = 1;
			mappedB[mapping.complexInB] = 1;
		}
		nonMappedA = nonMapped(mappedA);
		nonMappedB = nonMapped(mappedB);
		
	}
	
	/**
	 * Returns a list of indices of complexes which are not mapped,
	 * required bit-vector of mapped complexes 
	 */
	private ArrayList<Integer> nonMapped(byte[] mapped) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i=0; i<mapped.length; i++) {
			if (mapped[i] == 0)
				result.add(i);
		}
		return result;
	}
	
	/**
	 * Returns the first complex set involved in this mapping set.
	 * 
	 * @return first complex set of this mapping set
	 */
	public ComplexSet getSetA() {
		return setA;
	}

	/**
	 * Returns the second complex set involved in this mapping set.
	 * 
	 * @return second complex set of this mapping set
	 */
	public ComplexSet getSetB() {
		return setB;
	}

	/**
	 * Returns an iterator over the {@link ComplexMapping} objects int this list
	 */
	public Iterator<ComplexMapping> iterator() {
		return mappings.iterator(); 
	}
	
	/**
	 * Returns the indices of all complexes of set A which are <u>not</u>
	 * mapped in this mapping set
	 * @return indices of set A which are not mapped
	 */
	public List<Integer> getNonMappedComplexesA() {
		return nonMappedA;
	}

	/**
	 * Returns the indices of all complexes of set B which are <u>not</u>
	 * mapped in this mapping set
	 * @return indices of set B which are not mapped
	 */
	public List<Integer> getNonMappedComplexesB() {
		return nonMappedB;
	}

	/**
	 * Returns the number of mappings in this mapping set
	 * 
	 * @return number of mappings 
	 */
	public int size() {
		return mappings.size();
	}
	
}
