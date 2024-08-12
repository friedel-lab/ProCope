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
package procope.tools;


/**
 * This exception is thrown by various methods of this library. Please check
 * out the JavaDocs of the respective class members.
 * <p>This exception is a subclass of {@link RuntimeException} and does not
 * have to be caught by a {@code try/catch} block.
 * 
 * @author Jan Krumsiek
 */
public class ProCopeException extends java.lang.RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8702681983149314372L;

	
	 /**
     * Constructs an <code>LibraryNameException</code> with the 
     * specified detail message. 
     *
     * @param   msg   the detail message.
     */
	public ProCopeException(String msg) {
		super(msg);
	}
}
