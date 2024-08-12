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
package procope.data.purifications;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import procope.tools.ProCopeException;
import procope.tools.namemapping.ProteinManager;


/**
 * Contains static methods to read purification data from a file. Each line
 * of the input data contains one bait-prey interaction. The following
 * tab-separated fields are assumed (adapted from the purification data 
 * provided by <a target="_blank"
 * href="http://interactome-cmp.ucsf.edu/">
 * The Krogan Lab Interactome Database</a>):
 * <ol>
 * <li>purification name</li>
 * <li>identification</li>
 * <li>day</li>
 * <li>number</li>
 * <li>bait</li>
 * <li>prey</li>
 * <li>score</li>
 * </ol>
 * 
 * <p>Currently this reader only uses the <i>purification name</i>, <i>bait</i>
 * and <i>prey</i> information. All bait-prey interactions with the same 
 * purification name (which of course should also all have the same bait) will
 * be combined into one {@link PurificationExperiment}.
 * 
 * <p><font size="+1">Multiple preys</font>
 * <p>Multiple occurences of preys in the same experiment (e.g. due to 
 * different mass spectrometry methods) can be counted multiple times 
 * or just once. All methods provide signatures which allow the setting
 * of a {@code multiplePreys} flag.
 * 
 * @author Jan Krumsiek
 */
public class PurificationDataReader {

	/** 
	 * Read purification data from a given file. Multiple occurences
	 * of preys will be counted multiple times 
	 * (see {@link PurificationDataReader above}).
	 * 
	 * @param file file from which the purification data will be read
	 * @return purification data read from the file
	 * @throws IOException if the file could not be read	 
	 */
	
	public static PurificationData readPurifications(String file) throws IOException {
		return readPurifications(new File(file), true);
	}

	/**
	 * Read purification data from a given file.
	 * 
	 * @param file file from which the purification data will be read
	 * @param multiplyPreys {@code true}: count multiple preys, see {@link PurificationDataReader above}
	 * @return purification data read from the file
	 * @throws IOException if the file could not be read
	 */
	public static PurificationData readPurifications(String file, boolean multiplyPreys) throws IOException  {
		return readPurifications(new File(file), multiplyPreys);
	}

	/** 
	 * Read purification data from a given file. Multiple occurences
	 * of preys will be counted multiple times 
	 * (see {@link PurificationDataReader above}).
	 * 
	 * @param file file from which the purification data will be read
	 * @return purification data read from the file
	 * @throws IOException if the file could not be read
	 */
	public static PurificationData readPurifications(File file) throws IOException {
		return readPurifications(file, true);
	}

	/**
	 * Read purification data from a given file.
	 * 
	 * @param file file from which the purification data will be read
	 * @param multiplyPreys {@code true}: count multiple preys, see {@link PurificationDataReader above}
	 * @return purification data read from the file
	 * @throws IOException if the file could not be read
	 */
	public static PurificationData readPurifications(File file, boolean multiplyPreys) throws IOException  {
		FileInputStream in = new FileInputStream(file);
		PurificationData result = readPurifications(new FileInputStream(file), multiplyPreys);
		in.close();
		return result;
	}

	/** 
	 * Read purification data from a given input stream. Multiple occurences
	 * of preys will be counted multiple times 
	 * (see {@link PurificationDataReader above}).
	 * 
	 * @param input inputstream from which the data will be read
	 * @return purification data read from the stream
	 */	
	public static PurificationData readPurifications(InputStream input) {
		return readPurifications(input, false);
	}

	/** 
	 * Read purification data from a given input stream.
	 * 
	 * @param input inputstream from which the data will be read
	 * @param multiplePreys {@code true}: count multiple preys, see {@link PurificationDataReader above}
	 * @return purification data read from the stream
	 */	
	public static PurificationData readPurifications(InputStream input, boolean multiplePreys) {

		// any errors: probably wrong file
		try {

			// create empty purification data
			PurificationData data = new PurificationData();

			// data
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

			// initialize
			Collection<Integer> curpreys;
			curpreys = new ArrayList<Integer>();
			String lastexp = "";
			int lastbait = -1;

			String line;
			int num=0;
			boolean firstLine=false;
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0 && line.charAt(0) != '#') {
					String[] splitup = line.split("\t");

					// first line may be skipped
					if (!(firstLine && splitup.length < 5)) {
						// current bait & prey
						String strID = splitup[4];
						int bait = ProteinManager.getInternalID(strID);
						String strPrey = splitup[5];

						// did the experiment change?
						if (!lastexp.equals(splitup[0])) {

							// add to experiment-list?
							if (lastexp.length() > 0) {
								// create new experiment object & add preys
								PurificationExperiment experiment = new PurificationExperiment(lastbait, multiplePreys);
								experiment.addPreys(curpreys);
								// add to total list
								data.addExperiment(experiment);
							}

							// new experiment
							curpreys = new ArrayList<Integer>();
							num++;
						}

						lastexp = splitup[0];
						lastbait = bait;
						// add prey to list of preys
						int prey = ProteinManager.getInternalID(strPrey);
						curpreys.add(prey);
					}
				}

				firstLine = false;
			}
			// add last experiment
			PurificationExperiment experiment = new PurificationExperiment(lastbait, multiplePreys);
			experiment.addPreys(curpreys);
			// add to total list
			data.addExperiment(experiment);

			return data;

		} catch (Exception e) {
			throw new ProCopeException("File probably has invalid format!");
		}
	}

}
