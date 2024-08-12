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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import procope.data.networks.NetworkReader;
import procope.data.networks.ProteinNetwork;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;
import procope.userinterface.gui.dialogs.DialogSettings;
import procope.userinterface.gui.dialogs.ParameterDialog;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class NameMappingsDialog extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2892828549514088386L;

	private ArrayList<NameMapping> mappings;
	private JList lstMappings;
	private JButton btnAdd;
	private JButton btnRemove;

	public NameMappingsDialog(JFrame parent, ArrayList<NameMapping> mappings) {
		super("Name mappings");

		this.mappings = mappings;

		initializeGUI();
		// initial list
		for (NameMapping mapping : mappings)
			((DefaultListModel)lstMappings.getModel()).addElement(mapping.label);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(400,300));
		setResizable(false);
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
		pane.add(btnAdd = new JButton("Add name mapping"), c);
		c.gridx = 1;
		pane.add(btnRemove = new JButton("Remove selected mapping"), c);
		// action listeners
		btnAdd.addActionListener(this);
		btnRemove.addActionListener(this);
		btnRemove.setEnabled(false);
		// tooltips
		btnAdd.setToolTipText("Add new name mapping network");
		btnRemove.setToolTipText("Remove the currently selected name mappings");

		// list component
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1;
		c.gridwidth = 3;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		DefaultListModel model = new DefaultListModel();
		lstMappings = new JList(model);
		JScrollPane scrollPane = new JScrollPane(lstMappings);
		pane.add(scrollPane,c);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		lstMappings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstMappings.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				btnRemove.setEnabled(lstMappings.getSelectedIndex() >= 0);
			}
		});
		
		// info
		c.gridy = 2;
		c.weighty = 0.1;
		c.insets = new Insets(-5,5,-2,5);
		JMultilineLabel label = new JMultilineLabel();
		label.setText("Note: Changes to the name mapping settings will only " +
				"affect new data objects loaded from the file system.");
		pane.add(label, c);

		setContentPane(pane);

	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == btnAdd) {
			DialogSettings settings = new DialogSettings("Add name mapping");
			settings.addInfoLabel("Please select a directed network which contains the name mappings to be added.");
			settings.addFileParameter("Mapping network:");
			settings.addRadioOptions("Order in the file:", "@First the target identifier, then the synonym",
				"First the synonym, then the target identifier");
			Object[] result = ParameterDialog.showDialog(settings);
			if (result != null) {
				String file = (String)result[0];
				boolean targetFirst = ((Integer)result[1]).equals(0);
				if (file == null)
					GUICommons.warning("You did not select a name mapping file");
				else {
					try {
						// try to load it
						ProteinNetwork mapNet = NetworkReader.readNetwork(file, true);

						// add to list
						NameMapping newMapping = new NameMapping(mapNet, Tools.extractBaseFilename(file), targetFirst);
						mappings.add(newMapping);
						((DefaultListModel)lstMappings.getModel()).addElement(newMapping.label);
						// add to manager
						ProteinManager.addNameMappings(mapNet, targetFirst);
					} catch (Exception ex) {
						GUICommons.warning("Could not read directed network from file. Reason:\n\n" + ex.getMessage());
					}
				}
			} else if (source == btnRemove) {
				// remove selected name mapping
				int index = lstMappings.getSelectedIndex();
				((DefaultListModel)lstMappings.getModel()).remove(index);
				mappings.remove(index);
				// update protein manager
				updateManager(mappings);
			}
		}

	}
	
	/**
	 * Removes all mappings from the manager and adds the given list of mappings 
	 * (needed because no removal is possible otherwise)
	 */
	public static void updateManager(ArrayList<NameMapping> mappings) {
		ProteinManager.clearNameMappings();
		for (NameMapping mapping : mappings)
			ProteinManager.addNameMappings(mapping.mapNet, mapping.targetFirst);
	}

}
