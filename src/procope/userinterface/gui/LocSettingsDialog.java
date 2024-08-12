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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import procope.data.LocalizationData;
import procope.data.LocalizationDataReader;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;
import procope.userinterface.gui.dialogs.DialogSettings;
import procope.userinterface.gui.dialogs.ParameterDialog;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */

public class LocSettingsDialog extends JFrame implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8268660109581324883L;
	
	private ArrayList<LocalizationDataSetting> settings;
	private JButton btnAdd;
	private JButton btnRemove;
	private JList lstData;
	private JButton btnLookup;

	public LocSettingsDialog(JFrame parent, ArrayList<LocalizationDataSetting> settings) {
		super("Localization data");

		this.settings = settings;

		initializeGUI();
		// initial list
		for (LocalizationDataSetting setting : settings)
			((DefaultListModel)lstData.getModel()).addElement(setting.label);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	private void initializeGUI() {
		
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; 
		c.gridy=0;	
		c.weightx = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(5,5,5,5);

		// add network and remove network buttons
		c.weighty = 0;
		pane.add(btnAdd = new JButton("Add Localization data"), c);
		c.gridx = 1;
		pane.add(btnRemove = new JButton("Remove selected data"), c);
		c.gridx = 2;
		pane.add(btnLookup = new JButton("Lookup localizations"), c);
		// action listeners
		btnAdd.addActionListener(this);
		btnRemove.addActionListener(this);
		btnRemove.setEnabled(false);
		btnLookup.addActionListener(this);
		btnLookup.setEnabled(false);
		// tooltips
		btnAdd.setToolTipText("Add a new set of localization data from the file system");
		btnRemove.setToolTipText("Remove the currently selected localization data set");
		btnLookup.setToolTipText("Lookup localizations for proteins using the currently selected dataset");

		// list component
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1;
		c.gridwidth = 4;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		DefaultListModel puriModel = new DefaultListModel();
		lstData = new JList(puriModel);
		JScrollPane scrollPane = new JScrollPane(lstData);
		pane.add(scrollPane,c);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
		
		lstData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstData.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				btnRemove.setEnabled(lstData.getSelectedIndex() >= 0);
				btnLookup.setEnabled(lstData.getSelectedIndex() >= 0);
			}
		});
		
		
		setContentPane(pane);
		
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == btnAdd) {
			// let the user choose a file
			File open = GUICommons.chooseFile(this, "Select localization data file", "locdata", true);
			if (open != null) {
				LocalizationData data;
				// try to load them
				try {
					cursorWork();
					data = LocalizationDataReader.readLocalizationData(open);
					// add to list and vector
					LocalizationDataSetting setting = new LocalizationDataSetting(
							Tools.extractBaseFilename(open.getAbsolutePath()),data, open.getAbsolutePath());
					((DefaultListModel)lstData.getModel()).addElement(setting.label);
					settings.add(setting);
				} catch (Exception e1) {
					GUICommons.error("Could not read localization data from file. Reason:\n\n" + e1.getMessage());
				}
				cursorNoWork();
			}
		} else if (source == btnRemove) {
			// remove item from list and vector
			int index = lstData.getSelectedIndex();
			((DefaultListModel)lstData.getModel()).remove(index);
			settings.remove(index);
			
		} else if (source == btnLookup) {
			// lookup proteins
			DialogSettings dialogSettings = new DialogSettings("Lookup localizations");
			dialogSettings.addInfoLabel("Please enter one or more comma-separated protein IDs.");
			dialogSettings.addStringParameter("Protein IDs:", "");
			Object[] result = ParameterDialog.showDialog(dialogSettings);
			if (result != null) {
				cursorWork();
				
				LocalizationData locData = this.settings.get(lstData.getSelectedIndex()).data;
				// split up to get protein IDs
				String[] split = ((String)result[0]).split(",");
				// get all of them
				StringBuffer output = new StringBuffer();
				for (String strID : split) {
					strID = strID.trim();
					// get internal id
					int id = ProteinManager.getInternalID(strID);
					// lookup localizations and add to result
					Collection<Integer> locs = locData.getLocalizations(id);
					output.append(strID+":\t");
					if (locs == null)
						output.append("[none]");
					else {
						int index=0;
						for (int loc : locs) {
							output.append(locData.getLocalizationName(loc));
							if (index < locs.size() - 1)
								output.append(", ");
							index++;
						}						
					}
					output.append("\n");
				}
				// show it
				new TextWindow(this, "Localizations", output.toString());

				cursorNoWork();
			}
		}
		
	}
	

	private void cursorWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	private void cursorNoWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
}
