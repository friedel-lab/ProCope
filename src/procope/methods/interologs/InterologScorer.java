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

/**
 * Implemented by classes which calculate scores for homology-transferred 
 * interactions (interologs). 
 * 
 * @see InterologsCalculator
 * @author Jan Krumsiek
 *
 */
public interface InterologScorer {
	
	/**
	 * Calculates the score of a new interolog based on the source proteins 
	 * (the nodes of the original interaction) and the target proteins
	 * (the nodes of the transferred interaction).
	 * 
	 * @param source1 source protein 1
	 * @param target1 target protein 1
	 * @param source2 source protein 2
	 * @param target2 target protein 2
	 * @return score for the new interolog
	 */
	public float getInterologScore(int source1, int target1, int source2, int target2);

}
