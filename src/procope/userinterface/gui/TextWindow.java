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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */

// dialog for developer-defined user messages
public class TextWindow extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = -6831146639624661450L;

	private JTextArea txtMessage;
	
	private final Dimension TEXTAREA_SIZE = new Dimension(500,300);

	private JButton btnSave;
	private JButton btnQuit;
	
	public TextWindow(JFrame owner, String caption, String message) {
		this(owner, caption, message,false);		
	}
	
	public TextWindow(JFrame owner, String caption, String message, boolean hScroll) {
		super(caption);
		
		setupGUI(hScroll);
		txtMessage.setFont(new Font("Courier", Font.PLAIN, 11)); 
		txtMessage.setText(message);
		txtMessage.setCaretPosition(0);
		setResizable(true);
		pack();
		setLocationRelativeTo(owner);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
		toFront();
		
	}
		
	
	
	private void setupGUI(boolean hScroll) {
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(5,5,5,5);
		
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 2;
		
		txtMessage = new JTextArea();
		JScrollPane scrolling = new JScrollPane(txtMessage);
		add(scrolling, c);
		
		// save button
		c.gridx =0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = 1;
		c.insets = new Insets(3,5,8,5 );
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		add(btnSave = new JButton("Save to file"),c);
		// close button
		c.gridx = 1;
		c.weightx = 0;
		add(btnQuit = new JButton("Close"),c);
		
		
		// create border
		//txtMessage.setBorder(BorderFactory.createEtchedBorder() );
		// scrolling
		if (hScroll) {
			scrolling.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		} else {
			txtMessage.setLineWrap(true);
			txtMessage.setWrapStyleWord(true);
			scrolling.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		scrolling.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	
		// additional flags
		txtMessage.setEditable(false);
		scrolling.setPreferredSize(TEXTAREA_SIZE);
		
		// event handler
		btnQuit.addActionListener(this);
		btnSave.addActionListener(this);
		
	}

	
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		
		if (source == btnQuit) 
			// close dialog
			dispose();
		else if (source == btnSave)  {
			// save to file
			JFileChooser chooser = new JFileChooserAskOverwrite();
			int choice = chooser.showSaveDialog(this);
			File open = chooser.getSelectedFile();
			if (open != null && choice == JFileChooser.APPROVE_OPTION) {
				// save it
				try {
					FileWriter writer = new FileWriter(open);
					writer.write(txtMessage.getText());
					writer.close();
					GUICommons.info("File saved.");
				} catch (IOException e) {
					GUICommons.error("Could not save file. Reason:\n\n" + e.getMessage());
				}
			}
		}
			
	}
	
	public static void main(String[] args) {
		
		long e,s;
		s = System.currentTimeMillis();
		new TextWindow(null, "capt", "abc");
		e = System.currentTimeMillis();
		System.out.println((e-s)+" ms");
		
	}
	
	
}
