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
package procope.data.purifications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;

import procope.data.XGMMLWriter;
import procope.tools.namemapping.ProteinManager;


/**
 * Writes purification data to a given file or stream.
 * <p>This class is not instantiatable.
 * 
 * @author Jan Krumsiek
 */

public class PurificationDataWriter {

	// avoid instantiation
	private PurificationDataWriter() {
	}
	
	/**
	 * Writes a purification data set to a given file.
	 * 
	 * @param data purification data set to be written out
	 * @param outfile output file
	 * @throws IOException if the file could not be written
	 */
	public static void writePurificationData(PurificationData data, String outfile)
			throws IOException {
		writePurificationData(data, new File(outfile));
	}
	
	/**
	 * Writes a purification data set to a given file.
	 * 
	 * @param data purification data set to be written out
	 * @param outfile output file
	 * @throws IOException if the file could not be written
	 */
	public static void writePurificationData(PurificationData data, File outfile)
			throws IOException {
		FileOutputStream out = new FileOutputStream(outfile);
		writePurificationData(data, out);
		out.close();
	}
	
	/**
	 * Writes a purification data set to a given output stream
	 * 
	 * @param data purification data set to be written out
	 * @param stream output stream the data will be written to
	 * @throws IOException 
	 */
	public static void writePurificationData(PurificationData data, OutputStream stream) {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream));
		
		int count=0;
		for (PurificationExperiment exp : data) {
			String expID = String.format("exp%05d", count);
			String bait = ProteinManager.getLabel(exp.getBait());
			for (int preyID : exp.getPreys()) {
				String prey = ProteinManager.getLabel(preyID);
				writer.println(expID+"\t\t\t\t" + bait + "\t" + prey + "\t");
			}
			count++;
		}
		
		writer.flush();
		
	}
	
	/**
	 * Creates a Cytoscape-compatible XGMML file. A directed network will be 
	 * written which each bait pointing to its preys.
	 * 
	 * @param data purification data to be stored as XGMML
	 * @param outfile path to output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeXGMML(PurificationData data, String outfile) throws IOException {
		writeXGMML(data, new File(outfile));
	}
	
	/**
	 * Creates a Cytoscape-compatible XGMML file. A directed network will be 
	 * written which each bait pointing to its preys.
	 * 
	 * @param data purification data to be stored as XGMML
	 * @param outfile path to output file
	 * @throws IOException if the file could not be written
	 */
	public static void writeXGMML(PurificationData data, File outfile) throws IOException {
		FileOutputStream out = new FileOutputStream(outfile);
		writeXGMML(data, out);
		out.close();
	}


	/**
	 * Creates a Cytoscape-compatible XGMML file, but writes to an output 
	 * stream. A directed network will be 
	 * written which each bait pointing to its preys.
	 * 
	 * @param data purification data to be stored as XGMML
	 * @param outstream to which the XGMML will be written to
	 * @throws FileNotFoundException if the output file could not be opened
	 */
	public static void writeXGMML(PurificationData data, OutputStream outstream) {
		
		XGMMLWriter cyto = new XGMMLWriter(outstream);

		HashSet<Integer> preysWritten = new HashSet<Integer>();
		// iterate over experiments
		int index=0;
		for (PurificationExperiment exp : data) {
			// write out bait node
			String baitName = ProteinManager.getLabel(exp.getBait()) + " (bait "+(++index) + ")"; 
			cyto.writeNode(baitName, baitName, null);
			// iterate over preys
			for (int prey : exp.getPreys()) {
				// write prey node (if not already done)
				String preyName = ProteinManager.getLabel(prey);
				if (!preysWritten.contains(prey)) 
					cyto.writeNode(preyName, preyName, null);		
				// write edge
				String edgeLabel = ProteinManager.getLabel(exp.getBait()) + " (purifies) " +  ProteinManager.getLabel(prey);
				cyto.writeEdge(edgeLabel, baitName, preyName, null, true);
			}
		}
		
		cyto.footer();
		cyto.close();
	}
	
	
	
}
