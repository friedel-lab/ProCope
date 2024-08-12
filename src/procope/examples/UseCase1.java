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

import procope.data.LocalizationData;
import procope.data.LocalizationDataReader;
import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.data.networks.CombinationRules;
import procope.data.networks.NetworkGenerator;
import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationDataReader;
import procope.evaluation.comparison.ComplexSetComparison;
import procope.evaluation.complexquality.Colocalization;
import procope.evaluation.complexquality.go.FunctionalSimilarities;
import procope.evaluation.complexquality.go.FunctionalSimilaritiesSchlicker;
import procope.evaluation.complexquality.go.GOAnnotationReader;
import procope.evaluation.complexquality.go.GOAnnotations;
import procope.evaluation.complexquality.go.GONetwork;
import procope.evaluation.complexquality.go.TermSimilarities;
import procope.evaluation.complexquality.go.TermSimilaritiesSchlicker;
import procope.methods.clustering.Clusterer;
import procope.methods.clustering.HierarchicalClusterer;
import procope.methods.clustering.HierarchicalLinkage;
import procope.methods.scores.ComplexScoreCalculator;
import procope.methods.scores.PECalculator;
import procope.methods.scores.ScoresCalculator;
import procope.methods.scores.SocioAffinityCalculator;
import procope.tools.namemapping.ProteinManager;


/**
 * This sample use case of the ProCope Java API demonstrates the basic process 
 * of complex set predicition. The following steps will be performed:
 * 
 * - loading of purification data
 * - score network generation
 * - clustering => complex set prediction
 * - complex set evaluation
 * 
 * Consult the ProCope online manual for detailed explanations of this code.
 * 
 * @author Jan Krumsiek
 *
 */
public class UseCase1 {
	
	public static void main(String[] args) {
		
		// load the purification data sets from Gavin et. al, 2006 and Krogan et. al, 2006
		System.out.println("Loading purifications...");
		PurificationData dataKrogan=null, dataGavin=null;
		try {
			dataKrogan = PurificationDataReader.readPurifications("data/purifications/krogan_raw.txt");
			dataGavin = PurificationDataReader.readPurifications("data/purifications/gavin_raw.txt");
		} catch (Exception e) {
			// something went wrong, output error message
			System.err.println("Could not load purification data:");
			System.err.println(e.getMessage());
			System.exit(1);
		} 
		
		// now merge the datasets to get a combined purification data set
		System.out.println("Merging datasets...");
		PurificationData dataMerged = dataKrogan.merge(dataGavin);
		
		// calculate socio affinity score network, we only want scores >= 0
		System.out.println("Calculating socio affinity scores...");
		ScoresCalculator calcSocios = new SocioAffinityCalculator(dataMerged);
		ProteinNetwork scoresSocios = NetworkGenerator.generateNetwork(calcSocios, 0f);
		
		// calculate Purification enrichment score network, we only want scores >= 0
		// according to the authors the combined network is the result of 0.5*PE(Krogan)+PE(Gavin)
		// we use settings for the r and pseudocount parameter from the original paper
		System.out.println("Calculating PE networks...");
		ProteinNetwork scoresPEGavin = NetworkGenerator.generateNetwork(
				new PECalculator(dataGavin, 0.62f, 10f), 0f);
		ProteinNetwork scoresPEKrogan = NetworkGenerator.generateNetwork(
				new PECalculator(dataKrogan, 0.51f, 20f), 0f);
		// now combine the networks, we need to multiply the Krogan network with 0.5 and then add both nets
		System.out.println("Merging PE networks...");
		scoresPEKrogan.scalarMultiplication(0.5f);
		CombinationRules combiRules = new CombinationRules(CombinationRules.CombinationType.MERGE);
		combiRules.setWeightMergePolicy(CombinationRules.WeightMergePolicy.ADD);
		ProteinNetwork scoresPE = scoresPEGavin.combineWith(scoresPEKrogan, combiRules);
		
		// now cluster the networks using hierarchical agglomerative clustering, average link
		// we use an arbitrary cutoff values for both networks
		System.out.println("Clustering...");
		Clusterer clustererSocios = new HierarchicalClusterer(HierarchicalLinkage.UPGMA, 2.7f);
		ComplexSet clusteringSocios = clustererSocios.cluster(scoresSocios);
		Clusterer clustererPE = new HierarchicalClusterer(HierarchicalLinkage.UPGMA, 0.5f);
		ComplexSet clusteringPE = clustererPE.cluster(scoresPE);
		
		// load the MIPS gold standard set of complexes
		ComplexSet setMIPS = null;
		try {
			setMIPS = ComplexSetReader.readComplexes("data/complexes/mips_complexes.txt");
		} catch (Exception e) {
			// something went wrong, output error message
			System.err.println("Could not load complex set:");
			System.err.println(e.getMessage());
			System.exit(1);
		} 
		
		// compare both clusterings against mips using the Brohee measure
		System.out.println("Socio clusters against MIPS: "
				+ ComplexSetComparison.broheeComparison(clusteringSocios, setMIPS));
		System.out.println("PE clusters against MIPS:    "
				+ ComplexSetComparison.broheeComparison(clusteringPE, setMIPS));
		
		// load localization data
		System.out.println("Calculating colocalization scores...");
		LocalizationData locData = null;
		try {
			locData = LocalizationDataReader.readLocalizationData("data/localization/huh_loc_070804.txt");
		} catch (Exception e) {
			// something went wrong, output error message
			System.err.println("Could not load localization data:");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// colocalization scores
		Colocalization coloc = new Colocalization(locData);
		System.out.println("Average colocalization score of socio clusters: "
				+ coloc.getAverageColocalizationScore(clusteringSocios, true,  true));
		System.out.println("Average colocalization score of PE clusters:    "
				+ coloc.getAverageColocalizationScore(clusteringPE, true, true ));
		
		// add name mappings as the GO annotation files (see below) contain IDs in the form
		// S000000099 whereas we need systematic names, e.g. YBL003C
		System.out.println("Loading name mappings...");
		try {
				ProteinManager.addNameMappings(NetworkReader.readNetwork("data/yeastmappings_080415.txt", true), true);
		} catch (Exception e) {
			// something went wrong, output error message
			System.err.println("Could not load name mapping network:");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// load gene ontology annotations for yeast and the GO network
		System.out.println("Loading GO network...");
		GOAnnotations goAnno=null;
		GONetwork goNet=null;
		try {
			goAnno = GOAnnotationReader.readAnnotations("data/go/gene_association_080504.sgd");
			goNet = new GONetwork("data/go/gene_ontology_edit_080504.obo", 
					GONetwork.Namespace.BIOLOGICAL_PROCESS,
					GONetwork.Relationships.BOTH);
		} catch (Exception e) {
			// something went wrong, output error message
			System.err.println("Could not load GO data:");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		// define a term similarity
		TermSimilarities termSim = new TermSimilaritiesSchlicker(goNet, goAnno,
				TermSimilaritiesSchlicker.TermSimilarityMeasure.RELEVANCE, true);
		// define a functional similarity
		FunctionalSimilarities funSim = new FunctionalSimilaritiesSchlicker(
				goNet, goAnno, termSim,
				FunctionalSimilaritiesSchlicker.FunctionalSimilarityMeasure.TOTALMAX);
		
		// print functional similarities for both clusterings
		System.out.println("Functional similarity of socio clusters: " + 
				ComplexScoreCalculator.averageComplexSetScore(funSim, clusteringSocios, true, true));
		System.out.println("Functional similarity of PE clusters:    " + 
				ComplexScoreCalculator.averageComplexSetScore(funSim, clusteringPE, true, true));
		
		
	}

}
