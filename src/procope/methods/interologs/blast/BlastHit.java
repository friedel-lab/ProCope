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

import procope.tools.namemapping.ProteinManager;

/**
 * A single BLAST hit.
 * 
 * @author Jan Krumsiek
 */
public class BlastHit implements Comparable<BlastHit> {
	
	//              0          1            2              3             4             5            6       7        8        9       10        11   
	// # Fields: Query id, Subject id, % identity, alignment length, mismatches, gap openings, q. start, q. end, s. start, s. end, e-value, bit score

	// comparable for efficient searching in hit lists!
	
	private int hitID, queryID;
	private float identity;
	private int alignlen, mismatches, gapopenings, querystart, queryend, hitstart, hitend;
	private double evalue;
	private float bitscore;

	/**
	 * Creates an new BLAST hit object.
	 * 
	 * @param queryID internal ID of the query protein
	 * @param hitID internal ID of the hit protein (database protein)
	 * @param identity identity of the alignment
	 * @param alignlen length of the alignment
	 * @param mismatches number of mismatches in the alignment
	 * @param gapopenings gap openings in the alignment
	 * @param querystart start coordinate in the query sequence
	 * @param queryend end coordinate in the query sequence
	 * @param hitstart start coordinate in the hit sequence
	 * @param hitend end coordinate in the hit sequence
	 * @param evalue e-value of the hit
	 * @param bitscore bitscore of the hit
	 * 
	 * @see ProteinManager
	 */
	public BlastHit(int queryID, int hitID, float identity, int alignlen, int mismatches, int gapopenings, 
			int querystart, int queryend, int hitstart, int hitend, double evalue, float bitscore) {
		this.queryID = queryID;
		this.hitID = hitID;
		this.identity = identity;
		this.alignlen = alignlen;
		this.mismatches = mismatches;
		this.gapopenings = gapopenings;
		this.querystart = querystart;
		this.queryend = queryend;
		this.hitstart = hitstart;
		this.hitend = hitend;
		this.evalue = evalue;
		this.bitscore = bitscore;
	}
	
	/**
	 * Returns the internal ID of the hit protein
	 * @return internal ID of the hit protein
	 */
	public int getHitID() {
		return hitID;
	}
	
	/**
	 * Returns the internal ID of the query protein
	 * @return internal ID of the query protein
	 */
	public int getQueryID() {
		return queryID;
	}
	
	/**
	 * Returns the identity of the alignment
	 * @return identity of the alignment
	 */
	public float getIdentity() {
		return identity;
	}
	
	/**
	 * Returns the length of the alignment
	 * @return length of the alignment
	 */
	public int getAlignmentLength() {
		return alignlen;
	}
	
	/**
	 * Returns the number of mismatches in the alignment
	 * @return number of mismatches in the alignment
	 */
	public int getMismatches() {
		return mismatches;
	}
	
	/**
	 * Returns the gap openings in the alignment
	 * @return gap openings in the alignment
	 */
	public int getGapOpenings() {
		return gapopenings;
	}
	
	/**
	 * Returns the query start coordinate of the hit
	 * @return query start coordinate of the hit
	 */
	public int getQueryStart() {
		return querystart;
	}
	
	/** 
	 * Returns the query end coordinate of the hit
	 * @return query end coordinate of the hit
	 */
	public int getQueryEnd() {
		return queryend;
	}
	
	/**
	 * Returns the hit start coordinate of the hit
	 * @return hit start coordinate of the hit
	 */
	public int getHitStart() {
		return hitstart;
	}
	
	/** 
	 * Returns the hit end coordinate of the hit
	 * @return hit end coordinate of the hit
	 */
	public int getHitEnd() {
		return hitend;
	}
	
	/**
	 * Returns the e-value of the hit
	 * @return e-value of the hit
	 */
	public double getEvalue() {
		return evalue;
	}
	
	/**
	 * Returns the bit score of the hit
	 * @return bit score of the hit
	 */
	public float getBitScore() {
		return bitscore;
	}
	
	/**
	 * Returns a simple string representation of the hit. This contains the
	 * query and hit protein as well as the e-value.
	 */
	@Override
	public String toString() {
		return "Blast hit\tQuery: " + ProteinManager.getLabel(queryID)
				+ "\tHit: " + ProteinManager.getLabel(hitID) + "\tE-value: "
				+ evalue;
	}


	/**
	 * Orders the hits by their hit ID. Used for internal purposes.
	 */
	public int compareTo(BlastHit o) {
		return (int)Math.signum(hitID - o.hitID); 
	}

}
