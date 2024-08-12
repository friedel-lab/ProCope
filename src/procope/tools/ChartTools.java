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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import procope.evaluation.networkperformance.ROCCurveHandler;

/**
 * Contains static methods to handle JFreeChart chart objects.
 * 
 * @author Jan Krumsiek
 */
public class ChartTools {
	
	/**
	 * Writes a given JFreeChart to a PNG file.
	 * 
	 * @param chart the chart to be plotte
	 * @param output output file
	 * @param width width of the image
	 * @param height height of the image
	 * @throws IOException if the file could not be written
	 * @see #generateHistogram(String, double[], boolean, int)
	 * @see ROCCurveHandler#generateChart(java.util.List, java.util.List)
	 */
	public static void writeChartToPNG(JFreeChart chart, File output, int width, int height) throws IOException {
		BufferedImage image = drawChart(chart, width, height);
		ImageIO.write(image, "png", output);
	}
	
	/**
	 * Draws a JFreeChart into a BufferedImage
	 */
	private static BufferedImage drawChart(JFreeChart chart, int width, int height) {
		BufferedImage img = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();

		chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));

		g2.dispose();
		return img;
	}

	/**
	 * Generates a histogram chart from a given dataset.
	 * 
	 * @param data dataset from which the histogram will be calculated 
	 * @param relative show relative frequencies?
	 * @param bins number of bins
	 * @return the histogram chart
	 */
	public static JFreeChart generateHistogram(String title, double[] data, boolean relative, int bins) {
		// generate dataset
		HistogramDataset dataset = new HistogramDataset();
		if (relative)
			dataset.setType(HistogramType.RELATIVE_FREQUENCY);
		else
			dataset.setType(HistogramType.FREQUENCY);
	
		dataset.addSeries("Abc", data, bins  );
		// create chart
		JFreeChart chart = ChartFactory.createHistogram(
				title,
				null, 
				null,
				dataset,
				PlotOrientation.VERTICAL,
				false , 
				false,
				false
		);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setForegroundAlpha(0.75f);
		NumberAxis axis = (NumberAxis) plot.getDomainAxis();
		axis.setAutoRangeIncludesZero(false);
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		if (relative)
			rangeAxis.setNumberFormatOverride(NumberFormat.getPercentInstance());
	
		return chart;
	}
	

}
