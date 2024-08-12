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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import procope.data.networks.ProteinNetwork;


/**
 * Represents an immutable list of BLAST hits which are normally parsed from
 * a BLAST result file. Contains methods for the detection of best hits and 
 * bidirectional best hits.
 * 
 * @author Jan Krumsiek
 */


public class BlastHits implements Iterable<BlastHit> {

	private Collection<BlastHit> hits;
	
	private HashSet<Integer> queryIDs;
	private HashSet<Integer> hitIDs;
	
	private HashMap<Integer, List<BlastHit>> hitsPerQuery;
	private HashMap<Integer, List<BlastHit>> bestHitsPerQuery;

	/**
	 * Creates a new BLAST hits object from a given list of {@link BlastHit hits}.
	 * @param hits the BLAST hits list with which the object is initialized
	 */
	public BlastHits(Collection<BlastHit> hits) {
		this.hits = hits;
		findBestHits();
	}
	
	/**
	 * Returns an iterator over all BLAST hits contained in this set
	 */
	public Iterator<BlastHit> iterator() {
		return hits.iterator();
	}
	
	/**
	 * Find best hits for all proteins in this BLAST result
	 */
	private void findBestHits() {
		// initialize
		queryIDs = new HashSet<Integer>();
		hitIDs = new HashSet<Integer>();
		hitsPerQuery = new HashMap<Integer, List<BlastHit>>();
		bestHitsPerQuery = new HashMap<Integer, List<BlastHit>>();

		// iterate over all hits
		for (BlastHit hit : hits) {
			int queryID = hit.getQueryID();
			// accumulate query & database hit ids
			queryIDs.add(queryID);
			hitIDs.add(hit.getHitID());
			// accumulate hits per query
			List<BlastHit> queryHits = hitsPerQuery.get(queryID);
			if (queryHits == null) {
				queryHits = new ArrayList<BlastHit>();
				hitsPerQuery.put(queryID, queryHits);
			}
			queryHits.add(hit);
		}
		
		// sort hit lists (so they can be search using binary search later on)
		for (List<BlastHit> hitList : hitsPerQuery.values())
			Collections.sort(hitList);

		// calculate best hit(s) for each query
		for (Integer queryID : queryIDs) {
			Collection<BlastHit> queryHits = hitsPerQuery.get(queryID);
			// iterate over all hits and find hit with lowest evalue
			List<BlastHit> bestHits = new ArrayList<BlastHit>();
			double bestEval = Double.POSITIVE_INFINITY;
			int bestLength = 0;
			for (BlastHit hit : queryHits) {
				double evalue = hit.getEvalue();
				int length = hit.getHitEnd() - hit.getHitStart();
				if (evalue <= bestEval && length >= bestLength && (evalue < bestEval || length > bestLength)) {
					// new best, delete old list
					bestHits.clear();
					bestHits.add(hit);
					bestEval = evalue;
					bestLength = length;
				} else if (evalue == bestEval && bestLength == length) {
					// just add to list
					bestHits.add(hit);
				}
			}
			
			bestHitsPerQuery.put(queryID, bestHits);
		}
	}
	
	
	/**
	 * Returns all hits for a given query protein
	 * 
	 * @param queryID the protein used as query in the BLAST run
	 * @return all hits for the given query
	 */
	public Collection<BlastHit> getHitsPerQuery(int queryID) {
		return hitsPerQuery.get(queryID);
	}

	/**
	 * Returns all best hits for a given query. A best hit is a hit in the
	 * BLAST result with the best score for that query. Eventual multiple best 
	 * always have the same score.
	 * 
	 * @param queryID the query protein for which the best hits will be retrieved
	 * @return list of best hits for that query
	 */
	public Collection<BlastHit> getBestHits(Integer queryID) {
		return bestHitsPerQuery.get(queryID);
	}
	
	/**
	 * Returns a list of all proteins used as query in this BLAST result
	 * @return list of all query proteins
	 */
	public Collection<Integer> getAllQueryIDs() {
		return queryIDs;
	}
	
	/**
	 * Returns al ist of all proteins which were hit at least once in the database.
	 * @return list of all hit proteins
	 */
	public Collection<Integer> getAllHitIDs() {
		return hitIDs;
	}
	
	/**
	 * Returns the number of hits in this BLAST result
	 * @return number of hits in the list
	 */
	public int getNumberOfHits() {
		return hits.size();
	}
	
	/**
	 * Calculate bidirectional best hits (BBHs). A BBH is contained of two 
	 * proteins which mutually identify each other as the best-scoring blast
	 * hit in the database. The calculation if BBHs requires another BLAST
	 * result in the backward direction (query as database and database as query).
	 * <p>Note: If proteins have multiple best hits there might be more than one
	 * BBH associated with a single protein.
	 * 
	 * @param backward BLAST result which should have used the query of this
	 *                 result as the database and the database of this result
	 *                 as the query
	 * @param constraints constraints for filtering BBHs or {@code null} if all
	 *                    identified BBHs should be returned
	 * @return bipartite network containing one edge for each identified BBH.
	 */
	public ProteinNetwork getBidirectionalBestHits(BlastHits backward,
			BlastBBHConstraints constraints) {
		
		ProteinNetwork newNet = new ProteinNetwork(false);
		
		BlastHits forward = this;
		// iterate over all queries in the forward direction
		for (Integer forwardQueryID : forward.getAllQueryIDs()) {
			// iterate over best hits for this query
			for (BlastHit bestForwardHit : forward.getBestHits(forwardQueryID)) {
				// check whether these best hit proteins also have our current query ID as a best hit
				Collection<BlastHit> bestBackwardHits = backward.getBestHits(bestForwardHit.getHitID());
				if (bestBackwardHits != null) {
					for (BlastHit bestBackwardHit : bestBackwardHits) {
						if (bestBackwardHit.getHitID() == forwardQueryID) {
							if (constraints == null
									|| constraints.acceptBBH(forwardQueryID,
											bestBackwardHit.getQueryID(),
											bestForwardHit, bestBackwardHit)) {
								// we identified a pairwise best hit
								newNet.setEdge(forwardQueryID, bestBackwardHit.getQueryID());
							}
						}
					}
				}
			}
		}
		
		return newNet;
		
	}
	
	
	/**
	 * Retrieves a specific BLAST hit from the result.
	 *  
	 * @param query query protein
	 * @param hit hit (database) protein
	 * @return the hit if it exists or {@code null} if not
	 */
	public BlastHit getHit(int query, int hit) {
		List<BlastHit> hits = hitsPerQuery.get(query);
		if (hits == null) return null;
		// search correct entry and return (binary search)
		int low = 0;
		int high = hits.size()-1;
		int mid;
		while( low <= high ) {
			mid = (low + high) / 2;
			if(hit > hits.get(mid).getHitID())
				low = mid + 1;
			else if(hit < hits.get(mid).getHitID())
				high = mid - 1;
			else
				return hits.get(mid);
		}
		// nothing found => return null
		return null;
	}
	
}
