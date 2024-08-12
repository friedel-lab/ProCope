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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

// dialog for developer-defined user messages
public class BugReportDialog extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = -6831146639624661450L;


	private JTextArea txtMessage;
	private JButton btnSend;
	private String reportID;
		
	public BugReportDialog(String reportID) {
		
		super("Bug report");
		
		this.reportID = reportID;
		
		setSize(new Dimension(480,340));
		setupGUI();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
		
	}

	private void setupGUI() {
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(5,5,5,5);
		
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; 
		c.fill = GridBagConstraints.BOTH;  
		
		
		c.weighty=1;
		add(new JLabel("Bug report:"),c);
		
		c.gridy++;
		
		c.weighty = 1000;
		
		txtMessage = new JTextArea();
		JScrollPane scrolling = new JScrollPane(txtMessage);
		c.gridwidth = 3;
		add(scrolling, c);
		
		c.gridy++;
		c.weighty = 1; 
		c.gridwidth = 1;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.EAST;
		JButton btnQuit;
		c.insets = new Insets(5,5,7,5);
		c.weightx = 1000;
		add(new JLabel(""),c);
		c.gridx++;
		c.weightx = 1;
		add(btnSend = new JButton("  Send  "),c);
		c.gridx++;
		add(btnQuit = new JButton("Cancel"),c);
		
		// scrolling
		scrolling.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrolling.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// set font
		txtMessage.setFont(new Font("Courier", Font.PLAIN, 11));
		// additional flags
		txtMessage.setEditable(true);
		txtMessage.setLineWrap(true);
		txtMessage.setWrapStyleWord(true);
		
		// event handler
		btnQuit.addActionListener(this);
		btnSend.addActionListener(this);
		
	}

	public void actionPerformed(ActionEvent arg) {
		if (arg.getSource() == btnSend) {
			GUICommons.sendReport(txtMessage.getText(), reportID);
			dispose();
			GUICommons.info("Thank you");
		} else
			// close dialog
			dispose();		
		
	}
	
}
