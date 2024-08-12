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
package procope.tools.namemapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import procope.data.networks.ProteinNetwork;


/**
 * Manages synonyms for protein identifiers. Contains <i>identifier/synonym</i>
 * pairs. The {@link #resolveSynonym(String)} method then maps any incoming
 * <i>synonyms</i> to their <i>identifiers</i>. Transitive relations will be
 * followed. That is, if you add the synonym-mappings {@code a => b} and
 * {@code b => c} then {@code a} will be mapped to {@code c}.
 * 
 * @author Jan Krumsiek
 *
 * @see ProteinManager
 */
public class Synonyms {
	
	HashMap<String, String> synonymToTarget;
	HashMap<String, Set<String>> targetToSynoyms;
	
	/**
	 * Adds a new synonym. The given {@code synonym} will be mapped to
	 * {@code identifier} by {@link #resolveSynonym(String)}.
	 * 
	 * @param identifier identifier the synonym is mapped to
	 * @param synonym synonym of the identifier 
	 */
	public void addSynonym(String identifier, String synonym) {
		// 1. possibility: target already a synonym for another target
		String nextTarget = synonymToTarget.get(identifier);
		if (nextTarget != null) {
			addSynonymToLists(nextTarget, synonym);
		} else {
			// 2. possibility: the synonym is already a target with synonyms
			Set<String> synSynonyms = targetToSynoyms.get(synonym);
			if (synSynonyms != null) {
				// add all those synonyms as synonyms of the current target
				for (String synSyn : synSynonyms)
					addSynonymToLists(identifier, synSyn);
				// remove mapping for original synonym
				targetToSynoyms.remove(synonym);
				// finally add the synonym itsself
				addSynonymToLists(identifier, synonym);
			} else
				// 3. possibility: standard case, just add to list
				addSynonymToLists(identifier, synonym);
		}
		
	
	}
	
	/**
	 * Returns the identifier a synonym is mapped to or {@code id} directly
	 * of no synonym is associated with that string.
	 * 
	 * @param id identifier which is resolved
	 * @return mapped identifier if {@code id} is registered as a synonym or 
	 *         {@code id} directly otherwise
	 */
	public String resolveSynonym(String id) {
		if (synonymToTarget.size() == 0)
			return id;
		
		String real = synonymToTarget.get(id);
		if (real != null)
			return real;
		else
			return id;
	}
	
	/**
	 * Returns all synonyms associated with a given identifier.
	 * 
	 * @param identifier identifier for which synonyms are looked up
	 * @return synonyms for that identifier or {@code null} if no
	 *         synonyms are associated with the identifer
	 */
	public Set<String> getSynonyms(String identifier) {
		return targetToSynoyms.get(identifier);
	}
	
	/**
	 * Creates a new empty set of synonyms
	 */
	public Synonyms() {
		this.synonymToTarget = new HashMap<String, String>();
		this.targetToSynoyms = new HashMap<String, Set<String>>();
	}

	/**
	 * Adds all mappings induced by the edges of a given directed network to
	 * the synonyms list. For each directed edge {@code a => b} the 
	 * corresponding synonym mapping will be added with {@code a} being the 
	 * synonym and {@code b} its associated identifier or vice versa.
	 * <p>If the networks contains circles or nodes with multiple successors
	 * the method's behavious is undefined (but it will <u>not</i> crash).
	 * 
	 * @param mappings directed mapping network with synonyms to be added
 	 * @param targetFirst {@code true}: the first node of each edge (the source
	 *                    of the directed edge) is the target protein identifier
	 *                    where as the second node (the target of the directed
	 *                    edge) is its synonym; {@code false}: vice versa,
	 *                    synonym comes first, then the target 
	 * @throws IllegalArgumentException if the network is undirected
	 */
	public void addMappingNetwork(ProteinNetwork mappings, boolean targetFirst) throws IllegalArgumentException {
		if (!mappings.isDirected())
			throw new IllegalArgumentException("Synonym networks must be directed");
		
		// iterate through network
		int[] edges = mappings.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			// get both identifiers
			String id1 = ProteinManager.getLabel(edges[i]);
			String id2 = ProteinManager.getLabel(edges[i+1]);
			if (targetFirst)
				addSynonym(id1, id2);
			else
				addSynonym(id2, id1);
		}
	}
	
	/**
	 * Adds a given identifier/synonym mapping to the lists and maps
	 */
	private void addSynonymToLists(String target, String synonym) {
		
		// add to synonym->target mapping
		synonymToTarget.put(synonym, target);
		
		// add to target->synonym mapping
		Set<String> list = targetToSynoyms.get(target);
		// create if necessary
		if (list == null) {
			list = new HashSet<String>();
			targetToSynoyms.put(target, list);
		}
		// add to list
		list.add(synonym);
	}

}
