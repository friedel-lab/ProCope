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

/**
 * One single point in a ROC curve consisting of a true positive rate and a
 * false positive rate value. 
 * 
 * @author Jan Krumsiek
 */

public class ROCPoint {
	
	private float tp;
	private float fp;

	/** 
	 * Initialization constructr. Takes true positive rate and false
	 * positive rate value of that point in the curve
	 * 
	 * @param tp true positive rate value
	 * @param fp false positive rate value
	 */
	public ROCPoint(float tp, float fp) {
		this.tp = tp;
		this.fp = fp;
	}
	
	/**
	 * Returns the true positive rate value of that point of the curve
	 * 
	 * @return true positive rate value
	 */
	public float getTP() {
		return tp;
	}

	/**
	 * Returns the false positive rate value of that point of the curve
	 * 
	 * @return false positive rate value
	 */
	public float getFP() {
		return fp;
	}

}
