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

import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetWriter;
import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.methods.clustering.HierarchicalClusterer;
import procope.methods.clustering.HierarchicalLinkage;
import procope.methods.clustering.MCLParameters;
import procope.methods.clustering.MarkovClusterer;
import procope.tools.ProCopeException;
import procope.tools.Tools;
import procope.tools.userclasses.UserClusterer;
import procope.tools.userclasses.UserParameter;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class Clusterer {
	
	private static final String[] methods = Commons.getList("hcl", "mcl", "user");
	private static final String[] linkages = Commons.getList("single", "complete", "upgma", "wpgma");
	
	private static final String[] allowed = Commons.getList("p", "o", "net",
			"method", "linkage", "cutoff", "nosingle", "I", "mclbin",
			"namemap", "synfirst", "name", "P", "S", "R", "pct");
	private static final String[] numeric = Commons.getList("I", "cutoff", "P", "S", "R", "pct");
	

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
		
		// get network file
		if (!arguments.hasValue("net")) {
			System.err.println("You must specify a scores network using -net");
			System.exit(1);
		}
		String netFile = arguments.getValue("net");
		
		// get cutoff
		boolean noSingletons = arguments.isSet("nosingle");
		
		// get clustering method
		String method = arguments.getValue("method");
		Object[] parameters=null;
		if (!arguments.hasValue("method")) {
			System.err.println("You must specify a clustering method using -method");
			System.exit(1);
		} else {
			if (!Commons.inList(method, methods)) {
				// invalid method
				System.err.println("Invalid clustering method, must be one of: " + Commons.formatList(methods) );
				System.exit(1);
			} else {
				if (method.equals("hcl")) {
					// hierarchical clustering, check for parameters
					if (!arguments.hasValue("linkage") || !arguments.hasValue("cutoff")) {
						System.err.println("For hierarchical clustering you must provide values for -linkage and -cutoff");
						System.exit(1);
					} else {
						// valid linkage?
						HierarchicalLinkage linkage=null;
						String strLinkage = arguments.getValue("linkage");
						if (!Commons.inList(strLinkage, linkages)) {
							// invalid linkage
							System.err.println("Invalid linkage, must be one of: " + Commons.formatList(linkages) );
							System.exit(1);
						} else {
							// get actual linkage enum
							if (strLinkage.equals("single"))
								linkage = HierarchicalLinkage.SINGLE_LINK;
							else if (strLinkage.equals("complete"))
								linkage = HierarchicalLinkage.COMPLETE_LINK;
							else if (strLinkage.equals("upgma"))
								linkage = HierarchicalLinkage.UPGMA;
							else if (strLinkage.equals("wpgma"))
								linkage = HierarchicalLinkage.WPGMA;
						}
						// get cutoff
						Float cutOff = Float.parseFloat(arguments.getValue("cutoff"));
						parameters = new Object[]{linkage, cutOff};
					}
				} else if (method.equals("mcl")) {
					// markov clustering
					if (!arguments.hasValue("I")) {
						System.err.println("For markov clustering you must provide a value for -I");
						System.exit(1);
					} else {
						// create parameters object
						MCLParameters params = new MCLParameters();
						// get parameters
						params.setInflation(Float.parseFloat(arguments.getValue("I")));
						if (arguments.hasValue("mclbin"))
							MarkovClusterer.setMCLBinary(arguments.getValue("mclbin"));
						// advanced parameters
						if (arguments.hasValue("P"))
							params.setP(Float.parseFloat(arguments.getValue("P")));
						if (arguments.hasValue("S"))
							params.setS(Float.parseFloat(arguments.getValue("S")));
						if (arguments.hasValue("R"))
							params.setR(Float.parseFloat(arguments.getValue("R")));
						if (arguments.hasValue("pct"))
							params.setPct(Float.parseFloat(arguments.getValue("pct")));

						parameters = new Object[]{params};
					}
					
				} else if (method.equals("user")) { 
					// user clusterer
					if (!arguments.hasValue("name")) {
						System.err.println("Please specify the name of the user clusterer using -name");
						System.exit(1);
					} else {
						parameters = new Object[]{arguments.getValue("name")};
					}
				}
						
			}
		}
		
		// check for name mappings
		Commons.checkForMappings(arguments);
		
		// get output stream
		OutputStream outstream = Commons.getOutput(arguments);
		
		// create objects
		procope.methods.clustering.Clusterer clusterer=null;
		if (method.equals("hcl")) 
			clusterer = new HierarchicalClusterer((HierarchicalLinkage)parameters[0], (Float)parameters[1]);
		else if (method.equals("mcl")){
			// prepare markov clusterer
			MCLParameters params = (MCLParameters)parameters[0];
			clusterer = new MarkovClusterer(params);
		} else if (method.equals("user")) {
			// a user clusterer
			try {
				// try to load the XML file
				List<UserClusterer> clusterers = loadClusterers();
				if (clusterers == null) {
					System.err.println("File " + Tools.CLUSTERERSFILE + " does not exist.\nNo user clusterers are defined.");
					System.exit(1);
				}
				// find the clusterer
				UserClusterer userClust = null;
				for (UserClusterer curClust : clusterers) {
					if (curClust.getName().equals(parameters[0])) {
						userClust = curClust;
						break;
					}
				}
				// if we did not find it => output error
				if (userClust == null) {
					System.err.println("User clusterer '"+parameters[0]+"' not defined in " + Tools.CLUSTERERSFILE);
					System.exit(1);
				}
				// we got it, get parameters from the user
				Vector<Object> userInput = new Vector<Object>();
				if (userClust.getParameters().size() > 0) {
					System.out.println("Parameters for " + parameters[0] );
					for (UserParameter userPara : userClust.getParameters()) 
						userInput.add(Commons.inputUserParameter(userPara));
				}
				// now try to create the clusterer
				clusterer = userClust.generateClusterer(userInput.toArray(new Object[0]));
				
				
			} catch (ClassNotFoundException e) {
				System.err.println("User clusterer class not found in current classpath:\n" + e.getMessage());
				System.exit(2);
			} catch (NoSuchMethodException e) {
				System.err.println("Constructor does not exist:\n" + e.getMessage());
				System.exit(2);
			} catch (InvocationTargetException e) {
				System.err.println("User clusterer reported a problem while initializing:\n" + 
						e.getTargetException().getMessage());
				System.exit(2);
			} catch (Exception e) {
				System.err.println("Could not create clusterer:");
				System.err.println(e.getMessage());
				System.exit(2);
			}
		}
		
		// load protein network
		ProteinNetwork net=null;
		try {
			InputStream in = Commons.getInputStream(netFile);
			net = NetworkReader.readNetwork(in, false);
			in.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		// do it
		ComplexSet clustering=null;
		try {
			clustering = clusterer.cluster(net);
		} catch (Exception e) {
			System.err.println("An error occured while clustering:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		// cutoff too small clusters if needed
		if (noSingletons)
			clustering.removeSingletons();
		
		// write output
		ComplexSetWriter.writeComplexes(clustering, outstream);
		
		Commons.closeOutput(outstream);
	}
	
	private static List<UserClusterer> loadClusterers() {
		// if the XML file is there => try to parse it
		File xml = new File(Tools.CLUSTERERSFILE);
		if (xml.exists()) {
			try {
				FileInputStream stream = new FileInputStream(xml);
				List<UserClusterer> clusterers = UserClusterer.parseClusterers(stream);
				stream.close();
				return clusterers;
			} catch (Exception e) {
				throw new ProCopeException(e.getMessage());
			}
		} else
			return null;
	}

	private static void printUsage() {
		System.err.println();
		System.err.println("Score network clusterer");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("Required parameters");
		System.err.println("  -net         score network file");
		System.err.println("  -method      clustering method (see below)");
		System.err.println();
		System.err.println("Optional parameters");
		System.err.println("  -nosingle    exclude singletons (clusters with only one element)");
		System.err.println("  -namemap     use name mapping file");
		System.err.println("  -synfirst    name mappings file contains synonyms first,");
		System.err.println("                   otherwise targets first is assumed");
		System.err.println();
		System.err.println("Clustering methods and their parameters");
		System.err.println("  hcl          hierarchical agglomerative clustering");
		System.err.println("    -linkage   one of: single, complete, upgma, wpgma");
		System.err.println("    -cutoff    cutoff value to create clusters from dendrogram");
		System.err.println("  mcl          Markov Cluster Algorithm (requires installed 'mcl' program)");
		System.err.println("    -I         inflation coefficient");
		System.err.println("    -mclbin    [optional] path to 'mcl' binary if not in current PATH");
		System.err.println("    -P         [optional] pruning number");
		System.err.println("    -S         [optional] selection number");
		System.err.println("    -R         [optional] recover number");
		System.err.println("    -pct         [optional] ");
		System.err.println("  user         run a user clusterer");
		System.err.println("    -name      name of the user clusterer");
		System.err.println();
		System.err.println("Input/Output options");
		System.err.println("  -o           write to specified file instead of standard output");
		System.err.println();
	}
	
}
