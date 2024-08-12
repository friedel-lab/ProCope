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
package procope.evaluation.comparison;

import procope.tools.math.CorrelationCoefficient;

/**
 * Represents a pair of two values. 
 * <p>Can for instance be used to calculate correlation coefficients.
 * @author jan
 * @see CorrelationCoefficient
 */

public class Point {
	
	private float x;
	private float y;

	/**
	 * Creates a new point of two given values
	 * 
	 * @param x x value
	 * @param y y value
	 */
	public Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns the x coordinate
	 * @return x coordinate
	 */
	public float getX() {
		return x;
	}
	
	/**
	 * Returns the y coordinate
	 * @return y coordinate
	 */
	public float getY() {
		return y;
	}

}
