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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import procope.tools.BooleanExpression;
import procope.tools.InvalidExpressionException;
import procope.tools.namemapping.ProteinManager;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */

public class ProteinAnnotationsWindow extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -883320059307042299L;
	private JTable table;
	private JTextField txtFilter;
	private JLabel lblNumbers;
	private int shown=0;

	public ProteinAnnotationsWindow(Frame parent) {
		super("Protein annotations");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initializeGUI();
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
		
		updateNumbersLabel();
	}
	
	private void initializeGUI() {
		
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.insets = new Insets(5,5,5,5);
		
		// load button
		JButton btnLoad;
		pane.add(btnLoad = new JButton("Load"), c);
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadAnnotations();
			}
		});
		btnLoad.setToolTipText("Load annotations from a file");
		
		// clear button
		c.gridx++;
		JButton btnClear;
		pane.add(btnClear = new JButton("Clear"), c);
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProteinManager.clearAnnotations();
				cleanUp();
			}
		});
		btnClear.setToolTipText("Delete all annotations");
		
		// filter field
		c.gridx++;
		pane.add(new JLabel("Filter:"), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		pane.add(txtFilter = new JTextField(), c);
		txtFilter.setToolTipText("Enter an annotation filtering expression. Leave empty to show all proteins.");
		txtFilter.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				// pressed enter?
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						query();
					} catch (InvalidExpressionException e1) {
						cleanUp();
						GUICommons.warning("Invalid filtering expression:\n\n" + e1.getMessage());
					}
				}
			}
		});
		
		// the table
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1;
		c.gridwidth = 4;
		table = new JTable();
		JScrollPane scroll = new JScrollPane(table);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pane.add(scroll, c);
		
		// label below
		c.insets = new Insets(2 ,5,5,5);
		c.gridy++;
		c.weighty=0;
		pane.add(lblNumbers = new JLabel(), c);
		
		// tooltips
		btnLoad.setToolTipText("Load and insert new set of protein annotations");
		btnClear.setToolTipText("Delete all loaded protein annotations");
		txtFilter.setToolTipText("Enter a filter expression or leave empty, press ENTER to query");
		
		setContentPane(pane);
	}
	
	private void cleanUp() {
		cursorWork();
		shown = 0;
		table.setModel(new DefaultTableModel());
		System.gc();
		updateNumbersLabel();
		cursorNoWork();
	}

	private void loadAnnotations() {
		File annoFile = GUICommons.chooseFile(this, "Choose annotations file", "general", true);
		if (annoFile != null) {
			// try to load annotations
			try {
				cursorWork();
				FileInputStream stream = new FileInputStream(annoFile);
				ProteinManager.loadProteinAnnotations(stream);
				stream.close();
				updateNumbersLabel();
				cursorNoWork();
				GUICommons.info("Annotations successfully loaded");
			} catch (Exception e) {
				GUICommons.error("Could not load protein annotations:\n\n" + e.getMessage());
			}
		}
	}
	
	private void cursorWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	private void cursorNoWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@SuppressWarnings("unchecked")
	private void query() throws InvalidExpressionException {

		cursorWork();
		Object[] q = queryProteins(txtFilter.getText());
		Vector<Integer> proteins = (Vector<Integer>)q[0];
		Vector<Map<String, Object>> allAnnotations = (Vector<Map<String, Object>>)q[1];
	
		// determine all annotation keys
		HashSet<String> annoKeys = new HashSet<String>();
		for (Map<String, Object> annos : allAnnotations)
			annoKeys.addAll(annos.keySet());
		Vector<String> annoKeyList = new Vector<String>(annoKeys);
		
		HashMap<String, Integer> attributeToColumn = new HashMap<String, Integer>();
		for (int i=0; i<annoKeyList.size(); i++)
			attributeToColumn.put(annoKeyList.get(i), i+1);
		
		// write to table
		annoKeyList.insertElementAt("Protein", 0);
		DefaultTableModel model = new DefaultTableModel(annoKeyList, proteins.size() ) {
			private static final long serialVersionUID = 266394427425110127L;
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		// iterate over them
		for (int i=0; i<proteins.size(); i++) {
			// get them
			int protein = proteins.get(i);
			Map<String, Object> annos = allAnnotations.get(i);
			// show them
			model.setValueAt(ProteinManager.getLabel(protein).toString(), i, 0);
			// annotations?
			for (String key : annos.keySet()) {
				model.setValueAt(annos.get(key), i, attributeToColumn.get(key));
			}
			
		}
		
		table.setModel(model);
		
		shown = proteins.size();
		updateNumbersLabel();
		
		cursorNoWork();
		
	}
	
	/**
	 * Returns a vector of proteins along with a vector of their annotations
	 * @throws InvalidExpressionException 
	 */
	static Object[] queryProteins(String filter) throws InvalidExpressionException {
		
		// compile the expression (if there is one)
		BooleanExpression exp = null;
		if (filter != null && filter.trim().length()>0) 
			exp = new BooleanExpression(filter);
		
		Vector<Integer> proteins = new Vector<Integer>();
		Vector<Map<String, Object>> allAnnotations = new Vector<Map<String,Object>>();
		// iterate over all annotations
		int protCount = ProteinManager.getProteinCount();
		for (int i=1; i<=protCount; i++) {
			Map<String, Object> annotations = ProteinManager.getAnnotations(i);
			if (annotations.size() > 0 && (exp==null || exp.evaluate(annotations))) {
				// add to list
				proteins.add(i);
				allAnnotations.add(annotations);
			}
		}
		
		return new Object[]{proteins, allAnnotations};
		
	}
	
	
	private void updateNumbersLabel() {
		lblNumbers.setText("Annotated proteins: "
				+ ProteinManager.getAnnotatedProteinCount() + ", shown: " + shown);
	}
	
	
	public static void main(String[] args) {
		
		new ProteinAnnotationsWindow(null);
		
		
		
	}
	
	
}
