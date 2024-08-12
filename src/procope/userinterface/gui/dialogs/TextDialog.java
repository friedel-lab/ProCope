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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class TextDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4913765513300066025L;
	
	private Component parent;
	private JTextArea txtInput;
	private boolean accepted;


	public static String showDialog(Frame parent, String title, String query) {
		TextDialog dialog = new TextDialog(parent, title, query);
		return dialog.getUserInput();
	}

	public static String showDialog(String title, String query) {
		TextDialog dialog = new TextDialog((Frame)null, title, query);
		return dialog.getUserInput();
	}

	private TextDialog(Frame parent, String title, String query) {
		super(parent, title, true);
		this.parent = parent;
		initialize(query);
	}

	private TextDialog(String title, String query) {
		super((Frame)null, title, true);
		initialize(query);
	}

	private void initialize(String query) {
		setResizable(false);
		initializeGUI(query);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);

	}


	private String getUserInput() {
		if (accepted)
			return txtInput.getText();
		else
			return null;
	}

	private void initializeGUI(String query) {
		
		// initialize grid bag layout
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,5,2,5 );
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;  
		
		// query label
		c.weighty = 0;
		add(new JLabel(query),c);
		c.gridy++;
		
		// text area with scrollbars
		c.weighty = 1000;
		txtInput = new JTextArea();
		JScrollPane scrolling = new JScrollPane(txtInput);
		scrolling.setPreferredSize(new Dimension(400,200));
		add(scrolling, c);
		// settings
		scrolling.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrolling.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		txtInput.setLineWrap(true);
		txtInput.setWrapStyleWord(true);
		c.gridy++;
		
		// buttons
		c.weighty = 1;
		c.fill = GridBagConstraints.VERTICAL;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(0,5,4,5);

		JButton btnOK, btnCancel;
		JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonsPane.add(btnOK = new JButton("OK"));
		buttonsPane.add(btnCancel = new JButton("Cancel"));
		btnOK.setPreferredSize(btnCancel.getPreferredSize());
		// add to main panel
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(buttonsPane,c);
		

		// event handler
		// create both actionlisteners
		btnOK.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				accepted = true;
				dispose();
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});


	}

}
