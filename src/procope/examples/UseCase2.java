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
package procope.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.jfree.chart.JFreeChart;

import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.data.networks.CombinationRules;
import procope.data.networks.NetworkEdge;
import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.evaluation.comparison.NetworkComparison;
import procope.evaluation.comparison.Point;
import procope.evaluation.networkperformance.ComplexEnrichment;
import procope.evaluation.networkperformance.ROC;
import procope.evaluation.networkperformance.ROCCurve;
import procope.evaluation.networkperformance.ROCCurveHandler;
import procope.tools.BooleanExpression;
import procope.tools.ChartTools;
import procope.tools.InvalidExpressionException;
import procope.tools.math.CorrelationCoefficient;
import procope.tools.math.PearsonCoefficient;
import procope.tools.namemapping.ProteinManager;

/**
 * This example code demonstrates different functions for protein networks.
 * 
 * - Loading of networks
 * - Comparing two networks
 * - Different manipulation/merging/filtering functions
 * - ROC curves
 * - Iterating over the edges of a network
 * 
 * @author Jan Krumsiek
 */
public class UseCase2 {
	
	public static void main(String[] args) {
		
		// load some score networks (from gzipped files)
		System.out.println("Loading networks...");
		ProteinNetwork hart=null, pe=null;
		try {
			hart = NetworkReader.readNetwork(new GZIPInputStream(
					new FileInputStream("data/scores/hart_scores.txt.gz")));
			pe = NetworkReader.readNetwork(new GZIPInputStream(
					new FileInputStream("data/scores/pe_combined.txt.gz")));
		} catch (Exception e) {
			// something went wrong, output error message
			System.err.println("Could not load score networks:");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// compare scores of both networks with each other by calculating a correlation coefficient
		System.out.println("Comparing networks...");
		List<Point> overlap = NetworkComparison.weightsOverlap(hart, pe, false);
		CorrelationCoefficient coeff = new PearsonCoefficient(); // could also use Spearman
		coeff.feedData(overlap);
		System.out.println("Correlation between the networks: " + coeff.getCorrelationCoefficient());
		
		// cutoff all edges from both networks which are below a given threshold 
		// (so the ROC curve won't get too large) 
		System.out.println("Cutting off networks...");
		hart = hart.getCutOffNetwork(3);
		pe = pe.getCutOffNetwork(2);
		
		System.out.println("Generating randomized network...");
		// generate a randomized version of the hart network
		ProteinNetwork randomized = hart.randomizeByRewiring();
		
		
		// compare performance of the networks by calculating 
		// complex enrichments and a plot of ROC curves
		
		// first, load reference complex set
		ComplexSet mips=null;
		try {
			mips = ComplexSetReader.readComplexes("data/complexes/mips_complexes.txt");
		} catch (Exception e) {
			// something went wrong, output error message
			System.err.println("Could not load reference set:");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// calculate complex enrichment of both networks
		System.out.println("Complex enrichment of Hart: " +
				ComplexEnrichment.calculateComplexEnrichment(hart, mips, 100, true));
		System.out.println("Complex enrichment of PE: " +
				ComplexEnrichment.calculateComplexEnrichment(pe, mips, 100, true));
		System.out.println("Complex enrichment of Hart-randomized: " +
				ComplexEnrichment.calculateComplexEnrichment(randomized, mips, 100, true));
		
		// for true positive determination in the ROC curves we exclude complexes >= 50 proteins
		ComplexSet mips_below50 = mips.copy();
		mips_below50.removeComplexesBySize(50, false);

		// generate list of networks and list of name
		ArrayList<ProteinNetwork> networks = new ArrayList<ProteinNetwork>();
		networks.add(hart);
		networks.add(randomized);
		networks.add(pe);
		ArrayList<String> names = new ArrayList<String>();
		names.add("Hart");
		names.add("Randomized");
		names.add("PE");
		
		// calculate ROC curve, we do not use localization data this time
		System.out.println("Calculating and saving ROC curve...");
		List<ROCCurve> curves = ROC.calculateROCCurves(networks, mips_below50, mips, null, false); 
		// plot them & write to file
		JFreeChart chart = ROCCurveHandler.generateChart(curves, names);
		try {
			ChartTools.writeChartToPNG(chart, new File("roc.png"), 800, 600);
		} catch (IOException e) {
			// could not write the image
			System.err.println("Could not write image: roc.png\n\n" + e.getMessage());
			System.exit(1);
		}
		
		
		// next we merge both scores networks
		System.out.println("Combining networks...");
		// only keep proteins which are present in both networks => intersect
		CombinationRules rules = new CombinationRules(CombinationRules.CombinationType.INTERSECT);
		// the new network will have no edge weights, the old scores are stored as annotations
		rules.setWeightMergePolicy(CombinationRules.WeightMergePolicy.ANNOTATE_WEIGHTS, "Hart", "PE");
		// do the combination
		ProteinNetwork merged = hart.combineWith(pe, rules);
		
		// we want to find edges which have a Hart score > X and a PE score > XX 
		// (arbitrary values) and output these edges to the console
		BooleanExpression expression=null;
		try {
			expression = new BooleanExpression("Hart>=70 & PE>=25");
		} catch (InvalidExpressionException e) {
			// this won't happen as we know the expression is correct
		}
		ProteinNetwork filtered = merged.getFilteredNetwork(expression);
		
		// output these edges
		System.out.println("High-confidence interactions:");
		for (NetworkEdge edge : filtered) {
			int protein1 = edge.getSource(); // source and target are not relevant 
			int protein2 = edge.getTarget(); // as this is an undirected network
			System.out.println(ProteinManager.getLabel(protein1) + 
					"\t" + ProteinManager.getLabel(protein2));
		}
		
	}

}
