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
 * Implements the Pearson product-moment correlation coefficient which 
 * calculates the correlation between two given lists of data points.
 * <p>See also: <a href="http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient" 
 * target="_blank">Wikipedia</a>
 * 
 * @author Jan Krumsiek
 */
public class PearsonCoefficient implements CorrelationCoefficient {

	// Pearson coefficient variables
	private float sum_XY=0;
	private float sum_X=0;
	private float sum_X2=0;
	private float sum_Y=0;
	private float sum_Y2=0;
	private float N=0;

	// javadoc copied from interface
	public void feedData(Collection<Point> data) {
		for (Point p : data) {
			feedData(p.getX(), p.getY());
		}
	}
	
	// javadoc copied from interface
	public void feedData(Point p) {
		feedData(p.getX(), p.getY());
	}
	
	// javadoc copied from interface
	public void feedData(float x, float y) {
		// increase Pearson coefficient variables
		sum_XY += x*y;
		sum_X += x;
		sum_X2 += x*x;
		sum_Y += y;
		sum_Y2 += y*y;
		N++;

	}

	/**
	 * Calculates the Pearson correlation coefficient for the current dataset.
	 */
	public float getCorrelationCoefficient() {
		
		// calculate pearson coefficient
		float pearson_nom = sum_XY - (sum_X*sum_Y)/N;
		float pearson_denom_t1 = sum_X2 - (sum_X*sum_X) / N;
		float pearson_denom_t2 = sum_Y2 - (sum_Y*sum_Y) / N;
		float pearson = pearson_nom / (float)Math.sqrt(pearson_denom_t1*pearson_denom_t2);

		return pearson;
	}
	
	
}
