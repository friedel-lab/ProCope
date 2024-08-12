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
 * Represents one place in a Petri net. Each place has a unique string
 * identifier and optinally a set of attributes given as key/value pairs.
 * 
 * @author Jan Krumsiek
 * @see PetriNetCreator
 */

public class Place {
	
	private String id;
	private Map<String, String> attributes;

	/**
	 * Create place having the given string identifier
	 * @param id identifier for that place
	 */
	public Place(String id) {
		this.id = id;
		this.attributes = Tools.EMPTY_STRING_MAP;
	}
	
	/**
	 * Create places having the given string identifier and a set of attributes.
	 * 
	 * @param id identifier for that place
	 * @param attributes attributes for that place
	 */
	public Place(String id, Map<String, String> attributes) {
		this.id = id;
		this.attributes = attributes;
	}
	
	/**
	 * Returns the ID of this place
	 * 
	 * @return ID of this place
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * Returns the attributes of this place
	 * 
	 * @return attributes of this place
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

}
