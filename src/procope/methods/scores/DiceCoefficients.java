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
package procope.methods.scores;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationExperiment;
import procope.tools.SparseMatrixInt;
import procope.tools.Tools;

/**
 * Provides an efficient implementation of "Dice coefficients", a protein protein
 * affinity measure derived from purification datasets according to
 * 
 * <p>Zhang et al.<br/>
 * From pull-down data to protein interaction networks and complexes with 
 * biological relevance.<br/> 
 * Bioinformatics, 2008, 24, 979-986 
 *  
 * @author Jan Krumsiek
 */

public class DiceCoefficients extends ScoresCalculator {
	
	private Set<Integer> proteins;
	private int maxProt;
	private SparseMatrixInt sameExperiment;
	private int[] experimentCount;
	
	/**
	 * Creates a new Dice coefficients calculator
	 * 
	 * @param data purification dataset to be used
	 */
	public DiceCoefficients(PurificationData data) {
		this.proteins = data.getProteins();
		// create counter array
		maxProt = Tools.findMax(proteins);
		sameExperiment = new SparseMatrixInt(true);
		experimentCount = new int[maxProt+1];
		
		for (PurificationExperiment exp : data) {
			HashSet<Integer> dummy = new HashSet<Integer>(exp.getPreys());
			dummy.add(exp.getBait());
			Vector<Integer> prots = new Vector<Integer>(dummy); 
			// iterate over all pairwise proteins
			for (int i=0; i<prots.size(); i++) {
				int prot1 = prots.get(i);
				experimentCount[prot1]++;
				for (int j=i+1; j<prots.size(); j++) {
					int prot2 = prots.get(j);
					sameExperiment.add(prot1, prot2, 1);
				}
			}
		}
	}

	/**
	 * Returns the proteins involved in the purification data set for this
	 * Dice coefficients calculator
	 */
	@Override
	public Set<Integer> getProteins() {
		return proteins;
	}

	/**
	 * Returns the Dice coefficient for two given proteins, returns 0
	 * if the protein indices are out of range or if protein1==protein2
	 */
	@Override
	public float getScore(int protein1, int protein2) {
		
		if (protein1 > maxProt || protein2 > maxProt || protein1==protein2)
			return 0f;
		
		// calculate overlapping experiments (q), and those where only
		// one of the proteins is involved (r, s)
		int q = sameExperiment.get(protein1, protein2);
		if (q == Integer.MIN_VALUE) // value not set 
			q = 0;
		int r = experimentCount[protein1] - q;
		int s = experimentCount[protein2] - q;
		
		return (float)(2*q)/(float)(2*q+r+s);
	}
	
}

