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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;
import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationExperiment;
import procope.tools.ProCopeException;
import procope.tools.SparseMatrixInt;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;


/**
 * Generates a Petri net which integrates data from an arbitrary number of
 * {@link ProteinNetwork networks}, {@link ComplexSet complex sets} and
 * {@link PurificationData purification data sets}. To get a detailed 
 * description about the topology of these Petri nets please consult the
 * manual of this library.
 * <p><b>Note:</b> This implementation does not explicitly build the full
 * Petri net in-memory but just outputs it to a given output stream or file.
 * The Petri nets classes {@link Place}, {@link Transition} and {@link Arc} 
 * are currently only used as helping data objects for reading a Petri net
 * from a file. See also: {@link #parsePlace(String)} and
 * {@link #parseTransition(String)}
 * 
 * @author Jan Krumsiek
 */

public class PetriNetCreator {
	
	private ArrayList<WithName<ProteinNetwork>> networks;
	private ArrayList<WithName<ComplexSet>> sets;
	private ArrayList<WithName<PurificationData>> purifications;
	
	private OutputStream outstream;
	private PrintStream writer;
	
	/**
	 * Creates a new Petri net creator which writes its output to a given file.
	 * 
	 * @param file output file for the Petri net
	 * @throws FileNotFoundException if the output file could not be opened
	 */
	public PetriNetCreator(String file) throws FileNotFoundException {
		this(new FileOutputStream(file));
	}
	
	/**
	 * Creates a new Petri net creator which writes its output to a given file.
	 * 
	 * @param file output file for the Petri net
	 * @throws FileNotFoundException if the output file could not be opened
	 */
	public PetriNetCreator(File file) throws FileNotFoundException {
		this(new FileOutputStream(file));
	}
	
	/**
	 * Creates a new Petri net creator which writes its output to a given output stream.
	 * 
	 * @param out output stream the Petri net will be written to
	 */
	public PetriNetCreator(OutputStream out) {
		networks = new ArrayList<WithName<ProteinNetwork>>();
		sets = new ArrayList<WithName<ComplexSet>>();
		purifications = new ArrayList<WithName<PurificationData>>();
		outstream = out;
	}
	
	/**
	 * Adds a {@link ProteinNetwork} which will be integrated into the Petri net.
	 * 
	 * @param net network to be integrated
	 * @param name the name this network will be referenced by in the Petri net
	 * @param insertScores insert weights of edges into the Petri net?
	 */
	public void addInteractionNetwork(ProteinNetwork net, String name, boolean insertScores) {
		WithName<ProteinNetwork> toadd = new WithName<ProteinNetwork>(net, name);
		toadd.addInfo = insertScores; // store information about scores in additional info member
		networks.add(toadd);
	}

	/**
	 * Adds a {@link ComplexSet} which will be integrated into the Petri net.
	 * 
	 * @param set complex set to be integrated
	 * @param name the name this complex set will be referenced by in the Petri net
	 */
	public void addComplexSet(ComplexSet set, String name) {
		sets.add(new WithName<ComplexSet>(set, name));
	}
	
	/**
	 * Adds a {@link PurificationData purification data set} which will be 
	 * integrated into the Petri net.
	 * 
	 * @param data purification data set to be integrated
	 * @param name the name this dataset set will be referenced by in the Petri net
	 */
	public void addPurificationData(PurificationData data, String name) {
		purifications.add(new WithName<PurificationData>(data, name));
	}
	
	/**
	 * Creates the Petri net and writes it to the output specified in the constructor.
	 * @throws IOException if an input/output error occured
	 */
	public void createPetriNet() throws IOException {
		
		int numBinPlaces=0;
		SparseMatrixInt binPlaces = new SparseMatrixInt(true);
		
		writer = new PrintStream(outstream);
		
		// gather all involved proteins
		Set<Integer> allProteins = new HashSet<Integer>();
		for (WithName<ProteinNetwork> item : networks) allProteins.addAll(item.obj.getProteins());
		for (WithName<ComplexSet> item : sets) allProteins.addAll(item.obj.getProteins());
		for (WithName<PurificationData> item : purifications) allProteins.addAll(item.obj.getProteins());
		// cache all labels
		int numProteins = Tools.findMax(allProteins);
		String[] protNames = new String[numProteins+1];
		for (int i=1; i<=numProteins; i++) 
			protNames[i] = ProteinManager.getLabel(i)+"";
		// generate places for these proteins
		for (int protein : allProteins) {
			HashMap<String, String> annotations = genMap(arr("name", "type"), arr(ProteinManager.getLabel(protein).toString(),"protein"));
			// add eventual annotations of this protein
			Map<String, Object> existing = ProteinManager.getAnnotations(protein);
			if (existing.size() > 0) 
				addToMap(annotations, existing);
			place(protNames[protein], annotations);
		}
		int maxProteinID = Tools.findMax(allProteins);
		
		// iterate over networks
		for (WithName<ProteinNetwork> netWithName : networks) {
			String name = netWithName.name;
			ProteinNetwork network = netWithName.obj;
			boolean insertScores = (Boolean)netWithName.addInfo;
			// iterate over all edges
			int[] edges = network.getEdgesArray();
			for (int i=0; i<edges.length; i+=2) {
				float score = network.getEdge(edges[i], edges[i+1]);
				Map<String, Object> origAnnos = network.getEdgeAnnotations(edges[i], edges[i+1]);
				if (score==score || origAnnos.size() > 0) { // NaN check
					// check if that binary interaction place already existed, if not => create it
					int binID = binPlaces.get(edges[i], edges[i+1]);
					if (binID == Integer.MIN_VALUE) {
						// did not exist yet
						numBinPlaces++;
						binID = numBinPlaces;
						binPlaces.set(edges[i], edges[i+1], binID);
					}
					// write out transition between those proteins and the binary transition
					HashMap<String, String> annos = genMap(arr("type", "source"), arr("binary_interact", name));
					if (insertScores && score==score) // NaN check
						annos.put("score", score+"");
					if (origAnnos.size() > 0)
						addToMap(annos, origAnnos);
					transition(
							arr(protNames[edges[i]],protNames[edges[i+1]]), 
							arr("bin" + binID),
							annos);
					//System.out.println("transition, in: p" + edges[i] + "," + edges[i+1] + ", ");
				}
			}
		}
		
		// output all of those binary interactions
		for (int i=1; i<=numBinPlaces; i++) {
			place("bin"+i, genMap(arr("type", "name"),arr("binary_interaction", "bin"+i)));
		}
		
		// now iterate over purifications
		for (WithName<PurificationData> purWithName : purifications) {
			String name = purWithName.name;
			PurificationData data = purWithName.obj;
			int experimentCount = data.getNumberOfExperiments();
			ArrayList<String> placeIDs = new ArrayList<String>();
			
			// iterate over purifications
			int count=0;
			for (PurificationExperiment exp : data) {
				// generate a transition for each purification
				Collection<Integer> preys = exp.getPreys();
				String[] in = new String[preys.size()+1];
				in[0] = "("+protNames[exp.getBait()] + ":bait)";
				int i=0;
				for (int prey : preys) {
					in[i+1] = protNames[prey];
					i++;
				}
				// no output
				// generate output place = purification experiment
				String placeID = "purification_" + name + "_" + (count++);
				placeIDs.add(placeID);
				place(placeID, genMap(arr("type", "name"), arr("purification_experiment", placeID)));
				// generate transition
				transition(in, arr(placeID), genMap(arr("type", "source"), arr("purification", name)));
			}
			
			// now check consistencies with all binary interactions
			byte[][] purMatrix = getPurificationDataMatrix(data, maxProteinID);
			// iterate over the interactions
			for (int first : binPlaces.getFirstIndices()) {
				int[] neighbors = binPlaces.getNeighbors(first);
				int[] values = binPlaces.getValues(first);
				for (int i=0; i<neighbors.length; i++) {
					// check all complexes against this interaction
					for (int j=0; j<experimentCount; j++) {
						if (purMatrix[j][neighbors[i]] == 1 && purMatrix[j][first] == 1) {
							// store transition for this hit
							transition(arr("bin" + values[i]), arr(placeIDs.get(j)), genMap(arr("type"), arr("consistent_with")));
							
						}
					}
				}
			}
			
			// consistencies with complex sets (complex set contained in purification data)
			for (WithName<ComplexSet> setWithName : sets) {
				String setName = setWithName.name;
				ComplexSet set = setWithName.obj;
				// iterate over all complexes
				int complexCount=0;
				for (Complex complex : set) {
					// check if this complex is consistent with a purification experiment of the current dataset
					for (int j=0; j<experimentCount; j++) {
						boolean consistent=true;
						for (int complexProtein : complex) {
							if (purMatrix[j][complexProtein]==0) {
								consistent = false;
								break;
							}
						}
						// add transition if this complex was consistent with the experiment
						if (consistent)
							transition(arr( "complex_" + setName + "_" + complexCount),arr(placeIDs.get(j)) , genMap(arr("type"),arr("consistent_with"))  );
						
					}
					
					complexCount++;
				}
			}
			
		}
		
		// iterate over complex sets
		for (WithName<ComplexSet> setWithName : sets) {
			String name = setWithName.name;
			ComplexSet set = setWithName.obj;
			int complexCount = set.getComplexCount();
			ArrayList<String> placeIDs = new ArrayList<String>();
			// iterate over complexes
			int count=0;
			for (Complex complex : set) {
				String placeID = "complex_" + name + "_" + (count++);
				placeIDs.add(placeID);
				// generate place for this complex
				place(placeID, genMap(arr("name", "type"), arr(placeID, "complex")));
				// generate transition for this complex with all contained proteins
				String[] in = new String[complex.size()];
				int index=0;
				for (int protein : complex) 
					in[index++] = protNames[protein];
				transition(in, arr(placeID), genMap(arr("type", "source"), arr("consists_of", name)));
			}
			
			// now we need to check the consistencies with all binary interactions
			byte[][] setMatrix = getComplexSetMatrix(set, maxProteinID);
			// iterate over the interactions
			for (int first : binPlaces.getFirstIndices()) {
				int[] neighbors = binPlaces.getNeighbors(first);
				int[] values = binPlaces.getValues(first);
				for (int i=0; i<neighbors.length; i++) {
					// check all complexes against this interaction
					for (int j=0; j<complexCount; j++) {
						if (setMatrix[j][neighbors[i]] == 1 && setMatrix[j][first] == 1) {
							// store transition for this hit
							transition(arr("bin" + values[i]), arr(placeIDs.get(j)), genMap(arr("type"),arr("consistent_with"))   );
						}
					}
				}
			}
			
			// consistencies with purification data (purification data contained in complex set)
			for (WithName<PurificationData> purWithName : purifications) {
				String purName = purWithName.name;
				PurificationData data = purWithName.obj;
				// iterate over all experiments
				int expCount=0;
				for (PurificationExperiment exp : data) {
					ArrayList<Integer> allExpProteins = new ArrayList<Integer>();
					allExpProteins.add(exp.getBait());
					allExpProteins.addAll(exp.getPreys());
					// check against all complexes in this set
					for (int j=0; j<complexCount; j++) {
						// all proteins contained
						boolean consistent=true;
						for (int expProtein : allExpProteins) {
							if (setMatrix[j][expProtein] == 0)  {
								consistent = false;
								break;
							}
						}
						// consistent?
						if (consistent) {
							// store transition
							transition(arr("purification_" + purName + "_" + (expCount)), arr(placeIDs.get(j)), genMap(arr("type"),arr("consistent_with"))  );
						}
					}
				
					expCount++;
				}
			}
			
			
		}
		
		outstream.flush();
		
	}
	
	/**
	 * add a map to another map
	 */
	private void addToMap(Map<String, String> target, Map<String, Object> source) {
		for (String key : source.keySet()) {
			target.put(key, source.get(key).toString());
		}
	}
	
	/**
	 * get protein occurence map for a given purification dataset
	 */
	private byte[][] getPurificationDataMatrix(PurificationData data, int maxProteinID) {
		byte[][] counts = new byte[data.getNumberOfExperiments()][maxProteinID+1];
		int complexIDA=0;
		for (PurificationExperiment exp : data) {
			// count all proteins
			counts[complexIDA][exp.getBait()] = 1;
			for (int protein : exp.getPreys())
				counts[complexIDA][protein] = 1;
			complexIDA++;
		}
		return counts;
	}
	
	/**
	 * get protein occurence map for a given complex set
	 */
	private byte[][] getComplexSetMatrix(ComplexSet set, int maxProteinID) {
		byte[][] counts = new byte[set.getComplexCount()][maxProteinID+1];
		int complexIDA=0;
		for (Complex complex : set) {
			// count all proteins
			for (int protein : complex)
				counts[complexIDA][protein] = 1;
			complexIDA++;
		}
		return counts;
	}
	
	/**
	 * creates a transition
	 */
	private void transition(String[] in, String[] out, HashMap<String, String> attributes) {
		writer.print("t");
		// print inputs
		writer.print("\t");
		for (int i=0; i<in.length; i++) {
			writer.print(in[i]);
			if (i<in.length-1) writer.print(",");
		}
		// print outputs
		writer.print("\t");
		for (int i=0; i<out.length; i++) {
			writer.print(out[i]);
			if (i<out.length-1) writer.print(",");
		}
		// print annotations
		if (attributes != null) {
			writer.print("\t");
			int numKeys = attributes.size();
			int count=0;
			for (String key : attributes.keySet()) {
				writer.print(key+"=" + attributes.get(key));
				count++;
				if (count < numKeys) writer.print(";");
			}
		}
		writer.println();
	}
	
	/**
	 * creates a place
	 */
	private void place(String id, HashMap<String, String> attributes) {
		writer.print("p\t" + id);
		if (attributes != null) {
			writer.print("\t");
			int numKeys = attributes.size();
			int count=0;
			for (String key : attributes.keySet()) {
				writer.print(key+"=" + attributes.get(key));
				count++;
				if (count < numKeys) writer.print(";");
			}
		}
		writer.println();
	}

	/**
	 * Parses a place from a given Petri net input line
	 * @param line line from the input data
	 * @return Place object
	 * @throws ProCopeException if the line could not be parsed
	 */
	public static Place parsePlace(String line) throws ProCopeException {
		if (line.length() < 1 || line.charAt(0) != 'p')
			throw new ProCopeException("This is not a place line.");
			
		try {
			String[] split = line.split("\t");
			// get id
			String id = split[1];
			// get eventual annotations
			Map<String, String> annotations;
			if (split.length > 2)
				annotations = readAttributes(split[2]);
			else
				annotations = Tools.EMPTY_STRING_MAP;
			
			return new Place(id, annotations);
		} catch (Exception e) {
			throw new ProCopeException("Could not parse place. Probably invalid format.");
		}
	}

	/**
	 * Parses a transition from a given Petri net input line
	 * @param line line from the input data
	 * @return Transition object
	 * @throws ProCopeException if the line could not be parsed
	 */
	public static Transition parseTransition(String line) {
		if (line.length() < 1 || line.charAt(0) != 't')
			throw new ProCopeException("This is not a transition line.");
			
		try {
			String[] split = line.split("\t");
			// get input and output places
			Arc[] in = parsePlaceList(split[1].split(","));
			Arc[] out = parsePlaceList(split[2].split(","));
			// eventual annotations
			Map<String, String> annotations;
			if (split.length > 3)
				annotations = readAttributes(split[3]);
			else
				annotations = Tools.EMPTY_STRING_MAP;
			
			return new Transition(in, out, annotations);
			
		} catch (Exception e) {
			throw new ProCopeException("Could not parse transition. Probably invalid format.");
		}
	}
	
	/**
	 * Parses a list of places and returns the corresponding Arcs
	 */
	private static Arc[] parsePlaceList(String[] placeList) {
		Arc[] result = new Arc[placeList.length];
		int index=0;
		for (String place : placeList) {
			// check whether there are annotations
			if (place.charAt(0) == '(') {
				// yes, get id and list
				int pos = place.indexOf(':');
				String placeID = place.substring(1,pos);
				Map<String,String> atts = readAttributes(place.substring(pos+1, place.length()-1));
				result[index++] = new Arc(placeID, atts);
			} else {
				// no annotations, just the ID
				result[index++] = new Arc(place);
			}
		}
		return result;
	}

	/**
	 * Reads a set of attributes and returns the corresponding map
	 */
	private static Map<String, String> readAttributes(String source) {
		HashMap<String, String> result = new HashMap<String, String>();
		String[] split = source.split(";");
		for (String anno : split) {
			int sep = anno.indexOf('=');
			if (sep == -1) 
				result.put(anno,"");
			else
				result.put(anno.substring(0,sep),anno.substring(sep+1));
		}
		return result;
	}
	
	
	/**
	 * Array to array helper
	 */
	private String[] arr(String ...strings) {
		return strings;
	}
		
	/**
	 * Generates a map from two given arrays (keys and values)
	 */
	private HashMap<String, String> genMap(String[] keys, String[] values) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i=0; i<keys.length; i++)
			map.put(keys[i], values[i]);
		return map;
	}

	private class WithName<E> {
		public E obj;
		public String name;
		public Object addInfo;

		WithName(E obj, String name) {
			this.obj = obj;
			this.name = name;
		}
	}

	/**
	 * Closes the output stream this creator is writing to. Does not generate
	 * any errors if the stream is already closed.
	 */
	public void close() {
		try {
			outstream.flush(); // to be sure
			outstream.close();
		} catch (IOException e) {
			// do nothing
		}
	}

}
