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
package procope.methods.interologs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import procope.tools.ProCopeException;
import procope.tools.namemapping.ProteinManager;


/**
 * Reads sequences in (multi-)FASTA format. Deletes translation stop character (*) at the end of a
 * sequence. 
 * 
 * <p>This class provides static methods only and cannot be instantiated.
 * 
 * @author Jan Krumsiek
 * @see    procope.methods.interologs.Sequences
 */

public class FASTAParser {
	
	/**
	 * private constructor to avoid instantiation
	 */
	private FASTAParser() {
	}
	
	/**
	 * Loads sequences from a given multi-FASTA file
	 * 
	 * @param file path to the FASTA file
	 * @return {@link procope.methods.interologs.Sequences} object containing the read sequences
	 * @throws IOException if the file could not be read
	 */
	public static Sequences loadSequences(String file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		Sequences result = loadSequences(in);
		in.close();
		return result;
	}
	
	/**
	 * Loads sequences from a given multi-FASTA file
	 * 
	 * @param file path to the FASTA file
	 * @return {@link procope.methods.interologs.Sequences} object containing the read sequences
	 * @throws IOException if the file could not be read
	 */
	public static Sequences loadSequences(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		Sequences result = loadSequences(in);
		in.close();
		return result;
	}

	/**
	 * Loads sequences from a given InputStream, requires multi-FASTA format data
	 * 
	 * @param input input stream from which the sequence data will be read
	 * @return {@link procope.methods.interologs.Sequences} object containing the read sequences
	 * @throws IOException
	 */
	public static Sequences loadSequences(InputStream input) throws IOException {
		
		try {
			
			HashMap<Integer, String> result = new HashMap<Integer, String>();
			
			// read file line-by-line
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
	
			String line;
			String curid=null;
			StringBuffer curSeq= new StringBuffer();
			while ((line = reader.readLine()) != null) {
				// treat lines starting with # as comment lines
				if (line.charAt(0) != '#') {
					if (line.charAt(0) == '>') {
						// store old value if there was one
						if (curid != null) {
							deleteEndingAsterix(curSeq);
							int id = ProteinManager.getInternalID(curid);
							result.put(id, curSeq.toString());
							curSeq=new StringBuffer();
						}
						// status line, extract sequence id
						curid = line.substring(1);
					} else {
						// no status line => data
						curSeq.append(line.trim());
					}
				}
				                           
			}
			// store last value
			deleteEndingAsterix(curSeq);
			int id = ProteinManager.getInternalID(curid);
			result.put(id, curSeq.toString());
			
			reader.close();
			
			return new Sequences(result);
		} catch (Exception e) {
			throw new ProCopeException("Something seems to be wrong with this FASTA file.");
		}
	}
	
	/**
	 * Simple helper function to delete translation stop characters at the end of a sequence
	 */
	private static void deleteEndingAsterix(StringBuffer str) {
		int len = str.length();
		if (str.charAt(len-1) == '*')
			str.delete(len-1, len);
	}
	
}
