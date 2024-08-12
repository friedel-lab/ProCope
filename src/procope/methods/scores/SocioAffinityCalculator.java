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
 * Calculates <i>socio affinity scores</i> according to
 *
 * <p>Gavin et al.<br/>
 * Proteome survey reveals modularity of the yeast cell machinery.<br/>
 * <i>Nature</i>, 2006, 440, 631-636
 * 
 * <p>For more information about this scoring method please check out the
 * online manual.
 * 
 * @author Jan Krumsiek
 *
 */
public class SocioAffinityCalculator extends ScoresCalculator  {
	
	private float[] baitfrac;
	private float[] preyfrac;
	private int[] preysperbait;
	private int numexperiments;
	private SparseMatrixInt aretrievesb;
	private SparseMatrixInt abpreynobait;
	private int bigsum;
    
    boolean noMatrix=false;

    private Set<Integer> proteins;
	private int protcount;
    
    /**
     * Creates a new socio affinity scores calculator based on a given
     * purification data set.
     * 
     * @param data purification data set used for scores calculation
     */
    public SocioAffinityCalculator(PurificationData data) {

    	// save protein list
		proteins = data.getProteins();
	
    	// get experiments from purification data set
    	Collection<PurificationExperiment> experiments = data.getExperiments();
    	// counter variables, arrays
    	protcount = ProteinManager.getProteinCount();
    	int[] baitcount = new int[protcount+1];
    	int[] preycount = new int[protcount+1];
    	preysperbait = new int[protcount+1];
    	aretrievesb = new SparseMatrixInt(false);
    	abpreynobait = new SparseMatrixInt(false);

    	baitfrac = new float[protcount+1];
    	preyfrac = new float[protcount+1];
    	int[] preycount_nobait = new int[experiments.size()];
    	HashSet<Integer> baits = new HashSet<Integer>();
    	int totalpreycount=0;
    	this.numexperiments = experiments.size() ;

    	// do all the statistics!
    	
    	// loop
    	int expnum=0;
    	for (PurificationExperiment exp : experiments) {
    		int bait = exp.getBait();
    		Collection<Integer> colpreys = exp.getPreys();
    		Integer[] preys = colpreys.toArray(new Integer[0]);
    		// now count prey and bait occurrences for the corresponding fractions
    		// also count how often A retrieves B
    		// and which baits there are and which unique preys they have
    		baitcount[bait]++;
    		baits.add(bait);
    		preysperbait[bait] += preys.length;
    		totalpreycount += preys.length;
    		for (int prey : preys) {
    			preycount[prey]++;
    			aretrievesb.add(bait,prey,1);
    			// count unique preys (excluding bait itself)
    			if (prey != bait)
    				preycount_nobait[expnum]++;

    		}
    		// we also need to check how often A and B appear together when they are no baits
    		// check all against all preys
    		for (int i=0; i<preys.length; i++) {
    			for (int j=i+1; j<preys.length; j++) {
    				if ((bait != preys[i]) && (bait != preys[j])) {
    					abpreynobait.add(preys[i], preys[j], 1);
    					abpreynobait.add(preys[j], preys[i], 1);
    				}
    			}
    		}
    		expnum++;
    	}


    	// calculate fractions
    	for (int i=0; i<protcount; i++) {
    		if (baitcount[i] == 0 && preycount[i] == 1) {
    			baitfrac[i] = 0;
    			preyfrac[i] = 0;
    		} else {
    			baitfrac[i] = (float)baitcount[i] / (float)experiments.size();
    			preyfrac[i] = (float)preycount[i] / (float)totalpreycount;
    		}
    	}

    	// calculate big sum
    	bigsum=0;
    	for (int i=0; i<experiments.size(); i++) {
    		//bigsum += (   uniquepreys[bait].size() * (uniquepreys[bait].size()-1) / 2 ); 
    		bigsum += (   preycount_nobait[i] * (preycount_nobait[i]-1) / 2 );
    	}
    	
    	
    }

    /**
     * spokes term
     */
    private double S(int i, int j) {
		double odd =  (double)aretrievesb.get(i, j) / (baitfrac[i] * numexperiments * preyfrac[j] * preysperbait[i] );
		return Math.log(odd);
	}

    /**
     * matrix term
     */
	private double M(int i, int j) {
		double odd =  (double)abpreynobait.get(i, j) / (preyfrac[i] * preyfrac[j] * (double)bigsum);
		return Math.log( odd);
	}
	
	/**
	 * Calculates the socio affinity score for two given proteins based on the
	 * purification data set provided in the constructor.
	 */
	public float getScore(int protA, int protB) {
		
		// in range?
		if (protA > protcount || protB > protcount)
			return 0;
		
		double S_ij = S(protA,protB);
		double S_ji = S(protB,protA);
		double M_ij;
		if (!noMatrix)	M_ij = M(protA,protB);
		else			M_ij = 0;
		
		if (invalidDouble(S_ij))
			S_ij = 0;
		if (invalidDouble(S_ji))
			S_ji = 0;
		if (invalidDouble(M_ij))
			M_ij = 0;
		
		return (float)(S_ij + S_ji + M_ij);
	}
	
	/**
	 * Double infinite or NaN?
	 */
	private boolean invalidDouble(double num) {
		if (num != num || Double.isInfinite(num) )
			return true;
		else
			return false;
	}

	/**
	 * Returns all proteins of the purification data set used in this 
	 * scores calculator
	 */
	public Set<Integer> getProteins() {
		return proteins;
	}
	
}

