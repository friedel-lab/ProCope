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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import procope.tools.ProCopeException;
import procope.tools.namemapping.ProteinManager;


/**
 * Reads localization data from a file or stream. The following data format is
 * assumed:
 * 
 * <p>PROTEINNAME[tab]loc1,loc2,loc3
 * 
 * The protein identifier and the localizations are seperated by a TAB, each
 * localization information is then seperated by commas.
 * 
 * <p>This class is not instantiatable.
 * 
 * @author Jan Krumsiek
 *
 */
public class LocalizationDataReader {

	// avoid instantiation
	private LocalizationDataReader() {
	}
	
	/**
	 * Reads localization data from a given file.
	 * 
	 * @param file file from which the localization data will be read
	 * @return localization data object
	 * @throws IOException if the file could not be read
	 * @throws ProCopeException if the data format is invalid
	 */
	public static LocalizationData readLocalizationData(String file) throws IOException {
		return readLocalizationData(new File(file));
	}
		
	/**
	 * Reads localization data from a given file.
	 * 
	 * @param file file from which the localization data will be read
	 * @return localization data object
	 * @throws IOException 
	 * @throws ProCopeException if the data format is invalid
	 * @throws IOException if the file could not be read
	 */
	public static LocalizationData readLocalizationData(File file) throws IOException  {
		FileInputStream in = new FileInputStream(file);
		LocalizationData data = readLocalizationData(new FileInputStream(file));
		in.close();
		return data;
	}
	
	
	/**
	 * Reads localization data from an input stream
	 * 
	 * @param  stream stream from which the localization data will be read
	 * @return localization data object
	 * @throws ProCopeException if the data format is invalid
	 */
	public static LocalizationData readLocalizationData(InputStream stream)  {
			
		try {
			
			LocalizationData result = new LocalizationData();
			
			// open file and read line by line
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#") && line.length() > 0) {
					// up to first whitespace: ORF, rest: localizations
					int ws = line.indexOf(' ');
					String orf = line.substring(0,ws).toLowerCase().trim();
					String locs = line.substring(ws+1);
					
					// create vector of localizations
					String[] locsplit = locs.split(",");
					
					// determine ID of orf
					int orfID= ProteinManager.getInternalID(orf);
					
					// add all localizations
					for (String singleLoc : locsplit) {
						// map to integer ID and add to collection
					//	System.out.println("adding " + singleLoc + " to " + orfID);
						result.addLocalization(orfID, singleLoc);
					}
				}			
			}
	
			return result;
		} catch (Exception e) {
			throw new ProCopeException("File probably has invalid format!");
		}
	}
	
}
