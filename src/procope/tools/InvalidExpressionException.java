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
 * Thrown by {@link BooleanExpression#BooleanExpression(String)} if the
 * specified expression could not be parsed.
 */
public class InvalidExpressionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3362176095685798454L;

	/**
     * Constructs an <code>InvalidExpressionException</code> with the 
     * specified detail message. 
     *
     * @param   msg   the detail message.
     */
	public InvalidExpressionException(String msg) {
		super(msg);
	}
}
