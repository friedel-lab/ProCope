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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jfree.chart.JFreeChart;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.networks.ProteinNetwork;
import procope.tools.ChartTools;
import procope.tools.namemapping.ProteinManager;
import procope.userinterface.gui.dialogs.DialogSettings;
import procope.userinterface.gui.dialogs.ParameterDialog;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class ShowComplexes extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3577975199045887764L;
	
	
	private JTextField txtLookup;
	private JTextArea txtComplexes;
//	private JButton btnShow;
	private JButton btnSave;
	private String name;
	private GUIMain parent;
	private JCheckBox chkScores;

	private boolean showScores=false;
	ProteinNetwork scoresNet=null;
	private JButton btnHistogram;
	private GUIComplexScorer scorer;
	private ComplexSet set;
	
	private ArrayList<Integer> complexIndices;


	public ShowComplexes(GUIMain parent, ComplexSet set, String name, GUIComplexScorer scorer) {
		super("Showing: " + name);
		// store rest
		this.name = name;
		this.parent = parent;
		this.scorer = scorer;
		this.set = set;
		// GUI
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initializeGUI();
		pack();
		setLocationRelativeTo(parent);
		showComplexes();
		setVisible(true);


	}

	private void initializeGUI() {

		// initialize
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5,5,5,5);

		// lookup controls
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		pane.add(new JLabel("Lookup proteins:"),c);
		c.gridx = 1;
		c.weightx = 1;
		c.gridwidth = 2;
		pane.add(txtLookup = new JTextField(),c);
		txtLookup.setPreferredSize(new Dimension(200, txtLookup.getPreferredSize().height));
		// show all
//		c.gridx = 2;
//		c.weightx = 0;
//		pane.add(btnShow = new JButton("Show"),c);


		// textbox & scrollpane
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 3;
		JScrollPane scrolling = new JScrollPane(txtComplexes = new JTextArea());
		pane.add(scrolling,c);
		scrolling.setPreferredSize(new Dimension(400,400));

		// show scores & 
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth  = 1;
		c.insets = new Insets(2,5,8,5);
		pane.add(chkScores = new JCheckBox("Show scores"),c);
		c.gridx=1;
		c.fill = GridBagConstraints.NONE;
		pane.add(btnHistogram = new JButton("Histogram"),c);
		// save button
		c.gridx = 2;
		c.anchor = GridBagConstraints.EAST;
		pane.add(btnSave = new JButton("Save"),c);

		scrolling.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrolling.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		txtComplexes.setEditable(false);
		txtComplexes.setLineWrap(true);
		txtComplexes.setWrapStyleWord(true);

		btnHistogram.setEnabled(false);

		// listeners
		txtLookup.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				// pressed enter?
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					showComplexes();
				}
			}
		});
		ActionListener actionListener = getActionListener();
//		btnShow.addActionListener(actionListener);
		btnSave.addActionListener(actionListener);
		chkScores.addActionListener(actionListener);
		btnHistogram.addActionListener(actionListener);

		// tooltips
		txtLookup.setToolTipText("Enter one or more comma-seperated protein IDs and press ENTER, leave empty to show all proteins");
		btnSave.setToolTipText("Save currently displayed complexes to the complex set list");
		chkScores.setToolTipText("Show complex scores using a scores network");
		btnHistogram.setToolTipText("Show a histogram of all currently displayed complex scores");
				
		// set content
		setContentPane(pane);
	}

	private ActionListener getActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource(); 
				/*if (source == btnShow) {
					// show 
					showComplexes();
				} else*/ if (source == btnSave) {
					// save
					saveComplexes();
				} else if (source == chkScores) {
					actionShowScores();
				} else if (source == btnHistogram) {
					// histogram
					showHistogram();
				}
			}
		};
	}

	private void actionShowScores() {
		// scores?
		if (chkScores.isSelected()) {
			if(scorer.showSettings()) {
				// show scores!
		//		scorer.updateScores();
				showScores = true;
			} else {
				showScores = false;
				chkScores.setSelected(false);
			}
		} else
			showScores = false;
		
		showComplexes();
		btnHistogram.setEnabled(showScores);
	
	}
	
	public void activateScores() {
		chkScores.setSelected(true);
		actionShowScores();
	}
	
	
	/*
	private boolean prepareScores() {
		// as the user for the score settings
		DialogSettings settings = new DialogSettings("Complex scores");
		settings.addInfoLabel("Complex scores will be calculated as the average edge score " +
				"between all protein members using a given scores network.");
		String[] netNames = parent.getNetworkNames().toArray(new String[0]);
		if (netNames.length == 0) {
			GUICommons.warning("No networks loaded!");
			return false;
		}
		netNames[0] = "@" + netNames[0];
		settings.addListParameter("Network:", netNames);
		settings.addCheckParameter("Calculate weighted overall sum?", true);
		settings.addCheckParameter("Ignore missing scores?", false);
		Object[] result = ParameterDialog.showDialog(this, settings);
		if (result != null) {
			// settings
			weighted = (Boolean)result[1];
			boolean ignoreMissing = (Boolean)result[2];
			// get the network
			ProteinNetwork net = parent.getNetwork((Integer)result[0]);

			// calculates the score
			for (ComplexWithScore complexWithScore : complexes) {
				float score = ComplexScoreCalculator.averageComplexScore(
						net, complexWithScore.complex, ignoreMissing);

				complexWithScore.score = score;
			}
			// calc and save average
			return true;
		} else
			return false;
	}
	 */

	private void showComplexes() {
		cursorWork();
		
		StringBuffer result = new StringBuffer();

		// restrict to list?
		HashSet<Integer> proteins = null;
		if (txtLookup.getText().trim().length() > 0) {
			proteins = new HashSet<Integer>();
			String[] prots = txtLookup.getText().split(",");
			for (String prot : prots) {
				prot = prot.trim();
				if (prot.length() > 0)
					proteins.add(ProteinManager.getInternalID(prot));
			}
		}

		// iterate over complexes
		int complexIndex=0;
		complexIndices = new ArrayList<Integer>();
		for (Complex complex: set) {
			// accept complex?
			boolean accept=false;
			if (proteins == null)
				accept = true;
			else {
				for (int protein : complex) {
					if (proteins.contains(protein)) {
						accept = true;
						break;
					}
				}
			}

			if (accept) {
				// show this complex
				int index=0;
				int complexSize = complex.size();
				for (int protein : complex) {
					result.append(ProteinManager.getLabel(protein));
					if (index < complexSize -1) result.append(", ");
					index++;
				}
				result.append("\n");
				if (showScores) {
					float score = scorer.getScore(complexIndex);
					result.append("Score: "); 
					// do not print NaN
					if (!Float.isNaN(score))
						result.append(score);
					else
						result.append("-");

					result.append("\n");

				}
				result.append("\n");
				complexIndices.add(complexIndex);
	
			}
			complexIndex++;
		}

		// overall average?
		if (showScores) {
			result.insert(0,"Overall average score: " + scorer.getAverageComplexesScore(complexIndices) + "\n\n");
		}

		txtComplexes.setText(result.toString());
		txtComplexes.setSelectionStart(0);
		txtComplexes.setSelectionEnd(0);
		
		cursorNoWork();
	}

	private void saveComplexes() {
		DialogSettings settings = new DialogSettings("Complex set name");
		settings.addStringParameter("Name for the new complex set:", name);
		Object[] result = ParameterDialog.showDialog(settings);
		if (result != null) {
			// get them now
			ComplexSet newSet = new ComplexSet();
			for (int index : complexIndices)
				newSet.addComplex(set.getComplex(index));
			// save them
			parent.addComplexSet(newSet, (String)result[0]);

		}
	}

	private void showHistogram() {
		cursorWork();
		// gather data
		double[] data = new double[complexIndices.size()];
		int index=0;
		for (int cIndex : complexIndices)
			data[index++] = scorer.getScore(cIndex);
		JFreeChart hist = ChartTools.generateHistogram( "Complex scores histogram", data, false, parent.numHistBins); 
		new ChartWindow(this, "Complex scores histogram", hist, false);
		
		cursorNoWork();
		
	}


	private void cursorWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	private void cursorNoWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
}
