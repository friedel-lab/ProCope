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

import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

public class HelpButton extends JButton {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2533991156073064622L;
	
	
	private static ImageIcon icon =
		new ImageIcon(HelpButton.class.getResource("/img/question.png"), null);
	
	public HelpButton(final String helpText) {
		super(icon);
		setBorder(BorderFactory.createEmptyBorder());
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		final JButton button = this;
		
		addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
			//	JMultilineLabel label = new JMultilineLabel(helpText);
			//	label.setMaxWidth(500);
				String x = wordRap(helpText, 500,  Toolkit.getDefaultToolkit().getFontMetrics(button.getFont()));
				JOptionPane.showMessageDialog(null, x, "ProCope help", JOptionPane.INFORMATION_MESSAGE );
			}
		});
	}
	
	 protected String wordRap(String txt, int width, FontMetrics fm) {
	        StringBuffer sb = new StringBuffer(txt);
	        int start = 0;
	        int stop = 0;
	        int hold = 0;
	        int offset = 0;
	 
	        while(stop < txt.length()) {
	            while(fm.stringWidth(txt.substring(start, stop)) < width) {
	                if(txt.charAt(stop) == '\n') {
	                    start = ++stop;
	                } else {
	                    stop++;
	                }
	 
	                if(stop >= (txt.length() - 1)) {
	                    break;
	                }
	            }
	 
	            if(stop >= (txt.length() - 1)) {
	                break;
	            }
	 
	            hold = stop;
	            while((txt.charAt(stop) != ' ') && (start != stop)) {
	                stop--;
	            }
	 
	            if(start == stop) {
	                stop = hold;
	            }
	 
	            if(sb.charAt(stop + offset) == ' ') {
	                sb.setCharAt(stop + offset, '\n');
	            } else {
	                sb.insert(stop, '\n');
	                offset++;
	            }
	 
	            start = ++stop;
	        }
	 
	        sb.append('\n');
	 
	        /*
	        Vector v = new Vector();
	        start = 0;
	        for(int i = 0; i < sb.length(); i++) {
	            if(sb.charAt(i) == '\n') {
	                char[] c = new char[100];
	                sb.getChars(start, i, c, 0);
	                start = i;
	                v.addElement(new String(c).trim());
	            }
	        }*/
	        //System.out.println("returning "+v.size()+" lines..");
	        return sb.toString();
	    }

}
