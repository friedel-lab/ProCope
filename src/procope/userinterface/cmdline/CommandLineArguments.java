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
package procope.userinterface.cmdline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class CommandLineArguments {
	
	private HashMap<String,String> values;
	private List<String> freeArgs;
	
	/*
	public CommandLineArguments(String allowed, String[] args) throws InvalidArgumentsException {
		this(allowed.split(","), args);
	}
	*/
	
	public CommandLineArguments(String[] args, String ... allowed) throws InvalidArgumentsException {
		this(args, false, new String[0], allowed);
	}

	public CommandLineArguments(String[] args, boolean allowList, String[] switches, String[] allowed) throws InvalidArgumentsException {

		// initialize
		values = new HashMap<String,String>();
		freeArgs = new ArrayList<String>();
		Arrays.sort(allowed);
		Arrays.sort(switches);
		
		// parse command line arguments
		for (int i=0; i<args.length; i++) {
			String argument = args[i];


			if (! (argument.charAt(0) == '-')) {
				if (!allowList)
					// we only allow arguments starting with -
					throw new InvalidArgumentsException("Invalid argument: '" + argument 
							+ "'... argument names must start with -");
				else
					// free argument
					freeArgs.add(argument);
				continue;
			}
		
			// extract real key name
			String key = argument.substring(1).trim();
			
			// already in list?
			if (values.keySet().contains(key))
				throw new InvalidArgumentsException("Duplicate argument: '" + argument + "'");

			// valid?
			if (Arrays.binarySearch(allowed, key) < 0)
				throw new InvalidArgumentsException("Unknown argument: '" + key + "'"); 



			// VERY DIRT HARDCODE-HACK! :-(
			String value = "";
			if (!key.equals("synfirst")) { 
				// if the next argument does NOT start with a - => this is the value
			
				if (Arrays.binarySearch(switches, key) < 0) {
					if (i+1<args.length && !args[i+1].startsWith("-")) {
						value = args[i+1];
						i++;
					}
				}
			}
			// store in hashmap
			values.put(key, value);

	
		}
		
	}

	public boolean isSet(String switchName) {
		return values.get(switchName) != null;
	}
	
	public boolean hasValue(String switchName) {
		return values.get(switchName) != null && values.get(switchName).length() > 0;
	}
	
	public String getValue(String key) {
		return values.get(key);
	}
	
	public String requireValue(String key, String errMsg) {
		if (hasValue(key))
			return getValue(key);
		else {
			System.err.println(errMsg);
			System.exit(1);
			return null; // unreachable
		}
	}
	
	public void avoidSwitch(String key, String errMsg) {
		if (isSet(key) && !hasValue(key)) {
			System.err.println(errMsg);
			System.exit(1);
		}
	}

	public void checkNumericArguments(String ... names) throws InvalidArgumentsException {
		for (String name : names) {
			String value = values.get(name);
			if (value != null) {
				try {
					Float.parseFloat(value);
				} catch (NumberFormatException e) {
					throw new InvalidArgumentsException("Invalid numeric argument: '" + name + "' with value '" + value + "'"); 
				}
			}
		}
	}
	
	public void checkIntegerArguments(String ... names) throws InvalidArgumentsException {
		for (String name : names) {
			String value = values.get(name);
			if (value != null) {
				try {
					Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new InvalidArgumentsException("Invalid integer argument: '" + name + "' with value '" + value + "'"); 
				}
			}
		}
	}
	
	public List<String> getFreeArguments() {
		return freeArgs;
	}
}
