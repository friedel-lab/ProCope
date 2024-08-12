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
package procope.methods.scores.bootstrap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationExperiment;
import procope.tools.ProCopeException;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;


/**
 * Represents a set of bootstrap samples of a purification data set. To 
 * construct such a bootstrap sample, random purification experiments are
 * drawn from the original data set <u>with replacement</u> to generate a new
 * dataset having the same number of experiments.
 * 
 * @author Jan Krumsiek
 */

public class PurificationBootstrapSamples {

	private int numExperiments;
	private List<PurificationExperiment> list;
	private int numSamples;
	private int[][] samples;

	/**
	 * Reads bootstrap samples from a file.
	 * 
	 * @param file file from which the samples will be read
	 * @throws IOException if the file could not be read
	 * @throws ProCopeException if the file format is invalid
	 */
	public PurificationBootstrapSamples(String file) throws IOException {
		this(new FileInputStream(file));
	}

	/**
	 * Reads bootstrap samples from an input stream.
	 * 
	 * @param stream stream from which the samples will be read
	 * @throws ProCopeException if the file format is invalid
	 */
	public PurificationBootstrapSamples(InputStream stream) {

		try { 

			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

			list = new ArrayList<PurificationExperiment>();

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.charAt(0) == '#') 
					break;
				else {
					// purification line
					String[] split = line.split("\t");
					PurificationExperiment exp = new PurificationExperiment( ProteinManager.getInternalID(split[0]));
					// read preys
					for (int i=1; i<split.length; i++) {
						if (split[i].length() > 0) {
							exp.addPrey( ProteinManager.getInternalID(split[i]) );
						}
					}
					list.add(exp);
				}
			}

			//  now read # of samples and experiments
			line = reader.readLine();
			numSamples = Integer.parseInt(line);
			line = reader.readLine();
			numExperiments = Integer.parseInt(line);
			this.samples = new int[numSamples][numExperiments];  
			int sample=0;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split("\t");
				for (int i=0; i<numExperiments; i++) {
					samples[sample][i] = Integer.parseInt(split[i]);
				}
				sample++;
			}

			reader.close();

		} catch (Exception e) {
			throw new ProCopeException("File probably has invalid format!");
		}
	}

	/**
	 * Creates a new set of bootstrap samples.
	 * 
	 * @param data purification data to be sampled
	 * @param numberOfSamples number of samples to be created
	 */
	public PurificationBootstrapSamples(PurificationData data, int numberOfSamples) {

		Random random = Tools.random;

		this.list = data.getExperiments();
		this.numExperiments = list.size();
		this.numSamples = numberOfSamples;
		this.samples = new int[numberOfSamples][numExperiments];  

		for (int sample=0; sample<numberOfSamples; sample++) {
			// draw n random numbers
			for (int i=0; i<numExperiments; i++) {
				samples[sample][i] = random.nextInt(numExperiments);
			}
		}
	}

	/**
	 * Returns the number of bootstrap samples contained in this set.
	 * 
	 * @return number of bootstrap samples in this set
	 */
	public int getNumberOfSamples() {
		return numSamples;
	}

	/**
	 * Returns a given sample from the set. <b>Note:</b> The first bootstrap
	 * sample has the index 1, not 0.
	 * 
	 * @param sample sample to be returned (1-based)
	 * @return the bootstrap sample
	 */
	public PurificationData getSample(int sample) {
		if (sample < 1 || sample > numSamples)
			throw new IndexOutOfBoundsException("Invalid sample number: " + sample);

		// construct purification data
		PurificationData sampleData = new PurificationData();
		for (int i=0; i<numExperiments; i++)
			sampleData.addExperiment(list.get(samples[sample-1][i]));

		return sampleData;

	}

	/**
	 * Writes the bootstrap samples contained in this set to a file.
	 * 
	 * @param outfile output file
	 * @throws IOException if the file could not be written
	 */
	public void writeToFile(String outfile) throws IOException {
		FileOutputStream out = new FileOutputStream(outfile);
		writeToStream(out);
		out.close();

	}

	/**
	 * Writes the bootstrap samples contained in this set to a file.
	 * 
	 * @param outstream output stream the samples will be written to
	 */
	public void writeToStream(OutputStream outstream) {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outstream));

		// first step: write purifications
		for (PurificationExperiment experiment : list) {
			writer.print(ProteinManager.getLabel(experiment.getBait()) + "\t");
			for (int prey : experiment.getPreys()) {
				writer.print(ProteinManager.getLabel(prey) + "\t");
			}
			writer.println();
		}
		writer.println("#");
		// second step: write matrix
		writer.println(numSamples);
		writer.println(numExperiments);
		for (int sample=0; sample<numSamples; sample++) {
			for (int i=0; i<numExperiments; i++) {
				writer.print(samples[sample][i]+ "\t");
			}
			writer.println();
		}
		writer.flush();
	}

}
