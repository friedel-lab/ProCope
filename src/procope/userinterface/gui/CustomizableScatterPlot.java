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
package procope.userinterface.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

// extension of the FastScatterPlot to display larger dots and a x/y line
public class CustomizableScatterPlot extends FastScatterPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 598042582704234417L;
	
	private int dotSize=1;
	private int shift=0;

	public CustomizableScatterPlot(float[][] data, NumberAxis domainAxis,
			NumberAxis rangeAxis, int dotSize) {
		super(data, domainAxis, rangeAxis);
		
		this.dotSize = dotSize;
		this.shift = (int)Math.floor(dotSize/2);
	}

	public void render(Graphics2D g2, Rectangle2D dataArea,
			PlotRenderingInfo info, CrosshairState crosshairState) {
		
		g2.setPaint(this.getPaint());

		float[][] data = this.getData();
		ValueAxis domainAxis = this.getDomainAxis();
		ValueAxis rangeAxis = this.getRangeAxis();

		if (data != null) {
			
			// draw the points
			g2.setPaint(this.getPaint());
			for (int i = 0; i < data[0].length; i++) {
				float x = data[0][i];
				float y = data[1][i];

				//int transX = (int) (xx + ww * (x - domainMin) / domainLength);
				//int transY = (int) (yy - hh * (y - rangeMin) / rangeLength); 
				int transX = (int) domainAxis.valueToJava2D(x, dataArea,
						RectangleEdge.BOTTOM);
				int transY = (int) rangeAxis.valueToJava2D(y, dataArea,
						RectangleEdge.LEFT);
				g2.fillRect(transX-shift, transY-shift, dotSize, dotSize);
			}
			
			// draw the x=y line
			float minRange = (float)Math.min(domainAxis.getRange().getUpperBound(), 
					rangeAxis.getRange().getUpperBound());
			g2.setPaint(Color.red);
			int zeroX =  (int) domainAxis.valueToJava2D(0, dataArea, RectangleEdge.BOTTOM);
			int zeroY =  (int) rangeAxis.valueToJava2D(0, dataArea, RectangleEdge.LEFT);
			int targetX = (int) domainAxis.valueToJava2D(minRange, dataArea, RectangleEdge.BOTTOM);
			int targetY =  (int) rangeAxis.valueToJava2D(minRange, dataArea, RectangleEdge.LEFT);
			float [] Dashes = {8.0F, 7.0F};
			g2.setStroke( new BasicStroke (1.4F, BasicStroke.CAP_BUTT, 
                    BasicStroke.JOIN_MITER, 
                    10.0F, Dashes, 0.F));
			g2.drawLine(zeroX, zeroY, targetX, targetY);
			
		}
	}

}
