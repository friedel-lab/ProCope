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
package procope.tools.userclasses;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * Represents a single parameter of a the constructor of a user-defined class.
 * Used internally by ProCope and thus not very well documented.
 * 
 * @see UserClusterer
 * @see UserScoresCalculator
 * @author Jan Krumsiek
 */
public class UserParameter {
	
	private String name;
	private UserDataType dataType;
	private Class<?> dataClass;
	private String defVal;

	/**
	 * Creates a new user parameter
	 */
	public UserParameter(String name, String defVal, UserDataType dataType) {
		this.name = name;
		this.defVal = defVal;
		this.dataType = dataType;
		
		switch (dataType) {
		case INTEGER:
			dataClass = int.class;
			break;
		case FLOAT:
			dataClass = float.class;
			break;
		case STRING:
			dataClass = String.class;
			break;
		}
	}
	
	/**
	 * Returns the name of this parameter
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the data type of this parameter
	 */
	public UserDataType getDataType() {
		return dataType;
	}

	/**
	 * Returns the data class of this parameter
	 */
	public Class<?> getDataClass() {
		return dataClass;
	}

	/**
	 * Returns the default value (string representation) of this parameter
	 */
	public String getDefaultValue() {
		return defVal;
	}

	
	// static part
	
	private static Map<String, UserDataType> dataTypes;
	
	static {
		dataTypes = new HashMap<String, UserDataType>();
		dataTypes.put("int", UserDataType.INTEGER);
		dataTypes.put("float", UserDataType.FLOAT);
		dataTypes.put("string", UserDataType.STRING);
	}
	
	/**
	 * Get a user parameter from a SAX parses Attributes object 
	 */
	public static UserParameter getFromSAX(Attributes atts) throws UserClassSpecException {
	
		String name = atts.getValue("name");
		String defVal = atts.getValue("defval");
		String type = atts.getValue("type");
		
		// none must be zero
		if (name == null || type == null)
			throw new UserClassSpecException("Parameters must have a 'name' and a 'type' attribute");
		
		// now parse the data type
		UserDataType dataType = dataTypes.get(type);
		if (dataType == null) {
			throw new UserClassSpecException("Invalid data type: '" + type + "', must be one of: " + dataTypes.keySet());
		}
		
		return new UserParameter(name, defVal, dataType);
	}

}
