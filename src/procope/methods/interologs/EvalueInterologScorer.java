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
package procope.methods.interologs;

import procope.methods.interologs.blast.BlastHits;

/**
 * Implements interolog scoring using the geometric mean of the two BLAST
 * evalues involved in a transferred protein interaction (when transferred
 * using bidirectional best hits from BLAST).
 * 
 * @see InterologsCalculator
 * @author Jan Krumsiek
 */
public class EvalueInterologScorer implements InterologScorer {

	private BlastHits forward;
	private BlastHits backward;

	/**
	 * Creates a new e-value interolog scorer from the given BLAST results.
	 * 
	 * @param forward this is the result of the BLAST run where the organsim
	 *                which is <u>donating</u> interactions (the source organism)
	 *                was the <u>query</u> 
	 * @param backward this is the result of the BLAST run where the organsim
	 *                which is <u>receiving</u> interactions (the target organism)
	 *                was the <u>query</u>
	 */
	public EvalueInterologScorer(BlastHits forward, BlastHits backward) {
		this.forward = forward;
		this.backward = backward;
	}
	
	public float getInterologScore(int source1, int target1, int source2, int target2) {
		double evalue1forward = forward.getHit( source1, target1).getEvalue();
		double evalue2forward = forward.getHit(source2, target2).getEvalue();
		double evalue1backward = backward.getHit(target1, source1).getEvalue();
		double evalue2backward = backward.getHit(target2, source2).getEvalue();
		
		return (float)Math.pow(evalue1forward*evalue1backward*evalue2forward*evalue2backward, 1/4);
	}
	

}
