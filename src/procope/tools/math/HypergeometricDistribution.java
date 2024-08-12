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

/**
 * Contains static methods for the rapid calculation of values from the
 * hypergeometric distribution. All functions return <u>logarithms</u> of the
 * respective values. Apply {@link Math#exp(double)} to get the real numbers.
 * <p><b>Note:</u> To calculate the distribution value of the hypergeometric
 * distribution this implementation uses an approximation found in the 
 * <a href="http://www.koders.com/c/fid2C32E74981A6C54DA518C65BD8A74247A4B69223.aspx?s=Chebyshev" target="_blank">
 * source codes</a> of the <a href="http://www.r-project.org/" target="_blank">R Project</a>
 * 
 * @author Jan Krumsiek
 */

public class HypergeometricDistribution {

	private static double DBL_EPSILON = 2.2204460492503131e-16;
	// cached values
	private static ArrayList<Double> lfactorial;
	
	// static initialization code
	static {
		lfactorial = new ArrayList<Double>();
		lfactorial.add(0, new Double(0.0)); /* 0! = 1, therefore let log(0)=0 */
		lfactorial.add(1, new Double(0.0));
	}

	/**
	 * Approximation from the R project
	 */
	private static double pdhyper(double hits, double whiteBalls, double blackBalls, double drawings) {
		/*
		 * Calculate
		 *
		 *	    phyper (x, NR, NB, n, TRUE, FALSE)
		 *   [log]  ----------------------------------
		 *	       dhyper (x, NR, NB, n, FALSE)
		 *
		 * without actually calling phyper.  This assumes that
		 *
		 *     x * (NR + NB) <= n * NR
		 *
		 */
		double sum = 0;
		double term = 1;

		while (hits > 0 && term >= DBL_EPSILON * sum) {
			term *= hits * (blackBalls - drawings + hits) / (drawings + 1 - hits) / (whiteBalls + 1 - hits);
			sum += term;
			hits--;
		}
		
		return 1 + sum;
	}

	/**
	 * Calculates logarithm of a value of the cumulative distribution function.
	 * 
	 * @param hits number of white balls to be drawn
	 * @param whiteBalls number of white balls in the urn
	 * @param blackBalls number of black balls in the urn
	 * @param drawings number of drawings to be made
	 * @param lower_tail {@code true}: probability that <= {@code hits} white 
	 *                   balls will be drawn; {@code false}: probability that
	 *                   > {@code hits} white balls will be drawn.
	 * @return logarithmic probability that <= {@code hits} or > {@code hits} 
	 *         white balls are drawn from the urns
	 */
	public static double logphyper(int hits, int whiteBalls, int blackBalls, int drawings, boolean lower_tail) {
		/* Sample of  n balls from  NR red  and	 NB black ones;	 x are red */

		double d, pd;

		if (!lower_tail) {
			// swap parameters
			int oldBlackBalls = blackBalls;
			blackBalls = whiteBalls;
			whiteBalls = oldBlackBalls;
			hits = drawings - hits-1 ;
		}
		
		
		if (hits < 0)
			return 0;
		
		if (whiteBalls < 0 || blackBalls < 0 || Double.isInfinite(whiteBalls + blackBalls) || drawings < 0
				|| drawings > whiteBalls + blackBalls)
			return Double.NaN;

		
		d = logdhyper(hits, whiteBalls, blackBalls, drawings); 
		pd = pdhyper(hits, whiteBalls, blackBalls, drawings);
		
		return d + Math.log(pd);
	}
	
	/**
	 * Calculates logarithm of a value of the density function.
	 * 
	 * @param hits number of white balls to be drawn
	 * @param whiteBalls number of white balls in the urn
	 * @param blackBalls number of black balls in the urn
	 * @param drawings number of drawings to be made
	 * @return logarithmic probability to draw {@code hits} white balls
	 */
	public static double logdhyper(int hits, int whiteBalls, int blackBalls, int drawings) {
		
		/*
		 * It is not possible to draw more white balls from an urn containing M
		 * white balls. Hence the probability is 0.
		 */
		if (hits > whiteBalls)
			return Double.NEGATIVE_INFINITY;
		
		/*
		 * Of course it is also not possible to draw more white balls than the
		 * number of drawings. The probability is 0.
		 */
		if (hits > drawings) 
			return Double.NEGATIVE_INFINITY;
			
		/*
		 * Last but not least, it is also not possible to draw more black balls
		 * than there are within the urn.
		 */
		if (drawings - hits > blackBalls)  {
			return Double.NEGATIVE_INFINITY;
		}

		
		return lNchooseK(whiteBalls, hits)
				+ lNchooseK(blackBalls, drawings - hits)
				- lNchooseK(whiteBalls + blackBalls, drawings);

	}
	
	
	/**
	 * Calculate logarithmic binomial coefficient
	 */
	private static double lNchooseK(int n, int k) {
		double ans;
		ans = logfact(n) - logfact(k) - logfact(n - k);
		return ans;
	}

	/**
	 * return the log factorial of i. Use a cache to avoid repeatedly
	 * calculating this. If we have a cache miss, fill up all values from the
	 * last valid cache value to the value we currently need.
	 */
	private static double logfact(int i) {
		
	
		
		/*
		 * Make sure value is already in lfactorial. If not, calculate all
		 * values up to that for i
		 */
		if (i > (lfactorial.size() - 1)) {
			for (int j = lfactorial.size(); j <= i; j++) {
				double lf = lfactorial.get(j - 1).doubleValue()
						+ java.lang.Math.log(j);
				lfactorial.add(j, new Double(lf));
			}
		}

		return lfactorial.get(i).doubleValue();
	}

}
