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
package procope.userinterface.gui;

import java.awt.Frame;
import java.util.ArrayList;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;
import procope.evaluation.complexquality.Colocalization;
import procope.methods.scores.ComplexScoreCalculator;
import procope.methods.scores.ScoresCalculator;
import procope.userinterface.gui.dialogs.DialogSettings;
import procope.userinterface.gui.dialogs.ParameterDialog;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

public class GUIComplexScorer {

	int mode=-1;
	private ProteinNetwork network = null;
	private Colocalization coloc = null;
	private boolean colocPPV;
	private ScoresCalculator scoresCalc = null;
	private boolean ignoreMissing;
	private boolean weighted;
	
	private ComplexSet complexSet;
	private ArrayList<Float> scores;
	private Frame parent;
	private String title;
	
	// will use networks
	public GUIComplexScorer(ComplexSet complexSet, GUIMain parent, String title) {
		mode = 0;
		this.complexSet = complexSet;
		this.parent = parent;
		this.title = title;
	}

	// colocalization
	public GUIComplexScorer(ComplexSet complexSet, Colocalization coloc, boolean PPV, Frame parent, String title) {
		mode = 1;
		this.complexSet = complexSet;
		this.coloc = coloc;
		this.colocPPV = PPV;
		this.title = title;
	}

	// a scorer
	public GUIComplexScorer(ComplexSet complexSet, ScoresCalculator scoresCalc, Frame parent, String title) {
		mode = 2;
		this.complexSet = complexSet;
		this.scoresCalc = scoresCalc;
		this.title = title;
	}
	
	public void updateScores() {
		
		boolean allZero=true;
		scores = new ArrayList<Float>();
		// iterate over all complexes
		for (Complex complex : complexSet) {
			float score = calcComplexScore(complex); 
			scores.add(score);
			if (score != 0 && !Float.isNaN(score)) allZero = false;
		}
		
		// all zero and go or coloc? => output information
		if (allZero) {
			GUICommons.info("All scores are zero.\nMaybe you need to load some name mappings?\n" +
					"Note that name mappings must be loaded before the actual data files.");
			
		}
	}
	
	public float getAverageComplexesScore(ArrayList<Integer> indices) {
		float sum=0, count=0;
		// iterate over all indices
		for (int index : indices) {
			float score = scores.get(index);
			if ( Float.isNaN(score))
				score = 0;
			if (!ignoreMissing || score != 0){
				if (weighted) {
					int size = complexSet.getComplex(index).size();
					sum += score * size;
					count += size;
				} else {
					sum += score;
					count += 1;
				}
			}
		}
		
		return sum/count;
	}

	public float getScore(int index) {
		return scores.get(index);
	}

	
	private float calcComplexScore(Complex complex) {
		switch (mode) {
		case 0:
			// network
			return ComplexScoreCalculator.averageComplexScore(network, complex, ignoreMissing);
		case 1:
			// coloc
			if (!colocPPV)
				return coloc.getColocalizationScore(complex);
			else
				return coloc.getPPV(complex);
		case 2:
			// any scores calculator
			return ComplexScoreCalculator.averageComplexScore(scoresCalc, complex, ignoreMissing);

		default:
			// will never happen
			return Float.NaN;
		}
	}
	
	public void setWeighted(boolean weighted) {
		this.weighted = weighted;
	}

	public void setIgnoreMissing(boolean ignoreMissing) {
		this.ignoreMissing = ignoreMissing;
	}
	
	public boolean showSettings() {
		
		
		final String overall=
			"'Weighted overal sum' means that for average calculation, " +
			"each complex score is weighted by the size of the complex.\n\n";
		switch (mode) {
		case 0:
			// network
			GUIMain gui = (GUIMain)parent;
			// as the user for the score settings
			DialogSettings settings = new DialogSettings("Complex scores");
			settings.addInfoLabel("Complex scores will be calculated as the average edge score " +
					"between all protein members using a given scores network.");
			String[] netNames = gui.getNetworkNames().toArray(new String[0]);
			if (netNames.length == 0) {
				GUICommons.warning("No networks loaded!");
				return false;
			}
			netNames[0] = "@" + netNames[0];
			settings.addListParameter("Network:", netNames);
			settings.addCheckParameter("Calculate weighted overall sum?", true);
			settings.addCheckParameter("Ignore missing scores?", false);
			settings.setHelpText(overall +
					"When ignoring missing scores, functional similarities of 0 (which " +
					"occur due to missing GO term associations) will not be considered.");
			Object[] result = ParameterDialog.showDialog(gui, settings);
			if (result != null) {
				// settings
				weighted = (Boolean)result[1];
				ignoreMissing = (Boolean)result[2];
				// get the network
				network = gui.getNetwork((Integer)result[0]);
				updateScores();
				return true;
			} else
				return false;
			
		case 1:
		case 2:
			// coloc or scores calculator
			// only ask for weighted mean and missing scores
			DialogSettings settings2 = new DialogSettings(title);
			settings2.addCheckParameter("Calculate weighted overall sum?", weighted);
			settings2.addCheckParameter("Ignore missing scores?", ignoreMissing);
			settings2.setHelpText(overall +
					"When ignoring missing scores, colocalization scores of 0 (which " +
					"occur due to missing localization data or simply missing edges in a network)" +
					" will not be considered.");
			Object[] result2 = ParameterDialog.showDialog(parent, settings2);
			
			if (result2 != null) {
				weighted = (Boolean)result2[0];
				ignoreMissing = (Boolean)result2[1];
				updateScores();
				return true;
			} else
				return false;
			
		default:
			// won't happen
			return false;
		}
		

	}
	
	public static void main(String[] args) {
		
		GUIComplexScorer x = new GUIComplexScorer(null,null,null);
		x.showSettings();
		x.showSettings();
		x.showSettings();
	}
	
}
