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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import procope.tools.ProCopeException;


/**
 * Converts a Petri net to a ToPNet .places and .interactions file pair.
 * <p><b>Note:</b> This method will only work with the Petri nets generated
 * by {@link PetriNetCreator}. ToPNet does not support labelled edges 
 * and certain attribute types which thus require special handling.
 * 
 * @author Jan Krumsiek
 * @see PetriNetCreator
 */

public class ToPNetGenerator {
	
	/**
	 * Converts a given Petri net to ToPNet places and transitions 
	 * 
	 * @param petrinet Petri net input file
	 * @param topnetPlaces ToPNet .places output file
	 * @param topnetInteractions ToPNet .interactions output file
	 * @throws IOException if an input/output error occured
	 */
	public static void convertToToPNet(String petrinet, String topnetPlaces,
			String topnetInteractions) throws IOException {
		convertToToPNet(new File(petrinet), new File(topnetPlaces), new File(topnetInteractions), false);
	}
	
	/**
	 * Converts a given Petri net to ToPNet places and transitions 
	 * 
	 * @param petrinet Petri net input file
	 * @param topnetPlaces ToPNet .places output file
	 * @param topnetInteractions ToPNet .interactions output file
	 * @param additionalAnnotations flag defining whether any non-ToPNet-compatible annotations will be 
	 *        written to the output - this does not affect the actual ToPNet behaviour
	 *        but might be used in further versions or plugins
	 * @throws IOException if an input/output error occured
	 */
	public static void convertToToPNet(String petrinet, String topnetPlaces,
			String topnetInteractions, boolean additionalAnnotations)
			throws IOException {
		convertToToPNet(new File(petrinet), new File(topnetPlaces), new File(topnetInteractions), additionalAnnotations);
	}
	
	/**
	 * Converts a given Petri net to ToPNet places and transitions 
	 * 
	 * @param petrinet Petri net input file
	 * @param topnetPlaces ToPNet .places output file
	 * @param topnetInteractions ToPNet .interactions output file
	 * @throws IOException if an input/output error occured
	 */
	public static void convertToToPNet(File petrinet, File topnetPlaces,
			File topnetInteractions) throws IOException {
		convertToToPNet(petrinet, topnetPlaces, topnetInteractions, false);
	}
	
	/**
	 * Converts a given Petri net to ToPNet places and transitions 
	 * 
	 * @param petrinet Petri net input file
	 * @param topnetPlaces ToPNet .places output file
	 * @param topnetInteractions ToPNet .interactions output file
	 * @param additionalAnnotations flag defining whether any non-ToPNet-compatible annotations will be 
	 *        written to the output - this does not affect the actual ToPNet behaviour
	 *        but might be used in further versions or plugins
	 * @throws IOException if an input/output error occured
	 */
	public static void convertToToPNet(File petrinet, File topnetPlaces,
			File topnetInteractions, boolean additionalAnnotations)
			throws IOException {
		// get streams
		FileInputStream petriIn = new FileInputStream(petrinet);
		FileOutputStream placesOut = new FileOutputStream(topnetPlaces);
		FileOutputStream interOut = new FileOutputStream(topnetInteractions);
		// convert
		convertToToPNet(petriIn, placesOut, interOut, additionalAnnotations);
		// close the streams
		petriIn.close();
		placesOut.close();
		interOut.close();
	}

	
	/**
	 * Converts a given Petri net to ToPNet places and transitions 
	 * 
	 * @param petrinet inputstream to the Petri net
	 * @param topnetPlaces output stream to the ToPNet .places file
	 * @param topnetInteractions output stream to the ToPNet .interactions file
	 */
	public static void convertToToPNet(InputStream petrinet, OutputStream topnetPlaces, OutputStream topnetInteractions) {
		convertToToPNet(petrinet, topnetPlaces, topnetInteractions, false);
	}

	/**
	 * Converts a given Petri net to ToPNet places and transitions 
	 * 
	 * @param petrinet inputstream to the Petri net
	 * @param topnetPlaces output stream to the ToPNet .places file
	 * @param topnetInteractions output stream to the ToPNet .interactions file
	 * @param additionalAnnotations flag defining whether any non-ToPNet-compatible annotations will be 
	 *        written to the output - this does not affect the actual ToPNet behaviour
	 *        but might be used in further versions or plugins
	 */
	public static void convertToToPNet(InputStream petrinet,
			OutputStream topnetPlaces, OutputStream topnetInteractions,
			boolean additionalAnnotations) {
		
		try {
			
			// create printwriters to the output streams
			PrintWriter places = new PrintWriter(topnetPlaces);
			PrintWriter interactions = new PrintWriter(topnetInteractions);
			
			// iterate over network, line by line
			BufferedReader reader = new BufferedReader(new InputStreamReader(petrinet));
			String line;
			while ((line=reader.readLine())!=null) {
				// place or transition
				if (line.charAt(0)=='p') {
					// place
					Place place = PetriNetCreator.parsePlace(line);
					String id = place.getID();
					Map<String, String> attributes = place.getAttributes();
					// 1. place ID, use name if there is one
					String name = attributes.get("name");
					if (name !=null) {
						places.print(name);
					}
					places.print("\t");
					// 2. name, is our ID
					places.print(id);
					places.print("\t");
					// 3. type, if we have one (we should!)
					String type = attributes.get("type");
					if (type !=null) {
						places.print(type);
					}
					if (additionalAnnotations) {
						// add remaining annotations
						// copy the map and remove name and type
						Map<String, String> attCopy = new HashMap<String, String>();
						attCopy.putAll(attributes);
						attCopy.remove("name");
						attCopy.remove("type");
						// write out
						int c=0;
						if (attCopy.size() > 0) {
							places.print("\t\t\t\t\t\t");
							for (String key : attCopy.keySet()) {
								places.print(key+"="+attCopy.get(key));
								if (c<attCopy.size() -1) places.print(";");
								c++;
							}
						}
					}
					places.println();
					
				} else if (line.charAt(0)=='t') {
					interactions.print("\t");
					// transition
					Transition transition = PetriNetCreator.parseTransition(line);
					Arc[] in = transition.getIn();
					Arc[] out = transition.getOut();
					Map<String, String> attributes = transition.getAttributes();
					// write first in node
					String bait=null;
					if (in.length > 0) {
						// annotated edge => bait (special case!)
						interactions.print(in[0].getPlaceID());
						// bait associated?
						if (in[0].getAttributes().get("bait") != null)
							bait = in[0].getPlaceID(); 
					}
					interactions.print("\t");
					// write remaining in nodes
					if (in.length > 0) {
						for (int i=1; i<in.length; i++) {
							interactions.print(in[i].getPlaceID());
							if (i<in.length-1) interactions.print("|");
						}
					}
					interactions.print("\t");
					// write interaction
					String interaction = attributes.get("type");
					if (interaction != null)
						interactions.print(interaction);
					interactions.print("\t");
					// write through/via => only used for bait information here
					if (bait != null)
						interactions.print(bait);
					interactions.print("\t");
					// write first out node
					if (out.length > 0)
						interactions.print(out[0].getPlaceID());
					interactions.print("\t");
					// write remaining in nodes
					if (out.length > 1) {
						for (int i=1; i<out.length; i++) {
							interactions.print(out[i].getPlaceID());
							if (i<out.length-1) interactions.print("|");
						}
					}
					// skip organism, tissue, compartment, disease
					interactions.print("\t\t\t\t\t");
					// write confidence if there is one
					String confidence = attributes.get("score");
					if (confidence !=  null)
						interactions.print(confidence);
					
					if (additionalAnnotations) {
						// add remaining annotations
						// copy the map and remove name and type
						Map<String, String> attCopy = new HashMap<String, String>();
						attCopy.putAll(attributes);
						attCopy.remove("name");
						attCopy.remove("type");
						// write out
						int c=0;
						if (attCopy.size() > 0) {
							interactions.print("\t");
							for (String key : attCopy.keySet()) {
								interactions.print(key+"="+attCopy.get(key));
								if (c<attCopy.size() -1) interactions.print(";");
								c++;
							}
						}
					}
					interactions.println();
					
					
				} else 
					// invalid!
					throw new Exception(); // doesn't matter which exception, will be caught below anyway
				//System.out.println(line);
			}
			
			places.flush();
			interactions.flush();
			
		} catch (Exception e) {
			throw new ProCopeException("Could not read Petri net. File format seems to be invalid.");
		}
		
	}

}
