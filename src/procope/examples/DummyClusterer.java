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
package procope.examples;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;
import procope.methods.clustering.Clusterer;
import procope.tools.ProCopeException;

public class DummyClusterer implements Clusterer {

	ComplexSet set = new ComplexSet();
	
	public DummyClusterer(int a, int b) {
		if (a==b)
			throw new ProCopeException("This exception is thrown if a==b.");
		Complex complex = new Complex((int)a,b);
		set = new ComplexSet();
		set.addComplex(complex);
	}
	
	public ComplexSet cluster(ProteinNetwork net) {
		return set;
	}

}
