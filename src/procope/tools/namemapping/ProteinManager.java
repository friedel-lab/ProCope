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
package procope.tools.namemapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import procope.data.networks.ProteinNetwork;
import procope.tools.BooleanExpression;
import procope.tools.ProCopeException;
import procope.tools.Tools;


/**
 * This class contains static methods which perform the mapping of string 
 * identifiers for proteins to internal integer IDs. Identical strings return
 * identical internal IDs. Case-sensitivity is controlled using 
 * {@link #setCaseSensitivity(boolean)}.
 * 
 * <p><font size="+1">Name mappings</font>
 * <p>Name mappings are useful if you are working with proteins which have 
 * different synonyms or database identifiers. You simple provide the mappings
 * as a directed network (which you can read from a file) and the protein 
 * manager will automatically map those names according to the edges in the 
 * network. See also: {@link #addNameMappings(ProteinNetwork, boolean)}
 * 
 * <p><font size="+1">Regular expressions</font>
 * <p>If you are reading files where the needed identifiers are contained in
 * longer protein identification strings you can use regular expressions to
 * parse the information you need from the string. Use 
 * {@link #setRegularExpression(String)} to parse all incoming identifiers
 * using the given expression and {@link #unsetRegularExpression()} to stop
 * using a regular expression.
 * 
 * @author Jan Krumsiek
 */

@SuppressWarnings("unchecked")
public class ProteinManager {
	
	private static final int RESIZE_STEP = 1000;
	
	private static int counter=0;
	private static int arraySizes=0;

	private static HashMap<String, Integer> proteinToInternal;
	private static String[] labels;
	private static HashMap<String, Object>[] allAnnotations;
	
	static Synonyms synonyms;
	static Pattern regex=null;
	
	/**
	 * Should protein identifiers be handled case-sensitive? This value can
	 * be changed using {@link #setCaseSensitivity(boolean)}
	 */
	protected static boolean caseSensitive = false;

	// static initialization code
	static {
		labels = new String[0];
		allAnnotations = new HashMap[0];
		proteinToInternal = new HashMap<String, Integer>();
		synonyms = new Synonyms();
	}

	private static int annotatedProteins=0;
	
	// avoid instantation
	private ProteinManager() {
	}
	
	/**
	 * Add a list of name mappings to the protein manager. Name mappings are
	 * described by directed networks. An edge {@code A=>B} in the network 
	 * means that each occurence of the name {@code A} will be translated to
	 * {code B}. The name mappings will be added to the {@link Synonyms}
	 * object of the protein manager.
	 * 
	 * @param mappings directed network containing name mappings
	 * @param targetFirst {@code true}: the first node of each edge (the source
	 *                    of the directed edge) is the target protein identifier
	 *                    where as the second node (the target of the directed
	 *                    edge) is its synonym; {@code false}: vice versa,
	 *                    synonym comes first, then the target 
	 */
	public static void addNameMappings(ProteinNetwork mappings, boolean targetFirst) {
		synonyms.addMappingNetwork(mappings, targetFirst);
	}
	
	/**
	 * Removes all existing name mappings from the protein manager.
	 */
	public static void clearNameMappings() {
		// simply create new one
		synonyms = new Synonyms();
	}

	/**
	 * Return the {@link Synonyms} object currently used
	 * 
	 * @return {@link Synonyms} object used in the protein manager
	 */
	public static Synonyms getSynonyms() {
		return synonyms;
	}
	
	/**
	 * Returns the internal protein ID for a given String protein label
	 * 
	 * @param label protein label for which the internal ID will be returned
	 * @return internal ID of the given protein label
	 */
	public static int getInternalID(String label) {
		if (caseSensitive == false)
			label = label.toLowerCase();
		// resolve synonym
		label = synonyms.resolveSynonym(label);
		// regexp mapping?
		if (regex != null) {
			Matcher m = ProteinManager.regex.matcher(label);
			m.find();
			label = m.group(1);
		}
		
		// check if already in hashmap
		Integer id = proteinToInternal.get(label);
		if (id == null) {
			// does not exist: create next, add to list
			id = ++counter;
			proteinToInternal.put(label, id);
			// enlarge arrays?
			if (counter >= arraySizes)
				enlargeArrays();
			// create information object and add to backward map
			labels[id] = label;
		}
		
		return id;
	}
	
	/**
	 * Returns the protein label associated with a given internal id.
	 * If there is no label for this ID it will return a new label with
	 * the identifer {@code #UNASSIGNED ID: [id]#}
	 * @param internalID internal ID for which the protein label will be returned
	 * @return protein label for the given internal ID
	 */
	public static String getLabel(int internalID) {
		if (internalID > counter || internalID < 1) 
			return "#UNASSIGNED ID: " + internalID + "#";
		return labels[internalID];
	}
	
	
	/**
	 * Adds an annotation to the protein with a given internal ID. An annotation
	 * consists of a String key and an arbitrary Object which must be of an
	 * {@link Integer}, {@link Float}, {@link String} or a {@link java.util.List}.
	 * <p>Existing annotations with the same key will be overwritten
	 * 
	 * @param internalID internal ID of the protein for which an annotation is added
	 * @param key key of the annotation
	 * @param value value of the annotation
	 * @return old value if key already existed or {@code null} if this key is new
	 * @throws ProCopeException if the internal ID is not assigned
	 */
	public static Object addAnnotation(int internalID, String key, Object value) throws ProCopeException {
		Tools.verifyAnnotationType(value);
		if (internalID > counter || internalID < 1)
			throw new ProCopeException("Unassigned internal ID: " + internalID);
		// create hashmap if needed
		if (allAnnotations[internalID] == null) {
			allAnnotations[internalID] = new HashMap<String, Object>();
			annotatedProteins++;
		}
		
		return allAnnotations[internalID].put(key, value);
	}
	
	/**
	 * Adds set of annotations to a protein with a given internal ID. 
	 * See also: {@link #addAnnotation(int, String, Object)}.<
	 * <p>Existing annotations will be overwritten.
	 * 
	 * @param internalID internal ID of the protein for which the annotations
	 *                   will be added
	 * @param newAnnotations map of annotations to be added.
	 * @throws ProCopeException if the internal ID is not assigned
	 */
	public static void addAnnotations(int internalID, Map<String, Object> newAnnotations) throws ProCopeException {
		if (internalID > counter || internalID < 1)
			throw new ProCopeException("Unassigned internal ID: " + internalID);
		// create hashmap if needed
		if (allAnnotations[internalID] == null) {
			allAnnotations[internalID] = new HashMap<String, Object>();
			annotatedProteins++;
		}
		
		allAnnotations[internalID].putAll(newAnnotations);
		
	}
	
	/**
	 * Retrieves an annotation for a given protein.
	 * 
	 * @param internalID internal ID of the protein
	 * @param key key of the annotation
	 * @return the value of that annotation or {@code null}
	 * @throws ProCopeException if the internal ID is not assigned
	 */
	public static Object getAnnotation(int internalID, String key) throws ProCopeException {
		if (internalID > counter || internalID < 1)
			throw new ProCopeException("Unassigned internal ID: " + internalID);
		return (allAnnotations[internalID] == null) ? null : allAnnotations[internalID].get(key);
	}
	
	/**
	 * Retrieves all annotations for a given protein.
	 * 
	 * @param internalID internal ID of the protein
	 * @return map of annotations, will be empty if no annotations are 
	 *         associated with the protein
	 * @throws ProCopeException if the internal ID is not assigned
	 */
	public static Map<String, Object> getAnnotations(int internalID) throws ProCopeException {
		if (internalID > counter || internalID < 1)
			throw new ProCopeException("Unassigned internal ID: " + internalID);
		return (allAnnotations[internalID] == null) ? Tools.EMPTY_ANNOTATION_MAP : allAnnotations[internalID];
	}
	
	/**
	 * Enlarge annotation and label arrays to hold more proteins
	 */
	private static void enlargeArrays() {
		arraySizes += RESIZE_STEP;
		labels = Tools.arrCopyOf(labels, arraySizes);
		allAnnotations = Tools.arrCopyOf(allAnnotations, arraySizes);
	}
	
	/**
	 * Returns the number of registered proteins.
	 * 
	 * @return number of proteins registered in the manager
	 */
	public static int getProteinCount() {
		return counter;
	}
	
	/**
	 * Return the subset of proteins which match a given expression.
	 * See also: {@link BooleanExpression}
	 * 
	 * @param expression expression to be evaluated
	 * @return subset of proteins which match the expression
	 */
	public static Set<Integer> getFilteredProteins(BooleanExpression expression) {
		Set<Integer> result = new HashSet<Integer>();
		// iterate over annotations
		for (int i=0; i<allAnnotations.length; i++) {
			if (expression.evaluate(
					allAnnotations[i] != null ? allAnnotations[i]
							: Tools.EMPTY_ANNOTATION_MAP))
				result.add(i);
		}
		return result;
	}
	
	/**
	 * Use given regular expression to parse the actual identifier from the 
	 * next incoming text identifiers ({@link ProteinManager see above}).
	 * 
	 * @param regex regular expression to be used
	 * @throws  PatternSyntaxException
     *          If the expression's syntax is invalid
     */
	public static void setRegularExpression(String regex) throws PatternSyntaxException {
		ProteinManager.regex = Pattern.compile(regex);
	}
	
	/**
	 * Do not use regular expression parsing for the following incoming
	 * text identifiers
	 */
	public static void unsetRegularExpression() {
		ProteinManager.regex = null;
	}
	
	/**
	 * Set if text identifiers should be case sensitive. For example, if this
	 * value is set to {@code true}, <i>YPR173C</i> and <i>ypr173c</i> will
	 * return the same internal ID.
	 * 
	 * @param sensitive identifiers case-sensitive?
	 */
	public static void setCaseSensitivity(boolean sensitive) {
		caseSensitive = sensitive;
	}

	
	/**
	 * Saves protein annotations to a given file.
	 * 
	 * @param file output file
	 * @throws IOException if the file could not be written
	 * @see #addAnnotation(int, String, Object)
	 */
	public static void saveProteinAnnotations(String file) throws IOException {
		saveProteinAnnotations(new File(file));
	}
	
	/**
	 * Saves protein annotations to a given file.
	 * 
	 * @param file output file
	 * @throws IOException if the file could not be written
	 * @see #addAnnotation(int, String, Object)
	 */
	public static void saveProteinAnnotations(File file) throws IOException {
		FileOutputStream outstream = new FileOutputStream(file);
		saveProteinAnnotations(outstream);
		outstream.close();
	}
	
	/**
	 * Saves protein annotations to a given output stream.
	 * 
	 * @param stream output stream
	 * @see #addAnnotation(int, String, Object)
	 */
	public static void saveProteinAnnotations(OutputStream stream) {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream));
		
		// iterate over all annotations
		for (int i=0; i<allAnnotations.length; i++) {
			if (allAnnotations[i] != null) {
				String label = getLabel(i); 
				writer.print(label + "\t");
				Tools.writeAnnotations(allAnnotations[i], writer);
				writer.println();
			}
		}
		writer.flush();
	}
	
	/**
	 * Load protein annotations from a given file.
	 * 
	 * @param file file to load annotations from
	 * @throws IOException if the file could not be opend
	 * @throws ProCopeException if the file format is invalid or something else went wrong
	 */
	public static void loadProteinAnnotations(String file) throws IOException, ProCopeException {
		loadProteinAnnotations(new File(file));
	}
	
	/**
	 * Load protein annotations from a given file.
	 * 
	 * @param file file to load annotations from
	 * @throws IOException if the file could not be opend
	 * @throws ProCopeException if the file format is invalid or something else went wrong
	 */
	private static void loadProteinAnnotations(File file) throws IOException {
		FileInputStream instream = new FileInputStream(file);
		loadProteinAnnotations(instream);
		instream.close();
	}

	/**
	 * Load protein annotations from a given stream.
	 * 
	 * @param instream stream to load annotations from
	 * @throws IOException if the file could not be opend
	 * @throws ProCopeException if the file format is invalid or something else went wrong
	 */
	public static void loadProteinAnnotations(InputStream instream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
		String line;
		
		try {
		
		while ((line = reader.readLine())!=null) {
			String[] split = line.split("\t");
			if (split.length == 2) {
				// class information are not given
				String label = split[0];
				String annos = split[1];
				
				int protID = getInternalID(label);
				// parse annotations
				Map<String, Object> annotations = Tools.parseAnnotations(annos);
				if (annotations.size() > 0) {
					addAnnotations(protID, annotations);
				}

			} else
				throw new Exception();
			
		}
		} catch (Exception e) {
			throw new ProCopeException("File probably has invalid format!");
		}
		
		
	}

	public static void main(String[] args) throws IOException {
		
		loadProteinAnnotations("annos");
		System.out.println(getAnnotations(2));
		
	}

	/**
	 * Deletes all protein annotations.
	 */
	public static void clearAnnotations() {
		for (int i=0; i<allAnnotations.length; i++) 
			allAnnotations[i] = null;
		annotatedProteins = 0;
		
		System.gc();
		
	}
	
	/**
	 * Returns the number of proteins which have an annotation.
	 * 
	 * @return number of proteins which have an annotation
	 */
	public static int getAnnotatedProteinCount() {
		return annotatedProteins;
	}
	
}
