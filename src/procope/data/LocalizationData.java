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
package procope.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import procope.evaluation.complexquality.Colocalization;
import procope.evaluation.networkperformance.ROC;

/**
 * A set of cellular localization information for proteins.
 * <p><i>Colocalization</i> of two proteins means that they share at least
 * one localization in the given data set.
 * <p>Localizations might be for instance, <i>nuclear</i>, <i>ER</i>,
 * <i>cytoplasmatic</i> etc.
 * 
 * @see Colocalization
 * @see LocalizationDataReader
 * @see ROC#calculateROCCurves(java.util.List, procope.data.complexes.ComplexSet, procope.data.complexes.ComplexSet, LocalizationData, boolean)
 * @author Jan Krumsiek
 */
public class LocalizationData {
	
	private StringToIntMapper locMapper; // special name mapper for the localization strings
	
	private HashMap<Integer, Collection<Integer>> localizations;
	
	/**
	 * Creates an empty localization data set.
	 */
	public LocalizationData() {
		// create name mapper (here: for string=>int mapping of localization names)
		locMapper = new StringToIntMapper();
		// create hashmap
		localizations = new HashMap<Integer, Collection<Integer>>();	
	}

	/**
	 * Determines whether two proteins are colocalized. 
	 * 
	 * @param protein1 first protein
	 * @param protein2 second protein
	 * @return  {@code 1} if the proteins are colocalized, {@code 0} if they are
	 *         not colocalized and {@code -1} if at least one protein has no
	 *         localization information and thus colocalization cannot be 
	 *         determined
	 */
	public byte areColocalized(int protein1, int protein2) {
		// javadoc: 1 if colocalized, 0 if not colocalized, -1 if not known because at least one of the proteins has no data
		
		// get localizations for both proteins
		Collection<Integer> locs1 = localizations.get(protein1);
		Collection<Integer> locs2 = localizations.get(protein2);
		
		if (locs1 == null || locs2 == null)
			return -1;
		
		// any overlap => colocalized
		for (Integer loc1 : locs1) {
			if (locs2.contains(loc1))
				return 1;
		}
		
		return 0;
	}

	/**
	 * Get the set of localizations for a given protein. Returns
	 * a list of internal integer identifiers. The actual name of that 
	 * localization can be determined using
	 * {@link #getLocalizationName(int)}.
	 * 
	 * @param protein protein for which localizations are retrieved
	 * @return list of localizations for the given proteins (internal integer IDs) 
	 *         or {@code null} if there are no localization information for
	 *         the given protein
	 */
	public Collection<Integer> getLocalizations(int protein) {
		return localizations.get(protein);
	}
	
	/**
	 * Returns the name of a localization for a given internal integer ID
	 * 
	 * @param id internal integer ID
	 * @return name of that localization or {@code null} if that internal ID is
	 *         not assigned
	 */
	public String getLocalizationName(int id) {
		return locMapper.getStringID(id);
	}
	
	/**
	 * Returns the number of different localizations in the dataset
	 * @return number of localizations in the set
	 */
	public int getNumberOfLocalizations() {
		return locMapper.getItemCount();
	}

	/**
	 * Annotate a protein with a given localization.
	 * 
	 * @param protein protein to be annotated
	 * @param localization localization string
	 */
	public void addLocalization(int protein, String localization) {
		Collection<Integer> locCol = localizations.get(protein);
		
		if (locCol == null) {
			locCol = new HashSet<Integer>();
			localizations.put(protein, locCol);
		}
		locCol.add(locMapper.getIntID(localization));
		
	}
	
}
