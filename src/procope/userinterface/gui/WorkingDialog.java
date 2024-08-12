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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */

public class WorkingDialog extends JDialog {

	private static final long serialVersionUID = -2167036705605674312L;

	JLabel lblLabel;

	private JFrame owner;
	private Thread worker;

	private JButton btnCancel;

	public WorkingDialog(final JFrame owner) {
		super(owner, "Working...");

		this.owner = owner;
		
		setPreferredSize(new Dimension(300, 100));
	
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
//		c.fill = GridBagConstraints.BOTH;
		c.gridx=0;
		c.gridy=0;
		c.insets = new Insets(5,5,5,5);
		c.anchor = GridBagConstraints.CENTER;
		pane.add(lblLabel = new JLabel(), c);
		c.gridy++;
		pane.add(btnCancel = new JButton("Cancel"), c);
		setContentPane(pane);
		pack();
		setResizable(false);	
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(owner);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		
		// add a focus listener to the parent
		final Dialog dialog = this;
		owner.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				if (dialog.isVisible())
					dialog.toFront();
			}
		});
		
		// add canceler
		btnCancel.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				if (worker != null && worker.isAlive()) {
					worker.stop();
					System.gc();
					
					// we need to go away
					if (owner instanceof GUIMain)
						((GUIMain)owner).workingHide();
					else if (owner instanceof GOSettingsDialog)
						((GOSettingsDialog)owner).workingHide();
				}
			}
		});
	}

	public void setWorker(Thread worker) {
		this.worker = worker;
	}
	
	public void setMessage(final String message) {
		Runnable setText = new Runnable() {
			public void run() {
				lblLabel.setText(message);
			}
		};
		SwingUtilities.invokeLater(setText);	

	}
	
	public static void main(String[] args) {
		new WorkingDialog(null).setMessage("Hallo das ist die Nachricht");
	}

	public void activate() {
		setLocationRelativeTo(owner);
		setVisible(true);
		
	}
	
	public void setVisibility(final boolean b) {
		Runnable setVis = new Runnable() {
			public void run() {
				setVisible(b);
			}
		};
		SwingUtilities.invokeLater(setVis);	

	}


}
