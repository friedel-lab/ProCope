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

import java.awt.event.ActionEvent;

import procope.userinterface.gui.GUIMain;
import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;

/**
 * Plugin classes recognized by Cytoscape. Contains simple initialization steps.
 * 
 * @author Jan Krumsiek
 */

public class ProCopePlugin extends CytoscapePlugin {
	
	
	@Override
	public String describe() {
		return "ProCope plugin";
	}
	
	public ProCopePlugin() {
		// add to plugins menu
		CytoscapeAction action = new Callback();
		action.setPreferredMenu("Plugins");
		Cytoscape.getDesktop().getCyMenus().addAction(action);
	}
	
	private class Callback extends CytoscapeAction {
		private static final long serialVersionUID = -8286009496184529754L;
		public Callback() {
			super("ProCope");
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			userClicked();
		}
	}
	
	private void userClicked() {
		new GUIMain(true);
	}

}
