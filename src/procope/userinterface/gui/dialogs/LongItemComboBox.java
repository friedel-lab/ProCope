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
package procope.userinterface.gui.dialogs;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComboBox;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
// a combo box whose dropdown list width adapts to its contents
// (quite dirty solution)
public class LongItemComboBox extends JComboBox {

	private static final long serialVersionUID = 5298990683390051916L;

	private int maxitemwidth=0;
	
	public LongItemComboBox(Object[] list) {
		super(list);
		adaptPopupWidth();
	}

	public Dimension getSize() {
		Dimension size = super.getSize();
		if (maxitemwidth > size.width)
			size.width = maxitemwidth;
		return size;
	}

	public void doLayout() {
		super.doLayout();
	}
	
	public void adaptPopupWidth() {
		// create graphics dummy & font metrics
		Graphics2D g = (Graphics2D)(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB).createGraphics());
		g.setFont(this.getFont());
		FontMetrics fontMetrics = g.getFontMetrics(this.getFont());
		// iterate over list & calc max item width
		int max=0;
		for (int i=0; i<getItemCount(); i++) {
			String curitem = (String)getItemAt(i);
			if (curitem != null && curitem.length() > 0) {
				int curwidth = fontMetrics.stringWidth(curitem);
				if (curwidth > max) max = curwidth;
			}
		}
		
		maxitemwidth = max+4;	

	}
}
