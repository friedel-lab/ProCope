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

import java.util.Set;

import procope.data.ProteinSet;
import procope.tools.namemapping.ProteinManager;


/**
 * Extended by all classes which calculate interaction scores between
 * proteins based on experimental or other data.
 * 
 * @author Jan Krumsiek
 */
public abstract class ScoresCalculator implements ProteinSet {

	/**
	 * Returns the interaction score for two given proteins.
	 * 
	 * @param protein1 first protein
	 * @param protein2 second protein
	 * @return interaction score of the two proteins
	 */
	public abstract float getScore(int protein1, int protein2);
	
	/**
	 * Returns the interaction score for two given proteins
	 * provided as String identifiers. Does not have be overridden,
	 * the implementation in the abstract class automatically
	 * uses the ProteinManager to get internal IDs for the given proteins.
	 * 
	 * @param protein1 string identifier of first protein
	 * @param protein2 string identifier of second protein
	 * @return interaction score of the two proteins
	 * @see ProteinManager
	 */
	public float getScore(String protein1, String protein2) {
		return getScore(ProteinManager.getInternalID(protein1),
				ProteinManager.getInternalID(protein2));
	}
	
	/**
	 * Returns the set of proteins involved in this scores calculator.
	 */
	public abstract Set<Integer> getProteins();
	
}
