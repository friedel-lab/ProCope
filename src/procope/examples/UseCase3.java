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
package procope.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.data.complexes.ComplexSetWriter;
import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.tools.namemapping.ProteinManager;

public class UseCase3 {
	
	public static void main(String[] args) {
		
		// load a complex set and a scores network
		System.out.println("Loading datasets...");
		ComplexSet BT893=null;
		ProteinNetwork bootstrap=null;
		try {
			BT893 = ComplexSetReader.readComplexes("data/complexes/BT_893.txt");
			bootstrap = NetworkReader.readNetwork(new GZIPInputStream(
					new FileInputStream("data/scores/bootstrap_combined.txt.gz")));
		} catch (Exception e) {
			// we do not do any further error handling here
			System.err.println("Could not read file.");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		/*
		* Decompose complex set
		* Each complex is treated as a subgraph, the edges are taken from the
		* given scores network. All edges below a certain threshold are deleted
		* so that complexes may decompose into smaller subunits.
		*/
		System.out.println("Decomposing...");
		ComplexSet decomposed = BT893.decompose(bootstrap, 0.5f);
		
		// remove singletons from the decomposed set (clusters with only one element)
		decomposed.removeSingletons();
		
		// show numbers of complexes
		System.out.println("Original complexes:  " + BT893.getComplexCount());
		System.out.println("After decomposition: " + decomposed.getComplexCount());
		System.out.println("Without singletons:  " + decomposed.getComplexCount());
		
		// now we extract a set of highly confident complexes from the original
		// clustering be removing all complexes with an average score below a given threshold
		BT893.removeComplexesByScore(bootstrap, 0.8f, false);
		
		// output these complexes, one per line
		System.out.println("High-confidence complexes:");
		for (Complex complex : BT893) {
			// iterate over all proteins
			for (int protein : complex) {
				System.out.print(ProteinManager.getLabel(protein)+" ");
			}
			System.out.println();
		}
		
		// write these complexes to a file
		try {
			ComplexSetWriter.writeComplexes(BT893, "bt_highconfidence.txt");
		} catch (IOException e) {
			System.err.println("Could not write file.");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
	}

}
