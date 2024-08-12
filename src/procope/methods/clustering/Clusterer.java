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
package procope.methods.clustering;

import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;

/**
 * Common interface implemented by clustering methods which take a 
 * {@link ProteinNetwork} as input and return a set of clusters calculated
 * on that similarity network.
 * 
 * @author Jan Krumsiek
 */
public interface Clusterer {
	
	/**
	 * Calculates a clustering represented as {@link ComplexSet} from a given
	 * scores network. <b>Note:</b> All implementing clustering methods should
	 * work on <u>similarities</u>, not distances
	 * 
	 * @param net similarity network which will be clustered
	 * @return the resulting clustering
	 */
	public ComplexSet cluster(ProteinNetwork net);

}
