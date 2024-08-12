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
package procope.data.networks;

import java.util.Map;

/**
 * Represents one edge in a ProteinNetwork. Contains the IDs of both involved
 * proteins, the weight of the edge and any annotations associated with this
 * edge. The class is not instantiatable outside of the package.
 * <p><b>Note:</b>Source and target information are not relevant for undirected
 * networks. 
 * 
 * @author Jan Krumsiek
 *
 */

public class NetworkEdge implements Comparable<NetworkEdge> {
	
	private int from;
	private int to;
	private float weight;
	private Map<String, Object> annotations;

	/**
	 * Constructor which takes all values, package-private
	 */
	NetworkEdge(int source, int target, float weight, Map<String, Object> annotations) {
		this.from = source;
		this.to = target;
		this.weight = weight;
		this.annotations = annotations;
	}
	
	/**
	 * Returns the protein from which this edge is originating. 
	 * @return source protein ID
	 */
	public int getSource() {
		return from;
	}
	
	/**
	 * Returns the protein to which this edge is leading.
	 * @return target protein ID
	 */
	public int getTarget() {
		return to;
	}
	
	/**
	 * Returns the weight of this edge
	 * @return weight of the edge or {@code Float.NaN} if the edge has no 
	 *         weight associated
	 */
	public float getWeight() {
		return weight;
	}
	
	/**
	 * Returns all annotations associated with this edge
	 * 
	 * @return a map of associates or an empty, immutable map if no there are
	 *         no annotations for this edge
	 */
	public Map<String, Object> getAnnotations() { 
		return annotations;
	}
	
	/**
	 * Retrieves a single annotation from the annotation list
	 * 
	 * @param key key of the annotation
	 * @return the value of that annotation or {@code null} if no value
	 *         is associated with that key
	 */
	public Object getAnnotation(String key) {
		return annotations.get(key);
	}

	/**
	 * Compare this network edge with another. An edge is greater than another edge
	 * if its weight is greater.
	 */
	public int compareTo(NetworkEdge o) {
		return (int)Math.signum(this.getWeight()-o.getWeight());
	}
	
}
