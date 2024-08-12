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

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.tools.ProCopeException;
import procope.tools.Tools;


/**
 * This class contains static methods for the comparison of two complex sets.
 * The methods can be used to identify and investigate similarities and 
 * difference between two sets.
 * <p>The class is not instantiatable.
 * 
 * @author Jan Krumsiek
 */
public class ComplexSetComparison {

	// avoid instantiation of this class
	private ComplexSetComparison() {
	}
	
	/**
	 * Calculate complex set similarity after 
	 * 
	 * <p>Broh&eacute;e, S. & van Helden, J.<br/>
	 * Evaluation of clustering algorithms for protein-protein interaction networks.<br/> 
	 * <i>BMC Bioinformatics</i>, 2006, 7, 488<br/>
	 * Pubmed: 17087821
	 * 
	 * For more information see: {@link BroheeSimilarity}
	 * 
	 * @param candidate candidate complex set
	 * @param reference reference complex set
	 * @return similarity object for the two given complex sets
	 */
	public static BroheeSimilarity broheeComparison(ComplexSet candidate, ComplexSet reference) {
		
		int countA = candidate.getComplexCount();
		int countB = reference.getComplexCount();
				
		// calculate overlaps
		int[][] overlaps = complexSetsOverlap(candidate, reference);
		
		// *** SENSITIVITY
		// calculate coverage of prediction in reference
		int totalN=0;
		float sn_tot = 0;
		for (int i=0; i<countB; i++) {
			float Sn_i_max = Float.NEGATIVE_INFINITY;
			for (int j=0; j<countA; j++) {
				float Sn_ij = (float)overlaps[j][i] / (float)reference.getComplex(i).size();
				
				// new maximum?
				if (Sn_ij > Sn_i_max)	Sn_i_max = Sn_ij;
				
			}
			// add to total weighted average
			sn_tot += (  (float)reference.getComplex(i).size() * Sn_i_max  );
			// increase N counter
			totalN += reference.getComplex(i).size();
		}
		// now divide by total size to get weighted average
		float sn = (sn_tot / (float)totalN );
		
		// *** PPV
		// first calculate total overlap for each predicted cluster j
		int[] totaloverlap = new int[countA];
		for (int j=0; j<countA; j++) {
			for (int i=0; i<countB; i++) {
				totaloverlap[j] += overlaps[j][i];
			}
		}
		
		// now calc PPV for each predicted cluster j
		float PPV_tot = 0;
		int sumoverlap=0;
		for (int j=0; j<countA; j++) {
			float PPV_j_max = Float.NEGATIVE_INFINITY;
			for (int i=0; i<countB; i++) {
				// avoid divison by zero
				float PPV_ij = (float)overlaps[j][i] / totaloverlap[j];
				if (totaloverlap[j] > 0) PPV_ij = (float)overlaps[j][i] / totaloverlap[j];
				else PPV_ij = 0;
				// directcly calc maximum PPV for this cluster
				if (PPV_ij > PPV_j_max) PPV_j_max = PPV_ij;
			}
			
			// add to total weighted average
			PPV_tot += (   totaloverlap[j] * PPV_j_max  );
			// increase overlap sum counter
			sumoverlap += totaloverlap[j];
		}
		
		
		// now divide by overlap sum to get weighted average
		float ppv = (PPV_tot / (float)sumoverlap );
		
		return new BroheeSimilarity(sn, ppv);
		
	}
	
	/**
	 * Calculates a mapping between two given complex sets by comparing the 
	 * protein overlaps of their complexes. This method only maps two complexes
	 * if both complexes do not have any other overlaps above the given 
	 * threshold in the other set.
	 * 
	 * @param setA first complex set
	 * @param setB second complex set
	 * @param overlapThreshold minimum overlap of proteins needed to map two
	 *        complexes to each other (the literature often recommends a value
	 *        of 2 here)
	 * @return list of mappings between the two complex sets
	 */
	public static ComplexMappings mapComplexesConsistently(
			ComplexSet setA, ComplexSet setB, int overlapThreshold) {
		
		if (overlapThreshold < 1)
			throw new ProCopeException("Overlap threshold must be >= 1");
		
		ArrayList<ComplexMapping> mappings = new ArrayList<ComplexMapping>();

		int[][] overlaps = complexSetsOverlap(setA, setB);
		
		int countA = setA.getComplexCount();
		int countB = setB.getComplexCount();
		
		// iterate over all complexes in a
		for (int a=0; a<countA; a++) {
			// check for complexes above the threshold, but there may only be one
			int numMapped=0;
			int mappedInB=-1;
			for (int b=0; b<countB; b++) {
				if (overlaps[a][b] >= overlapThreshold) {
					numMapped++;
					mappedInB = b;
					if (numMapped > 1) break;
				}
			}
			// if we found one in B, check that this complex also has no further overlap in A
			if (numMapped == 1) {
				boolean consistent = true;
				for (int a2=0; a2<countA; a2++) {
					if (overlaps[a2][mappedInB] >= overlapThreshold && a != a2) {
						consistent = false;
						break;
					}
				}
				// add if everything was consistent
				if (consistent)
					mappings.add(new ComplexMapping(a,mappedInB,overlaps[a][mappedInB]));
			}
		}
		
		
		return new ComplexMappings(setA, setB, mappings);
		
	}
		
	/**
	 * Find complexes in two given complex sets which are identical 
	 * 
	 * @param setA first complex set
	 * @param setB second complex set
	 * @return list of mappings of identical complexes between the two complex 
	 *         sets
	 */
	public static ComplexMappings mapComplexesExactly(ComplexSet setA, ComplexSet setB) {
		
		ArrayList<ComplexMapping> mappings = new ArrayList<ComplexMapping>();

		int[][] overlaps = complexSetsOverlap(setA, setB);
		
		int countA = setA.getComplexCount();
		int countB = setB.getComplexCount();
		
		// precache complex sizes
		int[] sizesA = new int[countA];
		for (int a=0; a<countA; a++)
			sizesA[a] = setA.getComplex(a).size();
		int[] sizesB = new int[countB];
		for (int b=0; b<countB; b++)
			sizesB[b] = setB.getComplex(b).size();
		
		// iterate over all overlaps and check for exakt matches
		for (int a=0; a<countA; a++) {
			for (int b=0; b<countB; b++) {
				if (overlaps[a][b] == sizesA[a] && overlaps[a][b] == sizesB[b])
					mappings.add(new ComplexMapping(a,b,overlaps[a][b]));
			}
		}
		
		return new ComplexMappings(setA, setB, mappings);
	}
	
	
	/**
	 * Calculates a mapping between two given complex sets by comparing the 
	 * protein overlaps of their complexes. This method only maps each complex
	 * of a set <u>once</u>. If there is more than one mapping candidate in the 
	 * other set the one with the largest overlap will be used.
	 * 
	 * @param setA first complex set
	 * @param setB second complex set
	 * @param overlapThreshold minimum overlap of proteins needed to map two
	 *        complexes to each other (the literature often recommends a value 
	 *        of 2 here)
	 * @return list of mappings between the two complex sets
	 */
	public static ComplexMappings mapComplexes(
			ComplexSet setA, ComplexSet setB, int overlapThreshold) {
		
		if (overlapThreshold < 1)
			throw new ProCopeException("Overlap threshold must be >= 1");
		
		ArrayList<ComplexMapping> mappings = new ArrayList<ComplexMapping>();

		int[][] overlaps = complexSetsOverlap(setA, setB);
		
		int countA = setA.getComplexCount();
		int countB = setB.getComplexCount();
			
		int maxoverlap;
		// now find highest overlaps iterativly
		do {
			maxoverlap=0;
			int maxcluster1=-1, maxcluster2=-1;
			for (int i=0; i<countA; i++) {
				for (int j=0; j<countB; j++) {
					if (maxoverlap < overlaps[i][j]) {
						maxoverlap = overlaps[i][j];
						maxcluster1 = i;
						maxcluster2 = j;
					}
				}
			}
			// check if maximum overlap is still enough for a mapping, OR if exact hit
			if (maxoverlap >= overlapThreshold) {
				
				// add mapping
				mappings.add(new ComplexMapping(maxcluster1, maxcluster2, overlaps[maxcluster1][maxcluster2]));
				// "remove" both clusters by settings all of their overlaps to zero
				for (int i=0; i<countB; i++) overlaps[maxcluster1][i] = 0;
				for (int i=0; i<countA; i++) overlaps[i][maxcluster2] = 0;
				
			}
			
		} while (maxoverlap >= overlapThreshold);
		
		return new ComplexMappings(setA, setB, mappings);
	}
	
	/**
	 * Calculates a mapping between two given complex sets by comparing the 
	 * protein overlaps of their complexes. This method calculates multiple
	 * mappings, i.e. the result will contain one mapping for each pair of
	 * complexes in the two sets whose overlap is above the given threshold.
	 * 
	 * @param setA first complex set
	 * @param setB second complex set
	 * @param overlapThreshold minimum overlap of proteins needed to map two
	 *        complexes to each other (the literature often recommends a value 
	 *        of 2 here)
	 * @return list of mappings between the two complex sets
	 */
	public static ComplexMappings mapComplexesMultiple(final ComplexSet setA, 
			final ComplexSet setB, int overlapThreshold) {
		
		ArrayList<ComplexMapping> mappings = new ArrayList<ComplexMapping>();
		
		// calc overlaps
		int[][] overlaps = complexSetsOverlap(setA, setB);
		
		
		int countA = setA.getComplexCount();
		int countB = setB.getComplexCount();
		
		// any reference cluster which has at least one overlap of 'minoverlap'
		// is counted as mappable, => collect them
		for (int i=0; i<countA; i++) {
			for (int j=0; j<countB; j++) {
				if (overlaps[i][j] >= overlapThreshold) {
					mappings.add(new ComplexMapping(i,j,overlaps[i][j]));
				}
			}
		}
		return new ComplexMappings(setA, setB, mappings);
		
	}
	
	/**
	 * Calculate the number of proteins which overlap between two given
	 * complexes.
	 * 
	 * @param complex1 first complex
	 * @param complex2 second complex
	 * @return overlap between the two complexes
	 */
	public static int complexesOverlap(Complex complex1, Complex complex2) {
		int overlap=0;
		for (int i : complex1) {
			for (int j : complex2) {
				if (i==j)
					overlap += 1;
			}
		}
		return overlap;
	}
	
	
	/**
	 * Calculates the overlap matrix for two given complex sets. Each entry
	 * {@code m(i,j)} of the matrix contains the overlap between the 
	 * <i>i-th</i> complex of the first set and the <i>j-th</i> complex
	 * of the second complex set.
	 * 
	 * @param setA first complex set
	 * @param setB second complex set
	 * @return overlap matrix for the two given complex sets
	 */
	public static int[][] complexSetsOverlap(ComplexSet setA, ComplexSet setB) {
		
		int numComplexesA = setA.getComplexCount();
		int numComplexesB = setB.getComplexCount();
		int[][] overlaps = new int[numComplexesA][numComplexesB];
	
		// do the count for complex seta
		int maxID = Tools.findMax(setA.getProteins());
		byte[][] counts = new byte[numComplexesA][maxID+1];
		
		int complexIDA=0;
		for (Complex complex : setA) {
			// count all proteins
			for (int protein : complex)
				counts[complexIDA][protein] = 1;
			complexIDA++;
		}
		
		// now iterate over complex set B
		int complexIDB=0;
		for (Complex complex : setB) {
			// iterate over proteins in this complex
			for (int protein : complex) {
				// count all complexes it is contained in in set A
				for (int i=0; i<numComplexesA; i++) {
					if (protein <= maxID && counts[i][protein] == 1)
						overlaps[i][complexIDB]++;
				}
			}
			complexIDB++;
		}
		
		return overlaps;
	}
	

}
