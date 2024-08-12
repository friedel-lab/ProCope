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
package procope.data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import procope.data.networks.NetworkWriter;


/**
 * Writes nodes and edges of any network to XGMML format. Supports arbitrary
 * annotations for both edges and nodes.
 * 
 * @author Jan Krumsiek
 * @see NetworkWriter
 * @see procope.data.petrinets.XGMMLGenerator
 */

public class XGMMLWriter {
	
	private PrintWriter writer;

	private StringToIntMapper idToInternalCyto;

	/**
	 * Create a new XGMML writer to a specified output file 
	 * 
	 * @param outfile path to the output file
	 * @throws FileNotFoundException if the file could not be opened
	 */
	public XGMMLWriter(String outfile) throws FileNotFoundException {
		this(new FileOutputStream(outfile));
	}
	
	/**
	 * Creates a new XGMML writer to a given {@link OutputStream}
	 * 
	 * @param outstream the output stream
	 */
	public XGMMLWriter(OutputStream outstream) {
		this.writer = new PrintWriter(outstream);
		this.idToInternalCyto = new StringToIntMapper();
		writeHeader();
	}
	
	/**
	 * write XGMML header
	 */
	private void writeHeader() {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<graph label=\"Exported from ProCope\" xmlns=\"http://www.cs.rpi.edu/XGMML\" Graphic=\"1\" Layout=\"organic\">");
		writer.println("  <att name=\"backgroundColor\" value=\"#ffffff\"/>");
	}
	
	/**
	 * writer XGMML/XML footer
	 */
	private void writeFooter() {
		writer.println("</graph>");
	}
	
	/**
	 * Prints the XGMML footer
	 */
	public void footer() {
		writeFooter();
		writer.flush();
	}
	
	/**
	 * Closes the writer and the underlying stream
	 */
	public void close() {
		writer.flush();
		writer.close();
	}
	
	/**
	 * Write a network node in XGMML format. The ID will not be written to
	 * the data directly but functions as an internal unique identifier for 
	 * that node. Use the same IDs to identify the adjacent nodes of an edge.
	 * 
	 * @param ID internal identifier for that node, not written to the XGMML
	 *           data but used for uniquely identifying that node when creating
	 *           edges with this XGMML writer
	 * @param label label of that node, stored as {@code canonicalName} 
	 *              attribute in the XGMML data
	 * @param annotations list of key/values pairs which are annotated with 
	 *                    that node, the string representation of each value
	 *                    will be used
	 */
	public void writeNode(String ID, String label, Map<String, ? extends Object> annotations) {
		writeNode(ID, label, annotations, false);		
	}
	
	/**
	 * Write a network node in XGMML format. The ID will not be written to
	 * the data directly but functions as an internal unique identifier for 
	 * that node. Use the same IDs to identify the adjacent nodes of an edge.
	 * 
	 * @param id internal identifier for that node, not written to the XGMML
	 *           data but used for uniquely identifying that node when creating
	 *           edges with this XGMML writer
	 * @param label label of that node, stored as {@code canonicalName} 
	 *              attribute in the XGMML data
	 * @param annotations list of key/values pairs which are annotated with 
	 *                    that node, the string representation of each value
	 *                    will be used
	 * @param transition special argument used for petri net creation, normal
	 *                   nodes will be displayed as circles, but if 
	 *                   {@code transition} the node will be displayed as a
	 *                   rectangle
	 */
	public void writeNode(String id, String label, Map<String, ? extends Object> annotations, boolean transition) {
		
		int internal = idToInternalCyto.getIntID(id);
		
		writer.println("  <node id=\""+internal+"\" label=\""+label+"\">");
		// print eventual node annotations
		if (annotations != null && annotations.size() > 0) {
			for (String key : annotations.keySet()) {
				writer.println("    <att name=\""+key+"\" value=\""+annotations.get(key)+"\"/>");
			}
		}
		
		// write shape
		if (!transition) 
			writer.println("    <graphics type=\"ELLIPSE\" />");
		else
			writer.println("    <graphics type=\"RECTANGLE\" />");
		
		writer.println("  </node>");
	}
	
	/**
	 * Writes an edge to the XGMML data. Use the same internal identifiers you 
	 * used for the {@link #writeNode} calls.
	 * 
	 * @param label label of that edge, will be stored as a {@code label} attribute
	 * @param source internal identifier of the source node
	 * @param target internal identifier of the target node
	 * @param annotations list of key/values pairs which are annotated with 
	 *                    that node, the string representation of each value
	 *                    will be used
	 */
	public void writeEdge(String label, String source, String target, Map<String, ? extends Object> annotations) {
		writeEdge(label, source, target, annotations, false);
	}
	
	/**
	 * Writes an edge to the XGMML data. Use the same internal identifiers you 
	 * used for the {@link #writeNode} calls.
	 * 
	 * @param label label of that edge, will be stored as a {@code label} attribute
	 * @param source internal identifier of the source node
	 * @param target internal identifier of the target node
	 * @param annotations list of key/values pairs which are annotated with 
	 *                    that node, the string representation of each value
	 *                    will be used
	 * @param directed  if {@code true} there will be an arrow towards the
	 *                  target node in the graph
	 */
	public void writeEdge(String label, String source, String target, Map<String,  ? extends Object> annotations, boolean directed) {
	
		writer.println("  <edge label=\""+label+"\" " +
				"source=\""+idToInternalCyto.getIntID(source)+"\" target=\""+idToInternalCyto.getIntID(target)+"\">");
		if (directed)
			writer.println("<att type=\"string\" name=\"edge.targetArrowShape\" label=\"edge.targetArrowShape\" value=\"ARROW\"/>");
		// write annotations
		if (annotations != null && annotations.size() > 0) {
			for (String key : annotations.keySet()) {
				writer.println("    <att name=\""+key+"\" value=\""+annotations.get(key)+"\"/>");
			}
		}
		
		
		writer.println("  </edge>");
	}
	
}
