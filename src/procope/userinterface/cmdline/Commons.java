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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.tools.namemapping.ProteinManager;
import procope.tools.userclasses.UserParameter;

/**
 * This file is part of the ProCope command line tools
 * 
 * @author Jan Krumsiek
 */
public class Commons {
	
	public static String formatList(String[] list) {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<list.length; i++) {
			buffer.append(list[i]);
			if (i<list.length-1) buffer.append(", ");
		}
		return buffer.toString();
	}

	public static String[] getList(String ... list) {
		return list;
	}

	public static boolean inList(String toCheck, String[] list) {
		for (int i=0; i<list.length; i++) {
			if (toCheck.equals(list[i]))
				return true;
		}
		return false;
	}
	
	// checks -o and -oz
	public static OutputStream getOutput(CommandLineArguments arguments) {
		
		OutputStream result=null;
		
		if (arguments.isSet("oz") && !arguments.isSet("o")) {
			System.err.println("Cannot output GZIPed data to standard out. -oz requires -o");
			System.exit(1);
		}
			
		if (arguments.isSet("o")) {
			if (!arguments.hasValue("o")) {
				System.err.println("When using -o you must specify an output file name.");
				System.exit(1);
			}
			else {
				try {
					result = new FileOutputStream(arguments.getValue("o"));
					if (arguments.isSet("oz"))
						result = new GZIPOutputStream(result);
					
				} catch (IOException e) {
					System.err.println("Error while opening output file:");
					System.err.println(e.getMessage());
					System.exit(2);
				}				
			}
		} else
			result = System.out;
		
		return result;
	}
	
	public static InputStream getInputStream(String file) throws IOException {
		if (procope.tools.Tools.isGZIPed(file))
			return new GZIPInputStream(new FileInputStream(file));
		else
			return new FileInputStream(file);
	}
	
	public static void closeOutput(OutputStream outstream) {
		// close output
		try {
			if (outstream != System.out)
				outstream.close();
		} catch (IOException e) {
			System.err.println("Error while closing output file:");
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
	
	public static boolean isNumeric(String str) {
		try {
			Float.parseFloat(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	
	public static void checkForMappings(CommandLineArguments arguments) {
		
		if (arguments.isSet("synfirst") && !arguments.isSet("namemap"))
			System.err.println("Warning: -synfirst has no effect without -namemap");
		
		arguments.avoidSwitch("namemap", "When using -namemap you have to specify a name mapping file");
		if (arguments.isSet("namemap")) {
			String mapFile = arguments.getValue("namemap");
			// try to load the network
			ProteinNetwork net=null;
			try {
				net = NetworkReader.readNetwork(mapFile, true); 
			} catch (Exception e) {
				System.err.println("Could not read name mapping file:");
				System.err.println(e.getMessage());
				System.exit(2);
			}
			// add to name mapper
			ProteinManager.addNameMappings(net, !arguments.isSet("synfirst"));
			
		}
	}
	
	public static Object inputUserParameter(UserParameter para) {

		while (true) {
			String defVal = para.getDefaultValue();
			String defString = defVal != null ? " [def: " + para.getDefaultValue() +"]": "";
			
			System.out.print(para.getName()+defString+": ");
			System.out.flush();
			Scanner scanner = new Scanner(System.in);
			String input = scanner.nextLine();
			
			if (input.length() == 0 && defVal != null)
				input = defVal;				

			// verify the input type
			switch (para.getDataType()) {
			case INTEGER:
				try {
					return Integer.parseInt(input);
				} catch (NumberFormatException e) {
					System.out.println("Please enter an integer number.");
				}
				break;
			case FLOAT:
				try {
					return Float.parseFloat(input);
				} catch (NumberFormatException e) {
					System.out.println("Please enter an decimal number.");
				}
				break;
			case STRING:
				return input;
			default:
				// never gonna happen
				return null;
			}

		}

	}

}
