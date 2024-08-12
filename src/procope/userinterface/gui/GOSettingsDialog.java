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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import procope.data.networks.NetworkGenerator;
import procope.data.networks.ProteinNetwork;
import procope.evaluation.complexquality.go.FunctionalSimilarities;
import procope.evaluation.complexquality.go.FunctionalSimilaritiesSchlicker;
import procope.evaluation.complexquality.go.GOAnnotationReader;
import procope.evaluation.complexquality.go.GOAnnotations;
import procope.evaluation.complexquality.go.GONetwork;
import procope.evaluation.complexquality.go.TermSimilarities;
import procope.evaluation.complexquality.go.TermSimilaritiesSchlicker;
import procope.evaluation.complexquality.go.FunctionalSimilaritiesSchlicker.FunctionalSimilarityMeasure;
import procope.evaluation.complexquality.go.GONetwork.Namespace;
import procope.evaluation.complexquality.go.GONetwork.Relationships;
import procope.evaluation.complexquality.go.TermSimilaritiesSchlicker.TermSimilarityMeasure;
import procope.tools.Tools;
import procope.userinterface.gui.dialogs.DialogSettings;
import procope.userinterface.gui.dialogs.ParameterDialog;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

public class GOSettingsDialog extends JFrame implements ActionListener, TreeSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8855142320017262840L;

	JTree tree;
	DefaultMutableTreeNode root;
	DefaultTreeModel treeModel;

	JButton btnAdd, btnRemove;
	private ArrayList<GOSetting> settings;

	private JButton btnNetwork;
	private GUIMain parent;

	private WorkingDialog workingDialog;

	public GOSettingsDialog(GUIMain parent, ArrayList<GOSetting> settings) {
		super("GO settings");

		this.settings = settings;
		this.parent = parent;

		workingDialog = new WorkingDialog(this);;

		initializeGUI();
		// add existing settings
		for (GOSetting setting : settings)
			addToTree(setting);
		expandAll(tree);

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
		pane.add(btnAdd = new JButton("Add GO setting"), c);
		c.gridx = 1;
		pane.add(btnRemove = new JButton("Remove selected setting"), c);
		c.gridx = 2;
		pane.add(btnNetwork = new JButton("Create network"),c);
		// action listeners
		btnAdd.addActionListener(this);
		btnRemove.addActionListener(this);
		btnRemove.setEnabled(false);
		btnNetwork.addActionListener(this);
		btnNetwork.setEnabled(false);
		// tool tips
		btnAdd.setToolTipText("Add a new GO setting");
		btnRemove.setToolTipText("Remove the currently selected GO setting");
		btnNetwork.setToolTipText("Create a scores network from the currently selected GO setting");

		// generate the tree contents
		root = new DefaultMutableTreeNode("GO settings");

		// tree component
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1;
		c.gridwidth = 4;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		treeModel = new DefaultTreeModel(root);
		JScrollPane scrollPane = new JScrollPane(tree = new JTree(treeModel));
		pane.add(scrollPane,c);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// listener
		tree.addTreeSelectionListener(this);
		// customize
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(false);

		setContentPane(pane);


	}

	private void expandAll(JTree tree) {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}

	public void actionPerformed(final ActionEvent e) {
		new Thread() {
			public void run() {
				workingDialog.setWorker(this);
				Object source = e.getSource();
				if (source == btnAdd) {
					actionAdd();
				} else if (source == btnRemove) {
					actionRemove();
				} else if (source == btnNetwork) {
					actionNetwork();
				}

			}
		}.start();
	}

	private void actionAdd() {
		// ask for everything
		DialogSettings settings = new DialogSettings("New GO setting");
		settings.addInfoLabel("This creates a new GO settings consisting of a GO network, " +
		"annotations, a term similarity calculator and a functional similarity calculator.");
		settings.addFileParameter("GO network:");
		settings.addRadioOptions("Namespace:", "@biological process", "cellular component", "molecular function");
		settings.addRadioOptions("Relationships to follow:", "is_a", "part_of", "@both");
		settings.addFileParameter("Annotations file:");
		settings.addRadioOptions("GO term similarity:", "Resnik", "Lin", "@Relevance");
		settings.addRadioOptions("GO functional similarity: ", 
				"column/row max", "column/row average", "lord", "@total max");
		// show dialog
		Object[] result = ParameterDialog.showDialog(settings);
		if (result != null) {

			// get all variables
			String goFile = (String)result[0];
			int name = (Integer)result[1];
			int rel = (Integer)result[2];
			String annoFile = (String)result[3];
			int termChoice = (Integer)result[4];
			int funChoice = (Integer)result[5];

			// check if one of the files was not choosen
			if (goFile == null) {
				GUICommons.warning("You did not choose a GO network file.");
				return;
			}
			if (annoFile == null) {
				GUICommons.warning("You did not choose an annotations file.");
				return;
			}

			cursorWork();
			
			// create parameters map
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("gofile", goFile);
			parameters.put("annofile", annoFile);
			parameters.put("namespace", name+"");
			parameters.put("relationships", rel+"");
			parameters.put("term", termChoice+"");
			parameters.put("fun", funChoice+"");
			
			GOSetting newSetting = settingFromMap(parameters);
			this.settings.add(newSetting);
			addToTree(newSetting);

			cursorNoWork();
		}
	}

	private void addToTree(GOSetting setting) {
		int num = root.getChildCount()+1;
		DefaultMutableTreeNode settingRoot = new DefaultMutableTreeNode("GO setting " + num);
		for (String parameter : setting.strParameters) {
			settingRoot.add(new DefaultMutableTreeNode(parameter));
		}

		treeModel.insertNodeInto(settingRoot, root, root.getChildCount());
		expandAll(tree);

	}


	private void actionRemove() {
		cursorWork();
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		tree.getLastSelectedPathComponent();

		// get index of this node
		int index = root.getIndex(node);

		// remove node from the tree
		treeModel.removeNodeFromParent(node);
		// remove it from the list
		settings.remove(index);

		cursorNoWork();
	}

	private void actionNetwork() {
		if (GUICommons.yesNo("Calculating complete GO semantic similarity scores networks might take a while.\n\nContinue?")) {
			// do it in different thread

			workingShow("Calculating GO scores network...");

			// ask user
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
			tree.getLastSelectedPathComponent();

			// get index of this node
			int index = root.getIndex(node);
			// create network
			FunctionalSimilarities funSim = settings.get(index).funSim;
			ProteinNetwork net = NetworkGenerator.generateNetwork(funSim);
			// add to list
			parent.addNetwork(net, "From GO settings " + (index+1));

			workingHide();
		}

	}

	public void valueChanged(TreeSelectionEvent e) {
		// get the selected node
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		// if this node has no children => leaf, mark parent
		if (node != null && node.getChildCount() == 0) {
			Object[] path = new Object[2];
			path[0] = root;
			path[1] = node.getParent();
			tree.setSelectionPath(new TreePath(path) );
			node =(DefaultMutableTreeNode)node.getParent();
		}
		// if a node is selected and is has a parent and childern => correct one
		boolean isCorrect = node != null && node != root;
		// set remove button accordingly
		btnRemove.setEnabled(isCorrect);
		btnNetwork.setEnabled(isCorrect);
	}

	
	protected void workingShow(final String msg) {
		if (!workingDialog.isVisible())
			workingDialog.activate();
		workingDialog.setMessage(msg);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		enableGUI(false);
	}

	protected void workingHide() {
		workingDialog.setVisible(false);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		enableGUI(true);
	}
	
	private void cursorWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	private void cursorNoWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	private void enableGUI(boolean enabled) {
		tree.setEnabled(enabled);
		btnAdd.setEnabled(enabled);
		
		if (!enabled) {
			btnRemove.setEnabled(false);
			btnNetwork.setEnabled(false);
		} else
			// reactivate buttons according to user selection in tree
			valueChanged(null);
	}
	
	protected static GOSetting settingFromMap(Map<String,String> parameters) {
		
		ArrayList<String> strParameters = new ArrayList<String>();
		
		String goFile = parameters.get("gofile");
		String annoFile = parameters.get("annofile");
		
		// load annotations
		GOAnnotations annotations=null;
		try {
			annotations = GOAnnotationReader.readAnnotations(annoFile);
		} catch (Exception e) {
			GUICommons.warning("Could not load annotations. Reason:\n\n" + e.getMessage());
			return null;
		}
		strParameters.add("Annotations: " + Tools.extractFilename(annoFile));
		
		// get namespace
		int name = Integer.parseInt(parameters.get("namespace"));
		Namespace namespace = null;
		if (name == 0)
			namespace = Namespace.BIOLOGICAL_PROCESS;
		else if (name == 1)
			namespace = Namespace.CELLULAR_COMPONENT;
		else if (name == 2)
			namespace = Namespace.MOLECULAR_FUNCTION;
		strParameters.add("Namespace: " + namespace.toString());
		
		// get relationships
		int rel = Integer.parseInt(parameters.get("relationships"));
		Relationships relationships=null;
		if (rel == 0)
			relationships = Relationships.IS_A;
		else if (rel == 1)
			relationships = Relationships.PART_OF;
		else if (rel == 2)
			relationships = Relationships.BOTH;
		strParameters.add("Relationships: " + relationships.toString());
		
		// get term similarity measure
		int termChoice = Integer.parseInt(parameters.get("term"));
		TermSimilarityMeasure termSimMeasure = null;
		if (termChoice == 0)
			termSimMeasure = TermSimilarityMeasure.RESNIK;
		else if (termChoice == 1)
			termSimMeasure = TermSimilarityMeasure.LIN;
		else if (termChoice == 2)
			termSimMeasure = TermSimilarityMeasure.RELEVANCE;
		strParameters.add("Term similarity: " + termSimMeasure.toString());
		
		// get functional similarity measure
		int funChoice = Integer.parseInt(parameters.get("fun"));
		FunctionalSimilarityMeasure funSimMeasure = null;
		if (funChoice == 0)
			funSimMeasure = FunctionalSimilarityMeasure.COLROW_MAX;
		else if (funChoice == 1)
			funSimMeasure = FunctionalSimilarityMeasure.COLROW_AVERAGE;
		else if (funChoice == 2)
			funSimMeasure = FunctionalSimilarityMeasure.LORD;
		else if (funChoice == 3)
			funSimMeasure = FunctionalSimilarityMeasure.TOTALMAX;
		strParameters.add("Functional similarity: " + funSimMeasure.toString());
		
		// load the network
		GONetwork gonet = null;
		try {
			gonet = new GONetwork(goFile, namespace, relationships);
		} catch (Exception e) {
			GUICommons.warning("Could not load GO network. Reason:\n\n" + e.getMessage());
			return null;
		}
		// create term similarity calculator
		TermSimilarities termSim = new TermSimilaritiesSchlicker(gonet, annotations, termSimMeasure, true);
		// create functional similarity calculator
		FunctionalSimilarities funSim = new FunctionalSimilaritiesSchlicker(gonet, annotations, termSim, funSimMeasure);
		
		return new GOSetting(gonet, annotations, funSim, parameters, strParameters);
	}
	


}
