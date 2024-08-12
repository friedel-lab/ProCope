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
package procope.evaluation.complexquality.go;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import procope.tools.namemapping.ProteinManager;

/**
 * Implements the calculation of functional similarites between proteins as 
 * described in
 * 
 * <p>Schlicker, A.; Domingues, F. S.; Rahnenf&uuml;hrer, J. & Lengauer, T.<br/>
 * A new measure for functional similarity of gene products based on Gene Ontology.<br/>
 * <i>BMC Bioinformatics</i>, 2006, 7, 302<br/>
 * Pubmed: 16776819
 * 
 * @author Jan Krumsiek
 *
 */

public class FunctionalSimilaritiesSchlicker extends FunctionalSimilarities {
	
	
	private GOAnnotations annotations;
	private TermSimilarities termSim;
	private FunctionalSimilarityMeasure goScoreMethod;
	private GONetwork gonet;

	/**
	 * Creates a new functional similarity score calculator.
	 * 
	 * @param gonet GO network to be used
	 * @param annotations annotations to be used
	 * @param termSim term similarity calculator
	 * @param funSim functional similarity measure
	 */
	public FunctionalSimilaritiesSchlicker(GONetwork gonet, GOAnnotations annotations, 
			TermSimilarities termSim, FunctionalSimilarityMeasure funSim) {
		
		this.termSim = termSim;
		this.annotations = annotations;
		this.goScoreMethod = funSim;
		this.gonet = gonet;
		
		// delete non-ontology terms
		for (int i=1; i<ProteinManager.getProteinCount(); i++) {
			Collection<String> goTerms = annotations.getGOTerms(i);
			if (goTerms != null)
				deleteNonontologyTerms(goTerms);
		}
		
	}
	
	/**
	 * Calculates the functional similarity of two given proteins.
	 * @return functional similarity score or 0 if at least one of the proteins
	 *         has no annotated GO terms
	 */
	public float getScore(int protein1, int protein2) {
		 
		Collection<String> goTerms1 = annotations.getGOTerms(protein1);
		Collection<String> goTerms2 = annotations.getGOTerms(protein2);
		
		if (goTerms1 == null || goTerms1.size() == 0) { 
			return 0; 
		}
		if (goTerms2 == null || goTerms2.size() == 0) { 
			return 0; 
		}
	
		/*
		// remove from both lists those terms which are not contained
		// in the current ontology
		deleteNonontologyTerms(goTerms1);
		deleteNonontologyTerms(goTerms2);
		*/
		
		// convert to arrays for convenience
		String[] arrGoTerms1 = goTerms1.toArray(new String[0]);
		String[] arrGoTerms2 = goTerms2.toArray(new String[0]);
		
		int terms1 = goTerms1.size();
		int terms2 = goTerms2.size();
		
		// create & fill matrix
		float[][] matrix = new float[terms1][terms2];
		float totalMatrixSum = 0;
		float maxMatrixValue = Float.NEGATIVE_INFINITY;
		for (int i=0; i<terms1; i++) {
			for (int j=0; j<terms2; j++) {
				// write term similarity to matrix
				float sim = termSim.calculateSimilarity(arrGoTerms1[i], arrGoTerms2[j]);
				//System.out.println("go sim score " + arrGoTerms1[i] +"," +arrGoTerms2[j]+ "\t" + sim  );
				totalMatrixSum += sim;
				matrix[i][j] = sim;
				if (sim > maxMatrixValue) maxMatrixValue = sim;
			}
		}
		
		// use row score and col score?
		if (goScoreMethod == FunctionalSimilarityMeasure.COLROW_MAX || goScoreMethod == FunctionalSimilarityMeasure.COLROW_AVERAGE) 
                {
			// calculate row score of matrix
			float rowScore=0;
			for (int i=0; i<terms1; i++) {
				// calc max over this row
				float max = Float.NEGATIVE_INFINITY;
				for (int j=0; j<terms2; j++) {
					if (matrix[i][j] > max) max = matrix[i][j];
				}
				rowScore += max;
			}
			// calc avg to derive score
			rowScore /= terms1;
			
			// calculate col score of matrix
			float colScore=0;
			for (int j=0; j<terms2; j++) {		
				// calc max over this row
				float max = Float.NEGATIVE_INFINITY;
				for (int i=0; i<terms1; i++) {
					if (matrix[i][j] > max) max = matrix[i][j];
				}
				colScore += max;
			}
			// calc avg to derive score
			colScore /= terms2;
			
			if (goScoreMethod == FunctionalSimilarityMeasure.COLROW_MAX)
				return Math.max(rowScore, colScore);
			else // (goScoreMethod == Constants.GO_SCORE_AVERAGE)
				return (rowScore+colScore)/2;
			
		} else if (goScoreMethod == FunctionalSimilarityMeasure.LORD) {
			// lord's scoring method => get average over whole matrix
			return totalMatrixSum / (float)(terms1*terms2);
		
		} else if (goScoreMethod == FunctionalSimilarityMeasure.TOTALMAX) {
			// lord's scoring method => get average over whole matrix
			return maxMatrixValue;
			
		} else
			// not a valid method
			return Float.NaN;
	}

	/**
	 * Delete terms not contaned in the current network
	 */
	private void deleteNonontologyTerms(Collection<String> goTerms) {
		ArrayList<String> toRemove = new ArrayList<String>();
		// iterate over all terms and check which of these are not contained
		// in the current GO network (these are not in the current ontology)
		for (String term : goTerms) {
			if (gonet.goterms.get(term) == null)
				toRemove.add(term);
		}
		goTerms.removeAll(toRemove);
	}

	
	/**
	 * Functional semilarity measure. Consult 
	 * {@link TermSimilaritiesSchlicker the paper} for more information.
	 */
	public enum FunctionalSimilarityMeasure {
		COLROW_MAX, 
		COLROW_AVERAGE, 
		LORD, 
		TOTALMAX
	}


	/**
	 * Returns the set of proteins contained in the annotations used
	 * in this scores calculator.
	 */
	public Set<Integer> getProteins() {
		return annotations.getProteins();
	}

}
