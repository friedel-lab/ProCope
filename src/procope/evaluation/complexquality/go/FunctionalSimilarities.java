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

import procope.methods.scores.ScoresCalculator;

/**
 * This class is extended by classes which calculate a semantic
 * similarity score of two given proteins based on GO information.
 * <p>This class is basically just a wrapper of the {@link ScoresCalculator}
 * 
 * @author Jan Krumsiek
 */
public abstract class FunctionalSimilarities extends ScoresCalculator {
	
	/**
	 * Calculates the semantic similarity score of two given proteins.
	 */
	public abstract float getScore(int protein1, int protein2);
	
}
