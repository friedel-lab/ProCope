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
package procope.userinterface.cytoscape;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;
import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationExperiment;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;

/**
 * Contains methods for ProCope to speak with Cytoscape.
 * 
 * @author Jan Krumsiek
 */

public class CytoscapeAdapter {
	
//	private static VisualStyle style = new ProCopeVisualStyle();

	public static void exportNetwork(ProteinNetwork network, String netName) {
		
		boolean directed = network.isDirected();
		
		// create new cytoscape network
		CyNetwork cynet = Cytoscape.createNetwork(netName);
		CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
		CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();
		
		// output proteins
		Set<Integer> proteins = network.getProteins();
		int maxID = Tools.findMax(proteins);
		String[] protNames = new String[maxID+1];
		for (int protein : proteins) {
			String name = ProteinManager.getLabel(protein).toString();
			// cache label
			protNames[protein] = name;
			// add node
			CyNode node = Cytoscape.getCyNode(name, true);
			cynet.addNode(node);
			// store eventual annotations
			Map<String, Object> annotations = ProteinManager.getAnnotations(protein);
			for (String key : annotations.keySet()) {
				nodeAttr.setAttribute(node.getIdentifier(), key, annotations.get(key).toString());
			}
		}
		
		// output edges
		int[] edges = network.getEdgesArray();
		for (int i=0; i<edges.length; i+=2) {
			String label = protNames[edges[i]] + (directed?" (DirectedEdge) ":" (Edge) ") + protNames[edges[i+1]]; 
			// create and add edge
			CyEdge edge = Cytoscape.getCyEdge(protNames[edges[i]], label, protNames[edges[i+1]], "");
			cynet.addEdge(edge);
			// store score
			edgeAttr.setAttribute(edge.getIdentifier(), "weight", network.getEdge(edges[i], edges[i+1])+"");
			// store eventual annotations
			HashMap<String, Object> annos = new HashMap<String, Object>();
			annos.putAll(network.getEdgeAnnotations(edges[i], edges[i+1]));
			for (String key : annos.keySet()) {
				nodeAttr.setAttribute(edge.getIdentifier(), key, annos.get(key).toString());
			}
		}
		Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
	}
	
	public static void exportComplexSet(ComplexSet set, ProteinNetwork net, String setName) {
		
		// create new cytoscape network
		CyNetwork cynet = Cytoscape.createNetwork(setName);
		CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
		
		// output proteins
		Set<Integer> proteins = set.getProteins();
		int maxID = Tools.findMax(proteins);
		String[] protNames = new String[maxID+1];
		for (int protein : proteins) {
			String name = ProteinManager.getLabel(protein).toString();
			// cache label
			protNames[protein] = name;
			// add node
			CyNode node = Cytoscape.getCyNode(name, true);
			cynet.addNode(node);
			// store eventual annotations
			Map<String, Object> annotations = ProteinManager.getAnnotations(protein);
			for (String key : annotations.keySet()) {
				nodeAttr.setAttribute(node.getIdentifier(), key, annotations.get(key).toString());
			}
		}

		
		// iterate over complexes
		for (Complex complex : set) {
			// iterate over all pairwise proteins
			Integer[] complexProteins = complex.getComplex().toArray(new Integer[0]);
			for (int i=0; i<complexProteins.length; i++) {
				for (int j=i+1; j<complexProteins.length; j++) {
					String label = protNames[complexProteins[i]] + " (Edge) " + protNames[complexProteins[j]];
					// get annotations
					HashMap<String, Object> annos = new HashMap<String, Object>();
					if (net != null) {
						annos.putAll(net.getEdgeAnnotations(complexProteins[i], complexProteins[j]));
						// lookup weight
						float score = net.getEdge(complexProteins[i], complexProteins[j]);
						if (score != score) // NaN check
							score = 0;
						annos.put("weight", score);				
					} else
						annos.put("weight", 0);
					
					// create and add edge
					CyEdge edge = Cytoscape.getCyEdge(protNames[complexProteins[i]], label, protNames[complexProteins[j]], "");
					cynet.addEdge(edge);
					// store annotations
					for (String key : annos.keySet()) {
						nodeAttr.setAttribute(edge.getIdentifier(), key, annos.get(key).toString());
					}
					
				}
			}

		}
		
		Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
	}

	public static void exportPurificationData(PurificationData data, String dataname) {
		
		// create new cytoscape network
		CyNetwork cynet = Cytoscape.createNetwork(dataname);
		CyAttributes nodeAttr = Cytoscape.getEdgeAttributes();
		
		HashSet<Integer> preysWritten = new HashSet<Integer>();
		// iterate over experiments
		int index=0;
		for (PurificationExperiment exp : data) {
			// write out bait node
			String baitName = ProteinManager.getLabel(exp.getBait()) + " (bait "+(++index) + ")"; 
			// add node
			CyNode node = Cytoscape.getCyNode(baitName, true);
			cynet.addNode(node);
			// store eventual annotations
			Map<String, Object> annotations = ProteinManager.getAnnotations(exp.getBait());
			for (String key : annotations.keySet()) {
				nodeAttr.setAttribute(node.getIdentifier(), key, annotations.get(key).toString());
			}
			
			// iterate over preys
			for (int prey : exp.getPreys()) {
				// write prey node (if not already done)
				String preyName = ProteinManager.getLabel(prey);
				if (!preysWritten.contains(prey)) {
					// add node
					CyNode preyNode = Cytoscape.getCyNode(preyName, true);
					cynet.addNode(preyNode);
					// store eventual annotations
					Map<String, Object> preyAnnos = ProteinManager.getAnnotations(prey);
					for (String key : preyAnnos.keySet()) {
						nodeAttr.setAttribute(preyNode.getIdentifier(), key, preyAnnos.get(key).toString());
					}
				}
				// edge label?
				String edgeLabel = ProteinManager.getLabel(exp.getBait()) + " (purifies) " +  ProteinManager.getLabel(prey);
				// create and add edge
				CyEdge edge = Cytoscape.getCyEdge(baitName, edgeLabel, preyName, "" );
				cynet.addEdge(edge);
			}
		}
		
		// arrow heads
//		CyNetworkView cyview = Cytoscape.getCurrentNetworkView();
//		cyview.setVisualStyle(style.getName());
//		Cytoscape.getVisualMappingManager().setVisualStyle(style.getName());

		// redraw
		Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
	}

}
