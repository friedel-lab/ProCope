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
package procope.tools.math;

import java.util.Collection;

import procope.evaluation.comparison.Point;


/**
 * Interface implemented by classes which calculate correlation coefficients
 * of a list of pairs of data points.
 * 
 * @author Jan Krumsiek
 * @see PearsonCoefficient
 * @see SpearmanCoefficient
 */

public interface CorrelationCoefficient {
	
	/**
	 * Feeds one data point into the dataset
	 *  
	 * @param x data point's x
	 * @param y data point's y
	 */
	public void feedData(float x, float y);
	
	/**
	 * Feeds a list of data points into the dataset.
	 * 
	 * @param data list of {@link Point points}.
	 */
	public void feedData(Collection<Point> data);
	
	/**  
	 * Feeds one data point into the dataset
	 * 
	 * @param point data point to be added 
	 */
	public void feedData(Point point);
	
	/**
	 * Calculates the correlation coefficient for the current list of data points.
	 * 
	 * @return correlation coefficient of current list of data points
	 */
	public float getCorrelationCoefficient(); 
	

}
