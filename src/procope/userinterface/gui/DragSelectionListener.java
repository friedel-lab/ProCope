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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

// allows to drag a seletion on a JList
class DragSelectionListener extends MouseInputAdapter {
	Point lastPoint = null;

	public void mousePressed(MouseEvent e) {
		lastPoint = e.getPoint(); 
	}

	public void mouseReleased(MouseEvent e) {
		lastPoint = null;
	}

	public void mouseDragged(MouseEvent e) {
		JList list = (JList) e.getSource();
		if (lastPoint != null && !e.isConsumed()
				&& SwingUtilities.isLeftMouseButton(e) && !e.isShiftDown()) {
			// something was dropped, where?
			int row = list.locationToIndex(e.getPoint());
			if (row != -1) {
				// start?
				int leadIndex = list.locationToIndex(lastPoint);
				if (row != leadIndex) { // ignore drag within row
					// coordinates
					Rectangle cellBounds = list.getCellBounds(row, row);
					if (cellBounds != null) {
						// ensure visibility
						list.scrollRectToVisible(cellBounds);

						int anchorIndex = leadIndex;
						if (e.isControlDown()) {
							// select the entries accordingly
							if (list.isSelectedIndex(anchorIndex)) { // add selection
								list.removeSelectionInterval(anchorIndex,
										leadIndex);
								list.addSelectionInterval(anchorIndex, row);
							} else { // remove selection
								list.addSelectionInterval(anchorIndex,
										leadIndex);
								list.removeSelectionInterval(anchorIndex, row);
							}
						} else { // replace selection
							list.setSelectionInterval(leadIndex, row);
						}
					}
				}
			}
		}
	}
}
