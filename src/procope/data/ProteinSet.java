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

import java.util.Set;

/**
 * This interface is implemented by classes which work on or contain a distinct
 * set of proteins which are represented by internal IDs (see also: 
 * {@link procope.tools.namemapping.ProteinManager}).<p>Implementing classes should ensure 
 * that all proteins they are working on will be returned in the 
 * {@link #getProteins()} call.
 * 
 * @author Jan Krumsiek
 *
 */
public interface ProteinSet {

	/**
	 * Returns the set of proteins this object is working with or containing.
	 * 
	 * @return a set of internal IDs
	 */
	public Set<Integer> getProteins();

}
