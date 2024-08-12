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
package procope.evaluation.networkperformance;

import java.util.Iterator;
import java.util.List;

/**
 * An immutable list of ROC points, result of a ROC curve calculation 
 */
public class ROCCurve implements Iterable<ROCPoint> {
	
	private List<ROCPoint> data;

	/**
	 * Constructor which initializes the ROC curve with a given list of points
	 * 
	 * @param data list of {@link ROCPoint} objects 
	 */
	public ROCCurve(List<ROCPoint> data) {
		this.data= data;
	}
	
	/**
	 * Returns the list of {@link ROCPoint} objects in this curve. <b>Note:</b>
	 * This method returns the original object, changes to that list will also 
	 * affect the ROC curve.
	 * 
	 * @return list of {@link ROCPoint} objects which make up the ROC curve
	 */
	public List<ROCPoint> getData() {
		return data;
	}

	/**
	 * Returns an iterator over the points in this curve
	 */
	public Iterator<ROCPoint> iterator() {
		return data.iterator();
	}
	
	

}
