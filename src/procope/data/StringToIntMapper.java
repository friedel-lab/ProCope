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
package procope.data;

import java.util.HashMap;

/**
 * Maps strings to integers. Same strings will always return the same integer
 * values, the mapping is case-insensitive.
 * 
 * @author Jan Krumsiek
 */

public class StringToIntMapper {
	
	private HashMap<String, Integer> stringtoint;
	private HashMap<Integer, String> inttostring;

	/**
	 * Creates a new string to int mapper.
	 */
	public StringToIntMapper() {
		stringtoint = new HashMap<String, Integer>();
		inttostring = new HashMap<Integer, String>();
	}
	
	/**
	 * Gets the integer ID for a given string
	 * 
	 * @param str string to be mapped
	 * @return integer ID for that string
	 */
	public int getIntID(String str) {
		
		str = str.toLowerCase().trim();
		
		// initialize hashmap if necessary
		if (stringtoint == null) {
			stringtoint = new HashMap<String, Integer>();
			inttostring = new HashMap<Integer, String>();
		}
		
		Integer test = stringtoint.get(str);
		if (test != null)
			return test.intValue();
		else {
			int newid = stringtoint.size();
			stringtoint.put(str, newid);
			inttostring.put(newid, str);
			
			return newid;
		}
	}
	
	/**
	 * Returns the string associated with an integer ID. 
	 * 
	 * @param protID
	 * @return associated string or {@code null} if this ID not used yet.
	 */
	public String getStringID(int protID) {
		return inttostring.get(protID);
	}
	
	/**
	 * Returns the number of registered strings in this mapper.
	 * 
	 * @return number of mapped strings
	 */
	public int getItemCount() {
		return stringtoint.size();
	}

}
