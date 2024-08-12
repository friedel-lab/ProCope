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
package procope.methods.scores.bootstrap;

import procope.data.complexes.ComplexSet;

/**
 * Represents a single clustering in the bootstrap scores calculation process.
 * Contains the clustering itself, an arbitrary String which describes the 
 * parameters used to calculate this clustering and the MCL efficiency of that 
 * clustering with respect to the clustered network.
 * 
 * <p>
 * This class belongs to the helper classes needed for parallelizable bootstrap
 * scores calculation.
 * 
 * @author Jan Krumsiek
 */
public class BootstrapClustering {
	
	private ComplexSet set;
	private float efficiency;
	private String identifier;

	/**
	 * Create new bootstrap MCL clustering result.
	 * 
	 * @param set the clustering
	 * @param parameters an identifier for the parameter combination used in this clustering
	 * @param efficiency efficiency of that clustering with respect to the scores network
	 */
	public BootstrapClustering(ComplexSet set, String parameters, float efficiency) {
		this.set = set;
		this.efficiency = efficiency;
		this.identifier = parameters;
	
	}
	
	/**
	 * Returns the clustering.
	 * 
	 * @return the clustering
	 */
	public ComplexSet getClustering() {
		return set;
	}
	
	
	/**
	 * Returns the MCL efficiency of the clustering with respect to the
	 * clustered network.
	 * 
	 * @return MCL efficiecny of the clustering
	 */
	public float getEfficiency() {
		return efficiency;
	}
	
	public String getParameters() {
		return identifier;
	}
	
}
