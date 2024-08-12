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
package procope.data.purifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import procope.data.ProteinSet;


/**
 * Represents a single purification experiment having a bait protein and a list
 * of prey proteins which were purified by that bait.
 * 
 * @author Jan Krumsiek
 * @see procope.data.purifications.PurificationData
 */

public class PurificationExperiment implements ProteinSet {
	
	private int bait;
	private Collection<Integer> preys;
	private boolean multiplePreys; 
	
	/**
	 * Creates a new purification experiment having a given bait. Multiple
	 * occurrences of the same prey will not be counted. (see also: {@link
	 * #PurificationExperiment(int, boolean)}
	 * 
	 * @param bait bait protein for this experiment
	 */
	public PurificationExperiment(int bait) {
		this(bait, false);
	}
	
	
	/**
	 * Creates a new purification experiment having a given bait. The
	 * {@code multiplePreys} parameter defines whether multiple occurrences
	 * of a prey are counted or not
	 * 
	 * @param bait bait protein for this experiment
	 * @param multiplePreys if {@code false} each prey protein can only occur
	 *        in the preys list once 
	 */
	public PurificationExperiment(int bait, boolean multiplePreys) {
		this.bait = bait;
		this.multiplePreys = multiplePreys;
		if (multiplePreys)
			this.preys = new ArrayList<Integer>();
		else
			this.preys = new HashSet<Integer>();
	}
	
	/**
	 * Returns if multiple occurences of the same prey are allowed in the 
	 * preys list.
	 * 
	 * @return {@code true} if multiple prey occurences are allowed, {@code 
	 * false} if each prey will only occur once.
	 */
	public boolean multiplePreys() {
		return multiplePreys;
	}
	
	/**
	 * Returns the bait of this purification experiment
	 * @return protein protein
	 */
	public int getBait() {
		return bait;
	}
	
	/**
	 * Add a prey to the list of preys for this experiment
	 * 
	 * @param prey new prey protein
	 */
	public void addPrey(int prey) {
		preys.add(prey);
	}
	
	/**
	 * Add a list of preys to this experiment.
	 * 
	 * @param list list of prey proteins to add to the experiment
	 */
	public void addPreys(Collection<Integer> list) {
		preys.addAll(list);
	}
	
	/**
	 * Returns the list of prey proteins for this experiment. For {@code 
	 * multiplePreys == true} this {@link Collection} will be a {@link Vector},
	 * for {@code multiplePreys == false} it will be a {@link HashSet}
	 * 
	 * @return collection of prey proteins in this experiment
	 */
	public Collection<Integer> getPreys() {
		return preys;
	}

	/**
	 * Returns the set of proteins involved in this purification experiment 
	 * (the bait and all preys)
	 */
	public Set<Integer> getProteins() {
		// create copy of preys
		HashSet<Integer> proteins = new HashSet<Integer>(preys);
		// add prey
		proteins.add(bait);
		
		return proteins;
	}

	/**
	 * Checks if two purification experiments are equal. This is {@code true} 
	 * if and only if the other object is also a {@code PurificationExperiment},
	 * both experiments have the same bait and the prey lists are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof PurificationExperiment))
			throw new IllegalArgumentException("Cannot compare purification experiments with other types of objects.");
		
		PurificationExperiment other = (PurificationExperiment)obj;
		// check bait
		if (this.bait != other.bait) return false;
		// check number of preys
		if (this.preys.size() != other.preys.size()) return false;
		// check preys
		for (int prey : this.preys) {
			if (!other.preys.contains(prey)) return false;
		}
		return true;
	}
}
