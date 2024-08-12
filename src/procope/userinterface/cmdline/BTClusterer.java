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
package procope.userinterface.cmdline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import procope.data.complexes.ComplexSet;
import procope.data.networks.NetworkGenerator;
import procope.data.networks.NetworkWriter;
import procope.data.networks.ProteinNetwork;
import procope.data.purifications.PurificationData;
import procope.methods.clustering.MCLEfficiencyCalculator;
import procope.methods.clustering.MCLParameters;
import procope.methods.clustering.MarkovClusterer;
import procope.methods.scores.SocioAffinityCalculator;
import procope.methods.scores.bootstrap.BootstrapClustering;
import procope.methods.scores.bootstrap.BootstrapClusterings;
import procope.methods.scores.bootstrap.PurificationBootstrapSamples;
import procope.tools.ProCopeException;
import procope.tools.Tools;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class BTClusterer {
	
	private static final String DEFAULT_PREFIX = "bt_";
	private static final Float[] DEFAULT_INFLATIONS = new Float[]{2.0f,2.1f,2.2f,2.3f,2.4f,2.5f,2.6f,2.7f,2.8f,2.9f,3.0f};
	
	private static final String[] allowed = Commons.getList("i", "s", "prefix", "inf", "mclbin", "c");
	private static final String[] numeric = Commons.getList("lambda");
	
	public static void main(String[] args) {
		// no command line arguments => print usage
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		// get arguments object
		CommandLineArguments arguments = null;
		try {
			arguments = new CommandLineArguments(args, allowed);
			arguments.checkNumericArguments(numeric);
		} catch (InvalidArgumentsException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// samples file
		String samplesFile = arguments.requireValue("i", "You must specify a purification samples file using -i");
		
		// sample
		String strSamples = arguments.requireValue("s", "You must specify the samples to be handled using -s");
		// resolve sample
		int from=-1, to=-1; // inclusively!
		try {
			if (strSamples.contains("-")) {
				// its a range x-y
				String[] split = strSamples.split("-");
				from = Integer.parseInt(split[0]);
				to = Integer.parseInt(split[1]);
			} else
				from = to = Integer.parseInt(strSamples);
		} catch (NumberFormatException e) {
			System.err.println("Invalid argument for -s: '" + strSamples + "'");
			System.err.println("Must be a single integer number or a range, e.g. 1-10");
			System.exit(1);
		}
		
		// prefix
		arguments.avoidSwitch("prefix", "When using -prefix you have to specify a output file prefix");
		String prefix = DEFAULT_PREFIX;
		if (arguments.isSet("prefix")) prefix = arguments.getValue("prefix");
		
		// get eventual cutoff
		float cutOff = Float.NaN;
		if (arguments.isSet("c")) {
			if (!arguments.hasValue("c")) {
				System.err.println("When using -c you must specify a cut-off value.");
				System.exit(1);
			} else
				cutOff = Float.parseFloat(arguments.getValue("c"));
		}
		
		// mcl binary
		arguments.avoidSwitch("mclbin", "When using -mclbin you have to specify a path to the mcl binary");
		if (arguments.isSet("mclbin"))
			MarkovClusterer.setMCLBinary(arguments.getValue("mclbin"));
		
		// load coefficients
		Float[] inflations = DEFAULT_INFLATIONS;
		arguments.avoidSwitch("inf"	, "When using -inf you have to specify a list of inflation coefficients");
		if (arguments.isSet("inf")) {
			// parse coefficient list
			String strInf = arguments.getValue("inf");
			String[] split = strInf.split(",");
			inflations = new Float[split.length];
			try {
				for (int i=0; i<split.length; i++) {
					inflations[i] = Float.parseFloat(split[i]);
				}
			} catch (NumberFormatException e) {
				System.err.println("Invalid argument for -inf: '" + strInf + "'");
				System.err.println("Must be a list of numeric values, e.g. 1.5,2.0,2.5");
				System.exit(1);
			}
		}
	
		// load samples
		PurificationBootstrapSamples samples = null; 
		try {
			InputStream in = Commons.getInputStream(samplesFile);
			samples = new PurificationBootstrapSamples(in);
			in.close();
		} catch (Exception e) {
			System.err.println("Error while loading purification data samples file:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		// check if to and from are in range
		if (from < 1 || to < 1 || to < from || to > samples.getNumberOfSamples()) {
			System.err.println("Invalid sample range. Range must be within [1," + samples.getNumberOfSamples() + "]");
			System.exit(1);
		}
		
		// iterate over samples
		for (int sample=from; sample<=to; sample++) {
			// get actual sample
			PurificationData dataSample = samples.getSample(sample);
			System.out.println("Clustering sample number " + sample + "...");
			// calculate scores network
			ProteinNetwork scores;
			if (!Float.isNaN(cutOff))
				scores = NetworkGenerator.generateNetwork(new SocioAffinityCalculator(dataSample), cutOff);
			else
				scores = NetworkGenerator.generateNetwork(new SocioAffinityCalculator(dataSample));
			
			// do the mcl clusterings with all inflation coefficients
			BootstrapClusterings clusterings = new BootstrapClusterings();
			try {
				MCLParameters mclParam = new MCLParameters();
				MarkovClusterer clusterer = new MarkovClusterer(mclParam);
				for (float inflation : inflations) {
					mclParam.setInflation(inflation);
					ComplexSet clustering = clusterer.cluster(scores);
					clusterings.addClustering(new BootstrapClustering(clustering, inflation+"",
							(float)MCLEfficiencyCalculator.calculateEfficiency(scores, clustering)));
				}
			} catch (ProCopeException e) {
				System.err.println("Error while clustering:");
				System.err.println(e.getMessage());
				System.exit(2);
			
			}
			// save them to file
			try {
				clusterings.writeToFile(prefix + sample);
			} catch (IOException e) {
				System.err.println("Error writing sample number " + sample + ":");
				System.err.println(e.getMessage());
				System.exit(2);
			}
			// save network to file
			try {
				OutputStream out = new GZIPOutputStream(new FileOutputStream(prefix+sample+"_net"));
				NetworkWriter.writeNetwork(scores, out, "\t");
				out.close();
			} catch (IOException e) {
				System.err.println("Error writing network number " + sample + ":");
				System.err.println(e.getMessage());
				System.exit(2);
			}
		}
		System.out.println("Done.");
		
	}
	
	private static void printUsage() {
		System.err.println();
		System.err.println("Bootstrap clusterer");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("Required arguments:");
		System.err.println(" -i       bootstrap samples file");
		System.err.println(" -s       samples to be handled");
		System.err.println("          can be a single number or a range, e.g. 1-10");
		System.err.println("          sample numbers are 1-based");
		System.err.println();
		System.err.println("Optional arguments:");
		System.err.println(" -inf     mcl inflation coefficients to be used, comma separated"); 
		System.err.println("              default: 2.0-3.0 in 0.1 steps");
		System.err.println(" -c       apply cutoff, do not use scores less than this value");
		System.err.println(" -prefix  prefix for output files, can be a full path, default: bt_");
		System.err.println(" -mclbin  path to 'mcl' binary if not in current PATH");
		System.err.println();
	}
	
}
