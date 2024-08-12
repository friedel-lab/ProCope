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
import java.util.zip.GZIPInputStream;

import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.data.petrinets.PetriNetCreator;
import procope.data.petrinets.ToPNetGenerator;
import procope.data.petrinets.XGMMLGenerator;

/**
 * This sample code demonstrates how to integrate the information of different
 * datasets into a single Petri net in ProCope. In this case we only use very
 * few information to a get a quick and reasonably small Petri net suitable
 * for visual inspection.
 * 
 * @author Jan Krumsiek
 *
 */
public class UseCase4 {
	
	public static void main(String[] args) throws Exception {
		// simple throwing Exceptions out of the main is surely not a good
		// practice, but it helps to keep the code clean in this case
		
		
		System.out.println("Reading and restricting datasets...");
		// load bootstrap network, only retain those edges 
		// which have the maximum score of 1.0
		ProteinNetwork bt = NetworkReader.readNetwork(new GZIPInputStream(new FileInputStream("data/scores/bootstrap_combined.txt.gz")));
		ProteinNetwork btRestricted = bt.getCutOffNetwork(1f);
		
		// read the MIPS reference complex set, only retain those complexes which 
		// have at least one protein in the nodes set of the restricted bootstrap network
		ComplexSet mips = ComplexSetReader.readComplexes("data/complexes/mips_complexes.txt");
		ComplexSet mipsRestricted = mips.restrictToProteinSpace(btRestricted, false);
		
		// create the Petri net creator
		PetriNetCreator petriNet = new PetriNetCreator("petrinet.txt");
		
		// add the data we prepared above
		petriNet.addInteractionNetwork(btRestricted, "BT_restricted", true);
		petriNet.addComplexSet(mipsRestricted, "MIPS_restricted");
		// we could add additional networks, complex sets and also purification data here
		
		System.out.println("Generating Petri net...");
		// generate the network
		petriNet.createPetriNet();
		// finish the process (close files etc.)
		petriNet.close();

		System.out.println("Converting Petri net...");
		// next we convert this Petri net file into XGMML format 
		// (Cytoscape compatbile) and ToPNet format
		XGMMLGenerator.convertToXGMML("petrinet.txt", "petrinet.xgmml");
		ToPNetGenerator.convertToToPNet("petrinet.txt", "data.places", "data.interactions");
	}

}
