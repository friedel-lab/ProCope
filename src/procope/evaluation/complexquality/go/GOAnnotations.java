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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import procope.data.ProteinSet;


/**
 * Represents a set of gene-product to GO-term annotations which are normally
 * read from a file.
 * 
 * @author Jan Krumsiek
 */
public class GOAnnotations implements ProteinSet {
	
	// annotations per GO term
	private HashMap<String, Integer> annotationCount;
	// list of terms per gene product
	private HashMap<Integer, Set<String>> annotations;
	
	private Set<Integer> proteins;
	
	/**
	 * Create new empty GO annotations object.
	 */
	public GOAnnotations() {
		annotationCount = new HashMap<String, Integer>();
		annotations = new HashMap<Integer, Set<String>>();
		proteins = new HashSet<Integer>();
	}
	
	/**
	 * Adds the annotation of a given protein to a GO term.
	 * 
	 * @param protein protein for which the term is annotated
	 * @param term identifier of the annotated GO term 
	 */
	public void addAnnotation(int protein, String term) {

		// only do this stuff if gene not already annotated for term
		if (!isAnnotated(protein, term)) {
			
			// get list, create if needed
			Set<String> list = annotations.get(protein);
			if (list == null) {
				list = new HashSet<String>();
				annotations.put(protein, list);
			}
			// add to list
			list.add(term);
			
			// now count number of annotations for this term
			Integer count = annotationCount.get(term);
			if (count == null) 
				count = 1;
			else
				count++;
			// (re)put to list
			annotationCount.put(term, count);
			
		}
		
		proteins.add(protein);
		
	}


	/**
	 * Returns the number of gene products annotated to a given GO term
	 * 
	 * @param term identifier of the GO term
	 * @return number of annotations to this term
	 */
	public int getAnnotationCount(String term) {
		Integer count = annotationCount.get(term);
		if (count==null)
			return 0; 	
		else
			return count;
	}
	
	/**
	 * Returns a set of identifiers of GO terms which are annotated to a given protein.
	 * 
	 * @param protein protein for which the GO terms will be retrieved
	 * @return set of GO term identifiers associated with the given protein
	 */
	public Set<String> getGOTerms(int protein) {
		return annotations.get(protein);
	}
	
	/**
	 * Checks whether a protein is annotated to a GO term.
	 * 
	 * @param protein protein for which an annotation is checked
	 * @param term identifier of the GO term
	 * @return whether the given protein is annotated to the GO term
	 */
	public boolean isAnnotated(int protein, String term) {
		Set<String> annot = annotations.get(protein);
		if (annot == null)
			return false;
		else 
			return annot.contains(term);
	}

	/**
	 * Returns the proteins which are used in this annotation set.
	 */
	public Set<Integer> getProteins() {
		return proteins;
	}

	
}
