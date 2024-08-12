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
package procope.evaluation.complexquality.go;

/**
 * This interface must be implemented by classes which calculate a semantic 
 * similarity of two GO terms.

 * @author Jan Krumsiek
 */
public interface TermSimilarities {
	
	/**
	 * Calculates the similarity of two given GO terms.
	 * 
	 * @param term1ID ID of the first term
	 * @param term2ID ID of the second term
	 * @return similarity value for the two given terms
	 */
	public float calculateSimilarity(String term1ID, String term2ID);

}
