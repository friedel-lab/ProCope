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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import procope.tools.ProCopeException;
import procope.tools.namemapping.ProteinManager;


/**
 * This class contains static methods to read complexes from files or streams.
 * It is not instantiatable.
 * 
 * <p>A complex set file contains one complex per line. Each complex consists
 * of a list of proteins seperated by a separator character. Protein 
 * identifiers will be mapped to internal integer IDs (see also: 
 * {@link procope.tools.namemapping.ProteinManager})
 * 
 * @author Jan Krumsiek
 * @see procope.data.complexes.Complex
 * @see procope.data.complexes.ComplexSet
 */
public class ComplexSetReader {

	/**
	 * Private constructor to avoid instantiation
	 */
	private ComplexSetReader() {
	}
	
	/**
	 * Default separator which seperates the proteins of a complex: TAB
	 */
	public static final String DEFAULT_SEPARATOR = "\t";
	
	/**
	 * Reads complexes from a given file. Uses the default separator for 
	 * proteins in a complex (the tab character)
	 * 
	 * @param file path to the file which contains the complexes
	 * @return complex set read from the file
	 * @throws IOException on input/output errors
	 * @throws ProCopeException if the file could not be parsed
	 */
	public static ComplexSet readComplexes(String file) throws IOException, ProCopeException {
		return readComplexes(new File(file), DEFAULT_SEPARATOR);
	}
	
	
	/**
	 * Reads complexes from a given file. 
	 * 
	 * @param  file path to the file which contains the complexes
	 * @param  separator seperator character between proteins in a complex
	 * @return complex set read from the file
	 * @throws IOException on input/output errors
	 * @throws ProCopeException if the file could not be parsed
	 */
	public static ComplexSet readComplexes(String file, String separator) throws IOException, ProCopeException {
		return readComplexes(new File(file), separator);
	}
	
	/**
	 * Reads complexes from a given file. Uses the default separator for 
	 * proteins in a complex (the tab character)
	 * 
	 * @param file path to the file which contains the complexes
	 * @return complex set read from the file
	 * @throws IOException on input/output errors
	 * @throws ProCopeException if the file could not be parsed
	 */
	public static ComplexSet readComplexes(File file) throws IOException, ProCopeException {
		return readComplexes(file, DEFAULT_SEPARATOR);
	}
	
	/**
	 * Reads complexes from a given file. 
	 * 
	 * @param  file path to the file which contains the complexes
	 * @param  separator seperator character between proteins in a complex
	 * @return complex set read from the file
	 * @throws IOException on input/output errors
	 * @throws ProCopeException if the file could not be parsed
	 */
	public static ComplexSet readComplexes(File file, String separator) throws IOException, ProCopeException {
		FileInputStream in = new FileInputStream(file);
		ComplexSet result = readComplexes(new FileInputStream(file), separator);
		in.close();
		return result;
	}
		

	/**
	 * Reads complexes from a given InputStream. 
	 * 
	 * @param  input input stream from which complex data will be read
	 * @return complex set read from the stream
	 * @throws IOException on input/output errors
	 * @throws ProCopeException if the file could not be parsed
	 */
	public static ComplexSet readComplexes(InputStream input) throws IOException, ProCopeException {
		return readComplexes(input, DEFAULT_SEPARATOR);
	}
	
	/**
	 * Reads complexes from a given InputStream. 
	 * 
	 * @param  input input stream from which complex data will be read
	 * @return complex set read from the stream
	 * @throws IOException on input/output errors
	 * @throws ProCopeException if the file could not be parsed
	 */
	public static ComplexSet readComplexes(InputStream input, String separator) throws IOException, ProCopeException {

		try {
		
			// initialize
			ArrayList<ArrayList<Integer>> build = new ArrayList<ArrayList<Integer>>();
	
			// read file line by line
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line=reader.readLine()) != null) {
				// initialize new complex
				ArrayList<Integer> newcomplex = new ArrayList<Integer>();
				// extract proteins
				String[] proteins = line.split("\t");
				for (String protein : proteins) {
					int protID = ProteinManager.getInternalID(protein);
						
					newcomplex.add(protID);
				}
				build.add(newcomplex);
			}
	
			return new ComplexSet(build);
			
		} catch (IOException e) {
			// just a trick so this type of exception is not caught by the block below
			throw e;
		} catch (Exception e) {
			throw new ProCopeException("File probably has invalid format!");
		}
	}
	
}
