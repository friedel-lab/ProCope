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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationExperiment;
import procope.tools.SparseMatrixInt;
import procope.tools.math.HypergeometricDistribution;
import procope.tools.namemapping.ProteinManager;

/**
 * Calculates protein-protein interaction scores from purification data 
 * according to
 * 
 * <p>Hart, G. T.; Lee, I. & Marcotte, E. R.<br/>
 * A high-accuracy consensus map of yeast protein complexes reveals modular nature of gene essentiality.<br/>
 * <i>BMC Bioinformatics</i>, 2007, 8, 23
 * 
 * <p>For more information about this scoring method please check out the
 * online manual.
 */
public class HartCalculator extends ScoresCalculator {
	
	private SingleModel[] models;
	private Set<Integer> proteins;
	
	/**
	 * Creates a new scores calculator from a given purification data set.
	 * 
	 * @param dataset purification dataset to be used
	 */
	public HartCalculator(PurificationData dataset) {
		this(new PurificationData[]{dataset});
	}
		
	/**
	 * Creates a new scores calculator from a list of purification data sets.
	 * 
	 * @param datasets purfication datasets to be used
	 */
	public HartCalculator(Collection<PurificationData> datasets) {
		this(datasets.toArray(new PurificationData[0]));
	}
	
	/**
	 * Creates a new scores calculator from an array of purification data sets.
	 * 
	 * @param datasets purfication datasets to be used
	 */
	public HartCalculator(PurificationData[] datasets) {
		// create one model for each purification dataset
		models = new SingleModel[datasets.length];
		for (int i=0; i<datasets.length; i++)
			models[i] = new SingleModel(datasets[i]);
		// save protein list
		proteins = new HashSet<Integer>();
		for (int i=0; i<datasets.length; i++)
			proteins.addAll(datasets[i].getProteins());
			
	}

	/**
	 * Calculates the score of the given protein.
	 */
	public float getScore(int prot1, int prot2) {
		
		double total=0;
		for (int i=0; i<models.length; i++) {
			total += models[i].getLogPvalue(prot1, prot2);
		}
		return (float)(-total);
	}
	
	/**
	 * represents the scoring model based on one purification set
	 */
	private class SingleModel {
		
		SparseMatrixInt ppiCounts;
		int[] ppiCountsProt;
		int ppiTotal=0;
		int protCount=0;
		

		public SingleModel(PurificationData data) {
			
			protCount = ProteinManager.getProteinCount();
			// count absolute occurences of matrix model PPIs
			ppiCounts = new SparseMatrixInt(false );  
			ppiCountsProt = new int[protCount+1];
			
			for (PurificationExperiment exp : data) {
				Vector<Integer> prots = new Vector<Integer>(new HashSet<Integer>(exp.getPreys())); // TODO better
				prots.add(exp.getBait());
				// iterate over all pairwise proteins
				for (int i=0; i<prots.size(); i++) {
					int prot1 = prots.get(i);
					for (int j=i+1; j<prots.size(); j++) {
						int prot2 = prots.get(j);
						if (prot1 != prot2) { 
							ppiCounts.add(prot1, prot2, 1);
							ppiCounts.add(prot2, prot1, 1);
							ppiCountsProt[prot1]++;
							ppiCountsProt[prot2]++;
							ppiTotal++;
						}
					}
				}
			}
			
			
		}
		
		/*
		private Collection<PurificationExperiment> poolOverBaits(PurificationData data) {
			
			HashMap<Integer, PurificationExperiment> map = new HashMap<Integer, PurificationExperiment>();
			
			// iterate over experiments
			for (PurificationExperiment exp : data) {
				// check if we already had an experiment for this bait
				PurificationExperiment pooledExp = map.get(exp.getBait());
				if (pooledExp == null) {
					pooledExp = new PurificationExperiment(exp.getBait());
					map.put(exp.getBait(), pooledExp);					
				}
				// determine preys to add
				Collection<Integer> addPreys = new HashSet<Integer>(exp.getPreys());
				// remove bait
				addPreys.remove(exp.getBait());
				// add old baits
				addPreys.addAll(pooledExp.getPreys());
				// add to pooled experiment
				pooledExp.getPreys().clear();
				pooledExp.addPreys(addPreys);
			}
			
			return map.values();
			
		}
		*/
		
		public double getLogPvalue(int prot1, int prot2) {

			if (prot1 > protCount || prot2 > protCount)	
				return 0;									
			
			int n = ppiCountsProt[prot1];
			int m = ppiCountsProt[prot2];
			int count = ppiCounts.get(prot1, prot2);
			if (count == Integer.MIN_VALUE) return 0; // count = zero? p-value of 1 => log(1) = 0 
			
			double x = HypergeometricDistribution.logphyper(count-1, m, ppiTotal-m, n, false);
			if (x > 0) {
				x = 0; // inaccuracies
			}
			return x;
			
		}
	}

	
	/**
	 * Returns all proteins of the purification data set used in this 
	 * scores calculator
	 */
	public Set<Integer> getProteins() {
		return proteins;
	}
	
}
