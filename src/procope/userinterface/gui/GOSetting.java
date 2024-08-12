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

import java.util.ArrayList;
import java.util.Map;

import procope.evaluation.complexquality.go.FunctionalSimilarities;
import procope.evaluation.complexquality.go.GOAnnotations;
import procope.evaluation.complexquality.go.GONetwork;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

class GOSetting {

	protected FunctionalSimilarities funSim;
	protected Map<String,String> parameters;
	protected ArrayList<String> strParameters;
	protected GONetwork gonet;
	protected GOAnnotations annos;

	public GOSetting(GONetwork gonet, GOAnnotations annos,
			FunctionalSimilarities funSim, Map<String, String> parameters,
			ArrayList<String> strParameters) {
		this.gonet = gonet;
		this.annos = annos;
		this.funSim = funSim;
		this.parameters = parameters;
		this.strParameters = strParameters;
	}
	
}
