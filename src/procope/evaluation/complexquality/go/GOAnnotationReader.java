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
package procope.evaluation.complexquality.go;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import procope.tools.ProCopeException;
import procope.tools.namemapping.ProteinManager;



/**
 * Contains static methods to read annotations from a GO annotations file.
 * <p>This class is not instantiatable.
 * 
 * @see GOAnnotations
 * @author Jan Krumsiek
 */
public class GOAnnotationReader {
	
	private static final int DEF_COLUMN = 2;

	// avoid instantiation
	private GOAnnotationReader() {
	}
	
	/**
	 * Reads annotations from a given annotations file. The protein identifiers
	 * will be parsed from the <u>second</u> column of that tab-delimited file.
	 * 
	 * @param annotationFile annotations file to be read
	 * @return the annotations read from the file
	 * @throws IOException if the file could not be read
	 */
	public static GOAnnotations readAnnotations (String annotationFile) throws IOException {
		return readAnnotations(new File(annotationFile), DEF_COLUMN);
	}
	
	
	/**
	 * Reads annotations from a given annotations file.
	 * 
	 * @param annotationFile annotations file to be read
	 * @param useColumn read protein identifiers from this column, you might
	 *        want to use {@link ProteinManager regular expressions}
	 * @return the annotations read from the file
	 * @throws IOException if the file could not be read
	 */
	public static GOAnnotations readAnnotations (String annotationFile, int useColumn) throws IOException { 
		return readAnnotations(new File(annotationFile), useColumn);
	}

	
	/**
	 * Reads annotations from a given annotations file. The protein identifiers
	 * will be parsed from the <u>second</u> column of that tab-delimited file.
	 * 
	 * @param annotationFile annotations file to be read
	 * @return the annotations read from the file
	 * @throws IOException if the file could not be read
	 */
	public static GOAnnotations readAnnotations (File annotationFile) throws IOException {
		return readAnnotations(annotationFile, DEF_COLUMN);
	}
	
	
	/**
	 * Reads annotations from a given annotations file.
	 * 
	 * @param annotationFile annotations file to be read
	 * @param useColumn read protein identifiers from this column, you might
	 *        want to use {@link ProteinManager regular expressions}
	 * @return the annotations read from the file
	 * @throws IOException if the file could not be read
	 */
	public static GOAnnotations readAnnotations (File annotationFile, int useColumn) throws IOException { 
		FileInputStream in = new FileInputStream(annotationFile);
		GOAnnotations result = readAnnotations(in, useColumn);
		in.close();
		return result;
	}
	

	/**
	 * Reads annotations from a given input stream. The protein identifiers
	 * will be parsed from the <u>second</u> column of the tab-delimited data.
	 * 
	 * @param annotation input stream from which the annotation data will be read
	 * @return the annotations read from the stream
	 */
	public static GOAnnotations readAnnotations (InputStream annotation)  { 
		return readAnnotations(annotation, DEF_COLUMN);
	}
	
	
	/**
	 *  Reads annotations from a given input stream.
	 *  
	 * @param annotations input stream from which the annotation data will be read
	 * @param useColumn read protein identifiers from this column, you might
	 *        want to use {@link ProteinManager regular expressions}
	 * @return the annotations read from the stream
	 */
	public static GOAnnotations readAnnotations (InputStream annotations, int useColumn)  { 
		
		try {
			GOAnnotations result = new GOAnnotations();
	
			// open file and parse line by line
			BufferedReader reader = new BufferedReader(new InputStreamReader(annotations));
			String line;
			while ((line = reader.readLine()) != null) {
				//System.out.println(line);
				// ignore comment lines
				if (line.charAt(0) != '!') {
					// split up
					String[] splitup = line.split("\t");
					String ID = splitup[useColumn-1]; 
					// get GO id
					String goID = splitup[4];
					int geneID = ProteinManager.getInternalID(ID);
					
					result.addAnnotation(geneID, goID);
				}
	
			}
			
			return result;
			
		} catch (Exception e) {
			throw new ProCopeException("File probably has invalid format!");
		}
	}
		
}
