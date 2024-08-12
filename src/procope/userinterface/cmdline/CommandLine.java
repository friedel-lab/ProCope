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

import procope.tools.Tools;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class CommandLine {
	
	private static final String[] valid = new String[] { "btsampler",
			"btclusterer", "btfinisher", "clusterer", "colocalization",
			"complexcompare", "complexmanipulate", "complexscores",
			"generatescores", "gosim", "roc"};
	
	public static void main(String[] args) {
		
		// no arguments?
		if (args.length == 0) {
			System.err.println(Tools.FULL_LIB_NAME);
			System.err.println("The first argument must be the command line tool you want to use:");
			System.err.println(Commons.formatList(valid));
			System.exit(1);
		}
		
		// first argument = program to be started
		String prog = args[0];
		
		// shift the array
		args = shift(args);
		
		if (prog.equals("clusterer"))
			Clusterer.main(args);
		else if (prog.equals("colocalization")) 
			Coloc.main(args);
		else if (prog.equals("complexcompare"))
			ComplexSetComparer.main(args);
		else if (prog.equals("complexmanipulate"))
			ComplexSetManipulator.main(args);
		else if (prog.equals("complexscores"))
			ComplexScores.main(args);
		else if (prog.equals("generatescores"))
			GenerateScores.main(args);
		else if (prog.equals("gosim"))
			SemanticSimilarities.main(args);
		else if (prog.equals("roc"))
			ScoreROC.main(args);
		else if (prog.equals("btsampler"))
			BTSampler.main(args);
		else if (prog.equals("btclusterer"))
			BTClusterer.main(args);
		else if (prog.equals("btfinisher"))
			BTFinisher.main(args);
		else {
			System.err.println(Tools.FULL_LIB_NAME);
			System.err.println("Unknown command line tool name: '"+prog+"'.\nMust be one of: " + Commons.formatList(valid));
			System.exit(1);
		}
		
	}
	
	private static String[] shift(String[] arr) {
		String[] result = new String[arr.length-1];
		for (int i=1; i<arr.length; i++)
			result[i-1] = arr[i];
		return result;
	}

}
