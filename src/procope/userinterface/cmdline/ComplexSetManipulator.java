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

import java.io.IOException;
import java.io.InputStream;

import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.data.complexes.ComplexSetWriter;
import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.tools.Tools;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class ComplexSetManipulator {

	private static String[] allowed = Commons.getList("i", "o", "size", "scorecut", "decompose", "scorenet","namemap","synfirst");
	private static String[] numeric = Commons.getList("scorecut","decompose");

	public static void main(String[] args) {
		// check command line parameters
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		// get arguments object
		CommandLineArguments arguments = null;
		try {
			arguments = new CommandLineArguments(args, allowed );
			arguments.checkNumericArguments(numeric);
		} catch (InvalidArgumentsException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// in and out files
		String infile = arguments.requireValue("i", "You have to specify an input file using -i");
		String outfile = arguments.requireValue("o", "You have to specify an output file using -o");
		
		// size cut
		int cutsize = -1;
		boolean below=true;
		arguments.avoidSwitch("size", "When using -size you have to specify a cutoff value");
		if (arguments.isSet("size")) {
			String strCutSize = arguments.getValue("size");
			try {
				int len = strCutSize.length();
				// check for < or >
				if (strCutSize.charAt(len-1) == '+') {
					below = false;
					cutsize = Integer.parseInt(strCutSize.substring(0, len-1));
				} else if (strCutSize.charAt(len-1) == '-') {
					below = true;
					cutsize = Integer.parseInt(strCutSize.substring(0, len-1));
				} else {
					// neither > nor <
					throw new Exception(); // ugly trick to jump to the block below
				}
			} catch (Exception e) {
				// invalid value
				System.err.println("Invalid size cutoff: '" + strCutSize + "'");
				System.err.println("Must be an integer number followed by + or -");
				System.exit(1);
			}
		}
		
		// scorecut?
		arguments.avoidSwitch("scorecut", "When using -scorecut you have to specify a cutoff value");
		float scorecut = Float.NaN;
		if (arguments.isSet("scorecut")) 
			scorecut = Float.parseFloat(arguments.getValue("scorecut"));
		
		
		// decompose?
		arguments.avoidSwitch("decompose", "When using -decompose you have to specify a cutoff value");
		float decompose = Float.NaN;
		if (arguments.isSet("decompose")) 
			decompose = Float.parseFloat(arguments.getValue("decompose"));
		
		// scores file?
		String scoreNetFile=null;
		if (!arguments.isSet("scorenet") && (arguments.isSet("scorecut") || arguments.isSet("decompose"))) {
			System.err.println("When using -scorecut or -decompose you have to specify -scorenet");
			System.exit(1);
		} else if (arguments.isSet("scorenet") && !arguments.isSet("scorecut") && !arguments.isSet("decompose")) {
			System.err.println("Warning -scorenet has no effect if neither -scorecut nor -decompose is specified.");
		} else if (arguments.isSet("scorenet"))
			scoreNetFile = arguments.getValue("scorenet");
		
		// check for name mappings
		Commons.checkForMappings(arguments);
		
		// load complex set
		System.out.println("Reading complex set...");
		ComplexSet set = null;
		try {
			set = ComplexSetReader.readComplexes(infile);  
		} catch (Exception e) {
			System.err.println("Error while reading complex set:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		// read scores network if applicable
		ProteinNetwork scores=null;
		if (scoreNetFile != null) {
			try {
				InputStream in = Commons.getInputStream(scoreNetFile);
				scores = NetworkReader.readNetwork(in, false);
				in.close();
			} catch (Exception e) {
				System.err.println("Could not read network file:");
				System.err.println(e.getMessage());
				System.exit(2);
			}
		}

		
		// score cut?
		if (!Float.isNaN(scorecut)) {
			System.out.println("Applying score cutoff...");
			set.removeComplexesByScore(scores, scorecut);
		}
		
		// decompose?
		if (!Float.isNaN(decompose)) {
			System.out.println("Decomposing...");
			set = set.decompose(scores, decompose);
		}
		
		// apply size cutoff?
		if (cutsize >= 0) {
			System.out.println("Applying size cutoff...");
			set.removeComplexesBySize(cutsize, below);
		}
		
		// nothing set
		if (Float.isNaN(scorecut) && Float.isNaN(decompose) && cutsize < 0) 
			System.out.println("Warning: No manipulation method selected, complex set will simply be copied");
		
		// write to file
		System.out.println("Writing to " + outfile);
		try {
			ComplexSetWriter.writeComplexes(set, outfile);
		} catch (IOException e) {
			System.err.println("Error while writing result file:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
	}

	private static void printUsage() {
		System.err.println();
		System.err.println("Complex set manipulation");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("Required parameters");
		System.err.println("  -i          input complex set file");
		System.err.println("  -o          output complex set file");
		System.err.println();
		System.err.println("Manipulation method");
		System.err.println();
		System.err.println("  -scorecut   deletes all complexes whose average edge score is below a given cutoff");
		System.err.println("  -decompose  deletes all edges whose score is lower than this cutoff value");
		System.err.println("              complexes may decompose into smaller subcomplexes");
		System.err.println("  -scorenet   for -decompose and -scorecut you have to specify a score network");
		System.err.println();
		System.err.println("  -size       delete complexes larger or smaller than a given size");
		System.err.println("              e.g use -size 2- to delete all complexex smaller than 2 or");
		System.err.println("              -size 50+ to delete all complexes larger than 50 proteins");
		System.err.println();
		System.err.println("Note: Score cutting is performed first, then decomposition and finally size cutting");
		System.err.println();
		System.err.println("Optional parameters:");
		System.err.println("  -namemap  use name mapping file");
		System.err.println("  -synfirst    name mappings file contains synonyms first,");
		System.err.println("                   otherwise targets first is assumed");
		System.err.println();

	
	}
	
}
