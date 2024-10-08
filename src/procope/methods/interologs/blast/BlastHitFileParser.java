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
package procope.methods.interologs.blast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import procope.tools.ProCopeException;
import procope.tools.namemapping.ProteinManager;


/**
 * Contains static methods to parse hits from a BLAST hit file.
 * <p><b>Note:</b> The functions assume the output format generated by
 * the {@code -m 8} option in BLAST 2.x (<i>tabular</i> output).
 * <p>This class is not instantiatble.
 * 
 * @author Jan Krumsiek
 */

//              0          1            2              3             4             5            6       7        8        9       10        11   
// # Fields: Query id, Subject id, % identity, alignment length, mismatches, gap openings, q. start, q. end, s. start, s. end, e-value, bit score

// javadoc: erwartet blast2 output mit -m 8, tabular
public class BlastHitFileParser {
	
	private BlastHitFileParser() {
	}
	
	/**
	 * Parse BLAST hits from a given BLAST hit file in tabular format.
	 * 
	 * @param hitfile BLAST hit file
	 * @return BLAST hits read from the hit file
	 * @throws IOException if the file could not be opened
	 * @throws ProCopeException if the BLAST hit file format is not valid
	 */
	public static BlastHits parseBlastHits(String hitfile)
			throws IOException, ProCopeException {
		return parseBlastHits(new File(hitfile));
	}
	
	
	/**
	 * Parse BLAST hits from a given BLAST hit file in tabular format.
	 * 
	 * @param hitfile BLAST hit file
	 * @return BLAST hits read from the hit file
	 * @throws IOException if the file could not be opened
	 * @throws ProCopeException if the BLAST hit file format is not valid
	 */
	public static BlastHits parseBlastHits(File hitfile) throws IOException,
			ProCopeException {
		FileInputStream in = new FileInputStream(hitfile);
		BlastHits result = parseBlastHits(in);
		in.close();
		return result;
	}
	
	
	/**
	 * Parse BLAST hits from a given input stream.
	 * 
	 * @param input input stream from which the BLAST hits will be read
	 * @return BLAST hits read from the stream
	 * @throws ProCopeException if the BLAST hit file format is not valid
	 */
	public static BlastHits parseBlastHits(InputStream input) throws ProCopeException {
		
		try {
			Collection<BlastHit> result = new ArrayList<BlastHit>();
			
			// parse line-by-line
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
	
			String line;
			while ((line = reader.readLine()) != null) {
				// ignore comments
				if (line.charAt(0) != '#') {
					
					// split up to get single values
					String[] split = line.split("\t");
					
					if (split.length < 10) System.out.println(line);
					
					// parse IDs
					int queryid = ProteinManager.getInternalID(split[0]);
					int hitid =  ProteinManager.getInternalID(split[1]);
					// rest
					float identity = Float.parseFloat(split[2]);
					int alignlen = Integer.parseInt(split[3]);
					int mismatches = Integer.parseInt(split[4]);
					int gapopenings = Integer.parseInt(split[5]);
					int querystart = Integer.parseInt(split[6]);
					int queryend = Integer.parseInt(split[7]);
					int hitstart = Integer.parseInt(split[8]);
					int hitend = Integer.parseInt(split[9]);
					double evalue = Double.parseDouble(split[10]);
					float bitscore = Float.parseFloat(split[11]);
					// store hit
					result.add(new BlastHit(queryid, hitid, identity, alignlen, mismatches, gapopenings,
							querystart, queryend, hitstart, hitend, evalue, bitscore
					)); 
				}
			}
		
			reader.close();
			
			return new BlastHits(result);
		} catch (Exception e) {
			// something went wrong while parsing
			throw new ProCopeException("BLAST hit file probably " +
					"has invalid format. Use tabular BLAST output!");
		}
	}
	
	

}
