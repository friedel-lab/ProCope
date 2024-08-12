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
package procope.methods.scores.bootstrap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.tools.ProCopeException;
import procope.tools.namemapping.ProteinManager;


/**
 * Container for {@link BootstrapClustering bootstrap MCL clustering} lists.
 * Contains methods to write and load these clustering collections to and from
 * the file system.
 * <p>This class belongs to the helper classes needed for parallelizable 
 * bootstrap scores calculation.
 * 
 * @author Jan Krumsiek
 */
public class BootstrapClusterings implements Iterable<BootstrapClustering> {
	
	private ArrayList<BootstrapClustering> clusterings;

	/**
	 * Create empty clusterings list.
	 */
	public BootstrapClusterings() {
		this.clusterings = new ArrayList<BootstrapClustering>();
	}
	
	/**
	 * Adds a clustering to this clusterings list.
	 * 
	 * @param toAdd clustering to be added
	 */
	public void addClustering(BootstrapClustering toAdd) {
		clusterings.add(toAdd);
	}
	
	/**
	 * Reads a clustering collection from a given file.
	 * 
	 * @param file file from which the clusterings will be read
	 * @throws IOException if the file could not be read
	 * @throws ProCopeException if the file format is invalid
	 */
	public BootstrapClusterings(String file) throws IOException {
		this(file,false);
	}
	
	/**
	 * Reads a clusterings collection from a given file.
	 * 
	 * @param file file from which the clusterings will be read
	 * @param noRealClusterings if {@code true} then only the parameters and
	 *                         efficiencies but no actual clusterings will be
	 *                         loaded from the file
	 * @throws IOException if the file could not be read
	 * @throws ProCopeException if the file format is invalid
	 */
	public BootstrapClusterings(String file, boolean noRealClusterings) throws IOException {
		
		try {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new FileInputStream(file))));
			
			clusterings = new ArrayList<BootstrapClustering>();
			
			String line;
			boolean readNumbers=true;
			float curEfficiency=Float.NaN;
			String curParas = null;
			ComplexSet curComplexes = null;
			while ((line = reader.readLine())!=null) {
				if (readNumbers) {
					curComplexes =  new ComplexSet();
					// get efficiency
					curEfficiency = Float.parseFloat(line);
					curParas = reader.readLine();
					readNumbers = false;
				} else {
					// end of this clustering?
					if (line.charAt(0) == '#') {
						if (!noRealClusterings)
							clusterings.add(new BootstrapClustering(curComplexes, curParas, curEfficiency));
						else
							clusterings.add(new BootstrapClustering(null, curParas, curEfficiency));
						curEfficiency = Float.NaN;
						readNumbers = true;
					} else {
						if (!noRealClusterings) {
							// line with complexes
							String[] split = line.split("\t");
							Complex complex = new Complex();
							for (String protein : split) {
								complex.addProtein(ProteinManager.getInternalID(protein));
							}
							curComplexes.addComplex(complex);
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
	 * Returns the list of clusterings contained in this collection.
	 * 
	 * @return list of clusterings
	 */
	public List<BootstrapClustering> getClusterings() {
		return clusterings;
	}
	
	/**
	 * Returns the clustering at a specific index
	 * 
	 * @param i index of the clustering
	 * @return clustering at index {@code i}
	 */
	public BootstrapClustering getClustering(int i) {
		return clusterings.get(i);
	}

	/**
	 * Returns an iterator of the clusterings
	 */
	public Iterator<BootstrapClustering> iterator() {
		return clusterings.iterator();
	}
	
	/**
	 * Writes this clustering collection to a given file.
	 * 
	 * @see #BootstrapClusterings(String)
	 * @param outfile path of the file the clusterings will be written to
	 * @throws IOException if the file could not be written
	 */
	public void writeToFile(String outfile) throws IOException {
		
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				new GZIPOutputStream(new FileOutputStream(outfile))));
		// iterate over clusterings
		for (BootstrapClustering clustering : clusterings) {
			// write efficiency
			writer.println(clustering.getEfficiency());
			writer.println(clustering.getParameters());
			// write complexes
			for (Complex complex : clustering.getClustering()) {
				// write proteins, tab delimited
				for (int protein : complex)
					writer.print(ProteinManager.getLabel(protein) + "\t");
				writer.println();
			}
			// finish this clustering
			writer.println("#");
		}
		
		writer.flush();
		writer.close();
		
	}
	
}
