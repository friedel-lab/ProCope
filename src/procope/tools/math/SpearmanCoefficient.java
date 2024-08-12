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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import procope.evaluation.comparison.Point;
import procope.tools.Tools;


/**
 * Implements Spearman's rank correlation coefficient to calulate the 
 * correlation between two given rows of data points. It is defined as the
 * {@link PearsonCoefficient Pearson coefficient} of the 
 * {@link #rankArray(Comparable[]) rankings} of both data rows.
 * 
 * @author Jan Krumsiek
 */

public class SpearmanCoefficient implements CorrelationCoefficient {

	private ArrayList<Float> X;
	private ArrayList<Float> Y;

	/**
	 * Creates the Spearman rank coefficient object.
	 */
	public SpearmanCoefficient() {
		X = new ArrayList<Float>();
		Y = new ArrayList<Float>();		
	}
	
	// javadoc copied from interface
	public void feedData(float x, float y) {
		// gather data
		X.add(x);
		Y.add(y);
	}

	// javadoc copied from interface
	public void feedData(Collection<Point> data) {
		for (Point p : data) {
			X.add(p.getX());
			Y.add(p.getY());
		}
		
	}

	// javadoc copied from interface
	public void feedData(Point p) {
		X.add(p.getX());
		Y.add(p.getY());
	}
	
	/**
	 * Calculates Scorrelation coefficient for the current dataset.
	 */
	public float getCorrelationCoefficient() {
		// convert to arrays & calculate ranks
		float[] rankX = SpearmanCoefficient.rankArray(X.toArray(new Float[0]));
		float[] rankY = SpearmanCoefficient.rankArray(Y.toArray(new Float[0]));
		
		// calculate pearson coefficient between ranks
		PearsonCoefficient pearson = new PearsonCoefficient();
		for (int i=0; i<rankX.length; i++) 
			pearson.feedData(rankX[i], rankY[i]);
		
		float cor = (float)pearson.getCorrelationCoefficient();
		
		return cor;
		
	}
	
	/**
	 * Calculates the ranking for a given array of comparable objects. This 
	 * ranking contains ascending integer numbers in the natural ordering
	 * of the objects. Equal objects get averaged ranks. Note that a sorting
	 * step is required to calculate this ranking, thus the time complexity
	 * of the whole ranking process is <i>O(n<sup>2</sup>)</i>.
	 * 
	 * @param data array of comparable objects to be ranked
	 * @return ranking array for the given input array
	 */
	public static float[] rankArray(Comparable<?>[] data) {
		
		// copy & sort array
		Comparable<?>[] sorted = Tools.arrCopyOf(data, data.length);
		Arrays.sort(sorted);
			
			/*new Comparable[data.length];
		for (int i=0; i<data.length; i++)
			sorted[i] = data[i];
			*/
		
		// FIRST RUN: determine ranks
		float[] sortedRanks = new float[data.length];
		// iterate over sorted array
		int curRank=1;
		for (int i=0; i<sorted.length; i++) {
			// find if there are more items with the same value
			int next=0;
			for (int j=i+1; j<sorted.length && sorted[j].equals(sorted[i]); j++)
				next++;
	
			int totalRank=0;
			// calculate rank
			for (int j=0; j<=next; j++)
				totalRank += curRank + j;
			// calculate real rank
			float realRank = (float)totalRank / ((float)next+1); 
			
			// apply ranks
			for (int j=0; j<=next; j++)
				sortedRanks[i+j] = realRank;
			// increase current rank
			curRank += next + 1;
			// skip 'next' items
			i+=next;
			
			//System.out.println(sorted[i]);
		}
		
		// SECOND RUN: reassign ranks
		float[] ranks = new float[data.length];
		for (int i=0; i<data.length; i++) {
			// find position of current data item in sorted array
			int pos = Arrays.binarySearch(sorted, data[i]);
			// use corresponding sorted rank value
			ranks[i] = sortedRanks[pos];
		}
		
		return ranks;
			
	}
}
