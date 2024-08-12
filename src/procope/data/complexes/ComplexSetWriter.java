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
package procope.data.complexes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import procope.data.XGMMLWriter;
import procope.data.networks.ProteinNetwork;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;


/**
 * This class contains static methods to write complex sets to files or 
 * streams. It is not instantiatable.
 * 
 * <p>Complex sets are written out as one complex per line where the proteins
 * of each complexes are seperated by a specified delimiter character (by 
 * default the TAB character).
 * 
 * @author Jan Krumsiek
 * @see procope.data.complexes.Complex
 * @see procope.data.complexes.ComplexSet
 */


public class ComplexSetWriter {

	/**
	 * Default delimiter which seperates the proteins of a complex: TAB
	 */
	public static final String DEFAULT_DELIMITER = "\t";

	/**
	 * Writes a complex set to a specified file.
	 * 
	 * @param set the set to be written
	 * @param outfile path to the output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeComplexes(ComplexSet set, String outfile) throws IOException {
		writeComplexes(set, new File(outfile), DEFAULT_DELIMITER);
	}

	/**
	 * Writes a complex set to a specified file. Accepts a custom delimiter 
	 * string for the separation of proteins of a complex.
	 * 
	 * @param set the set to be written
	 * @param outfile path to the output file
	 * @param delimiter custom delimiter character
	 * @throws IOException if the file could not be written
	 */
	public static void writeComplexes(ComplexSet set, String outfile, String delimiter) throws IOException {
		writeComplexes(set, new File(outfile), delimiter);
	}

	/**
	 * Writes a complex set to a specified file.
	 * 
	 * @param set the set to be written
	 * @param outfile path to the output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeComplexes(ComplexSet set, File outfile) throws IOException {
		writeComplexes(set, outfile, DEFAULT_DELIMITER);
	}

	/**
	 * Writes a complex set to a specified file. Accepts a custom delimiter 
	 * string for the separation of proteins of a complex.
	 * 
	 * @param set the set to be written
	 * @param outfile path to the output file
	 * @param delimiter custom delimiter character
	 * @throws IOException if the file could not be written
	 */
	public static void writeComplexes(ComplexSet set, File outfile, String delimiter) throws IOException {
		FileOutputStream out = new FileOutputStream(outfile);
		writeComplexes(set, out, delimiter);
		out.close();
	}

	/**
	 * Writes a complex set to an OutputStream.
	 * 
	 * @param set the set to be written
	 * @param outstream output stream the complexes will be written to
	 */
	public static void writeComplexes(ComplexSet set, OutputStream outstream) {
		writeComplexes(set, outstream, DEFAULT_DELIMITER);
	}

	/**
	 * Writes a complex set to an OutputStream. Accepts a custom delimiter
	 * string for the separation of proteins of a complex.
	 * 
	 * @param set the set to be written
	 * @param outstream output stream the complexes will be written to
	 * @param delimiter custom delimiter character
	 */
	public static void writeComplexes(ComplexSet set, OutputStream outstream, String delimiter) {

		PrintWriter writer = new PrintWriter(outstream);

		// iterate over complexes
		for (Complex complex : set) {
			// iterate over proteins
			int complexSize = complex.size();
			int cur=0;
			for (int protein : complex) {
				writer.print(ProteinManager.getLabel(protein));
				if (cur < complexSize-1)
					writer.print(delimiter);
				cur++;
			}
			writer.println();
		}

		writer.flush();

	}

	/**
	 * Creates a Cytoscape-compatible XGMML file. Optionally, a network can
	 * be specified to annotate the inner-complex edges with given weights.
	 * 
	 * @param set  complex set to be stored as XGMML
	 * @param net  network for edge weights, can be {@code null}
	 * @param outfile path to output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeXGMML(ComplexSet set, ProteinNetwork net, String outfile) throws IOException {
		writeXGMML(set,net, new File(outfile));
	}

	/**
	 * Creates a Cytoscape-compatible XGMML file. Optionally, a network can
	 * be specified to annotate the inner-complex edges with given weights.
	 * 
	 * @param set  complex set to be stored as XGMML
	 * @param net  network for edge weights, can be {@code null}
	 * @param outfile path to output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeXGMML(ComplexSet set, ProteinNetwork net, File outfile) throws IOException {
		FileOutputStream out = new FileOutputStream(outfile);
		writeXGMML(set,net, out);
		out.close();
	}

	/**
	 * Creates a Cytoscape-compatible XGMML file, but writes to an output
	 * stream. Optionally, a network can be specified to annotate the
	 * inner-complex edges with given weights.
	 * 
	 * @param set complex set to be stored as XGMML
	 * @param net  network for edge weights, can be {@code null}
	 * @param outstream outstream to which the XGMML will be written to
	 * @throws FileNotFoundException
	 *             if the output file could not be opened
	 */
	public static void writeXGMML(ComplexSet set, ProteinNetwork net, OutputStream outstream) {

		XGMMLWriter cyto = new XGMMLWriter(outstream);

		// output proteins
		Set<Integer> proteins = set.getProteins();
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

		// iterate over complexes
		for (Complex complex : set) {
			// iterate over all pairwise proteins
			Integer[] complexProteins = complex.getComplex().toArray(new Integer[0]);
			for (int i=0; i<complexProteins.length; i++) {
				for (int j=i+1; j<complexProteins.length; j++) {
					String label = protNames[complexProteins[i]] + " (Edge) " + protNames[complexProteins[j]];
					// get annotations
					HashMap<String, Object> newAnnos = new HashMap<String, Object>();
					if (net != null) {
						newAnnos.putAll(net.getEdgeAnnotations(complexProteins[i], complexProteins[j]));
						// lookup weight
						float score = net.getEdge(complexProteins[i], complexProteins[j]);
						if (score != score) // NaN check
							score = 0;
						newAnnos.put("weight", score);				
					} else
						newAnnos.put("weight", 0);
					cyto.writeEdge(label, protNames[complexProteins[i]] , protNames[complexProteins[j]], newAnnos);
				}
			}

		}

		cyto.footer();
		cyto.close();
	}

}
