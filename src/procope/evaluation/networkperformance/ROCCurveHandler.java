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
package procope.evaluation.networkperformance;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import procope.tools.ProCopeException;

/**
 * Contains static functions for writing ROC curves to files and creating
 * actual plots from the ROC curves using Gnuplot or the JFreeChart library.
 * 
 * @author Jan Krumsiek
 */

public class ROCCurveHandler {
	
	private static final String DELIMITER = "\t";
	
	/**
	 * Writes the data of a ROC curve to a file. One line contains one point
	 * of the curve, false positive rate and true positive rate are seperated 
	 * by a TAB character.
	 * 
	 * @param rocCurve ROC curve to be written out
	 * @param outFile output file 
	 * @throws FileNotFoundException if the file could not be opened
	 */
	public static void writeToFile(ROCCurve rocCurve, String outFile) throws FileNotFoundException {
		writeToStream(rocCurve, new FileOutputStream(outFile));
	}
	
	/**
	 * Writes the data of a ROC curve to a file. One line contains one point
	 * of the curve, false positive rate and true positive rate are seperated 
	 * by a TAB character.
	 * 
	 * @param rocCurve ROC curve to be written out
	 * @param outFile output file 
	 * @throws FileNotFoundException if the file could not be opened
	 */
	public static void writeToFile(ROCCurve rocCurve, File outFile) throws FileNotFoundException {
		writeToStream(rocCurve, new FileOutputStream(outFile));
	}
	
	/**
	 * Write the data of a ROC curve to an output stream.  One line contains 
	 * one point of the curve, false positive rate and true positive rate are 
	 * seperated by a TAB character.
	 * 
	 * @param rocCurve ROC curve to be written out
	 * @param stream output stream the data will be written to
	 */
	public static void writeToStream(ROCCurve rocCurve, OutputStream stream) {
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(stream));
		for (ROCPoint point : rocCurve)
			writer.println(point.getFP() + DELIMITER + point.getTP());
		writer.close();
	}
	
	/**
	 * Creates a line diagram as a JFreeChart from a given list of ROC curves.
	 * 
	 * @param rocCurves list of ROC curves
	 * @param names list of names for the ROC curves to be used in the diagram
	 * @return diagram chart containing the ROC curves
	 */
	public static JFreeChart generateChart(List<ROCCurve> rocCurves,
			List<String> names) {
		return generateChart(rocCurves, names, Float.POSITIVE_INFINITY);
	}

	/**
	 * Creates a line diagram as a JFreeChart from a given list of ROC curves.
	 * 
	 * @param rocCurves list of ROC curves
	 * @param names list of names for the ROC curves to be used in the diagram
	 * @param fpMax only plot false-positive rate up to this value
	 * @return diagram chart containing the ROC curves
	 */
	public static JFreeChart generateChart(List<ROCCurve> rocCurves,
			List<String> names, float fpMax) {
		
		if (rocCurves.size() != names.size())
			throw new ProCopeException("Curves list and names list do not have the same length.");
		
		// create the dataset by adding one series for each roccurve
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i=0; i<rocCurves.size(); i++) {
			// get curve and object
			ROCCurve curve = rocCurves.get(i);
			String name = names.get(i);
			// create series of data
			XYSeries series = new XYSeries(name);
			for (ROCPoint p : curve)  {
				float fp = p.getFP();
				if (fp > fpMax) break;
				series.add(fp, p.getTP());
			}
			// add to the dataset
			dataset.addSeries(series);
		}
		
		
		// create the chart
        final JFreeChart chart = ChartFactory.createXYLineChart("ROC curves",
				"False positive rate", "True positive rate", dataset,
				PlotOrientation.VERTICAL, true, false, false);

        // customization of the chart
        chart.setBackgroundPaint(Color.white);
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        // set line rendering
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i=0; i<rocCurves.size(); i++) {
        	renderer.setSeriesLinesVisible(i, true);
        	renderer.setSeriesShapesVisible(i, false);
        	renderer.setSeriesStroke(i, new BasicStroke(2));
        }
        plot.setRenderer(renderer);
	
        return chart;
		
	}
	
	
	
}
