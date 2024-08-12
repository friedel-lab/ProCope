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
package procope.data.networks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import procope.tools.ProCopeException;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;


/**
 * Contains various static methods to read {@link ProteinNetwork networks} from
 * files and streams.
 * <p>Check out {@link NetworkWriter} to get further information about the
 * format in which network edges will be stored.
 * <p>This class is not instantiatable.
 * 
 * @author Jan Krumsiek
 */
public class NetworkReader {
	
	private static String seperator = "\t";

	/**
	 * Set the separator used to divide protein identifiers, scores and
	 * eventual annotations in each line of the network data.
	 * <p>Be careful to use any separators besides the whitespace
	 * and TAB character as parsing problems might occur.
	 * 
	 * @param newSeparator new separator 
	 */
	public static void setSeparator(String newSeparator) {
		seperator = newSeparator;
	}
	
	// avoid instantiation
	private NetworkReader() {
	}
	
	/**
	 * Reads an undirected network from a given file.
	 * 
	 * @param file file to read network from
	 * @return undirected network read from the file
	 * @throws IOException if the file can not be written
	 */
	public static ProteinNetwork readNetwork(String file) throws IOException {
		return readNetwork(new File(file), false);
	}
 	
	/**
	 * Reads a network from a given file.
	 * 
	 * @param file file to read network from
	 * @param directed flag which sets if the network should be directed, if 
	 *                 {@code true} the first protein in each entry will be the
	 *                 edge's source and the second protein will be the target
	 * @return network read from file
	 * @throws IOException if the file can not be written
	 */
	public static ProteinNetwork readNetwork(String file, boolean directed) throws IOException {
		return readNetwork(new File(file), directed);		
	}
	
	/**
	 * Reads an undirected network from a given file.
	 * 
	 * @param file file to read network from
	 * @return undirected network read from the file
	 * @throws IOException if the file can not be written
	 */
	public static ProteinNetwork readNetwork(File file) throws IOException {
		return readNetwork(file, false);
	}
	
	/**
	 * Reads a network from a given file.
	 * 
	 * @param file file to read network from
	 * @param directed flag which sets if the network should be directed, if 
	 *                 {@code true} the first protein in each entry will be the
	 *                 edge's source and the second protein will be the target
	 * @return network read from file
	 * @throws IOException if the file can not be written
	 */
	public static ProteinNetwork readNetwork(File file, boolean directed) throws IOException {
		FileInputStream in = new FileInputStream(file);
		ProteinNetwork net = readNetwork(in, directed);	
		in.close();
		return net;
	}
	
	/**
	 * Reads an undirected network from a given {@link InputStream}. 
	 * 
	 * @param input input stream from which the network will be read
	 * @return network read from the stream
	 */
	public static ProteinNetwork readNetwork(InputStream input) {
		return readNetwork(input, false);
	}
	
	/**
	 * Reads a network from a given {@link InputStream}. 
	 * 
	 * @param input input stream from which the network will be read
	 * @param directed flag which sets if the network should be directed, if 
	 *                 {@code true} the first protein in each entry will be the
	 *                 edge's source and the second protein will be the target
	 * @return network read from the stream
	 */
	public static ProteinNetwork readNetwork(InputStream input, boolean directed) {
		try {

			// create empty network
			ProteinNetwork net = new ProteinNetwork(directed);

			// read file file, line by line
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = reader.readLine()) != null) {
				// split up, map, add to network
				String[] split = line.split(seperator);
				int prot1 = ProteinManager.getInternalID(split[0]);
				int prot2 = ProteinManager.getInternalID(split[1]);
				// score
				float score=Float.NaN;
				if (split.length > 2 && split[2].length() > 0) 
					score = Float.parseFloat(split[2]);
				else {
					if (split.length < 3) // no annotations and no score
						score = 1.0f;
				}
				if (score == score) // NaN check
					net.setEdge(prot1, prot2, score);
				// annotations
				if (split.length > 3 && split[3].length() > 0) {
					Map<String, Object> annotations = Tools.parseAnnotations(split[3]);
					if (annotations.size() > 0)
						net.setEdgeAnnotations(prot1, prot2, annotations);
				}
			}



			return net;
		
		} catch (ProCopeException e) {
			// just a trick so this type of exception is not caught by the block below
			throw e;
		} catch (Exception e) {
			throw new ProCopeException("File probably has invalid format!");
		}
	}

	
	


}
