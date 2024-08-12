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

import java.util.Collection;
import java.util.Random;

import procope.methods.scores.ScoresCalculator;
import procope.tools.Tools;


/** 
 * Contains static methods to generate networks from 
 * {@link ScoresCalculator scores calculators} or random networks.
 * <p>This class is not instantiatable.
 * @author Jan Krumsiek
 */

public class NetworkGenerator {

	// avoid instantiation
	private NetworkGenerator() {
	}
	
	/**
	 * Generate a network from a given 
	 * {@link ScoresCalculator scores calculator}. All proteins which
	 * are contained in the calculator will also be present in the network.
	 * 
	 * @param calculator scores calculator from which the network 
	 *                   will be calculated
	 * @return generated network
	 */
	public static ProteinNetwork generateNetwork(ScoresCalculator calculator) {
		return generateNetwork(calculator, Float.NEGATIVE_INFINITY);
	}
	
	/**
	 * Generate a network from a given 
	 * {@link ScoresCalculator scores calculator}. All proteins which
	 * are contained in the calculator will also be present in the network.
	 * 
	 * @param calculator scores calculator from which the network 
	 *                   will be calculated
	 * @param cutOff only write scores greater than or equal to this value
	 *               to the network
	 * @return generated network
	 */
	public static ProteinNetwork generateNetwork(ScoresCalculator calculator, float cutOff) {

		ProteinNetwork newNet = new ProteinNetwork(false);
		
		// get protein list and convert to array for convenience
		Collection<Integer> colproteins = calculator.getProteins();
		Integer[] proteins = colproteins.toArray(new Integer[0]);
		int protcount = proteins.length;
		// iterate over all pairwise proteins
		for (int i=0; i<protcount; i++) {
			for (int j=i+1; j<protcount; j++) {
				float score = calculator.getScore(proteins[i], proteins[j]);
				if (score != 0 && score >= cutOff) {
					newNet.setEdge(proteins[i], proteins[j], score);
				}
			}
		//	System.out.println(i + " done");
		}
		
		return newNet;
	}
	
	/**
	 * Generates a random network with the given number of nodes and edges. Each
	 * random edge is created by choosing two random nodes which are not yet 
	 * connected. Each edge has a random weight between 0 and 1. This method uses
	 * {@link Tools#random} for random number generation.
	 * 
	 * @param nodes number of nodes in the random network
	 * @param edges number of edges in the random network
	 * @return the randomly generated network
	 */
	public static ProteinNetwork generateRandomNetwork(int nodes, int edges) {
		return generateRandomNetwork(nodes, edges, Tools.random);
	}
	
	/**
	 * Generates a random network with the given number of nodes and edges. Each
	 * random edge is created by choosing two random nodes which are not yet 
	 * connected. Each edge has a random weight between 0 and 1. Uses a caller-
	 * supplied {@link java.util.Random random number generator}.
	 * 
	 * @param nodes number of nodes in the random network
	 * @param edges number of edges in the random network
	 * @param random {@link java.util.Random random number generator} to be used
	 * @return the randomly generated network
	 */
	public static ProteinNetwork generateRandomNetwork(int nodes, int edges, Random random) {
		ProteinNetwork newNet = new ProteinNetwork(false );
		
		Random rand = random;
		
		int insertedEdges = 0;
		while (insertedEdges < edges) {
			int node1 = rand.nextInt(nodes)+1;
			int node2 = rand.nextInt(nodes)+1;
			
			if (node1!=node2) {
				
						
				if (!newNet.hasEdge(node1, node2)) { 
					newNet.setEdge(node1, node2, rand.nextFloat());
				//	newNet.addAnnotation(node1, node2, "score" + rand.nextInt(3), rand.nextFloat());
					insertedEdges++;
				} 
			}
		}
		
		return newNet;
		
	}
	

}
