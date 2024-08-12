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

import java.util.HashMap;
import java.util.Map;

/**
 * A set of sequences internally represented by a internal_id=>sequence 
 * mapping. Registered protein can only have one sequence.
 * 
 * 
 * @author Jan Krumsiek
 * @see    procope.methods.interologs.FASTAParser
 * @see	   procope.tools.namemapping.ProteinManager
 */

public class Sequences {
	
	// the map which contains all sequences
	private Map<Integer, String> sequences;

	/**
	 * Creates an empty sequences set.
	 */
	public Sequences() {
		// initialize with empty map
		this.sequences = new HashMap<Integer, String>();
	}
	
	/**
	 * Creates a sequence object from a given map. The keys must be internal
	 * protein IDs, the values are the sequences.
	 * 
	 * @param sequences Map containing the sequences 
	 */
	public Sequences(Map<Integer, String> sequences) {
		this.sequences = sequences;
	}
	
	/**
	 * Returns the sequence for a given internal protein ID.
	 * 
	 * @param key internal ID of the protein 
	 * @return Sequence as a String or <tt>null</tt> if no sequence is 
	 * associated with this protein
	 */
	public String getSequence(int key) {
		return sequences.get(key);
	}
	
	/**
	 * Adds all sequences of another Sequences object to this set. Sequences of
	 * proteins which are already in the current set will be overridden
	 * 
	 * @param toAdd Sequences to add
	 */
	public void addAll(Sequences toAdd) {
		sequences.putAll(toAdd.getAll());
	}
	
	/**
	 * Adds a single sequence to this set. If there already is a sequence for 
	 * this proteins in the set it will be overridden.
	 * 
	 * @param id internal ID of the protein
	 * @param sequence sequence to be added
	 */
	public void add(int id, String sequence) {
		sequences.put(id,sequence);
	}
	
	/**
	 * Returns the map which backs this object. <b>Attention</b>: A directed 
	 * reference and no copy will be returned. Changes to 
	 * this map object will also affect the Sequences object.
	 * 
	 * @return Map object backing the sequences collection
	 */
	public Map<Integer, String> getAll() {
		return sequences;
	}

}
