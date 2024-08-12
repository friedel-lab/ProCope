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

import javax.swing.InputVerifier;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class SingleSetting {
	
	private SettingType type;
	private String label;
	private Object value;
	private InputVerifier verifier;
	
	public SingleSetting(SettingType type, String label, Object value, InputVerifier verifier) {
		// no verifiers for lists
		if (type == SettingType.LIST && verifier != null)
			throw new IllegalArgumentException("No input verifiers allowed for lists");
		this.type = type;
		this.label = label;
		this.value = value;
		this.verifier = verifier;
	}
	
	public SingleSetting(SettingType type, String label, Object value) {
		this(type,label,value,null);
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

	public SettingType getType() {
		return type;
	}
	
	public String getLabel() {
		return label;
	}
	
	public Object getValue() {
		return value;
	}
	
	public InputVerifier getVerifier() {
		return verifier;
	}
	
}
