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
// done
package procope.evaluation.complexquality.go;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import procope.tools.ProCopeException;


/**
 * Represents the ontology network of one GO namespace.
 * 
 * @author Jan Krumsiek
 */
public class GONetwork {
	
	// collection of GO terms with ID as key
	HashMap<String, GOTerm> goterms;
	// namespace (biological process etc)
	private Namespace namespace;
	

	/**
	 * Creates a GO network from a given file in OBO format, using a given
	 * namespaces and the specified relationships
	 * 
	 * @param fileOntologies file with ontologies in OBO format
	 * @param namespace the namespace to be used
	 * @param relationships the relationships which are followed to contruct the network
	 * @throws IOException if something went wrong reading the ontology file
	 */
	public GONetwork(String fileOntologies, Namespace namespace, 
			Relationships relationships) throws IOException {
		this(new File(fileOntologies), namespace, relationships);
	}
	
	/**
	 * Creates a GO network from a given file in OBO format, using a given
	 * namespaces and the specified relationships
	 * 
	 * @param fileOntologies file with ontologies in OBO format
	 * @param namespace the namespace to be used
	 * @param relationships the relationships which are followed to contruct the network
	 * @throws IOException if something went wrong reading the ontology file
	 */
	public GONetwork(File fileOntologies, Namespace namespace, 
			Relationships relationships) throws IOException {
		this.namespace = namespace;
		
		// create hashmap
		goterms = new HashMap<String, GOTerm>();
		
		// what relations will be used?
		boolean isa=false;
		boolean partof=false;
		if (relationships == Relationships.IS_A)		 isa = true;
		else if (relationships == Relationships.PART_OF) partof = true;
		else isa = partof = true;	// both
		
		// load ontologies from file
		loadOntologies(fileOntologies, this.namespace, isa, partof);
		
	}
	
	/**
	 * Load ontologies from a file
	 */
	private void loadOntologies(File file, Namespace namespace, boolean useisa, boolean usepartof) throws IOException {
		
		try {
			
			String strNamespace = null;
			if (namespace == Namespace.BIOLOGICAL_PROCESS)
				strNamespace = "biological_process";
			else if (namespace == Namespace.CELLULAR_COMPONENT)
				strNamespace = "cellular_component";
			else
				strNamespace = "molecular_function";
			
			strNamespace = strNamespace.toLowerCase();
			
			// first run: parse file once to get all GO terms
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				// id line?
				if (line.startsWith("id: GO")) {
					String id = line.substring(line.indexOf(' ')+1);
					goterms.put(id, new GOTerm(id));
				}
			}
			reader.close();
			
			// second run: parse file again to get relations
			reader = new BufferedReader(new FileReader(file));
			String curid=null;
			String curnamespace=null;
			while ((line = reader.readLine()) != null) {
				
				if (line.startsWith("id: GO"))
					// id line
					curid = line.substring(line.indexOf(' ')+1);
				else if (line.startsWith("name: ")) {
					// name
					GOTerm term = goterms.get(curid); 
					if (term != null) 
						term.name = line.substring(line.indexOf(' ')+1);
					
				} else if (line.startsWith("namespace: ")) {
					// namespace line
					curnamespace = line.substring(line.indexOf(' ')+1).toLowerCase();
					// remove from GO list if not the correct namespace
					if (!curnamespace.equals(strNamespace)) {
						goterms.remove(curid);
					}
				}			
				else if (line.startsWith("is_a") && useisa) {
					// only add to the network if correct namespace
					if (curnamespace.equals(strNamespace)) {
						// extract partner GO id & add relation (if the GO term exists, might be on the exclusion list)
						String[] split = line.split(" ");
						GOTerm child = goterms.get(curid);
						GOTerm parent = goterms.get(split[1]);
						// if the parent is zero it comes from another ontology (bp, mf, cc) => ignore relationship
						if (parent !=  null) {
							child.parents.add(parent);
							parent.children.add(child);
						}
					}
				} else if (line.startsWith("relationship: part_of") && usepartof) {
					// only add to the network if correct namespace
					if (curnamespace.equals(strNamespace)) {
						// extract partner GO id & add relation (if the GO term exists, might be on the exclusion list)
						String[] split = line.split(" ");
						GOTerm child = goterms.get(curid);
						GOTerm parent = goterms.get(split[2]);
						// if the parent is zero it comes from another ontology (bp, mf, cc) => ignore relationship
						if (parent !=  null) {
							child.parents.add(parent);
							parent.children.add(child);
						}
					}
				} 
			}
			reader.close();
			
		} catch (IOException e) {
			// just a trick so this type of exception is not caught by the block below
			throw e;
		} catch (Exception e) {
			throw new ProCopeException("File probably has invalid format!");
		}
			
	}
	
	/**
	 * Return name of root term of a given namespace, hardcoded, 
	 * hopefully this does not change!
	 */
	private static String getRootTermNames(Namespace namespace) {
		if (namespace == Namespace.BIOLOGICAL_PROCESS)
			return "GO:0008150";
		else if (namespace == Namespace.CELLULAR_COMPONENT)
			return "GO:0005575";
		else if (namespace == Namespace.MOLECULAR_FUNCTION)
			return "GO:0003674";
		else
			return null;
		}
	
	
	/**
	 * Returns the root term of this ontology network (actually of the 
	 * namespace which is used in this network).
	 * 
	 * @return root term of the network
	 */
	public GOTerm getRoot() {
		return goterms.get(getRootTermNames(namespace));
	}

	/**
	 * Returns the term object with a given GO identifier.
	 * 
	 * @param ID identfier of term to look up
	 * @return the term object or {@code null} if this term is not present in 
	 *         the network
	 */
	public GOTerm getTerm(String ID) {
		return goterms.get(ID);
	}
	
	/**
	 * Get set of all term objects in the current network.
	 * 
	 * @return Set of all terms in the network.
	 */
	public Collection<GOTerm> getAllTerms() {
		return goterms.values();
	}
 	
	
	/**
	 * GO ontology relationships which can be followed while constructing
	 * the ontology network.
	 */
	public enum Relationships {
		/**
		 * follow "is_a" relationships
		 */
		IS_A, 
		/**
		 * follow "part_of" relationships
		 */
		PART_OF, 
		/**
		 * follow both "is_a" and "part_of" relationships
		 */
		BOTH
	}
	
	/**
	 * The GO namespaces.
	 */
	public enum Namespace {
		BIOLOGICAL_PROCESS, 
		CELLULAR_COMPONENT, 
		MOLECULAR_FUNCTION
	}

	/**
	 * Returns the namespace used in this network.
	 * 
	 * @return namespace used in this network
	 */
	public Namespace getNamespace() {
		return namespace;
	}
		
}
