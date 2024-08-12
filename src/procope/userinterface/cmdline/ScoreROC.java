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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;

import procope.data.LocalizationData;
import procope.data.LocalizationDataReader;
import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.evaluation.networkperformance.ROC;
import procope.evaluation.networkperformance.ROCCurve;
import procope.evaluation.networkperformance.ROCCurveHandler;
import procope.evaluation.networkperformance.ROCPoint;
import procope.tools.ChartTools;
import procope.tools.Tools;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class ScoreROC {
	
	private static final String[] allowed = Commons.getList(
			"ref", "out", "png", "negref", "netprots" ,"namemap", "width", "height","loc", "fpmax","synfirst");
	private static final String[] explicitSwitches = Commons.getList("png", "netprots");
	private static final String[] numeric = Commons.getList("widht", "height", "fpmax");
	
	private static int width = 800;
	private static int height = 600;
	
	public static void main(String[] args) {

		// no command line arguments => print usage
		if (args.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		// get arguments object
		CommandLineArguments arguments = null;
		try {
			arguments = new CommandLineArguments(args, true, explicitSwitches, allowed);
			arguments.checkNumericArguments(numeric);
		} catch (InvalidArgumentsException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// reference set
		String setFile = arguments.requireValue("ref", "You have to specify a reference set using -ref");
		// negative ref set
		arguments.avoidSwitch("negref", "When using -negref you must specify a set file.");
		String negSetFile = null;
		if (arguments.isSet("negref"))
			negSetFile = arguments.getValue("negref");
		
		// localization data
		String locFile = null;
		if (arguments.isSet("loc"))
			locFile = arguments.getValue("loc");
		
		// fpmax
		float fpMax = Float.POSITIVE_INFINITY;
		arguments.avoidSwitch("fpmax", "Whenn using -fpmax you must specifiy a value.");
		if (arguments.isSet("fpmax"))
			fpMax = Float.parseFloat(arguments.getValue("fpmax"));
		
		// check for name mappings
		Commons.checkForMappings(arguments);
		
		// out file
		String outFile = arguments.requireValue("out", "You have to specify an output file name using -out");
		
		// png, width and height
		boolean png = arguments.isSet("png");
		if (arguments.isSet("width") || arguments.isSet("height")) {
			if (!png) {
				System.err.println("-width and -height can only be used with -png");
				System.exit(1);
			} else {
				width = Integer.parseInt(arguments.getValue("width"));
				height = Integer.parseInt(arguments.getValue("height"));
			}
		}
		
		// protein networks
		List<String> netFiles = arguments.getFreeArguments();
		if (netFiles.size() == 0) {
			System.err.println("You have to provide at least one score network file");
			System.exit(1);
		}
		
		// load reference set
		ComplexSet set = null;
		try {
			InputStream inStream = new FileInputStream(setFile);
			set = ComplexSetReader.readComplexes(inStream, "\t");
			inStream.close();
		} catch (Exception e) {
			System.err.println("Error while reading complex set file:");
			System.err.println(e.getMessage());
			System.exit(2);
		}
		
		// load negative set
		ComplexSet negSet = null;
		if (negSetFile != null) {
			try {
				negSet = ComplexSetReader.readComplexes(negSetFile);
			} catch (Exception e) {
				System.err.println("Error while reading negative complex set file:");
				System.err.println(e.getMessage());
				System.exit(2);
			}
		} else
			negSet = set;
	
		// load localization data
		LocalizationData locData = null;
		if (locFile != null) {
			try {
				locData = LocalizationDataReader.readLocalizationData(locFile);
			} catch (Exception e) {
				System.err.println("Could not load localization data:");
				System.err.println(e.getMessage());
				System.exit(2);
			}
		}
		
		System.out.print("Loading networks... ");
		// read protein networks
		ArrayList<ProteinNetwork> nets = new ArrayList<ProteinNetwork>();
		ArrayList<String> netNames = new ArrayList<String>();
		for (String netFile : netFiles) {
			try {
				// get stream
				InputStream instream = Commons.getInputStream(netFile);
				// read it
				nets.add(NetworkReader.readNetwork(instream, false));
				netNames.add(procope.tools.Tools.extractBaseFilename(netFile));
				instream.close();
			} catch (Exception e) {
				System.err.println("Could not read network file " + netFile + ":");
				System.err.println(e.getMessage());
				System.exit(2);
			}
		}
		System.out.println("done.");
		
		
		// calculate the ROC
		System.out.print("Calculating ROC curve... ");
		List<ROCCurve> curves = ROC.calculateROCCurves(nets, set, negSet, locData, arguments.isSet("netprots"));
		System.out.println("done.");
	
		// decide what to do with it
		if (arguments.isSet("png")) {
			
			try {
				System.out.print("Drawing curve... ");
				JFreeChart chart = ROCCurveHandler.generateChart(curves, netNames, fpMax);
				System.out.println("done.");
				ChartTools.writeChartToPNG(chart, new File(outFile), width, height);
				System.out.println("Written " + outFile);
			} catch (Exception e) {
				System.err.println("Something went wrong while running gnuplot:");
				System.err.println(e.getMessage());
				System.exit(2);
			}
		} else {
			
			// write to files
			int index=0;
			for (ROCCurve curve : curves) {
				// generate file name
				String fileName = outFile + netNames.get(index);
				try {
					// write to file
					PrintWriter writer = new PrintWriter(new FileWriter(fileName));
					for (ROCPoint point : curve) {
						writer.println(point.getFP()+"\t"+point.getTP());
					}
					writer.close();
					// output message
					System.out.println("Written " + fileName);
				} catch (IOException e) {
					System.err.println("Error writing ROC data file " + fileName + ":");
					System.err.println(e.getMessage());
					System.exit(2);
				}
				
				
				index++;
			}
			
		}
		
	}
	

	private static void printUsage() {
		System.err.println();
		System.err.println("Score network ROC curve calculator");
		System.err.println(Tools.FULL_LIB_NAME);
		System.err.println();
		System.err.println("parameters: <options> <score network 1> (<score network 2> ...)"); 
		System.err.println();
		System.err.println("Required:");
		System.err.println(" -ref       complex set used for true positive determination");
		System.err.println(" -out       output file name");
		System.err.println("            without -png this is the prefix for roc curve file names");
		System.err.println("            when using -png this is the output PNG file name");
		System.err.println();
		System.err.println("Optional:");
		System.err.println(" -negref    use a different reference set for the calculation of the negative set");
		System.err.println(" -loc       use localization data for the generation of the negative set");
		System.err.println(" -netprots  switch, determines whether the positive and negatives sets are restricted");
		System.err.println("            to proteins present in at least one of the score networks.");
		System.err.println(" -fpmax     only plot false-positive rate up to this value");
		System.err.println("");
		System.err.println(" -png       draw ROC curve and save to png file"); 
		System.err.println(" -width     width of the image (default: 800)");
		System.err.println(" -height    height of the image (default: 600)");
		System.err.println("");
		System.err.println(" -namemap   use name mapping file");
		System.err.println(" -synfirst  name mappings file contains synonyms first,");
		System.err.println("               otherwise targets first is assumed");
		System.err.println();
		
		
	}
	

}
