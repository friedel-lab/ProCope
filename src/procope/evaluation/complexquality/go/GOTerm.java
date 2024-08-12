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
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single GO term in an ontology network. Contains the identifier
 * of the term as well as its parents and children.
 * 
 * @author Jan Krumsiek
 */
public class GOTerm {
	
	/**
	 * Identifier of this GO term, for example: GO:0005471
	 */
	String ID;
	/**
	 * List of parents of this term.
	 */
	Collection<GOTerm> parents;	
	/** 
	 * List of children of this term
	 */
	Collection<GOTerm> children; 
	
	/**
	 * Name of the GO term
	 */
	String name;

	float prob = Float.NaN; // calculated probability of this term, not always set

	/**
	 * Creates a term object with the given GO term ID and name
	 * @param ID ID of the GO term
	 */
	public GOTerm(String ID) {
		this.ID = ID;
		// create container objects
		parents = new ArrayList<GOTerm>();
		children = new ArrayList<GOTerm>();
	}
	
	/**
	 * Returns a String representation of this GO term containing its id but
	 * no parent or child relationships.
	 */
	public String toString() {
		return "GO-ID: " + ID;
	}
	
	@Override
	public int hashCode() {
		return ID.hashCode();
	}
	
	/**
	 * Returns the ID of this GO term.
	 * @return ID of the GO term 
	 */
	public String getID() {
		return ID;
	}
	
	/**
	 * Returns the list of parent terms for this GO term.
	 * @return parent terms
	 */
	public Collection<GOTerm> getParents() {
		return parents;
	}
	
	/**
	 * Returns the list of child terms for this GO term.
	 * @return child terms
	 */
	public Collection<GOTerm> getChildren() {
		return children;
	}
	
	/**
	 * Returns the name of this GO term.
	 * @return name of this GO term
	 */
	public String getName() {
		return name;
	}
}
