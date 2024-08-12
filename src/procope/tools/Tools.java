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
package procope.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


import procope.tools.math.MTRandom;
import procope.userinterface.gui.GUICommons;


/**
 * Contains helper methods used throughout the whole library.
 * <p>This class is not instantiatable.
 * 
 * @author Jan Krumsiek
 */

public class Tools {

	/**
	 * Default minimum overlap required for two complexes to be mapped to each other
	 */
	public static final int MINOVERLAP = 2;

	/**
	 * Globally used random number generator, uses a 
	 * {@link MTRandom Mersenne twister}
	 */
	public static Random random = new MTRandom(System.currentTimeMillis());

	private static final String BADTYPE = "Annotations must be of type String, " +
			"Integer, Float or java.util.List. Invalid type: ";

	/**
	 * An empty, immutable map of annotations, i.e. a String =&#62; Object map
	 */
	public static final Map<String, Object> EMPTY_ANNOTATION_MAP =
		new AbstractMap<String, Object>() {
		public boolean containsKey(Object key) {
			return false;
		}
		public boolean containsValue(Object value) {
			return false;
		}
		public Set<java.util.Map.Entry<String, Object>> entrySet() {
			return Collections.emptySet();
		}
		public Object get(Object key) {
			return null;
		}
		public boolean isEmpty() {
			return true;
		}
		public Set<String> keySet() {
			return Collections.<String>emptySet();
		}

		public int size() {
			return 0;
		}
		public Collection<Object> values() {
			return Collections.<Object>emptySet();
		}

	};

	/**
	 * An empty, immutable String =&#62; String map
	 */
	public static final Map<String, String> EMPTY_STRING_MAP =
		new AbstractMap<String, String>() {
		public boolean containsKey(Object key) {
			return false;
		}
		public boolean containsValue(Object value) {
			return false;
		}
		public Set<java.util.Map.Entry<String, String>> entrySet() {
			return Collections.emptySet();
		}
		public String get(Object key) {
			return null;
		}
		public boolean isEmpty() {
			return true;
		}
		public Set<String> keySet() {
			return Collections.<String>emptySet();
		}

		public int size() {
			return 0;
		}
		public Collection<String> values() {
			return Collections.<String>emptySet();
		}

	};

	/**
	 * Name of this library
	 */
	public static final String LIBRARY_NAME = "ProCope";
	/**
	 * Current version of this library
	 */
	public static final String VERSION = "1.2";
	/**
	 * Full program string assembled from libary name and version
	 */
	public static final String FULL_LIB_NAME = LIBRARY_NAME + " " + VERSION;
	
	/**
	 * Homepage of the library
	 */
	public static final String HOMEPAGE = "http://www.bio.ifi.lmu.de/Complexes/ProCope/";
	public static final String HOMEPAGE_DOCS = "http://www.bio.ifi.lmu.de/Complexes/ProCope/doc/";
	
	public static final String CONFIGPATH = System.getProperty("user.home")  + File.separator +".procope" + File.separator; 
	public static final String CONFIGFILE= CONFIGPATH + "config.xml";
	public static final String CLUSTERERSFILE = CONFIGPATH + "clusterers.xml";
	public static final String CALCULATORSFILE = CONFIGPATH + "scorecalc.xml";

	public final static int GZIP_MAGIC = 0x8b1f;

	

	// avoid instantiation
	private Tools() {
	}


	/**
	 * Extracts the file name from a full path
	 * 
	 * @param fullPath full path
	 * @return file name
	 */
	public static String extractFilename(String fullPath) {
		int index = fullPath.lastIndexOf(File.separator)+1;
		if (index >= 0)
			return fullPath.substring(index);
		else
			return fullPath;
	}

	/**
	 * Extract the base file name (without extension) from a full path
	 * @param fullPath full path
	 * @return base file name
	 */
	public static String extractBaseFilename(String fullPath) {
		// extract filename first
		String fileName = extractFilename(fullPath);
		// cut of extension if there is any
		int index = fileName.lastIndexOf(".");
		if (index >= 0)
			return fileName.substring(0, index);
		else
			return fileName;
	}

	/**
	 * Returns the minimum of two {@link Comparable} objects
	 * 
	 * @param first first object
	 * @param second second object
	 * @return <i>smaller</i> object according to their natural ordering 
	 */
	@SuppressWarnings("unchecked")
	public static Comparable min(Comparable first, Comparable second) {
		if (first.compareTo(second) < 0)
			return first;
		else
			return second;
	}

	/**
	 * Returns the maximum of two {@link Comparable} objects
	 * 
	 * @param first first object
	 * @param second second object
	 * @return <i>larger</i> object according to their natural ordering 
	 */
	@SuppressWarnings("unchecked")
	public static Comparable max(Comparable first, Comparable second) {
		if (first.compareTo(second) >= 0)
			return first;
		else
			return second;
	}

	/**
	 * Returns the maximum of a given integer collection.
	 * 
	 * @param col integer collection
	 * @return maximum value of the given collection
	 */
	public static int findMax(Collection<Integer> col) {

		if (col.isEmpty())
			throw new IllegalArgumentException("Can only findMax() of non-empty collections");

		int max = Integer.MIN_VALUE;
		for (Integer val : col) {
			if (val > max) max=val;
		}
		return max;
	}

	/**
	 * Returns an unused temporary file name. Uses the system's temp directory.
	 * 
	 * @return path to temporary file
	 */
	public static String getTempFilename() {
		String file = null;
		do {
			file = System.getProperty("java.io.tmpdir") + File.separator
			+"c" +  System.currentTimeMillis() + random.nextInt(1000000);
		} while (new File(file).exists());
		return file;
	}

	/**
	 * Determines whether a file contains a GZIP header
	 * 
	 * @param file path to file
	 * @return if the file is GZIPed
	 */
	public static boolean isGZIPed(String file) {
		FileInputStream input;
		try {
			input = new FileInputStream(file);
			float head = GUICommons.readUShort(input);
			input.close();

			return head == GZIP_MAGIC;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Checks whether a given object is an {@link Integer}, a {@link Float} or
	 * a {@link java.util.List}. Throws an exception if not.
	 * 
	 * @param value object whose type will be checked
	 * @throws ProCopeException if the object is not an Integer, Float
	 *                                 or List
	 */
	@SuppressWarnings("unchecked")
	public static void verifyAnnotationType(Object value) throws ProCopeException {

		if (!(value instanceof String) && !(value instanceof Integer) && !(value instanceof Float)) {
			// might still be a list
			if (!(value instanceof java.util.List)) {
				throw new ProCopeException(Tools.BADTYPE + value.getClass().getCanonicalName());			
			} else {
				// check contents of that list
				for (Object obj : (java.util.List)value) {
					if (!(obj instanceof String) && !(obj instanceof Integer) && !(obj instanceof Float)) 
						throw new ProCopeException(Tools.BADTYPE + value.getClass().getCanonicalName());		
				}
			}
		}
	}



	/**
	 * Replaces invalid characters from strings. Needed for storing annotations to files.
	 * 
	 * @param toEscape string to be escaped
	 * @return escaped string
	 */
	private static String escapeAnnotationString(String toEscape) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < toEscape.length(); i++) {
			char c = toEscape.charAt(i);
			switch (c) {
			case '%':
				sb.append('%');
				sb.append('p');
				break;
			case '=':
				sb.append('%');
				sb.append('e');
				break;
			case '[':
				sb.append('%');
				sb.append('b');
				break;
			case ']':
				sb.append('%');
				sb.append('c');
				break;
			case ',':
				sb.append('%');
				sb.append('s');
				break;
			case ';':
				sb.append('%');
				sb.append('t');
				break;
			case '\n':
				sb.append('%');
				sb.append('n');
				break;
			default:
				sb.append(c);
			}
		}
	    return sb.toString();
	}
	
	/**
	 * Writes a given annotations set to a writer.
	 * 
	 * @param annotations annotations to be written
	 * @param writer the writer
	 */
	public static void writeAnnotations(Map<String, Object> annotations, PrintWriter writer) {
		int numAnnotations = annotations.size();
		if (numAnnotations > 0) {
			int anno=0;
			for (String key : annotations.keySet()) {
				Object value = annotations.get(key);
				writer.print(Tools.escapeAnnotationString(key)+"=");
				if (!(value instanceof List)) {
					// string, int or float
					if (value instanceof String)
						writer.print(Tools.escapeAnnotationString((String)value));
					else
						writer.print(value);
				} else {
					// write out list
					List<?> lst = (List<?>)value;
					int items = lst.size();
					int num=0;
					writer.print("[");
					for (Object item : lst) {
						if (item instanceof String)
							writer.print(Tools.escapeAnnotationString((String)item));
						else
							writer.print(item);
						if (num < items-1) writer.print(",");
						num++;
					}
					writer.print("]");
				}
				if (anno < numAnnotations -1) writer.print(";");
				anno++;

			}
		}
	}
	
	/**
	 * Parse annotations from a string coming from a network or annotations file.
	 * 
	 * @param parse string to be parsed
	 * @return annotations parsed from that string
	 */
	public static Map<String, Object> parseAnnotations(String parse) {
		String[] annoPairs = parse.split(";");
		Map<String, Object> annotations = new HashMap<String, Object>();
		for (String annoPair : annoPairs) {
			// get key and value
			String[] keyValue = annoPair.split("=");
			String key = deescapeString(keyValue[0]);
			// list?
			if (keyValue[1].charAt(0) != '[') {
				// no list, parse the value and add to list
				annotations.put(key, parseValue(deescapeString(keyValue[1])));
			} else {
				// its a list
				String[] listItems = keyValue[1].substring(1,keyValue[1].length()-1).split(",");
				if (listItems.length > 0) {
					// create the list
					List<Object> list = new ArrayList<Object>();
					for (int i=0; i<listItems.length; i++)
						list.add(parseValue(deescapeString(listItems[i])));
					// add to map
					annotations.put(key, list);
				}
			}
		}
		return annotations;
	}
	
	/**
	 * Try to parse an integer or a float from a given string
	 */
	private static Object parseValue(String value) {

		// try int first
		try {
			return  Integer.parseInt(value);
		} catch (NumberFormatException e) {}

		// try float 
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {}

		// treat as string
		return value;

	}
	
	/**
	 * Remove escaped invalid characters from a string
	 */
	private static String deescapeString(String deEscape) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < deEscape.length(); i++) {
			char c = deEscape.charAt(i);
			
			if (c == '%') {
				// next character is important
				i++;
				c = deEscape.charAt(i);
				switch (c) {
				case 'p':
					sb.append('%');
					break;
				case 'e':
					sb.append('=');
					break;
				case 'b':
					sb.append('[');
					break;
				case 'c':
					sb.append(']');
					break;
				case 's':
					sb.append(',');
					break;
				case 't':
					sb.append(';');
					break;
				case 'n':
					sb.append('\n');
					break;
				}
			} else
				sb.append(c);
		}
		
		return sb.toString();
	}
	
	/**
	 * Exact copy of the Arrays#copyOf method from the Sun JRE 6.0
	 * Copied into this project to assure Java 5.0 compatibility
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] arrCopyOf(T[] original, int newLength) {
        return (T[]) arrCopyOf(original, newLength, original.getClass());
    }
	
	/**
	 * Exact copy of the Arrays#copyOf method from the Sun JRE 6.0
	 * Copied into this project to assure Java 5.0 compatibility
	 */
    @SuppressWarnings("unchecked")
	public static <T,U> T[] arrCopyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
    
	/**
	 * Exact copy of the Arrays#copyOf method from the Sun JRE 6.0
	 * Copied into this project to assure Java 5.0 compatibility
	 */
    public static int[] arrCopyOf(int[] original, int newLength) {
        int[] copy = new int[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
    
    /**
	 * Exact copy of the Arrays#copyOf method from the Sun JRE 6.0
	 * Copied into this project to assure Java 5.0 compatibility
	 */
    public static float[] arrCopyOf(float[] original, int newLength) {
        float[] copy = new float[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
    
    /**
	 * Exact copy of the Arrays#copyOf method from the Sun JRE 6.0
	 * Copied into this project to assure Java 5.0 compatibility
	 */
    public static double[] arrCopyOf(double[] original, int newLength) {
    	double[] copy = new double[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }


	
}
