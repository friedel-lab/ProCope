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
package procope.methods.interologs.blast;

import procope.methods.interologs.Sequences;

/**
 * Ensures that BBHs are only accepted if their BLAST alignment covers a
 * minimum fraction of both aligned protein sequences. Requires access to the 
 * sequence data of all involved proteins to determine their length.
 * 
 * @author Jan Krumsiek
 */
public class BlastBBHAlignmentLengthConstraint implements BlastBBHConstraints {

	
	private Sequences sequencePool;
	private float minAlign;

	/**
	 * Create new alignment length object.
	 * 
	 * @param sequencePool The sequence pool which contains all involved sequences.
	 *                     Needed for sequence length determination.
	 * @param minAlign fraction of both aligned protein sequences which have to
	 *                 be covered by the alignment in order for the BBH to be accepted
	 */
	public BlastBBHAlignmentLengthConstraint(Sequences sequencePool, float minAlign) {
		this.sequencePool = sequencePool;
		this.minAlign = minAlign;
	}

	/**
	 * Accepts a BBH if the give minimum fraction of both protein sequences is 
	 * covered by the BLAST alignment.
	 */
	public boolean acceptBBH(int protein1, int protein2, BlastHit forwardHit, BlastHit backwardHit) {
		
		// get lengths of sequences
		float len1 = sequencePool.getSequence(protein1).length();
		float len2 = sequencePool.getSequence(protein2).length();
		// get alignment lengths
		int align1 = forwardHit.getAlignmentLength();
		int align2 = backwardHit.getAlignmentLength();
		
		boolean forwardOK = ( ( (float) align1 > minAlign * len1) && ( (float)align1 > minAlign * len2) );
		boolean backwardOK = ( ( (float) align2 > minAlign * len1) && ( (float)align2 > minAlign * len2) ); 
		
		return forwardOK && backwardOK;
	}
	
}
