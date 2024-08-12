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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;

import procope.data.networks.NetworkGenerator;
import procope.data.networks.NetworkWriter;
import procope.data.networks.ProteinNetwork;
import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationDataReader;
import procope.methods.scores.DiceCoefficients;
import procope.methods.scores.HartCalculator;
import procope.methods.scores.PECalculator;
import procope.methods.scores.ScoresCalculator;
import procope.methods.scores.SocioAffinityCalculator;
import procope.tools.ProCopeException;
import procope.tools.Tools;
import procope.tools.userclasses.UserParameter;
import procope.tools.userclasses.UserScoresCalculator;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class GenerateScores {
	
	private static final String[] scoreTypes = Commons.getList("socio", "hart", "pe", "dice", "user");
	private static final String[] allowed = Commons.getList("p", "o", "score", "c", "peR", "peP", "oz", "namemap", "name");
	private static final String[] numeric = Commons.getList("c", "peR", "peP");
	
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
		
		// check for purification data file
		if (!arguments.hasValue("p")) {
			System.err.println("You must specify a purification data file using -p");
			System.exit(1);
		}
		String[] puriFiles = arguments.getValue("p").split(",");
		
		
		// get eventual cutoff
		float cutOff = Float.NaN;
		if (arguments.isSet("c")) {
			if (!arguments.hasValue("c")) {
				System.err.println("When using -c you must specify a cut-off value.");
				System.exit(1);
			} else
				cutOff = Float.parseFloat(arguments.getValue("c"));
		}
		
		// get scoring method	
		String scoreType = arguments.getValue("score");
		Object[] parameters=null;
		if (!arguments.hasValue("score")) {
			System.err.println("You must specify a score type using -score");
			System.exit(1);
		} else {
			if (!Commons.inList(scoreType, scoreTypes)) {
				// invalid score type
				System.err.println("Invalid score type, must be one of: " + Commons.formatList(scoreTypes) );
				System.exit(1);
			} else {
				if (scoreType.equals("socio") || scoreType.equals("hart") || scoreType.equals("dice")) {
					// do nothing here, no parameters required
				} else if (scoreType.equals("pe")) {
					// check if both parameters are there
					if (!arguments.hasValue("peR") || !arguments.hasValue("peP")) {
						System.err.println("For PE scores you must provide values for -peR and -peP");
						System.exit(1);
					} else {
						parameters = new Object[]{Float.parseFloat(arguments.getValue("peR")), Float.parseFloat(arguments.getValue("peP"))};
					}
				} else if (scoreType.equals("user")) {
					// check for the name
					if (!arguments.hasValue("name")) {
						System.err.println("Please specifiy the -name of the user scores calculator.");
						System.exit(1);
					} else {
						parameters = new Object[]{arguments.getValue("name")};
					}
				}
			}
		}
		// if the scoring type is NOT hart and we have more than one purification data set: error
		if (puriFiles.length > 1 && (!scoreType.equals("hart") && !scoreType.equals("user"))) {
			System.err.println("Can only accept multiple purification files for -score hart or -score user");
			System.exit(1);
		}
	
		// check for name mappings
		Commons.checkForMappings(arguments);
		
		// get output stream
		OutputStream outstream = Commons.getOutput(arguments);
		
		// COMMAND LINE ARGUMENT CHECKING DONE		

		// read purifications
		PurificationData[] puriData = new PurificationData[puriFiles.length];
		try {
			int index=0;
			for (String file : puriFiles) {
				InputStream in = Commons.getInputStream(file);
				puriData[index++] = PurificationDataReader.readPurifications(in);
				in.close();
			}
		} catch (Exception e) {
			System.err.println("Could not read purification data:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		// create scores calculator
		ScoresCalculator scoreCalc=null;
		if (scoreType.equals("socio")) 
			scoreCalc = new SocioAffinityCalculator(puriData[0]);
		else if (scoreType.equals("pe"))
			scoreCalc = new PECalculator(puriData[0], (Float)parameters[0], (Float)parameters[1]);
		else if (scoreType.equals("hart"))
			scoreCalc = new HartCalculator(puriData); 
		else if (scoreType.equals("dice"))
			scoreCalc = new DiceCoefficients(puriData[0]);
		else if (scoreType.equals("user")) {
			try{
				// try to load the XML file
				List<UserScoresCalculator> calculators = loadCalculators();
				if (calculators == null) {
					System.err.println("File " + Tools.CALCULATORSFILE + " does not exist.\nNo user scores calculators are defined.");
					System.exit(1);
				}
				// find the calculator
				UserScoresCalculator userCalc = null;
				for (UserScoresCalculator curCalc : calculators) {
					if (curCalc.getName().equals(parameters[0])) {
						userCalc = curCalc;
						break;
					}
				}
				// if we did not find it => output error
				if (userCalc == null) {
					System.err.println("User scores calculator '"+parameters[0]+"' not defined in " + Tools.CALCULATORSFILE);
					System.exit(1);
				}
				// multiple purification data but calculator does not support them?
				if (puriData.length > 1 && !userCalc.multiplePurifications()) {
					System.err.println("The user calculator '"+parameters[0]+"' does not support multiple purification datasets.");
					System.exit(1);
				}
				// we got it, get parameters from the user
				Vector<Object> userInput = new Vector<Object>();
				if (userCalc.getParameters().size() > 0) {
					System.out.println("Parameters for " + parameters[0]);
					for (UserParameter userPara : userCalc.getParameters()) 
						userInput.add(Commons.inputUserParameter(userPara));
				}
				// try o create the object
				scoreCalc = userCalc.generateScoresCalculator(puriData, userInput.toArray(new Object[0]));
			} catch (ClassNotFoundException e) {
				System.err.println("User calculator class not found in current classpath:\n" + e.getMessage());
				System.exit(2);
			} catch (NoSuchMethodException e) {
				System.err.println("Constructor does not exist:\n" + e.getMessage());
				System.out.println("You probably entered wrong parameters or a wrong 'multipuri' setting.");
				System.exit(2);
			} catch (InvocationTargetException e) {
				System.err.println("User scores calculator reported a problem while initializing:\n" + 
						e.getTargetException().getMessage());
				System.exit(2);
			} catch (Exception e) {
				System.err.println("Could not load clusterers from " + Tools.CLUSTERERSFILE + ":");
				System.err.println(e.getMessage());
				System.exit(2);
			}
		}


		// create network
		if (Float.isNaN(cutOff)) cutOff = Float.NEGATIVE_INFINITY;
		ProteinNetwork scores = NetworkGenerator.generateNetwork(scoreCalc, cutOff);
		
		// write to output
		NetworkWriter.writeNetwork(scores, outstream , "\t");
		
		Commons.closeOutput(outstream);
		
	}
	
	
	
	private static List<UserScoresCalculator> loadCalculators() {
		// if the XML file is there => try to parse it
		File xml = new File(Tools.CALCULATORSFILE);
		if (xml.exists()) {
			try {
				FileInputStream stream = new FileInputStream(xml);
				List<UserScoresCalculator> calculators = UserScoresCalculator.parseCalculators(stream);
				stream.close();
				return calculators;
			} catch (Exception e) {
				throw new ProCopeException(e.getMessage());
			}
		} else
			return null;
	}
	
	private static void printUsage() {
		System.err.println();
		System.err.println("Score network calculator");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("Required parameters");
		System.err.println("  -p        Purification data file");
		System.err.println("            Note: for -score hart or -score user this can be ");
		System.err.println("            a comma-seperated list of purification data files"); 
		System.err.println("  -score    score method (see below)");
		System.err.println();
		System.err.println();
		System.err.println("Optional parameters");
		System.err.println("  -c        apply cutoff, do not output scores less than this value");
		System.err.println("  -namemap  use name mapping file");
		System.err.println("  -synfirst    name mappings file contains synonyms first,");
		System.err.println("                   otherwise targets first is assumed");
		System.err.println();
		System.err.println("Score methods and their parameters");
		System.err.println("  socio     socio affinity scores (Gavin, 2006); no further parameters required")  ;
		System.err.println("  hart      Scores after Hart, 2007; no further parameters required")  ; 
		System.err.println("  pe        Purification enrichment scores (Collins, 2007)"); 
		System.err.println("     -peR   PE score R parameter");
		System.err.println("     -peP   PE score pseudocount parameter");
		System.err.println("  dice      Dice coefficients (Zhang, 2008); no further parameters required");
		System.err.println("  user      run a user scores calculator");
		System.err.println("     -name  name of the user calculator");
		System.err.println();
		System.err.println("Input/Output options");
		System.err.println("  -o        write to specified file instead of standard output");
		System.err.println("  -oz       GZIP the output");
		System.err.println();
	}
	
	
	


}
