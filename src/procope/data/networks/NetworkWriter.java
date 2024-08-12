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
package procope.data.networks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import procope.data.XGMMLWriter;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;


/**
 * Contains different methods to write {@link ProteinNetwork} objects to files 
 * or streams. Each line of the output contains one edge, a line looks as 
 * follows:
 * <pre>protein1[s]protein2[s]score[s]annotations</pre>
 * where [s] is the separator (TAB by default). If and edge has no score or no
 * annotations that field is simply left empty. Annotations are stored as key/
 * value pairs in the form {@code key1=value1;key2=value2;...}.
 * <p><font size="+1">XGMML</font>
 * <p>This class also contains an XGMML export function for protein networks.
 * These files are compatible with Cytoscape for instance.
 * 
 * @author Jan Krumsiek
 */

public class NetworkWriter {
	
	// avoid instantiation
	private NetworkWriter() {
	}

	/**
	 * Default separator for the fields of each edge: TAB
	 */
	public static final String DEFAULT_SEPARATOR = "\t";

	/**
	 * Writes a network to a specified file. TAB will be used as a separator 
	 * between the fields of each edge.
	 * 
	 * @param network the network to be written
	 * @param outfile path to the output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeNetwork(ProteinNetwork network, String outfile) throws IOException {
		writeNetwork(network, new File(outfile), DEFAULT_SEPARATOR);
	}

	/**
	 * Writes a network to a specified file. 
	 * 
	 * @param network the network to be written
	 * @param outfile path to the output file
	 * @param separator separator between the fields of each edge  
	 * @throws IOException if the file could not be written
	 */
	public static void writeNetwork(ProteinNetwork network, String outfile, String separator) throws IOException {
		writeNetwork(network, new File(outfile), separator);
	}
	
	/**
	 * Writes a network to a specified file. TAB will be used as a separator 
	 * between the fields of each edge.
	 * 
	 * @param network the network to be written
	 * @param outfile path to the output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeNetwork(ProteinNetwork network, File outfile) throws IOException {
		writeNetwork(network, outfile, DEFAULT_SEPARATOR);
	}

	/**
	 * Writes a network to a specified file. 
	 * 
	 * @param network the network to be written
	 * @param outfile path to the output file
	 * @param separator separator between the fields of each edge  
	 * @throws IOException if the file could not be written
	 */
	public static void writeNetwork(ProteinNetwork network, File outfile, String separator) throws IOException {
		FileOutputStream out = new FileOutputStream(outfile);
		writeNetwork(network, out, separator);
		out.close();
	}
	
	/**
	 * Writes a network to a specified outputstream. TAB will be used as a 
	 * separator between the fields of each edge.
	 * 
	 * @param network the network to be written
	 * @param outstream the stream to which the network will be written
	 * @throws FileNotFoundException if the file could not be opened
	 */
	public static void writeNetwork(ProteinNetwork network, OutputStream outstream) {
		writeNetwork(network, outstream, DEFAULT_SEPARATOR);
	}

	/**
	 * Writes a network to a specified outputstream. 
	 * 
	 * @param network the network to be written
	 * @param outstream the stream to which the network will be written
 	 * @param separator separator between the fields of each edge  
	 * @throws FileNotFoundException if the file could not be opened
	 */
	public static void writeNetwork(ProteinNetwork network, OutputStream outstream, String separator) {

		// iterate over all interactions
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outstream),true);
		// iterate over all edges
		int[] edges = network.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			writer.print(
					ProteinManager.getLabel(edges[i]) + separator+ ProteinManager.getLabel(edges[i+1])
			);
			writer.print("\t");
			// write score
			float score = network.getEdge(edges[i], edges[i+1]);
			if (score == score) writer.print(score);  // NaN check
			// write annotations
			Map<String, Object> annotations = network.getEdgeAnnotations(edges[i], edges[i+1]);
			if (annotations.size() > 0) {
				writer.print("\t");
				Tools.writeAnnotations(annotations, writer);	
			}
			

			writer.println();
		}
		
		writer.flush();
	}


	/**
	 * Creates a Cytoscape-compatible XGMML file. The edge weights will be 
	 * annotated to the graph as an attribute called {@code weight}. All
	 * other annotations are written out using their respective key.
	 * 
	 * @param network network to be stored as XGMML
	 * @param outfile path to output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeXGMML(ProteinNetwork network, String outfile) throws IOException {
		writeXGMML(network, new File(outfile));
	}
	
	/**
	 * Creates a Cytoscape-compatible XGMML file. The edge weights will be 
	 * annotated to the graph as an attribute called {@code weight}. All
	 * other annotations are written out using their respective key.
	 * 
	 * @param network network to be stored as XGMML
	 * @param outfile path to output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeXGMML(ProteinNetwork network, File outfile) throws IOException {
		FileOutputStream out = new FileOutputStream(outfile);
		writeXGMML(network, out);
		out.close();
	}


	/**
	 * Creates a Cytoscape-compatible XGMML file, but writes to an output 
	 * stream. The edge weights will be 
	 * annotated to the graph as an attribute called {@code confidence}. All
	 * other annotations are written out using their respective key.
	 * 
	 * @param network network to be stored as XGMML
	 * @param outstream to which the XGMML will be written to
	 * @throws FileNotFoundException if the output file could not be opened
	 */
	public static void writeXGMML(ProteinNetwork network, OutputStream outstream) {

		boolean directed = network.isDirected();
		
		// iterate over all interactions
		XGMMLWriter cyto = new XGMMLWriter(outstream);

		// output proteins
		Set<Integer> proteins = network.getProteins();
		int maxID = Tools.findMax(proteins);
		String[] protNames = new String[maxID+1];
		for (int protein : proteins) {
			String name = ProteinManager.getLabel(protein).toString();
			// cache label
			protNames[protein] = name;
			// print eventual node annotations
			Map<String, Object> annotations = ProteinManager.getAnnotations(protein);
			cyto.writeNode(name, name, annotations);
		}

		// iterate over all edges
		int[] edges = network.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			
			String label = protNames[edges[i]] + (directed?" (DirectedEdge) ":" (Edge) ") + protNames[edges[i+1]]; 
			// get annotations
			HashMap<String, Object> newAnnos = new HashMap<String, Object>();
			newAnnos.putAll(network.getEdgeAnnotations(edges[i], edges[i+1]));
			// add score if applicable
			float score = network.getEdge(edges[i], edges[i+1]);
			if (score == score) // NaN check
				newAnnos.put("weight", score);
			
			cyto.writeEdge(label, protNames[edges[i]], protNames[edges[i+1]]+"", newAnnos, directed);
		}
		
		cyto.footer();
	}

	
	
}
