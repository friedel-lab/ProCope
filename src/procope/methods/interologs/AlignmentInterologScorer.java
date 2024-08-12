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

import procope.tools.SequenceAligner;
import jaligner.Alignment;

/**
 * Implements interolog scoring using similarity or identity from pairwise 
 * sequence alignments of the homologs. The actual interolog score is calculated
 * as the geometric mean of the alignment measure of both source/target 
 * alignments.
 * 
 * <p>Requires a {@link SequenceAligner} which contains all involved
 * protein sequences.
 * 
 * @see InterologsCalculator
 * @author Jan Krumsiek
 */

public class AlignmentInterologScorer implements InterologScorer {
	
	/**
	 * Defines the alignment measure which will be used
	 */
	public enum AlignmentScoring {
		/**
		 * Use sfrom the generated docs thequence identity from the alignment
		 */
		USE_IDENTITY,
		/**
		 * Use sequence similarity from the alignment
		 */
		USE_SIMILARITY
	}

	private AlignmentScoring scoring;
	private SequenceAligner aligner;
	
	/**
	 * Creates a new alignment length interolog scorer.
	 * 
	 * @param aligner the sequence aligner used for calculating pairwise 
	 *                alignments from which the identity or similarity information
	 *                will be used
	 * @param scoring use sequence identity or similarity
	 */
	public AlignmentInterologScorer(SequenceAligner aligner, AlignmentScoring scoring) {
		this.aligner = aligner;
		this.scoring = scoring;
	}

	public float getInterologScore(int source1, int target1, int source2, int target2) {
		// get both alignments
		Alignment alignment1 = aligner.getAlignment(source1, target1);
		Alignment alignment2 = aligner.getAlignment(source2, target2);
		
		// return score
		if (scoring == AlignmentScoring.USE_IDENTITY) {
			// use identities
			float id1 = (float)alignment1.getIdentity()/(float)alignment1.getSequence1().length;
			float id2 = (float)alignment2.getIdentity()/(float)alignment2.getSequence1().length;
			return (float)Math.sqrt(id1 * id2);
		} else {
			// use similarities
			float sim1 = (float)alignment1.getSimilarity()/(float)alignment1.getSequence1().length;
			float sim2 = (float)alignment2.getSimilarity()/(float)alignment2.getSequence1().length;
			return (float)Math.sqrt(sim1 * sim2);
		}
	}

}
