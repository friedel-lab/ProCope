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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.methods.scores.ComplexScoreCalculator;
import procope.tools.Tools;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class ComplexScores {
	
	private static final String[] allowed = Commons.getList("i","net", "avg", "weighted", "namemap","synfirst");
	private static final String[] numeric = Commons.getList();
	
	
	public static void main(String[] args) {
		
		// no command line arguments => print usage
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		// parse command line arguments
		CommandLineArguments arguments = null;
		try {
			arguments = new CommandLineArguments(args, allowed);
			arguments.checkNumericArguments(numeric);
		} catch (InvalidArgumentsException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// check for input file
		if (!arguments.hasValue("i")) {
			System.err.println("You must specify a complex set input file using -i");
			System.exit(1);
		}
		String inputFile = arguments.getValue("i");
		
		// get network file
		if (!arguments.hasValue("net")) {
			System.err.println("You must specify a scores network using -net");
			System.exit(1);
		}
		String netFile = arguments.getValue("net");
		
		// weighted?
		boolean weighted = arguments.isSet("weighted");
		boolean average = arguments.isSet("avg");
		if (weighted && !average) 
			System.err.println("Warning: -weighted has no effect without -average");
		
		// check for name mappings
		Commons.checkForMappings(arguments);
		
		// get output stream
		OutputStream outstream = Commons.getOutput(arguments);
		
		// load protein network
		ProteinNetwork net=null;
		try {
			InputStream in = Commons.getInputStream(netFile);
			net = NetworkReader.readNetwork(in, false);
			in.close();
		} catch (Exception e) {
			System.err.println("Could not read network file:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		// load complex set
		ComplexSet set = null;
		try {
			set = ComplexSetReader.readComplexes(inputFile);
		} catch (Exception e) {
			System.err.println("Error while reading complex set file:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		PrintWriter writer = new PrintWriter(outstream);
		// iterate over complexes
		float total=0;
		float count=0;
		int index=0;
		for (Complex complex : set) {
			// calculate score for this complex
			float score = ComplexScoreCalculator.averageComplexScore(net, complex);
			if (weighted) {
				total += score * complex.size();
				count += complex.size();
			} else {
				total += score;
				count += 1.0f;
			}
			index++;
			if (!average)
				writer.println(score);
		}
		float avg = total / count;
		
		// print average or complex-wise scores
		if (average) 
			writer.println(avg);
		
		writer.close();
		
		Commons.closeOutput(outstream);
		
	}
	
	private static void printUsage() {
		System.err.println();
		System.err.println("Complex score calculator");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("Required parameters");
		System.err.println("  -i    file with protein complexes");
		System.err.println("  -net  score network file");
		System.err.println();
		System.err.println("Optional parameters");
		System.err.println("  -avg       print score average instead of complex-wise scores");
		System.err.println("  -weighted  calculate weighted average, only relevant with -avg");
		System.err.println("  -namemap   use name mapping file");
		System.err.println("  -synfirst    name mappings file contains synonyms first,");
		System.err.println("                   otherwise targets first is assumed");
		System.err.println();
		
	}
}
