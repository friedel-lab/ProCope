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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import procope.tools.Tools;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */
public class AboutDialog extends JFrame  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8007797170913184546L;

	AboutDialog(Frame owner) {
		
		super("About ProCope");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initializeGUI();
		setResizable(false);
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
		
	}
	
	private void initializeGUI() {
		JPanel pane = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx=0;
		c.gridy=0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5 ,5,2,5);
		
		// name of the program and version
		c.gridwidth = 2;
		pane.add(boldLabel(Tools.LIBRARY_NAME), c);
		c.insets = new Insets(0 ,5,2,5);
		c.gridy++;
		pane.add(normalLabel("Protein Complex Prediction and Evaluation"), c);
		c.gridy++;
		pane.add(normalLabel("Version: " + Tools.VERSION),c);
		c.gridy++;
		pane.add(new JLabel(" "),c);
		c.gridy++;
		
		// contact and web
		c.gridwidth = 1;
		c.gridx=0;
		c.gridy++;
		pane.add(boldLabel("Web:"),c);
		c.gridx=1;
		final JLabel web; 
		pane.add(web = linkLabel("http://www.bio.ifi.lmu.de/Complexes/ProCope"),c);
		
		// close button
		c.gridwidth = 2;
		c.gridx=0;
		c.gridy++;
		c.insets = new Insets(7 ,5,6,5);
		c.anchor = GridBagConstraints.EAST;
		final JButton close;
		pane.add(close = new JButton("Close"),c);
		
		web.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					GUICommons.showBrowser(new URI(web.getText()));
				} catch (URISyntaxException e1) {}
			}
		});
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		
		
		
		setContentPane(pane);
	}
	
	private void close() {
		dispose();
	}
	
	private JLabel normalLabel(String text) {
		JLabel label = new JLabel(text);
		Font font = label.getFont();
		label.setFont(new Font(font.getName(), Font.PLAIN, font.getSize()));
		return label;
	}
	
	private JLabel boldLabel(String text) {
		JLabel label = new JLabel(text);
		Font font = label.getFont();
		label.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
		return label;
	}

	private JLabel linkLabel(String text) {
		JLabel label = new JLabel(text);
		Font font = label.getFont();
		label.setFont(new Font(font.getName(), Font.PLAIN, font.getSize()));
		label.setForeground(Color.BLUE);
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return label;
	}
	
	
	public static void main(String[] args) {
		new AboutDialog(null);
	}
	
	

}
