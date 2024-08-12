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

/**
 * Interface which is implemented by classes which define constraints for
 * the calculation of BBHs from BLAST hits.
 * 
 * @author Jan Krumsiek
 */
public interface BlastBBHConstraints {

	/**
	 * Function which is called by the BBH calculator to ask whether a given
	 * BBH is accepted or not.
	 * 
	 * @param protein1 first protein of the BBH
	 * @param protein2 second protein of the BBH
	 * @param forwardHit first BLAST hit leading to that BBH
	 * @param backwardHit second BLAST hit leading to that BBH
	 * @return whether the BBH is accepted or not
	 */
	public boolean acceptBBH(int protein1, int protein2, BlastHit forwardHit, BlastHit backwardHit);
	
}
