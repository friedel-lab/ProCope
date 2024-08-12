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
package procope.evaluation.comparison;

/**
 * Contains similarity comparison results of two complex sets after
 * 
 * <p>Broh&eacute;e, S. & van Helden, J.<br/>
 * Evaluation of clustering algorithms for protein-protein interaction networks.<br/> 
 * <i>BMC Bioinformatics</i>, 2006, 7, 488<br/>
 * Pubmed: 17087821
 * 
 * <p>This measure is assymetrical and provides a similarity measure of a given 
 * candidate complex set with respect to a reference complex set.
 * <p>The <i>sensitivity</i> describes how much of the reference complex sets
 * are also contained in the candidate complex set, where as the <i>positive
 * predictive value</i> (PPV) is a measure how much of the prediction in the 
 * candidate set is correct. The <i>accuracy</i> is then the geometric mean
 * of sensitivity and PPV.
 * <p>All three measures range between a minimum value of 0.0 and a maximum 
 * value of 1.0. Note that for complex sets where proteins occur in multiple 
 * complexes it might not be possible for the PPV to reach the maximum of 1.0.
 * <p>This class is not instantiatable from outside of the package.
 * 
 * @author Jan Krumsiek
 *
 */

public class BroheeSimilarity {
	
	private float sens, ppv, acc;

	/**
	 * Creates a new similarity object.
	 * Accuracy is calculated as the geometric mean of sensitivity and PPV.
	 * 
	 * @param sens sensitivity
	 * @param PPV positive predictive value
	 */
	public BroheeSimilarity(float sens, float PPV) {
		this.sens = sens;
		this.ppv = PPV;
		this.acc = (float)Math.sqrt(sens*PPV);
	}
	
	/**
	 * Returns the sensitivity of the candidate complex set regarding the
	 * reference complex set.
	 * @return the sensitivity
	 */
	public float getSensitivity() {
		return sens;
	}
	
	/**
	 * Returns the positive predictive value (PPV) of the candidate set
	 * regarding the reference complex set.
	 * @return the PPV
	 */
	public float getPPV() {
		return ppv;
	}
	
	/**
	 * Return the accuracy of the candidate set regarding the reference
	 * complex set. Accuracy is defined as the geometric mean of sensitivity
	 * and PPV.
	 * @return the accuracy
	 */
	public float getAccuracy() {
		return acc;
	}
	
	/**
	 * Returns a string representation of the comparison results
	 */
	@Override
	public String toString() {
		return "Sensitivity: " + sens + ", PPV: " + ppv + ", Accuracy: " + acc;
	}
	

}
