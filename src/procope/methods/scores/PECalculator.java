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

import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationExperiment;
import procope.tools.SparseMatrixInt;
import procope.tools.namemapping.ProteinManager;





/** 
 * Calculates <i>Purification Enrichment</i> scores according to
 * 
 * <p>Collins et al.<br/>
 * Toward a comprehensive atlas of the physical interactome of Saccharomyces cerevisiae. <br/>
 * <i>Mol Cell Proteomics</i>, 2007, 6, 439-450 
 * 
 * <p>For more information about this scoring method please check out the
 * online manual.
 * 
 * @author Jan Krumsiek
 */
public class PECalculator extends ScoresCalculator {
	
	// exparray[i][k]
	private PurificationExperiment[][] exparray;
	private int protcount;
	
	// external values
	private float r;
	private float n_pseudo;
	
	// purification statistics
	private int[] preycounts;
	private int[] baitcounts;
	private SparseMatrixInt preypreyoccurences;
	private float distinctpreys;
	private float totalpreycount;
	private float totalpreypreypairs;
	private HashSet<Integer> distinctpreyset;
	private float[] f;

	private Set<Integer> proteins;
	
	/**
	 * Creates a new Purification Enrichment calculator. 
	 * 
	 * @param data purification data to be used
	 * @param r r parameter (see original literature)
	 * @param pseudocount pseudo-count (see original literature)
	 */
	public PECalculator(PurificationData data, float r, float pseudocount) {
		
		// save protein list
		proteins = data.getProteins();
		// save parameters
		this.r = r;
		this.n_pseudo = pseudocount;

		
		this.protcount = ProteinManager.getProteinCount();
		// perform array preperation steps
		prepareArray(data.getExperiments());
		
		// *** calculate statistics
		// count baits & preys, distinct preys
		distinctpreyset = new HashSet<Integer>();
		preycounts = new int[protcount+1];	// = n_j^preyobs
		preypreyoccurences = new SparseMatrixInt(false);	// number of purifications with both preys
		totalpreycount = 0;		// n_tot^preyobs
		totalpreypreypairs = 0;	// n_tot^prey-prey
		// distinctpreys = n_distinctpreys
		for (PurificationExperiment exp : data.getExperiments()) {
			
			Collection<Integer> preys = exp.getPreys();
			
			// iterate over preys
			for (int prey : preys) {
				preycounts[prey]++;
				distinctpreyset.add(prey);
				// again to count all prey-prey occurences
				for (int prey2 : preys) {
					if (prey != prey2) {
						preypreyoccurences.add(prey, prey2, 1);
//						preypreyoccurences.add(prey2, prey, 1);
						totalpreypreypairs++;
					}
				}
			}
			
			totalpreycount += preys.size();
			// increase total prey-prey pairs
//			totalpreypreypairs += (preys.size() * (preys.size() -1)) / 2;
			//totalpreypreypairs += (preys.size() * ( preys.size()-1)) ;
//			totalpreypreypairs += preys.size();
			
		}
		
		// get number of distinct preys
		distinctpreys = distinctpreyset.size();
		
		// precache f[j]
		f = new float[protcount+1];
		for (int i=1; i<=protcount; i++)
			f[i] = f(i);
		
	}
	
	/**
	 * Sorts the purification experiments such that the experiments 
	 * of all baits are grouped
	 */
	private void prepareArray(Collection<PurificationExperiment> experiments) {
		// count which bait occurs how often
		baitcounts = new int[protcount+1];
		for (PurificationExperiment exp : experiments)
			baitcounts[exp.getBait()]++;
		
		// create array of purification experiments
		exparray = new PurificationExperiment[protcount+1][];
		// create helper position array
		for (int i=0; i<=protcount; i++)
			exparray[i]	= new PurificationExperiment[baitcounts[i]];
		
		// insert experiments
		int[] helpcounter = new int[protcount+1];
		for (PurificationExperiment exp : experiments) {
			exparray[exp.getBait()][helpcounter[exp.getBait()]] = exp;
			helpcounter[exp.getBait()]++;
		}
	}

	/**
	 * Calculates the Purification Enrichment score for two given proteins.
	 */
	public float getScore(int protA, int protB) {
		// in range?
		if (protA > protcount || protB > protcount)
			return 0;
		
		return S(protA,protB) + S(protB,protA) + M(protA,protB);
	}


	private float S(int i, int j) {
		// sum over all experiments
		float totalsum=0;
		for (int k=0; k<exparray[i].length; k++) 
			totalsum += s(i, j, k);
		
		return totalsum;
	}
	

	/**
	 * Spokes term
	 */
	private float s(int i, int j, int k) {
	//	System.out.println("isprey: "  + isPrey(i,j,k));
		if (isPrey(i,j,k)) {
			float ps =  p_s(i,j,k);
			return (float)Math.log10( (r + (1-r) * ps )  / ps  );
		} else
			return (float)Math.log10(1-r);
		

	}
	private boolean isPrey(int i, int j, int k) {
		//if (exparray[i].length > 0) {
			return exparray[i][k].getPreys().contains(j);
		//} else
		//	return false;
	
	}

	private float p_s(int i, int j, int k) {
		return 1f - (float)Math.exp(-f[j] * (float)exparray[i][k].getPreys().size() * baitcounts[i] );
	}
	
	private float f(int j) {
		float nom = (float)(preycounts[j] + n_pseudo );
		float denom = (float)totalpreycount + (distinctpreys * n_pseudo);
		return nom / denom;
	}
	
	/**
	 * Matrix term
	 */
	private float M(int i, int j) {
		return preypreyoccurences.get(i, j) * m(i,j,0);
	}
	
	private float m(int i, int j, int k) {
		float pm = p_m(i,j,k);
		return (float)Math.log10( (r + (1-r) * pm )  / pm );
	}

	private float p_m(int i, int j, int k) {
		//int exppreys = exparray[i][k].preys.length;
		return 1f - (float)Math.exp( -f[i] * f[j] * totalpreypreypairs     );
	}
	
	/**
	 * Returns all proteins of the purification data set used in this 
	 * scores calculator
	 */
	public Set<Integer> getProteins() {
		return proteins;
	}

}
