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
package procope.tools.userclasses;

import org.xml.sax.SAXException;

/**
 * Thrown if a user scores calculator or clusterer could not no be loaded
 * from the respective XML file due to specification errors (e.g. missing
 * attributes).
 * 
 * @see UserClusterer
 * @see UserScoresCalculator
 * @author Jan Krumsiek
 */
public class UserClassSpecException extends SAXException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7380488335133345595L;

	/**
	 * Creates a new UserClassSpecException
	 */
	public UserClassSpecException() {
	}
	
	/**
	 * Creates a new UserClassSpecException with the given message
	 * @param msg
	 */
	public UserClassSpecException(String msg) {
		super(msg);
	}


}
