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
package procope.methods.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;
import procope.tools.ProCopeException;
import procope.tools.Tools;




/**
 * Performs clustering using the <i>Markov Cluster Algorithm</i> as proposed in 
 * the PhD thesis <i>Graph Clustering by Flow Simulation</i> by Stijn van 
 * Dongen.<p><b>Note:</b> This class does not contain an actual implementation
 * but an interface to the binaries of the mcl algorithm. Thus it requires
 * the {@code mcl} program to be installed on the system it is executed on.
 * <p>See also: <a target="_blank" href="http://micans.org/mcl/">
 * http://micans.org/mcl/</a>
 * 
 * @author Jan Krumsiek
 *
 */

public class MarkovClusterer implements Clusterer {
	
	private static final String NEWLINE = System.getProperty("line.separator");
	private MCLParameters params;
	
	/**
	 * Default MCL binary: {@code mcl}, requires this binary in the 
	 * current {@code PATH}
	 */
	public static final String DEFAULT_BINARY = "mcl";
	private static String binary = DEFAULT_BINARY;
	
	/**
	 * Creates an MCL cluster with default {@link MCLParameters parameters}.
	 */
	public MarkovClusterer() {
		this.params = new MCLParameters();
	}
	
	/**
	 * Creates an MCL clusterer with a given set of parameters.
	 * 
	 * @param params MCL parameters to be used for this clusterer
	 */
	public MarkovClusterer(MCLParameters params) {
		this.params = params;
	}
	
	/**
	 * Returns the {@link MCLParameters mcl parameters} for this clusterer
	 * @return parameters for this clusterer
	 */
	public MCLParameters getParamemeters() {
		return params;
	}
	
	/**
	 * call mcl binary for a given protein network with given settings, write to specified out file
	 */
	private static void doMCLClustering(ProteinNetwork net, MCLParameters params, String outfile, int timeoutSeconds) throws IOException {
		
		// get temp file name
		String graphfile = Tools.getTempFilename();
		
		// *** write graph file
		PrintWriter writer = new PrintWriter(new FileWriter(graphfile));
		// iterate over all edges
		int[] pairs = net.getEdgesArray();
		for (int i=0; i<pairs.length; i+=2) {
			int prot1 = pairs[i];
			int prot2 = pairs[i+1];
			writer.println(prot1 + "\t" + prot2 + "\t" + net.getEdge(prot1, prot2));
		}
		
		writer.close();
		
		// *** run MCL
		String command = binary + " " + graphfile + " --abc "+params.getCommandLineParameters()+"-o " + outfile;
		
		Runtime run = Runtime.getRuntime();
		Process p = run.exec(command);
		long start = System.currentTimeMillis();
		try {
			boolean hasExited=false;
			while (!hasExited) {
				Thread.sleep(333);
				try {
					p.exitValue();
					hasExited = true;
				} catch (IllegalThreadStateException e) {}
				// timed out?
				long now = System.currentTimeMillis();
				if ( ((now-start) / 1000) > timeoutSeconds)
					throw new ProCopeException("MCL timed out after " + timeoutSeconds + " seconds");
			}
			
			//p.waitFor();
		} catch (InterruptedException e) {
			throw new ProCopeException("MCL call was interupted. Message: " + e.getMessage());
		}
		
		// errors?
		if (p.exitValue() != 0) {
			// yes! assemble output
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			while ((line = reader.readLine())!=null) {
				builder.append(line);
				builder.append(NEWLINE);
			}
			reader.close();
			// throw exception
			throw new ProCopeException("MCL call failed. Error: " + NEWLINE + builder.toString());
		}
		
		// clean up
		new File(graphfile).deleteOnExit();
		
	}

	/**
	 * Perform MCL clustering. Ensure that the {@code mcl} binary is in the
	 * current {@code PATH} or set the path to that binary using 
	 * {@link #setMCLBinary(String)}
	 * 
	 * @throws ProCopeException if and error occurs during the execution
	 *                                 of the mcl binary
	 */
	public ComplexSet cluster(ProteinNetwork net) {
		
		// verify directedness of network
		if (net.isDirected()) 
			throw new ProCopeException("MCL clustering can only be done on undirected graph");
		
		try {
			// get temp out file name
			String tempfile = Tools.getTempFilename();
			// do clustering
			doMCLClustering(net, params, tempfile, params.timeoutSeconds);
			// read it
			ComplexSet result = new ComplexSet();
			BufferedReader reader = new BufferedReader(new FileReader(tempfile));
			String line;
			while ((line = reader.readLine()) != null) {
				// split up by tabs
				String[] splitUp = line.split("\t");
				// add to new complex
				Complex newComplex = new Complex();
				for (int i=0; i<splitUp.length; i++)
					newComplex.addProtein(Integer.parseInt(splitUp[i]));
				// add to final set
				result.addComplex(newComplex);
			}
			reader.close();
			
			// set delete flag
			new File(tempfile).deleteOnExit();

			return result;
			
		} catch (IOException e) {
			throw new ProCopeException("Got IO error while executing MCL. Message: " + e.getMessage());
		}
		

	}

	/**
	 * Sets the path to the {@code mcl} binary to be used. This method is 
	 * static and the binary valid for all MCL clusterer objects.
	 * 
	 * @param path {@code mcl} binary to be used
	 */
	public static void setMCLBinary(String path) {
		binary = path;
	}
	

}
