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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetWriter;
import procope.data.networks.NetworkReader;
import procope.data.networks.NetworkWriter;
import procope.data.networks.ProteinNetwork;
import procope.methods.clustering.MCLParameters;
import procope.methods.clustering.MarkovClusterer;
import procope.methods.scores.bootstrap.Bootstrap;
import procope.methods.scores.bootstrap.BootstrapClustering;
import procope.methods.scores.bootstrap.BootstrapClusterings;
import procope.tools.Tools;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class BTFinisher {

	private static final float DEFAULT_LAMBDA = 0.95f;
	
	private static final String[] allowed = Commons.getList("prefix", "net", "o", "oz", "c", "lambda", "clust"
			);
	private static final String[] numeric = Commons.getList("c");
	
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
		
		// get prefix
		String prefix = arguments.requireValue("prefix", "You have to specify a -prefix");
		
		// cutoff
		arguments.avoidSwitch("c", "When using -c you have to specify a cut-off value");
		float cutoff = Float.NaN;
		if (arguments.isSet("c"))
			cutoff = Float.parseFloat(arguments.getValue("c"));
		
		// lambda
		arguments.avoidSwitch("lambda", "When using -lambda you have to specify a value");
		float lambda = DEFAULT_LAMBDA;
		if (arguments.isSet("lambda")) {
			lambda = Float.parseFloat(arguments.getValue("lambda"));
			// in range?
			if (lambda < 0 || lambda > 1) {
				System.err.println("Lambda must be a decimal value between 0.0 and 1.0");
				System.exit(1);
			}
		}
		
		// clustering output?
		arguments.avoidSwitch("clust", "When using -clust you have to specify an output file name");
		String clusteringFile = null;
		if (arguments.isSet("clust"))
			clusteringFile = arguments.getValue("clust");

		// network output?
		arguments.avoidSwitch("net", "When using -net you have to specify an output file name");
		String networkFile = null;
		if (arguments.isSet("net"))
			networkFile = arguments.getValue("net");
		
		// -clust or -net should be set
		if (!arguments.isSet("clust") && !arguments.isSet("net")) {
			System.err.println("Nothing do to. Please use at least one of -clust and -net");
			System.exit(1);
		}
		
		// check the data
		int samples=1;
		for (; samples<Integer.MAX_VALUE; samples++) {
			// check if this file does not exist
			if (!(new File(prefix + samples).exists())) {
				// if the next file does exist, something is wrong
				if (new File(prefix + (samples+1)).exists()) {
					System.err.println("Clusterings do not seem to be complete, did you calcuate all samples?");
					System.err.println("Problem: Could not find " + prefix + samples);
					System.exit(1);
				}
				break;
			} 
		}
		if (samples == 1) {
			System.err.println("There is no clustering file with the prefix " + prefix);
			System.exit(1);
		}
		samples--; // <= now has actual number of samples
		System.err.println("Found samples 1-" + samples + " with prefix " + prefix);
		
		// open clustering output
		OutputStream clustOut = null;
		if (clusteringFile != null) {
			try {
				clustOut = new FileOutputStream(clusteringFile);
			} catch (IOException e) {
				System.err.println("Could not open output file:");
				System.out.println(e.getMessage());
				System.exit(2);
			}
		}
		
		// open network output
		OutputStream netOut = null;
		if (networkFile != null) {
			try {
				netOut = new FileOutputStream(networkFile);
				if (arguments.isSet("nz"))
					netOut = new GZIPOutputStream(netOut);
			} catch (IOException e) {
				System.err.println("Could not open output file:");
				System.out.println(e.getMessage());
				System.exit(2);
			}
		}
		
		// only load efficiencies 
		ArrayList<BootstrapClusterings> clusteringsOnlyParas = new ArrayList<BootstrapClusterings>();
		for (int i=1; i<=samples; i++) {
			try {
				clusteringsOnlyParas.add(new BootstrapClusterings(prefix+i,  true));
			} catch (Exception e) {
				System.err.println("An error occured while reading clustering number " + i + ":");
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}
		int bestIndex = Bootstrap.findBestIndex(clusteringsOnlyParas);
		String bestPara = clusteringsOnlyParas.get(0).getClustering(bestIndex).getParameters();  
		System.out.println("best parameter: " + bestPara);
		
		
		System.out.print("Loading best clusterings and calculating shared proteins...");
		// iterate over files again and get the ones with the best inflation
		ArrayList<ComplexSet> sets = new ArrayList<ComplexSet>();
		for (int i=1; i<=samples; i++) {
			try {
				// load clustering
				BootstrapClusterings curClusts = new BootstrapClusterings(prefix+i);
				BootstrapClustering clustering = curClusts.getClustering(bestIndex);
				// load scores network for this sample
				ProteinNetwork scores = NetworkReader.readNetwork(
						new GZIPInputStream(new FileInputStream(prefix+i+"_net")), false);
				ComplexSet set = clustering.getClustering();
				// calculate shared proteins
				set = set.calculateSharedProteinsBootstrap(scores, lambda);
				sets.add(set);
				if (samples >= 20 && i%(samples/20)==0)
					System.err.print(".");
			
			} catch (Exception e) {
				System.err.println("An error occured while reading clustering number " + i + ":");
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}
		System.err.println("done.");
		
		
		// calculate bootstrap network
		System.err.print("Calculating bootstrap scores network... ");
		ProteinNetwork btnet = Bootstrap.createBootstrapNetwork(sets);
		// cut off?
		if (!Float.isNaN(cutoff))
			btnet = btnet.getCutOffNetwork(cutoff);

		System.err.println("done.");
		
		if (arguments.isSet("net")) {
			// output the network
			NetworkWriter.writeNetwork(btnet, netOut , "\t");
			Commons.closeOutput(netOut);
		} 
		if (arguments.isSet("clust")) {
			System.err.print("Calculating final clustering...");
			// we need to create the final clustering
			MCLParameters params = new MCLParameters();
			params.setInflation(Float.parseFloat(bestPara));
			procope.methods.clustering.Clusterer clusterer = new MarkovClusterer(params);
			ComplexSet clustering = clusterer.cluster(btnet);
			System.err.println("All done.");
			// size cutoff
			clustering.removeSingletons();
			// output
			ComplexSetWriter.writeComplexes(clustering, clustOut);
			Commons.closeOutput(clustOut);
		}
		
		
	}

	private static void printUsage() {
		System.err.println();
		System.err.println("Bootstrap finisher"); 
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("Required arguments:");
		System.err.println("  -prefix   prefix to bootstrap clustering files, can be a full path");
		System.err.println();
		System.err.println("Optional arguments:");
		System.err.println("  -clust    write the final clustering to a file");
		System.err.println("  -net      output the bootstrap scoring network to a file");
		System.err.println("  -lambda   parameter for shared protein calculation, default: 0.95");
		System.err.println("  -c        apply cutoff, do not output scores less than this value");
		System.err.println();
		System.err.println("Input/Output options");
		System.err.println("  -nz       GZIP the network file");
		System.err.println();
	
	}
	
}
