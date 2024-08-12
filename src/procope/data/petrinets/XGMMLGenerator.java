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
package procope.data.petrinets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

import procope.data.XGMMLWriter;
import procope.tools.ProCopeException;


/**
 * Converts a Petri net file to a Cytoscape-compatible XGMML file.
 * 
 * @author Jan Krumsiek
 * @see XGMMLWriter
 * @see PetriNetCreator
 */

public class XGMMLGenerator {

	// avoid instantiation
	private XGMMLGenerator() {
	}
	
	/**
	 * Converts the given Petri net file to the XGMML format.
	 * 
	 * @param petrinetInfile input file containing the Petri net
	 * @param XGMMLOutfile output file for the XGMML data
	  * @throws IOException if an input/output error occured
	 */
	public static void convertToXGMML(String petrinetInfile, String XGMMLOutfile) throws IOException {
		convertToXGMML(new File(petrinetInfile), new File(XGMMLOutfile));
	}
	
	/**
	 * Converts the given Petri net file to the XGMML format.
	 * 
	 * @param petrinetInfile input file containing the Petri net
	 * @param XGMMLOutfile output file for the XGMML data
	 * @throws IOException if an input/output error occured
	 */
	public static void convertToXGMML(File petrinetInfile, File XGMMLOutfile) throws IOException {
		FileInputStream in = new FileInputStream(petrinetInfile);
		FileOutputStream out = new FileOutputStream(XGMMLOutfile);
		convertToXGMML(in, out);
		in.close();
		out.close();
	}

	/**
	 * Converts a Petri net coming from an input stream to the XGMML format 
	 * written to an output stream.
	 * 
	 * @param petrinetIn input stream containing the Petri net
	 * @param XGMMLOut output stream for the XGMML data
	 */
	public static void convertToXGMML(InputStream petrinetIn, OutputStream XGMMLOut) {

		try {

			XGMMLWriter cyto = new XGMMLWriter(XGMMLOut);
			
			int transitions=0;
			
			// iterate over file
			BufferedReader reader = new BufferedReader(new InputStreamReader(petrinetIn));
			String line;
			while ((line=reader.readLine())!=null) {
				// place or transition
				if (line.charAt(0)=='p') {
					// place
					Place place = PetriNetCreator.parsePlace(line);
					String id = place.getID();
					Map<String, String> attributes = place.getAttributes();
					attributes.put("canonicalName", attributes.get("name"));
					// write it
					cyto.writeNode(id, attributes.get("name"), attributes);

				} else if (line.charAt(0)=='t') {
					// transition
					Transition transition = PetriNetCreator.parseTransition(line);
					Arc[] in = transition.getIn();
					Arc[] out = transition.getOut();
					Map<String, String> attributes = transition.getAttributes();
					attributes.put("canonicalName",  attributes.get("type"));
					// write node for this transition
					String tID= attributes.get("type") + transitions;
					cyto.writeNode(tID, tID , attributes, true);
					transitions++;
					
					// now write arcs as edges
					for (Arc inArc : in) 
						cyto.writeEdge("", inArc.getPlaceID(), tID, inArc.getAttributes(), true);
					for (Arc outArc : out) 
						cyto.writeEdge("", tID, outArc.getPlaceID(), outArc.getAttributes(), true);
				}
			}

			cyto.footer();
			XGMMLOut.flush();



		} catch (Exception e) {
			throw new ProCopeException("Could not read Petri net. File format seems to be invalid.");
		}

	}
	
}
