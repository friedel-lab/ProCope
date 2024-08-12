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
 * Represents one transition in a Petri net. It consists of a set of input
 * and output arcs and optionally a set of attributes  given as key/value pairs.
 * 
 * @author Jan Krumsiek
 * @see PetriNetCreator
 */

public class Transition {
	
	private Arc[] in;
	private Arc[] out;
	private Map<String, String> attributes;

	/**
	 * Create transition consisting of given sets of input and output arcs
	 * @param in array of input arcs
	 * @param out array of output arcs
	 */
	public Transition(Arc[] in, Arc[] out) {
		this.in = in;
		this.out = out;
		this.attributes = Tools.EMPTY_STRING_MAP;
	}
	
	/**
	 * Create transition consisting of given sets of input and output arcs and
	 * a map of attributes
	 * @param in array of input arcs
	 * @param out array of output arcs
	 * @param attributes attributes for that transition
	 */
	public Transition(Arc[] in, Arc[] out, Map<String, String> attributes) {
		this.in = in;
		this.out = out;
		this.attributes = attributes;
	}

	/** 
	 * Returns the attributes associated with this transition.
	 * 
	 * @return attributes for this transition
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * Returns the input arcs for this transition.
	 * 
	 * @return array of input arcs for this transition
	 */
	public Arc[] getIn() {
		return in;
	}
	
	/**
	 * Returns the output arcs for this transition
	 * 
	 * @return array of output arcs for this transition
	 */
	public Arc[] getOut() {
		return out;
	}

}
