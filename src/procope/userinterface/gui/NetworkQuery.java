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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import procope.data.networks.NetworkEdge;
import procope.data.networks.ProteinNetwork;
import procope.tools.BooleanExpression;
import procope.tools.InvalidExpressionException;
import procope.tools.namemapping.ProteinManager;
import procope.userinterface.gui.dialogs.DialogSettings;
import procope.userinterface.gui.dialogs.ParameterDialog;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class NetworkQuery extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4056287111676326642L;
	
	private String name;
	private ProteinNetwork network;

	private JTextField txtProtein1, txtProtein2, txtFilter;
	private JTable table;

	private HashMap<String, Integer> attributeToColumn;
	int nextColumn = 3;

	private String[] colNames;
	private JButton btnSave;

	GUIMain parent;
	private JLabel lblResult;


	public NetworkQuery(GUIMain parent, ProteinNetwork network, String name) {
		super("Network query tool: " + name);
		this.name = name;
		this.network = network;
		this.parent = parent;

		initializeGUI();
		pack();
		setLocationRelativeTo(parent);

		ArrayList<String> vecColNames = new ArrayList<String>();
		vecColNames.add("Protein 1");
		vecColNames.add("Protein 2");
		vecColNames.add("Weight");
		// map internal attributes to columns
		attributeToColumn = new HashMap<String, Integer>();
		for (String key : network.getAnnotationKeys()) {
			attributeToColumn.put(key, nextColumn++);
			vecColNames.add(key);
		}
		colNames = vecColNames.toArray(new String[0]);

		setVisible(true);

	}

	private void initializeGUI() {

		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;

		// add controls
		GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.WEST;
		c2.insets = new Insets(5,5,5,5);
		JPanel controls = new JPanel(new GridBagLayout());
		c2.weightx = 0;
		c2.fill = GridBagConstraints.BOTH;
		c2.gridy = 0;
		controls.add(new JLabel("Proteins 1: "), c2);
		c2.gridy++;
		controls.add(new JLabel("Proteins 2: "), c2);
		c2.gridy++;
		controls.add(new JLabel("Filter: "), c2);
		c2.gridy++;

		// text fields
		c2.weightx=1;
		c2.gridx = 1;
		c2.gridy=0;
		controls.add(txtProtein1 = new JTextField(), c2);
		c2.gridy++;
		controls.add(txtProtein2 = new JTextField(), c2);
		c2.gridy++;
		controls.add(txtFilter = new JTextField(), c2);
		// and the button and result string
		c2.gridx = 0;
		c2.gridy = 3;
		c2.gridwidth = 2;
		c2.anchor = GridBagConstraints.EAST;
		c2.insets = new Insets(5,5,9, 6 );

		JPanel btnResult = new JPanel(new BorderLayout());
		btnResult.add(lblResult = new JLabel(), BorderLayout.LINE_START);
		btnResult.add(btnSave = new JButton("Save result"), BorderLayout.LINE_END);
		controls.add(btnResult,c2);

		c.weighty = 0;
		pane.add(controls, c);
		// min size
		txtProtein1.setPreferredSize(new Dimension(100, txtProtein1
				.getPreferredSize().height));
		txtProtein2.setPreferredSize(txtProtein1.getPreferredSize());
		txtFilter.setPreferredSize(txtProtein1.getPreferredSize());
		btnSave.setPreferredSize(txtProtein1.getPreferredSize());
		Dimension dim = btnSave.getPreferredSize();
		btnSave.setPreferredSize(new Dimension((int)dim.getWidth()+30, txtProtein1.getPreferredSize().height));
		btnSave.setEnabled(false);

		// set key listeners
		KeyListener keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				// pressed enter?
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					displayQuery();
				}
			}
		};
		txtProtein1.addKeyListener(keyListener);
		txtProtein2.addKeyListener(keyListener);
		txtFilter.addKeyListener(keyListener);

		// action listener for the button
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveQuery();
			}
		});

		// create result table
		c.weighty = 1;
		c.gridy++;
		c.gridx=0;
		table = new JTable();
		//table.setPreferredSize(new Dimension(300, 300));
		// add with scrollpane
		JScrollPane scroll = new JScrollPane(table);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pane.add(scroll, c);
		
		// help button
		c.insets = new Insets(0,4, 4,2);
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridy++;
		c.anchor = GridBagConstraints.WEST;
		HelpButton help = new HelpButton(
				"Enter one or more (comma-seperated) protein IDs in the 'Proteins 1/2' textfields " +
				"to filter for the first and second proteins of an edge. This is especially " +
				"useful for directed networks where 'Proteins 1' refers to the source nodes " +
				"and 'Proteins 2' to the target nodes of the directed edges. \n\n" +
				"Both fields can be left blank to match any edges.\n\n" +
				"To filter you result edges by their edge weights or annotatios, enter a " +
				"filtering expression in the 'Filter' textfield. You find more information " +
				"about filtering in the documentation."
			);
		pane.add(help, c);
		
		
		// tooltips
		txtProtein1.setToolTipText("Enter protein IDs of source node or leave empty, press ENTER to query");
		txtProtein2.setToolTipText("Enter protein IDs of target node or leave empty, press ENTER to query");
		txtFilter.setToolTipText("Enter filtering expression or leave empty, press ENTER to query");
		btnSave.setToolTipText("Save currently displayed edges to the networks list");

		setContentPane(pane);
	}

	private void displayQuery() {
		
		cursorWork();

		ArrayList<NetworkEdge> allEdges = query();

		if (allEdges == null)  {
			btnSave.setEnabled(false);
			return;
		}

		// write to table
		DefaultTableModel model = new DefaultTableModel(colNames, allEdges.size() ) {
			private static final long serialVersionUID = 266394427425110127L;
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		int row = 0;
		for (NetworkEdge edge : allEdges) {
			model.setValueAt(ProteinManager.getLabel(edge.getSource()), row, 0);
			model.setValueAt(ProteinManager.getLabel(edge.getTarget()), row, 1);
			float weight=edge.getWeight();
			if (weight == weight) // not NaN
				model.setValueAt(weight, row, 2);
			// annotations?
			Map<String, Object> annos = edge.getAnnotations();
			for (String key : annos.keySet()) {
				model.setValueAt(annos.get(key), row, attributeToColumn.get(key)  );
			}
			row++;
		}
		table.setModel(model);

		btnSave.setEnabled(allEdges.size() > 0);

		String edges = allEdges.size() != 1 ? "edges" : "edge";
		lblResult.setText(allEdges.size() + " " + edges + " found");
	
		cursorNoWork();
	}

	private void saveQuery() {

		// get name for new network
		DialogSettings settings = new DialogSettings("Network name");
		settings.addStringParameter("Name for the new network:", name);
		Object[] result = ParameterDialog.showDialog(settings);
		if (result != null) {
			cursorWork();
			// query
			ArrayList<NetworkEdge> allEdges = query();

			if (allEdges == null)
				return;

			ProteinNetwork newNet = new ProteinNetwork(network.isDirected());

			for (NetworkEdge edge : allEdges) {
				newNet.setFullEdge(edge);
			}

			// save it now
			parent.addNetwork(newNet, (String)result[0]);
			GUICommons.info("Network saved.");
			
			cursorNoWork();
		}
	}

	private ArrayList<NetworkEdge> query() {

		// compile expression if needed
		String strexpr = txtFilter.getText().trim();
		BooleanExpression expression = null;
		if (strexpr.length() > 0) {
			try {
				expression = new BooleanExpression(strexpr);
			} catch (InvalidExpressionException e) {
				GUICommons.warning("Invalid expression. Problem:\n\n" + e.getMessage());
				txtFilter.requestFocus();
				txtFilter.setSelectionStart(0);
				txtFilter.setSelectionEnd(strexpr.length());
				return null;
			}
		}


		// get all edges
		ArrayList<NetworkEdge> allEdges = new ArrayList<NetworkEdge>();

		// get all to edges
		HashSet<Integer> idsTo = new HashSet<Integer>();
		String[] prots2 = txtProtein2.getText().split(",");
		for (String prot2 : prots2) {
			prot2 = prot2.trim();
			if (prot2.length() > 0)
				idsTo.add(ProteinManager.getInternalID(prot2));
		}

		
		// get from proteins
		Collection<Integer> prots1;
		if (txtProtein1.getText().trim().length() > 0) {
			String[] splitProts1 = txtProtein1.getText().split(",");
			// get from list
			prots1 = new ArrayList<Integer>();
			for (String str : splitProts1) 
				prots1.add(ProteinManager.getInternalID(str));
		} else {
			// get all
			prots1 = network.getProteins();
		}
		
		for (int prot1 : prots1) {
			Collection<NetworkEdge> edges =	(network.isDirected()) ?
					network.getDirectedNeighbors(prot1, true) :	
					network.getNeighbors(prot1);

			for (NetworkEdge edge : edges) {
				// edge in second list?
				if (idsTo.size() == 0 || idsTo.contains(edge.getTarget())) {
					// matches filter?
					if (expression == null)
						// no filter, just add
						allEdges.add(edge);
					else {
						// edge must match the filter!
						Map<String, Object> original = edge.getAnnotations();
						Map<String, Object> annotations;
						// add score if existing
						float score = edge.getWeight();
						if (score == score) { // not NaN
							annotations = new HashMap<String, Object>(original);
							annotations.put("@weight", score);
						} else
							annotations = original;

						if (expression.evaluate(annotations))
							allEdges.add(edge);
					}
				}
			}
			

		}

		return allEdges;

	}

	
	private void cursorWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	private void cursorNoWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

}
