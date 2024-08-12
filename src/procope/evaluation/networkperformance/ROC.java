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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import procope.data.LocalizationData;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;
import procope.tools.ProCopeException;
import procope.tools.Tools;


/**
 * Contains static methods to calculate ROC curves for protein networks. These
 * ROC curves are a measure for the performance of a network, a reference
 * complex set is needed for calculation.
 * <p>For a detailed description of the ROC curve calcuation process, please
 * read the documentation of ProCope.
 * 
 * @author Jan Krumsiek
 */

public class ROC {
	
	private ROC() {
	}

	/**
	 * Calculate the ROC curves for a given set of networks. Can use a different
	 * complex set for the identification
	 * 
	 * @param scoreNetworks list of networks for which the ROC curves will be calculated
	 * @param reference reference set to be used
	 * @param referenceForNegativeSet reference set used to sample the set of true negatives,
	 *               may be set to {@code null} to use the same set as the reference set
	 * @param locData {@link LocalizationData localization data}, use {@code null} 
	 *                if you do not want to use localization data for negative set
	 *                generation
	 * @param restrictToNetworkProteins Determines whether the positive and negative sets
	 *            may only contain edges, where both proteins are contained in at least one
	 *            of the score networks. If this is set to {@code false}, the true-positive
	 *            rate probably does not converge against 1.0.
	 * @return list of ROC curves for the given networks
	 */
	public static List<ROCCurve> calculateROCCurves (List<ProteinNetwork> scoreNetworks, 
			ComplexSet reference, ComplexSet referenceForNegativeSet, LocalizationData locData,
			boolean restrictToNetworkProteins) {
		
		// generate protein restriction if necessary
		Set<Integer> protRestriction = null;
		if (restrictToNetworkProteins) {
			protRestriction = new HashSet<Integer>();
			for (ProteinNetwork net : scoreNetworks)
				protRestriction.addAll(net.getProteins());
		}

		// generate negative interactions as edges which are not in the positive set and not colocalized
		if (referenceForNegativeSet == null)
			referenceForNegativeSet = reference;
		// get (restricted) network
		ProteinNetwork netNegativeReference = referenceForNegativeSet.getComplexInducedNetwork();
		if (protRestriction != null)
			netNegativeReference.restrictToProteins(protRestriction, true);
		
		// calculate the negative set
		ProteinNetwork negativeSet = generateNegativeSet(
				netNegativeReference, locData, 10f, protRestriction);
				
		ArrayList<ROCCurve> rocs = new ArrayList<ROCCurve>();

		// get positive set used for ROC calculation
		ProteinNetwork rocPositiveSet = reference.getComplexInducedNetwork();
		// restrict?
		if (protRestriction != null)
			rocPositiveSet = rocPositiveSet.restrictToProteins(protRestriction, true);

		for (ProteinNetwork scores : scoreNetworks) {
			rocs.add(calc(scores, rocPositiveSet, negativeSet));
		}

		return rocs;

	}

	private static ROCCurve calc(ProteinNetwork scoreNet, ProteinNetwork positiveSet, ProteinNetwork negativeSet) {

		ArrayList<ROCPoint> result = new ArrayList<ROCPoint>();

		if (scoreNet.isDirected()) 
			throw new ProCopeException("This only works with undirected networks.");

		// get the interaction network induced by the reference complexes => positive interactions
		int positiveTotal = positiveSet.getEdgeCount();
		int negativeTotal = negativeSet.getEdgeCount();

		int numEdges = scoreNet.getEdgeCount();
		float[] scores = new float[numEdges];
		byte[] real = new byte[numEdges];

		// iterate over all edges
		int[] edgeConnections = scoreNet.getEdgesArray();

		for (int i=0; i<edgeConnections.length; i+=2) {

			scores[i/2] = scoreNet.getEdge(edgeConnections[i], edgeConnections[i+1]);
			// now assign if this is a real edge, a false edge or an unknown edge
			if (negativeSet.hasEdge(edgeConnections[i], edgeConnections[i+1]))
				real[i/2] = 0; // false edge
			else if (positiveSet.hasEdge(edgeConnections[i], edgeConnections[i+1]))
				real[i/2] = 1; // true edge
			else
				real[i/2] = -1; // unknown edge
		}

		// sort list
		myMergeSort(scores, real);

		// iterate over the list backwards (as the largest elements are at the bottom)
		int positiveEdges=0, negativeEdges=0;
		for (int i=scores.length-1; i>=0; i--) {
			// do the count
			if (real[i] == 1)
				positiveEdges++;
			else if (real[i] == 0)
				negativeEdges++;
			// else: real[i] == -1 => ignore
			// create point if the next score is the same or this is the last item 
			if (i == 0 || scores[i-1] != scores[i]) {
				result.add(new ROCPoint((float)positiveEdges/(float)positiveTotal, (float)negativeEdges/(float)negativeTotal));
			}

		}

//		System.out.println("true edges: " + trueEdges);
//		System.out.println("false edges: " + falseEdges);

//		System.out.println((float)negativeEdges/(float)negativeTotal);

		return new ROCCurve(result);
	}

	private static ProteinNetwork generateNegativeSet(ProteinNetwork positiveSet, 
			LocalizationData locData, float timesPositive, Set<Integer> restriction) {

		ProteinNetwork negative = new ProteinNetwork(false);

		Random random = Tools.random;

		int needed = (int)((float)positiveSet.getEdgeCount() * timesPositive);

		int found=0;
		Integer[] proteins = positiveSet.getProteins().toArray(new Integer[0]);

		while (found < needed) {

			// select random edge
			int partner1 = proteins[random.nextInt(proteins.length)];
			int partner2 = proteins[random.nextInt(proteins.length)];
			if (partner1 != partner2) {
				// only use this edge if it is not contained in the positive set
				if (!positiveSet.hasEdge(partner1, partner2)) {
					// only edges in the restriction
					if (restriction == null || (restriction.contains(partner1) && restriction.contains(partner2))) {
						// only non-colocalized proteins
						if (locData == null || locData.areColocalized(partner1, partner2) == 0) {
							if (!negative.hasEdge(partner1, partner2)) {
								negative.setEdge(partner1, partner2);
								found++;
							}
						}
					}

				}
			}
		}

		return negative;

	}


	private static void myMergeSort(float[] array, byte[] coArray) {
		recMergeSort(array, coArray, 0, array.length);
	}

	private static void recMergeSort(float x[], byte[] y, int off, int len) {

		// Choose a partition element, v
		int m = off + (len >> 1);       // Small arrays, middle element
		if (len > 7) {
			int l = off;
			int n = off + len - 1;
			if (len > 40) {        // Big arrays, pseudomedian of 9
				int s = len/8;
				l = med3(x, l,     l+s, l+2*s);
				m = med3(x, m-s,   m,   m+s);
				n = med3(x, n-2*s, n-s, n);
			}
			m = med3(x, l, m, n); // Mid-size, med of 3
		}
		double v = x[m];

		// Establish Invariant: v* (<v)* (>v)* v*
		int a = off, b = a, c = off + len - 1, d = c;
		while(true) {
			while (b <= c && x[b] <= v) {
				if (x[b] == v)
					swap(x, y, a++, b);
				b++;
			}
			while (c >= b && x[c] >= v) {
				if (x[c] == v)
					swap(x, y, c, d--);
				c--;
			}
			if (b > c)
				break;
			swap(x, y, b++, c--);
		}

		// Swap partition elements back to middle
		int s, n = off + len;
		s = Math.min(a-off, b-a  );  vecswap(x, y, off, b-s, s);
		s = Math.min(d-c,   n-d-1);  vecswap(x, y, b,   n-s, s);

		// Recursively sort non-partition-elements
		if ((s = b-a) > 1)
			recMergeSort(x, y, off, s);
		if ((s = d-c) > 1)
			recMergeSort(x, y, n-s, s);

	}

	private static int med3(float x[], int a, int b, int c) {
		return (x[a] < x[b] ?
				(x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
					(x[b] > x[c] ? b : x[a] > x[c] ? c : a));
	}

	private static void swap(float x[], byte y[], int a, int b) {
		float t = x[a];
		x[a] = x[b];
		x[b] = t;

		byte t2 = y[a];
		y[a] = y[b];
		y[b] = t2;
	}

	private static void vecswap(float x[], byte[] y,  int a, int b, int n) {
		for (int i=0; i<n; i++, a++, b++)
			swap(x, y, a, b);
	}




}
