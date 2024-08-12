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

import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationDataReader;
import procope.methods.scores.bootstrap.PurificationBootstrapSamples;
import procope.tools.Tools;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class BTSampler {

	private static final String[] allowed = Commons.getList("p", "o", "n", "namemap","synfirst");
	private static final String[] integer = Commons.getList("n");

	public static void main(String[] args) {

		// no command line arguments => print usage
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}

		// get arguments object
		CommandLineArguments arguments = null;
		try {
			arguments = new CommandLineArguments(args, allowed );
			arguments.checkIntegerArguments(integer);
		} catch (InvalidArgumentsException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// get purification data file
		if (!arguments.hasValue("p")) {
			System.err
					.println("You must specify a purification data file using -p");
			System.exit(1);
		}
		String puriFile = arguments.getValue("p");

		// get output stream
		if (!arguments.isSet("o")) {
			System.err.println("You must specify an output file using -o");
			System.exit(1);
		}
		String outfile = arguments.getValue("o");

		// get number of samples
		if (!arguments.hasValue("n")) {
			System.err.println("You must specify a number of samples using -n");
			System.exit(1);
		}
		int samples = Integer.parseInt(arguments.getValue("n"));
		
		Commons.checkForMappings(arguments);

		// read purifications
		PurificationData data = null;
		try {
			InputStream in = Commons.getInputStream(puriFile);
			data = PurificationDataReader.readPurifications(in, true);
			in.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(2);
		}

		// create sample and write out
		PurificationBootstrapSamples btsamples = new PurificationBootstrapSamples(
				data, samples);
		try {
			btsamples.writeToFile(outfile);
		} catch (IOException e) {
			System.err.println("Could not write output file:");
			System.err.println(e.getMessage());
			System.exit(2);
		}

		// success
		System.out.println("Successfully written " + samples
				+ " bootstrap samples to " + outfile);

	}

	private static void printUsage() {
		System.err.println();
		System.err.println("Purification data sampler");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("Required arguments:");
		System.err.println(" -p   purification data file");
		System.err.println(" -o   output file");
		System.err.println(" -n   number of samples");
		System.err.println();
		System.err.println("Optional parameters");
		System.err.println("  -namemap     use name mapping file");
		System.err.println("  -synfirst    name mappings file contains synonyms first,");
		System.err.println("                   otherwise targets first is assumed");
		System.err.println();
	
	}

}
