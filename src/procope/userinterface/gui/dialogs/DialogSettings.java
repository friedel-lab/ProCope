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
package procope.userinterface.gui.dialogs;


import java.util.ArrayList;

import javax.swing.InputVerifier;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class DialogSettings {

	private ArrayList<SingleSetting> settings = new ArrayList<SingleSetting>();
	private String title;
	private String helpText;
	
	public DialogSettings(String title) {
		this.title = title;
	}
	
	public void addStringParameter(String label, String def) {
		settings.add(new SingleSetting(SettingType.STRING, label, def));
	}
	
	public void addStringParameter(String label, String def, InputVerifier verifier) {
		settings.add(new SingleSetting(SettingType.STRING, label, def, verifier));
	}

	
	public void addIntegerParameter(String label, Integer def) {
		addIntegerParameter(label, def, new IntVerifier());
	}
	
	public void addIntegerParameter(String label, Integer def, InputVerifier verifier) {
		settings.add(new SingleSetting(SettingType.INTEGER, label, def.toString(), verifier));
	}
	
	public void addFloatParameter(String label, Float def, InputVerifier verifier) {
		settings.add(new SingleSetting(SettingType.FLOAT, label, def, verifier));
	}
	
	public void addFloatParameter(String label, Float def) {
		addFloatParameter(label, def, new FloatVerifier());
	}
	
	public void addListParameter(String label, String ... options) {
		settings.add(new SingleSetting(SettingType.LIST, label, options));
	}
	
	public void addRadioOptions(String label, String ... options) {
		settings.add(new SingleSetting(SettingType.RADIO, label, options));
	}
	
	public void addCheckParameter(String label, boolean def) {	
		settings.add(new SingleSetting(SettingType.CHECK, label, def));
	}
	
	public ArrayList<SingleSetting> getSettings() {
		return settings;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void addInfoLabel(String label) {
		settings.add(new SingleSetting(SettingType.INFOLABEL, label, null));
	}
	
	public void setDefaultValue(int item, Object def) {
		settings.get(item).setValue(def);
	}
	
	public void addFileParameter(String label) {
		addFileParameter(label, null);
	}
	
	public void addFileParameter(String label, String def) {
		settings.add(new SingleSetting(SettingType.FILE, label, def));
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}
	
	public String getHelpText() {
		return helpText;
	}
}
