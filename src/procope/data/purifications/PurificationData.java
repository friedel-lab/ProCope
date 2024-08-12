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
package procope.data.purifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import procope.data.ProteinSet;
import procope.data.networks.ProteinNetwork;


/**
 * A set of protein purification experiments where each experiment constists of
 * a bait protein along with a list of prey proteins it purified. 
 * 
 * <p>The score calculation methods in this library are based on such 
 * collections of purification experiments and require a {@code 
 * PurificationData} object as an input.
 * 
 * <p>This class is a simple container method with basic accession and some
 * data manipulation methods.
 * 
 * @author Jan Krumsiek
 * @see    procope.data.purifications.PurificationExperiment
 * @see    procope.data.purifications.PurificationDataReader
 * @see    procope.methods.scores.SocioAffinityCalculator
 * @see    procope.methods.scores.PECalculator
 * @see    procope.methods.scores.HartCalculator
 * @see    procope.methods.scores.bootstrap.Bootstrap 
 */

public class PurificationData implements ProteinSet, Iterable<PurificationExperiment> {
	
	private List<PurificationExperiment> experiments;
	
	private int totalPreys=0;

	/**
	 * Returns the list of PurificationExperiment objects backing this dataset.
	 * <b>Note:</b> This returns the original list, changes will also alter the
	 * {@code PurificationData} object.
	 * 
	 * @return list of purifcation experiments in this dataset
	 */
	public List<PurificationExperiment> getExperiments() {
		return experiments;
	}

	/**
	 * Creates an empty purification dataset
	 */
	public PurificationData() {
		experiments = new ArrayList<PurificationExperiment>();		
	}
	
	/**
	 * Adds a specified experiment to the dataset.
	 * 
	 * @param experiment the experiment to be added
	 */
	public void addExperiment(PurificationExperiment experiment) {
		experiments.add(experiment);
		totalPreys += experiment.getPreys().size();
	}
	
	/**
	 * Adds a list of purification experiments to the dataset
	 * 
	 * @param experiments list of experiments to be added
	 */
	public void addExperiments(Collection<PurificationExperiment> experiments) {
		this.experiments.addAll(experiments);
	}
	
	/**
	 * Returns the number of experiments contained in this dataset
	 * 
	 * @return number of experiments in the dataset
	 */
	public int getNumberOfExperiments() {
		return experiments.size();
	}
	
	/**
	 * Calculates a directed network containing all bait-prey interactions.
	 * That means that for each bait this network will contain one directed 
	 * edge to each of its preys. The networks edges get a standard weight 
	 * of 1.0.   
	 * 
	 * @return directed network containing all bait-prey interactions
	 */
	public ProteinNetwork getBaitPreyInteractions() {
		
		ProteinNetwork result = new ProteinNetwork(true);
		// extract all bait-prey interactions
		for (PurificationExperiment exp : experiments) {
			int bait = exp.getBait();
			for (int prey : exp.getPreys()) {
				result.setEdge(bait, prey);
			}
		}
		
		// convert to proper int array
		return result;
	}
	
	/**
	 * Returns the set of proteins which are contained in this purification
	 * data set
	 */
	public Set<Integer> getProteins() {
		// create hashset from proteins of all member experiments
		HashSet<Integer> proteins = new HashSet<Integer>();
		for (PurificationExperiment exp: experiments)
			proteins.addAll(exp.getProteins());
		return proteins;
	}

	/**
	 * Returns the total number of preys in this dataset. That is, this number
	 * represents the total number of bait-prey interactions in the set.
	 * 
	 * @return total number of preys in the dataset
	 */
	public int getPreyCount() {
		return totalPreys;
	}

	/**
	 * Returns an iterator over all purification experiments in this dataset.
	 */
	public Iterator<PurificationExperiment> iterator() {
		return experiments.iterator();
	}

	/**
	 * Merges two purification datasets. This is achieved by simple merging
	 * the list of purification experiments. Duplicates are not handled.
	 * 
	 * @param mergeWith other purification data set this one will be merged with
	 * @return a new {@code PurificationData} object containing the 
	 *         purification experiments of both source datasets
	 */
	public PurificationData merge(PurificationData mergeWith) {
		PurificationData data = new PurificationData();
		data.addExperiments(this.getExperiments());
		data.addExperiments(mergeWith.getExperiments());
		return data;
	}

}
