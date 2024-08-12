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

import java.util.List;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.evaluation.comparison.BroheeSimilarity;
import procope.evaluation.comparison.ComplexSetComparison;
import procope.tools.Tools;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class ComplexSetComparer {

	private static final String[] allowed = Commons.getList("html", "namemap","synfirst");
	private static final String[] explicitSwitches = Commons.getList("html");

	private static boolean html = false;
	private static Outputter output;
	
	public static void main(String[] args) {
		
		// check command line parameters
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		// get arguments object
		CommandLineArguments arguments = null;
		try {
			arguments = new CommandLineArguments(args, true, explicitSwitches, allowed);
		//	arguments.checkNumericArguments(numeric);
		} catch (InvalidArgumentsException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// get command line parameters
		html = arguments.isSet("html");
		
		// enough complex sets
		List<String> setGroups = arguments.getFreeArguments();
		if (setGroups.size() < 2) {
			System.err.println("You have to specify at least two complex set files");
			System.exit(1);
		}

		// extract file name groups
		int numGroups = setGroups.size();
		ComplexSetWithName[][] groups = new ComplexSetWithName[numGroups][];
		int index=0;
		for (String group : setGroups) {
			// get all files of this group
			String[] members = group.split(",");
			// create new group
			groups[index] = new ComplexSetWithName[members.length];
			// load members
			for (int i=0; i<members.length; i++) {
				try {
					groups[index][i] = new ComplexSetWithName(
							ComplexSetReader.readComplexes(members[i]),
							Tools.extractBaseFilename(members[i]));
				} catch (Exception e) {
					System.err.println("Cannot load complexes from " + members[i]+ ":");
					System.err.println(e.getMessage());
					System.exit(2);
				}
			}
			
			index++;
		}
		output = new Outputter(html);
		
		// check for name mappings
		Commons.checkForMappings(arguments);
		
		if (html) {
			// print html headers
			output.printlnDirectly("<html><head></head><body>");
			output.printlnDirectly("<table style=\"font-size: 10pt; font-family: Monospace; \" border=\"1\" cellpadding=\"5\" cellspacing=\"0\">");
			// first line = scoring methods
			output.printlnDirectly("<tr valign=\"top\"><td></td>");
				output.printlnDirectly("<td><b>Comparison</b></td>");
			output.printlnDirectly("</tr>");
			
		}
		boolean first = true;
		output.printlnDirectly("");
		// iterate over all pairwise groups
		for (int i=0; i<numGroups; i++) {
			// iterate over all files in this group
			for (ComplexSetWithName setAndName1 : groups[i]) {
				// iterate over other groups
				for (int j=i+1; j<numGroups; j++) {
					// iterate over files in this group
					for (ComplexSetWithName setAndName2 : groups[j]) {
						
						// get sets and names
						ComplexSet set1 = setAndName1.set;
						String name1 = setAndName1.name;
						ComplexSet set2 = setAndName2.set;
						String name2 = setAndName2.name;

						// do the comparison
						if (html) {
							output.printlnDirectly("<tr valign=\"top\">");
							// first column: compared sets
							output.printlnDirectly("<td>");
							output.printlnDirectly("<b>" + name1 + "<br>vs<br>" + name2 + "</b>");
							output.printlnDirectly("</td>");
						}
							
							if (!html) {
								if (!first) {
									output.println("//");
									output.println();
								} else
									first = false;
							} else
								output.printlnDirectly("<td>");
							
							// do comparisons
							output.println(name1 + " - " +name2);
							output.println();
							printMappableComplexes(set1, set2, name1, name2);
							output.println();
							printBroheeComparison(set1, set2, name1, name2);

							if (html) 
								output.printlnDirectly("</td>");

						if (html) output.printlnDirectly("</tr>");
					}				
				}
			}
		}		
		
		if (html) {
			// print html footer
			output.printlnDirectly("</table>");
			output.printlnDirectly("</body></html>");
		}
	
	}
	
	private static class ComplexSetWithName {
		private ComplexSet set;
		private String name;

		public ComplexSetWithName(ComplexSet set, String name) {
			this.set = set;
			this.name = name;
		}
	}
	
	private static void printUsage() {
		System.err.println();
		System.err.println("Complex set comparison");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("parameters: [options] <setA file> <setB file> (<setC file> ...)"); 
		System.err.println();
		System.err.println("Optional parameters:");
		System.err.println("  -html       create HTML output");
		System.err.println("  -namemap    use name mapping file");
		System.err.println("  -synfirst   name mappings file contains synonyms first,");
		System.err.println("                 otherwise targets first is assumed");
		System.err.println();
		System.err.println("Set files may be seperated by commas instead of whitespaces");
		System.err.println("Those sets seperated by , will not be compared with each other");
		System.err.println();
		
		
	}

	private static void printMappableComplexes(ComplexSet set1, ComplexSet set2, String nameA, String nameB) {
	
		Complex[] arrSet1 = set1.getComplexes().toArray(new Complex[0]);
		Complex[] arrSet2 = set2.getComplexes().toArray(new Complex[0]);
		
		// print sizes
		output.println("Mappables");
		output.println(nameA + " (A):\t" + arrSet1.length + " complexes");
		output.println(nameB + " (B):\t" + arrSet2.length + " complexes");
		
		// count mappables complexes in both directions
		int AtoB = countMappables(arrSet1, arrSet2);
		int BtoA = countMappables(arrSet2, arrSet1);
		
		// print results
		output.println("Mappable (Set A to Set B): " + AtoB + 
				" (" +  String.format("%.3f%%", (float)AtoB / (float)arrSet1.length*100f) + ")");
		output.println("Mappable (Set B to Set A): " + BtoA + 
				" (" +  String.format("%.3f%%", (float)BtoA / (float)arrSet2.length*100f) + ")");
		
		
	}
	
	private static int countMappables(Complex[] checkSet, Complex[] refSet) {
		// iterate over pairwise complexes
		int mappables = 0;
		for (int i=0; i<checkSet.length; i++) {
			boolean isMappable=false;
			for (int j=0; j<refSet.length && !isMappable; j++) {
				// calc overlap
				int overlap = ComplexSetComparison.complexesOverlap(checkSet[i], refSet[j]);
				// mappable?
				if (overlap >= procope.tools.Tools.MINOVERLAP) 
					isMappable = true;
			}	
			// complex was mappable?
			if (isMappable) 
				mappables++;
		}
		
		return mappables;
	
	}
	
	private static void printBroheeComparison(
			ComplexSet setA,  ComplexSet setB,
			String nameA, String nameB) {
		
		output.println("Brohee");
		
		// calc in both directions
		BroheeSimilarity brohee = ComplexSetComparison.broheeComparison(setA, setB);
		BroheeSimilarity brohee_r = ComplexSetComparison.broheeComparison(setB, setA);
		
		// print everything
		output.println("Sn:  " + brohee.getSensitivity());
		output.println("PPV: " + brohee.getPPV());
		output.println("Acc: " + brohee.getAccuracy());
		
		output.println("Sn^-1:  " + brohee_r.getSensitivity());
		output.println("PPV^-1: " + brohee_r.getPPV());
		output.println("Acc^-1: " + brohee_r.getAccuracy());
		output.println();
			
	}
	
	
	private static class Outputter {
		private boolean html;

		public Outputter(boolean html) {
			this.html = html;
		}
		public void println() {
			if (html)
				println("<br>");
			else
				println("");
		}
		public void println(String out) {
			if (html)
				System.out.println(out + "<br>");
			else
				System.out.println(out);
		}
		
		public void printlnDirectly(String out) {
			System.out.println(out);
		}
		
	}
	
	
}
