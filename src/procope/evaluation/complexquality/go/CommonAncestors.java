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
// done
package procope.evaluation.complexquality.go;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Calculates and holds common ancestors for a given GO network. The ancestors
 * of a GO Term are all parent terms on the way to the root node.
 * 
 * @see GONetwork
 * @author Jan Krumsiek
 */
public class CommonAncestors {
	
	private HashMap<String, Set<String>> ancestorset;

	
	/**
	 * Creates common ancestors object for a given GO network. The common
	 * ancestors are automatically calculated in this initialization step.
	 * 
	 * @param goNetwork the GO network for which common ancestors are calculated
	 */
	public CommonAncestors(GONetwork goNetwork)  {
		ancestorset = new HashMap<String, Set<String>>();
		// iterate over all GO terms and calculate the ancestors recursively
		for (GOTerm term : goNetwork.getAllTerms()) {
			recFindAncestors(term);
		}
	}
	
	/**
	 * Returns all ancestors of a given GO term.
	 * 
	 * @param term identifier of the GO term
	 * @return identifiers of all ancestors of that term
	 */
	public Collection<String> getAncestors(String term) {
		return ancestorset.get(term);
	}
	
	/**
	 * Returns the set of common ancestors of two given GO terms.
	 * 
	 * @param term1 identifier of first GO term
	 * @param term2 identifier of second GO term
	 * @return identifiers of the common ancestors of the two given terms
	 */
	public Set<String> getCommonAncestors(String term1, String term2) {
		// get both ancestor lists from hashmap
		Set<String> ancestors1 = ancestorset.get(term1);
		Set<String> ancestors2 = ancestorset.get(term2);
		
		if (ancestors1 == null || ancestors2 == null)
			return Collections.<String>emptySet(); 
		
		// calculate and return intersection
		Set<String> intersection = new HashSet<String>(ancestors1);
		intersection.retainAll(ancestors2);
		
		return intersection;
	}
	
	/**
	 * Find ancestors recursively
	 */
	private Set<String> recFindAncestors(GOTerm curTerm) {
		
		Set<String> myancestors = ancestorset.get(curTerm.ID);
		
		if (myancestors == null) {
			// not cached yet
			// add ancestors of all parents
			myancestors = new HashSet<String>();
			for (GOTerm parent : curTerm.parents)
				myancestors.addAll(recFindAncestors(parent));
			// add term itsself
			myancestors.add(curTerm.ID);
			// cache the list
			ancestorset.put(curTerm.ID, myancestors);
		}

		return myancestors;
	}
		
}
