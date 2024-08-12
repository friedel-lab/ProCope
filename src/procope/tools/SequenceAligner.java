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
package procope.tools;

import jaligner.Alignment;
import jaligner.SmithWatermanGotoh;
import jaligner.matrix.Matrix;
import jaligner.matrix.MatrixLoader;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import procope.methods.interologs.Sequences;


/**
 * Calculates pairwise sequence alignments using the 
 * <a href="http://jaligner.sourceforge.net/">JAligner library</a>. Implements
 * a caching functionality to avoid multiple calulations of the same 
 * alignments.
 * <p>Before aligning two sequences you have to add them to the 
 * {@link #addToSequencePool(Sequences) sequence pool} of the aligner object
 * to provide actual sequence data for the proteins.
 * <p>Gap penalties are specified as <u>positive</u> values. 
 * <p>Note: Only local alignments (<i>Smith-Waterman</i>) are supported at the
 * moment.
 * 
 * @author Jan Krumsiek
 */


// use jaligner methods to do alignments and cache them
public class SequenceAligner {
	
	// list of jaligner matrices, hardcoded
	private static String[] JALIGNER_MATRICES = new String[] { "BLOSUM100", "BLOSUM30",
			"BLOSUM35", "BLOSUM40", "BLOSUM45", "BLOSUM50", "BLOSUM55",
			"BLOSUM60", "BLOSUM62", "BLOSUM65", "BLOSUM70", "BLOSUM75",
			"BLOSUM80", "BLOSUM85", "BLOSUM90", "BLOSUMN", "DAYHOFF",
			"EDNAFULL", "GONNET", "IDENTITY", "MATCH", "PAM10", "PAM100",
			"PAM110", "PAM120", "PAM130", "PAM140", "PAM150", "PAM160",
			"PAM170", "PAM180", "PAM190", "PAM20", "PAM200", "PAM210",
			"PAM220", "PAM230", "PAM240", "PAM250", "PAM260", "PAM270",
			"PAM280", "PAM290", "PAM30", "PAM300", "PAM310", "PAM320",
			"PAM330", "PAM340", "PAM350", "PAM360", "PAM370", "PAM380",
			"PAM390", "PAM40", "PAM400", "PAM410", "PAM420", "PAM430",
			"PAM440", "PAM450", "PAM460", "PAM470", "PAM480", "PAM490",
			"PAM50", "PAM500", "PAM60", "PAM70", "PAM80", "PAM90" };
	
	private static Matrix matrix;
	private float gapOpen;
	private float gapExtend;

	private Sequences sequencePool;
	
	private HashMap<String, Alignment> alignments;
	private boolean cache; 

	/**
	 * Returns a list of possible substitution matrix names.
	 * 
	 * @return list of substitution matrix names
	 */
	public static String[] getMatrixNames() {
		return JALIGNER_MATRICES;
	}
	
	/**
	 * Sets the substitution matrix. To get a list of possible names call
	 * {@link #getMatrixNames()}.
	 * 
	 * @param matrixName name of substitution matrix.
	 * @throws ProCopeException if the specified substitution matrix
	 *                                 was not found
	 */
	public void setMatrix(String matrixName) throws ProCopeException {
		try {
			matrix = MatrixLoader.load(matrixName);
		} catch (Exception e) {
			throw new ProCopeException("Matrix not found: '" + matrixName + "'. " +
					"Call getMatrixNames() to get a list of possible matrices.");
		}
	}
	
	/**
	 * Sets the gap open penality.
	 * 
	 * @param gapOpen gap open penality
	 */
	public void setGapOpen(float gapOpen) {
		this.gapOpen = gapOpen;
	}
	
	/**
	 * Sets the gap extension penality.
	 * 
	 * @param gapExt gap extension penalty
	 */
	public void setGapExtend(float gapExt) {
		this.gapExtend = gapExt;
	}
	
	/**
	 * Creates a new sequence aligner with activated caching.
	 * Default parameters: gap-open = 10, gap-extend = 0.5, matrix = BLOSUM62
	 */
	public SequenceAligner() {
		this(true);
	}
	
	/**
	 * Creates a new sequence aligner.
	 * Default parameters: gap-open = 10, gap-extend = 0.5, matrix = BLOSUM62
	 * 
	 * @param cache cache alignments to avoid multiple pairwise alignment 
	 *              calculations?
	 */
	public SequenceAligner(boolean cache) {
		
		// disable jaligner loggers
		Logger.getLogger(jaligner.SmithWatermanGotoh.class.getName()).setLevel(Level.OFF);
		Logger.getLogger(jaligner.matrix.MatrixLoader.class.getName()).setLevel(Level.OFF);
		
		// store variables
		try {
			matrix = MatrixLoader.load("BLOSUM62");
		} catch (Exception e) {
			// this should never happen
			throw new ProCopeException("Could not load BLOSUM62 matrix from JAligner. " +
					"Something very weird must have happened.");
		}
		gapOpen = 10f;
		gapExtend = 0.5f;
		// create sequence pool
		sequencePool = new Sequences();
		
		this.cache = cache;
		if (cache) alignments = new HashMap<String, Alignment>();
		
	}
	
	/**
	 * Add a set of sequences to the sequence pool of this aligner.
	 * 
	 * @param sequences sequences to be added to the pool
	 */
	public void addToSequencePool(Sequences sequences) {
		// convert to sequence objects and add to pool
		sequencePool.addAll(sequences);
	}
	
	/**
	 * Returns the local sequence alignment for two given proteins. 
	 * 
	 * @param protein1 first protein
	 * @param protein2 second protein
	 * @return alignment object (from JAligner library)
	 * @throws ProCopeException if at least one of the proteins has no
	 *  sequence in the current {@link #addToSequencePool(Sequences) sequence pool}
	 */
	public Alignment getAlignment(int protein1, int protein2) throws ProCopeException {
		if (cache) {
			// already cached?
			String id = Math.min(protein1, protein2) + "#" + Math.max(protein1, protein2);
			Alignment alignment = alignments.get(id);
			if (alignment != null)
				// return cached alignment
				return alignment;
			else {
				// calculate now
				alignment = calculateAlignment(protein1, protein2);
				alignments.put(id, alignment);
				return alignment;
			}
		} else
			// no caching
			return calculateAlignment(protein1, protein2);
		
	}
	
	/**
	 * Calculate alignment for two given proteins, throw exception if at least
	 * one of the sequences is not in the sequence pool
	 */
	private Alignment calculateAlignment(int protein1, int protein2) {
		
		// get and verify sequences
		String seq1 = sequencePool.getSequence(protein1);
		if (seq1 == null)
			throw new ProCopeException("Sequence not in pool: "  + protein1);
		String seq2 = sequencePool.getSequence(protein2);
		if (seq2 == null)
			throw new ProCopeException("Sequence not in pool: "  + protein2);
		
		return SmithWatermanGotoh.align(new jaligner.Sequence(seq1),
				new jaligner.Sequence(seq2), matrix, gapOpen, gapExtend);
	}
	
	public Sequences getSequencePool() {
		return sequencePool;
	}
	
	
}
