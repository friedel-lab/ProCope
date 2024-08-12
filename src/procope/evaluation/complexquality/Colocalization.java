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
package procope.evaluation.complexquality;

import java.util.Collection;

import procope.data.LocalizationData;
import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;


/**
 * Implements measures for the degree of colocalization of proteins contained in 
 * a predicted protein complex.
 * <p>Two scoring methods are implemented, one of them (colocalization score) is part 
 * of our own work, the second method (PPV) was proposed in
 * 
 * <p>Pu et al.<br/>
 * Identifying functional modules in the physical interactome of Saccharomyces cerevisiae.<br/>
 * <i>Proteomics</i>, 2007, 7, 944-960<br/>
 * 
 * <p>For detailed information about how both types of scores are calculated 
 * please consult the online manual of this library.
 * 
 * @author Jan Krumsiek
 */

public class Colocalization {
	
	private LocalizationData data;

	/**
	 * Creates a new colocalization score calculator.
	 * 
	 * @param data localization data to be used
	 */
	public Colocalization(LocalizationData data) {
		this.data = data;
	}
	
	/**
	 * Calculates the PPV according to Pu et al., 2007
	 * 
	 * @param complex for which the PPV will be calculated
	 * @return colocalization PPV of that complex or 0 if not at least
	 *         two proteins have localization data in the set
	 */
	public float getPPV(Complex complex) {

		// iterate over proteins in this cluster and count the occurences of each localization group
		int numLocs = data.getNumberOfLocalizations();
		int[] occs = new int[numLocs];
		int noLocs=0;
		for (int protein : complex) {
			// iterate over localizations of this protein and count occurences
			Collection<Integer> localizations = data.getLocalizations(protein);
			if (localizations != null) {
				for (int singleLoc : localizations) 
					occs[singleLoc]++;
			} else
				noLocs++;
		}
		// find maxmimum and sum
		int maxCount = -1;
		int sum = 0;
		for (int i=0; i<numLocs; i++) {
			if (occs[i] > maxCount) maxCount = occs[i];
			sum += occs[i];
		}
		
		if (maxCount == 0)
			return 0;
		else
			return (float)maxCount / (float)sum;
	}
	
	/**
	 * Calculates the colocalization score of a given complex. 
	 * 
	 * @param complex complex for which the score will be calculated
	 * @return colocalization score of that complex or 0 if not at least
	 *         two proteins have localization data in the set
	 */
	public float getColocalizationScore(Complex complex) {
		// iterate over proteins in this cluster and count the occurences of each localization group
		// and the number of proteins having localization data
		int numLocs = data.getNumberOfLocalizations();
		int protsWithData=0;
		int[] occs = new int[numLocs];
		for (int protein : complex) {
			// iterate over localizations of this protein and count occurences
			Collection<Integer> localizations = data.getLocalizations(protein);
			if (localizations != null) {
				for (int singleLoc : localizations) 
					occs[singleLoc]++;
				protsWithData++;
			} 
		}
		// find maxmimum and sum
		int maxCount = -1;
		for (int i=0; i<numLocs; i++) {
			if (occs[i] > maxCount) maxCount = occs[i];
		}
		
		return maxCount/(float)protsWithData;
	}
	
	/**
	 * Calculates the average colocalization score for a given complex set.
	 * 
	 * @param complexes complex set for which the average score will be calculated
	 * @param weighted calculate sum weighted by the complex sizes?
	 * @param ignoreMissing ignore cases where there are no localization data 
	 *                      for any protein in a complex (colocalization score == 0)?
	 * @return average colocalization score for that complex set
	 */
	public float getAverageColocalizationScore(ComplexSet complexes, boolean weighted, boolean ignoreMissing) {
		float total=0;
		float count=0;
		for (Complex complex : complexes) {
			float score = getColocalizationScore(complex);
			if (score != score) score = 0; // NaN check
			if (!ignoreMissing || score != 0) {
				if (weighted) {
					total += score * (float)complex.size();
					count += (float)complex.size();
				} else {
					total += score;
					count++;
				}
			}
		}
		return total/count;
	}
	
	/**
	 * Calculates the average PPV for a given complex set.
	 * 
	 * @param complexes complex set for which the average PPV will be calculated
	 * @param weighted calculate sum weighted by the complex sizes?
	 * @param ignoreMissing ignore cases where there are no localization data 
	 *                      for any protein in a complex (colocalization score == 0)?
	 * @return average PPV for that complex set
	 */
	public float getAveragePPV(ComplexSet complexes, boolean weighted, boolean ignoreMissing) {
		float total=0;
		float count=0;
		for (Complex complex : complexes) {
			float score = getPPV(complex);
			if (score != score) score = 0; // NaN check
			if (!ignoreMissing || score != 0) {
				if (weighted) {
					total += score * (float)complex.size();
					count += (float)complex.size();
				} else {
					total += score;
					count++;
				}
			}
		}
		return total/count;
	}
}
