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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import procope.data.LocalizationData;
import procope.data.LocalizationDataReader;
import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.evaluation.complexquality.Colocalization;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class Coloc {
	
	private static final String[] allowed =	Commons.getList("i", "loc", "oz", "o", "outtype", "nomiss", "nonweighted", "namemap","synfirst", "ppv");
	private static final String[] numeric = Commons.getList();

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
		
		// check for input file
		if (!arguments.hasValue("i")) {
			System.err.println("You must specify a complex set input file using -i");
			System.exit(1);
		}
		String inputFile = arguments.getValue("i");
		
		// check for localization data file
		if (!arguments.hasValue("loc")) {
			System.err.println("You must specify a input file using -loc");
			System.exit(1);
		}
		String locFile = arguments.getValue("loc");
		
		// check for output type
		int output = 0;
		if (arguments.isSet("outtype")) {
			if (!arguments.hasValue("outtype")) {
				System.err.println("When using -outtype you must specify an output type between 0 and 2.");
				System.exit(1);
			} else {
				String strOut = arguments.getValue("outtype");
				if (Commons.isInt(strOut))
					output = Integer.parseInt(strOut);
				if (!Commons.isInt(strOut) || output < 0 || output > 2) {
					System.err.println("The output type must 0, 1 or 2");
					System.exit(1);

				}
			}
		}
		
		// PPV?
		boolean ppv = arguments.isSet("ppv");

		// missing scores ignored
		boolean ignoreMissing = arguments.isSet("nomiss");
		if (ignoreMissing && output == 2)
			System.err.println("Warning: -nomiss has no effect for -outtype 2");
		
		// weighted average?
		boolean nonWeighted=false;
		if (arguments.isSet("nonweighted")) {
			if (output != 0)
				System.err.println("Warning: -nonweighted has no effect for -outtype " + output);
			nonWeighted=true;
		}
		
		// check for name mappings
		Commons.checkForMappings(arguments);
		
		// get output stream
		OutputStream outstream = Commons.getOutput(arguments);
		
		// load localization data
		LocalizationData data = null;
		Colocalization coloc = null;
		try {
			InputStream inStream = new FileInputStream(locFile);
			data = LocalizationDataReader.readLocalizationData(inStream);
			coloc = new Colocalization(data);
		} catch (Exception e) {
			System.err.println("Error while reading localization data file:");
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
		
		// do the job
		if (output < 2) {
			// we need to calculate colocalization scores
			
			float total=0;
			float count=0;
			int index=0;
			for (Complex complex : set) {
				// calculate score for this complex
				float score;
				if (! ppv)
					// colocalization score
					score = coloc.getColocalizationScore(complex);
				else
					// PPV
					score = coloc.getPPV(complex);
				
				// if we are ignoring missing scores there might be NaNs => ignore them
				if (!ignoreMissing || !Float.isNaN(score)) {
					if (Float.isNaN(score)) score = 0;
					if (!nonWeighted) {
						// weighted average
						total += score * complex.size();
						count += complex.size();
					} else {
						// non-weighted average
						total += score;
						count += 1.0f;
					}
					if (output == 1)
						writer.println(score);
				}
				index++;
				
			}
			float avg = total / count;
			
			// print average or complex-wise scores
			if (output == 0) 
				writer.println(avg);
				
		} else {
			// print network
			for (Complex complex : set) {
				// iterate over all pairwise proteins
				Integer[] arrProts = complex.getComplex().toArray(new Integer[0]);
				for (int i=0; i<arrProts.length; i++) {
					for (int j=i+1; j<arrProts.length; j++) {
						if (data.areColocalized(arrProts[i], arrProts[j])==1) {
							writer.println(
									ProteinManager.getLabel(arrProts[i]) + "\t"+ ProteinManager.getLabel(arrProts[j]) + " 1.0"
							);
						}
					}
				}
			}
		}
		
		writer.close();
		Commons.closeOutput(outstream);
	}
	
	private static void printUsage() {
		System.err.println();
		System.err.println("Colocalization score calculator");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("Required parameters");
		System.err.println("  -i            file with protein complexes");
		System.err.println("  -loc          localization data file");
		System.err.println();
		System.err.println("Optional parameters");
		System.err.println("  -outtype      output type, default=0");
		System.err.println("    0           print overall colocalization score");
		System.err.println("    1           print complex-wise colocalization scores");
		System.err.println("    2           print unweighted network of colocalized proteins in the complex set");
		System.err.println("  -nomiss       missing colocalization data are ignored for score calculation");
		System.err.println("  -nonweighted  calculate non-weighted average, only relevant for -outtype 0");
		System.err.println("  -ppv          calculate PPV instead of colocalization score");
		System.err.println();
		System.err.println("  -namemap      use name mapping file");
		System.err.println("  -synfirst    name mappings file contains synonyms first,");
		System.err.println("                   otherwise targets first is assumed");

		System.err.println();
		System.err.println("Input/Output    options");
		System.err.println("  -o            write to specified file instead of standard output");
		System.err.println("  -oz           GZIP the output");
		System.err.println();
	}
}
