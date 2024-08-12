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

import procope.data.LocalizationData;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class LocalizationDataSetting {
	
	public String label;
	public LocalizationData data;
	public String file;

	public LocalizationDataSetting(String label, LocalizationData data, String file) {
		this.label = label;
		this.data = data;
		this.file = file;
	}

}
