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


import java.io.OutputStream;
import java.io.PrintWriter;

import procope.evaluation.complexquality.go.FunctionalSimilarities;
import procope.evaluation.complexquality.go.FunctionalSimilaritiesSchlicker;
import procope.evaluation.complexquality.go.GOAnnotationReader;
import procope.evaluation.complexquality.go.GOAnnotations;
import procope.evaluation.complexquality.go.GONetwork;
import procope.evaluation.complexquality.go.TermSimilarities;
import procope.evaluation.complexquality.go.TermSimilaritiesSchlicker;
import procope.evaluation.complexquality.go.FunctionalSimilaritiesSchlicker.FunctionalSimilarityMeasure;
import procope.evaluation.complexquality.go.GONetwork.Namespace;
import procope.evaluation.complexquality.go.GONetwork.Relationships;
import procope.evaluation.complexquality.go.TermSimilaritiesSchlicker.TermSimilarityMeasure;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class SemanticSimilarities {
	
	private static final String[] allowed =	Commons.getList("gonet", "name", "anno", "termsim", "funsim", "c", "o", "oz", "rel", "namemap","synfirst");
	private static final String[] numeric = Commons.getList("c");
	
	private static String[] NAMESPACES = Commons.getList("bp","cc", "mf");
	private static String[] TERMSIMS = Commons.getList("resnik", "lin", "relevance");
	private static final String DEFAULT_TERMSIM = "relevance";
	private static String[] FUNSIMS = Commons.getList("colrowmax", "colrowavg", "lord", "totalmax");
	private static final String DEFAULT_FUNSIM = "totalmax";
	private static final String[] RELATIONSHIPS = Commons.getList("isa", "partof", "both");;
	private static final String DEFAULT_RELATIONSHIP = "both";
	
	
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
		
		// get go network file
		if (!arguments.hasValue("gonet")) {
			System.err.println("You must specify a GO network file using -gonet");
			System.exit(1);
		}
		String goNetFile  = arguments.getValue("gonet");
		
		// namespace
		Namespace namespace = null;
		if (!arguments.hasValue("name")) {
			System.err.println("You must specify a GO namespace file using -name");
			System.exit(1);
		} else {
			String strNamespace = arguments.getValue("name");
			if (!Commons.inList(strNamespace, NAMESPACES)) {
				// invalid value
				System.err.println("Invalid namespace, must be one of: " + Commons.formatList(NAMESPACES));
				System.exit(1);
			} else {
				// choose actual measure
				if (strNamespace.equals("bp"))
					namespace = Namespace.BIOLOGICAL_PROCESS;
				else if (strNamespace.equals("cc"))
					namespace = Namespace.CELLULAR_COMPONENT;
				else if (strNamespace.equals("mf"))
					namespace = Namespace.MOLECULAR_FUNCTION;
			}
		}
		
		// annotation file
		if (!arguments.hasValue("anno")) {
			System.err.println("You must specify a GO annotation file using -anno");
			System.exit(1);
		} 
		String annofile = arguments.getValue("anno");
		
		// term similarity
		TermSimilarityMeasure termSim = null;
		String strTermSim = DEFAULT_TERMSIM;
		arguments.avoidSwitch("termsim", "When using -termsim you have to specify a value");
		if (arguments.hasValue("termsim")) 
			strTermSim = arguments.getValue("termsim");
		// verify
		if (!Commons.inList(strTermSim, TERMSIMS)) {
			// invalid value
			System.err.println("Invalid term similarity, must be one of: " + Commons.formatList(TERMSIMS));
			System.exit(1);
		} else {
			// choose actual measure
			if (strTermSim.equals("resnik"))
				termSim = TermSimilarityMeasure.RESNIK;
			else if (strTermSim.equals("lin"))
				termSim = TermSimilarityMeasure.LIN;
			else if (strTermSim.equals("relevance"))
				termSim = TermSimilarityMeasure.RELEVANCE;;
		}
		
		// functional similarity
		FunctionalSimilarityMeasure funSim = null;
		String strFunSim = DEFAULT_FUNSIM;
		arguments.avoidSwitch("funsim", "When using -funsim you have to specify a value");
		if (arguments.hasValue("funsim")) 
			strFunSim = arguments.getValue("funsim");
		// verify
		if (!Commons.inList(strFunSim, FUNSIMS)) {
			// invalid value
			System.err.println("Invalid functional similarity, must be one of: " + Commons.formatList(FUNSIMS));
			System.exit(1);
		} else {
			// choose actual measure
			if (strFunSim.equals("colrowmax"))
				funSim = FunctionalSimilarityMeasure.COLROW_MAX;
			else if (strFunSim.equals("colrowavg"))
				funSim = FunctionalSimilarityMeasure.COLROW_AVERAGE;
			else if (strFunSim.equals("lord"))
				funSim = FunctionalSimilarityMeasure.LORD;
			else if (strFunSim.equals("totalmax"))
				funSim = FunctionalSimilarityMeasure.TOTALMAX;
		}
		
		// relationships
		Relationships relships = null;
		String strRelation = DEFAULT_RELATIONSHIP;
		arguments.avoidSwitch("rel", "When using -rel you have to specify a value");
		if (arguments.hasValue("rel")) 
			strRelation = arguments.getValue("rel");
		// verify
		if (!Commons.inList(strRelation, RELATIONSHIPS)) {
			// invalid value
			System.err.println("Invalid relationship, must be one of: " + Commons.formatList(RELATIONSHIPS));
			System.exit(1);
		} else {
			// choose actual measure
			if (strRelation.equals("isa"))
				relships = Relationships.IS_A;
			else if (strRelation.equals("partof"))
				relships = Relationships.PART_OF;
			else if (strRelation.equals("both"))
				relships = Relationships.BOTH;
		}
		
		// get eventual cutoff
		float cutOff = Float.NEGATIVE_INFINITY;
		if (arguments.isSet("c")) {
			if (!arguments.hasValue("c")) {
				System.err.println("When using -c you must specify a cut-off value.");
				System.exit(1);
			} else
				cutOff = Float.parseFloat(arguments.getValue("c"));
		}
		
		// check for name mappings
		Commons.checkForMappings(arguments);
		
		// get output stream
		OutputStream outstream = Commons.getOutput(arguments);
	
		// load go network
		GONetwork gonet = null;
		try {
			gonet = new GONetwork(goNetFile, namespace, relships);
		} catch (Exception e) {
			System.err.println("Error while reading go network:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		// load annotations
		GOAnnotations annos  = null;
		try {
			 annos = GOAnnotationReader.readAnnotations(annofile);
		} catch (Exception e) {
			System.err.println("Error while reading go annotations:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		// create term similarity calculator
		TermSimilarities termSimCalc = new TermSimilaritiesSchlicker(gonet, annos, termSim, true);
		// create functional similarity calculator
		FunctionalSimilarities funSimCalc =	new FunctionalSimilaritiesSchlicker(gonet, annos, termSimCalc, funSim);
		
		PrintWriter writer = new PrintWriter(outstream);
		// now iterate over all pairwise proteins and calculate their semantic similarity value
		
		Integer[] arrProteins = annos.getProteins().toArray(new Integer[0]);
		for (int i=0; i<arrProteins.length; i++) {
			for (int j=i+1; j<arrProteins.length; j++) {
				float score = funSimCalc.getScore(arrProteins[i], arrProteins[j]);
				if (score >= cutOff && score != 0) {
					writer.print(
							ProteinManager.getLabel(arrProteins[i]) + "\t"+ ProteinManager.getLabel(arrProteins[j]) + " "
					);
					writer.println(score);
				}
			}
		}
		writer.close();
		
		Commons.closeOutput(outstream);
	}

	private static void printUsage() {
		System.err.println();
		System.err.println("Semantic similarity score calculator");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("Required parameters");
		System.err.println("  -gonet    go network file in OBO format");
		System.err.println("  -name     go namespace (" + Commons.formatList(NAMESPACES) + ")");
		System.err.println("    bp      biological process");
		System.err.println("    cc      cellular component");
		System.err.println("    mf      molecular function");
		System.err.println("  -anno     gene to GO term annotation file");  
		System.err.println();
		System.err.println("Optional parameters");
		System.err.println("  -termsim   term similarity measure (" + Commons.formatList(TERMSIMS) +"), default: " + DEFAULT_TERMSIM);
		System.err.println("  -funsim    functional similarity measure (" + Commons.formatList(FUNSIMS) +"), default: " + DEFAULT_FUNSIM);
		System.err.println("  -rel       GO relationsships to use (" + Commons.formatList(RELATIONSHIPS) + "), default: " + DEFAULT_RELATIONSHIP);
		System.err.println("  -c         apply cutoff, do not output scores less than this value");
		System.err.println("  -namemap   use name mapping file");
		System.err.println("  -synfirst  name mappings file contains synonyms first,");
		System.err.println("                   otherwise targets first is assumed");
		System.err.println();
		System.err.println("Input/Output options");
		System.err.println("  -o        write to specified file instead of standard output");
		System.err.println("  -oz       GZIP the output");
		System.err.println();
	}

}
