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
package procope.data.petrinets;

import java.util.Map;

import procope.tools.Tools;


/**
 * Represents one arc in a Petri net (the connection between a place and a
 * transition). It consists of the place ID it is associated with and optionally
 * a set of annotations for that edge represented as key/value pairs.
 * 
 * @author Jan Krumsiek
 */
public class Arc {
	
	private String place;
	private Map<String, String> attributes;

	/**
	 * Creates an arc to a given place
	 * 
	 * @param place place to which this arc leads
	 */
	public Arc(String place) {
		this.place = place;
		this.attributes = Tools.EMPTY_STRING_MAP;
	}
	
	/**
	 * Creates an arc to a given places having a specified set of attributes
	 * 
	 * @param place place to which this arc leads
	 * @param attributes attributes for that arc
	 */
	public Arc(String place, Map<String, String> attributes) {
		this.place = place;
		this.attributes = attributes;
	}
	
	/**
	 * Returns the ID of the place associated with this arc
	 * 
	 * @return ID of the place for this arc
	 */
	public String getPlaceID() {
		return place;
	}
	
	/**
	 * Returns the attributes of this arc
	 * 
	 * @return attributes of this arc
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

}
