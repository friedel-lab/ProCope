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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.event.MouseInputListener;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;

import procope.data.LocalizationData;
import procope.data.LocalizationDataReader;
import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.data.complexes.ComplexSetReader;
import procope.data.complexes.ComplexSetWriter;
import procope.data.networks.CombinationRules;
import procope.data.networks.NetworkGenerator;
import procope.data.networks.NetworkReader;
import procope.data.networks.NetworkWriter;
import procope.data.networks.ProteinNetwork;
import procope.data.networks.CombinationRules.CombinationType;
import procope.data.networks.CombinationRules.WeightMergePolicy;
import procope.data.petrinets.PetriNetCreator;
import procope.data.petrinets.ToPNetGenerator;
import procope.data.petrinets.XGMMLGenerator;
import procope.data.purifications.PurificationData;
import procope.data.purifications.PurificationDataReader;
import procope.data.purifications.PurificationDataWriter;
import procope.evaluation.comparison.BroheeSimilarity;
import procope.evaluation.comparison.ComplexMapping;
import procope.evaluation.comparison.ComplexMappings;
import procope.evaluation.comparison.ComplexSetComparison;
import procope.evaluation.comparison.NetworkComparison;
import procope.evaluation.comparison.Point;
import procope.evaluation.complexquality.Colocalization;
import procope.evaluation.complexquality.go.FunctionalSimilarities;
import procope.evaluation.complexquality.go.GOAnnotations;
import procope.evaluation.networkperformance.ComplexEnrichment;
import procope.evaluation.networkperformance.ROC;
import procope.evaluation.networkperformance.ROCCurve;
import procope.evaluation.networkperformance.ROCCurveHandler;
import procope.methods.clustering.Clusterer;
import procope.methods.clustering.HierarchicalClusteringTrees;
import procope.methods.clustering.HierarchicalLinkage;
import procope.methods.clustering.HierarchicalTreeNode;
import procope.methods.clustering.MCLParameters;
import procope.methods.clustering.MarkovClusterer;
import procope.methods.scores.DiceCoefficients;
import procope.methods.scores.HartCalculator;
import procope.methods.scores.PECalculator;
import procope.methods.scores.ScoresCalculator;
import procope.methods.scores.SocioAffinityCalculator;
import procope.tools.BooleanExpression;
import procope.tools.ChartTools;
import procope.tools.InvalidExpressionException;
import procope.tools.ProCopeException;
import procope.tools.Tools;
import procope.tools.math.CorrelationCoefficient;
import procope.tools.math.PearsonCoefficient;
import procope.tools.math.SpearmanCoefficient;
import procope.tools.namemapping.ProteinManager;
import procope.tools.userclasses.UserClusterer;
import procope.tools.userclasses.UserParameter;
import procope.tools.userclasses.UserScoresCalculator;
import procope.userinterface.cytoscape.CytoscapeAdapter;
import procope.userinterface.gui.dialogs.DialogSettings;
import procope.userinterface.gui.dialogs.FloatVerifier;
import procope.userinterface.gui.dialogs.IntVerifier;
import procope.userinterface.gui.dialogs.ParameterDialog;
import procope.userinterface.gui.dialogs.TextDialog;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

public class GUIMain extends JFrame {

	private static final long serialVersionUID = 5035540275412000922L;

	private static String likelyWorkingDirectory;

	private ArrayList<NetworkInList> networks;
	private ArrayList<ComplexSetInList> complexSets;
	private ArrayList<PurificationDataInList> purifications;
	private ArrayList<GOSetting> goSettings;
	private ArrayList<LocalizationDataSetting> locSettings;
	private ArrayList<NameMapping> nameMappings;

	private static final HierarchicalLinkage[] LINKAGES = new HierarchicalLinkage[] {
		HierarchicalLinkage.UPGMA, HierarchicalLinkage.SINGLE_LINK,
		HierarchicalLinkage.COMPLETE_LINK, HierarchicalLinkage.WPGMA };

	private JList lstNetworks;
	private JList lstComplexSets;
	private JList lstPurifications;
	private DropTargetListener lstNetworksDrop;
	private DropTargetListener lstComplexSetsDrop;
	private DropTargetListener lstPurificationsDrop;

	JPopupMenu popNetworks;
	JMenu mnuNetManipulate;
	JMenuItem mnuNetSave, mnuNetLoad, mnuNetCyto, mnuNetDispose, mnuNetCluster,
	mnuNetCutoff, mnuNetRename, mnuNetQuery, mnuNetFilter,
	mnuNetRestrictProteins, mnuNetMultiply, mnuNetRandomize,
	mnuNetDerivePuri, mnuNetHistogram, mnuNetMerge, mnuNetEnrichment,
	mnuNetROC, mnuNetCompare;

	JPopupMenu popComplexSets;
	JMenuItem mnuSetsLoad, mnuSetsSave, mnuSetsCyto, mnuSetsRename,
	mnuSetsDispose, mnuSetsBrohee, mnuSetsShow, mnuSetsMap,
	mnuSetsSizecut, mnuSetsScorecut, mnuSetsRestrictProteins,
	mnuSetsDecompose, mnuSetsRandomExchange, mnuSetsRandomRemap,
	mnuSetsInducedNet, mnuSetsColoc, mnuSetsGO, mnuSetsShared,
	mnuSetsHisto;

	private JPopupMenu popPurifications;
	JMenuItem mnuPuriLoad, mnuPuriSave, mnuPuriCyto, mnuPuriRename,
	mnuPuriDispose, mnuPuriScores, mnuPuriMerge, mnuPuriBaitPrey,
	mnuMainPetri;

	JMenuItem mnuMainGO, mnuMainLoc, mnuMainLoadSess, mnuMainSaveSess,
	mnuMainExit, mnuMainPetriXGMML, mnuMainPetriTopnet, mnuMainNameMap,
	mnuMainGZIP, mnuMainBins, mnuMainProtAnno, mnuMainWeb, mnuMainHelp,
	mnuMainAbout;

	private WorkingDialog workingDialog;

	private Config config;

	int numHistBins;

	private JMenu mnuNetQuality;

	private JMenu mnuSetsComparison;

	private JMenu mnuSetsQuality;

	private JMenu mnuSetsManipulate;

	private JMenu mnuSetsRandomize;

	private List<UserClusterer> userClusterers;

	private List<UserScoresCalculator> userCalculators;

	private boolean cytoscapeMode;
	private static final int DEF_BINS = 20;

	public GUIMain() {
		this(false);
	}

	public GUIMain(boolean cytoscapeMode) {

		super("ProCope GUI");

		this.cytoscapeMode = cytoscapeMode;

		Locale.setDefault(Locale.ENGLISH);

		// config 

		this.config = Config.getInstance();
		// number of histogram bins
		numHistBins = config.getIntVal("histbins", DEF_BINS);

		// GUI stuff
		setPreferredSize(new Dimension(500, 500));
		if (!cytoscapeMode)
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		else
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		generateNetworksPopup();
		generateComplexSetPopup();
		generatePurificationDataPopup();
		initializeGUI();
		workingDialog = new WorkingDialog(this);

		// data stuff
		networks = new ArrayList<NetworkInList>();
		complexSets = new ArrayList<ComplexSetInList>();
		purifications = new ArrayList<PurificationDataInList>();
		locSettings = new ArrayList<LocalizationDataSetting>();
		nameMappings = new ArrayList<NameMapping>();
		goSettings = new ArrayList<GOSetting>();

		// eventual user-defined classes
		try {
			this.userClusterers = loadUserClusterers();
		} catch (ProCopeException e) {
			GUICommons.warning("Could not load user clusterers from file\n" + 
					Tools.CLUSTERERSFILE + "\n\n" + e.getMessage() );
		}

		try {
			this.userCalculators = loadUserCalculators();
		} catch (ProCopeException e) {
			GUICommons.warning("Could not load user scores calculators from file\n" + 
					Tools.CALCULATORSFILE + "\n\n" + e.getMessage() );
		}

		pack();
		loadAndSetWindowPos();
		setVisible(true);

		questionMarkInfo();

	}

	private void initializeGUI() {

		MouseInputListener mil = new DragSelectionListener();

		GridBagConstraints c = new GridBagConstraints();
		JPanel pane = new JPanel(new GridBagLayout());
		c.gridx=0;
		c.gridy=0;
		c.weightx = 1;
		c.insets = new Insets(4,4,4,4);
		c.fill = GridBagConstraints.BOTH;

		// networks list
		c.weighty = 0;
		pane.add(new JLabel("Networks:"), c);
		c.gridy++;
		c.weighty = 2;
		DefaultListModel netModel = new DefaultListModel();
		JScrollPane lstNetworkScroll = new JScrollPane(lstNetworks = new JList(netModel));
		pane.add(lstNetworkScroll, c);
		lstNetworkScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		lstNetworkScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		c.gridy++;
		// add listener for popup menu
		lstNetworks.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showNetworksPopup(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		lstNetworks.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					actionNetworkDispose();
				}
			}
		});
		lstNetworks.addMouseListener(mil);
		lstNetworks.addMouseMotionListener(mil);
		// add drag&drop listener
		new DropTarget(lstNetworks, lstNetworksDrop = new FileDragDrop(this));

		// complex sets list
		c.weighty = 0;
		pane.add(new JLabel("Complex sets:"), c);
		c.gridy++;
		c.weighty = 2;
		DefaultListModel setModel = new DefaultListModel();
		JScrollPane lstComplexScroll = new JScrollPane(lstComplexSets = new JList(setModel));
		pane.add(lstComplexScroll, c);
		lstComplexScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		lstComplexScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// add listener for popup menu
		lstComplexSets.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showComplexSetsopup(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		lstComplexSets.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					actionSetsDispose();
				}
			}
		});
		lstComplexSets.addMouseListener(mil);
		lstComplexSets.addMouseMotionListener(mil);
		// add drag&drop listener
		new DropTarget(lstComplexSets, lstComplexSetsDrop = new FileDragDrop(this));
		c.gridy++;

		// purification data list
		c.weighty = 0;
		pane.add(new JLabel("Purification data sets:"),c);
		c.gridy++;
		c.weighty = 1;
		DefaultListModel puriModel = new DefaultListModel();
		JScrollPane lstPuriScroll = new JScrollPane(lstPurifications = new JList(puriModel));
		pane.add(lstPuriScroll, c);
		lstComplexScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		lstComplexScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// add listener for popup menu
		lstPurifications.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPurificationsPopup(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		lstPurifications.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					actionPuriDispose();
				}
			}
		});
		lstPurifications.addMouseListener(mil);
		lstPurifications.addMouseMotionListener(mil);
		// add drag&drop listener
		new DropTarget(lstPurifications, lstPurificationsDrop = new FileDragDrop(this));

		// create and add main menu
		this.setJMenuBar(generateMainMenus());

		// close listener
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// tell controller program is exitting
				actionExit();
			}

		});

		// tooltips
		lstNetworks.setToolTipText("List of protein networks, right-click to get a pop-up menu");
		lstComplexSets.setToolTipText("List of complex sets, right-click to get a pop-up menu");
		lstPurifications.setToolTipText("List of purification datasets, right-click to get a pop-up menu");

		setContentPane(pane);
	}

	public void reportFileDragDrop(final List<String> files, final DropTargetListener source) {


		// all of this should not run in the AWT event queue
		final GUIMain main = this;
		new Thread() {

			public void run() {

				try {
					workingSetWorker(this);

					// for which list?
					if (source == lstNetworksDrop) {

						// check which networks will be directed
						DialogSettings settings = new DialogSettings("Directed?");
						settings.addInfoLabel("Select the networks which should be loaded as directed networks.");
						for (String file : files) 
							settings.addCheckParameter(Tools.extractFilename(file), false);
						Object[] result = ParameterDialog.showDialog(main, settings);

						if (result != null) {

							// iterate over files and try to load them
							int index=0;
							for (String file : files) {
								workingShow("Loading " + Tools.extractFilename(file));
								NetworkInList net = loadNetwork(file, (Boolean)result[index]);
								if (net != null)
									addNetwork(net);
								index++;
							}
							workingHide();
						}

					} else if (source == lstComplexSetsDrop) {

						// iterate over files and try to load them
						for (String file : files) {
							workingShow("Loading " + Tools.extractFilename(file));
							ComplexSetInList set = loadComplexSet(file);
							if (set != null)
								addComplexSet(set);
						}
						workingHide();

					} else if (source == lstPurificationsDrop) {

						// iterate over files and try to load them
						for (String file : files) {
							workingShow("Loading " + Tools.extractFilename(file));
							PurificationDataInList data = loadPurificationData(file);
							if (data != null)
								addPurificationData(data);
						}
						workingHide();
					}
				} catch (OutOfMemoryError e) {
					outOfMemory();
				}
			}

		}.start();
	}

	private void actionExit() {
//		JOptionPane.showMessageDialog(null, "exiting");
		// save settings
		try {
			saveWindowPos();
			config.storeConfig();
		} catch (FileNotFoundException e) {
			System.err.println("Could not write config file " + Tools.CONFIGFILE);
		}
	}

	private void showNetworksPopup(Component invoker, int x, int y) {
		// something selected?
		boolean someSelected = lstNetworks.getSelectedIndices().length > 0;

		// set ability accordingly
		mnuNetSave.setEnabled(someSelected);
		mnuNetCyto.setEnabled(someSelected);
		mnuNetDispose.setEnabled(someSelected);
		mnuNetRename.setEnabled(someSelected);
		mnuNetQuery.setEnabled(someSelected);
		mnuNetManipulate.setEnabled(someSelected);
		mnuNetRandomize.setEnabled(someSelected);
		mnuNetDerivePuri.setEnabled(someSelected);
		mnuNetCluster.setEnabled(someSelected);
		mnuNetHistogram.setEnabled(someSelected);
		mnuNetQuality.setEnabled(someSelected);
		mnuNetMerge.setEnabled(someSelected);
		mnuNetCompare.setEnabled(someSelected);

		// now show the menu
		popNetworks.show(invoker, x, y);

	}

	private void showComplexSetsopup(Component invoker, int x, int y) {
		// something selected?
		boolean someSelected = lstComplexSets.getSelectedIndices().length > 0;

		// set ability accordingly
		mnuSetsSave.setEnabled(someSelected);
		mnuSetsCyto.setEnabled(someSelected);
		mnuSetsDispose.setEnabled(someSelected);
		mnuSetsRename.setEnabled(someSelected);
		mnuSetsShow.setEnabled(someSelected);
		mnuSetsComparison.setEnabled(someSelected);
		mnuSetsQuality.setEnabled(someSelected);
		mnuSetsManipulate.setEnabled(someSelected);
		mnuSetsRandomize.setEnabled(someSelected);
		mnuSetsInducedNet.setEnabled(someSelected);
		mnuSetsHisto.setEnabled(someSelected);

		// now show the menu
		popComplexSets.show(invoker, x, y);
	}

	private void showPurificationsPopup(Component invoker, int x, int y) {
		boolean someSelected = lstPurifications.getSelectedIndex() >= 0;

		// some menu items should not be shown if no item is selected
		mnuPuriSave.setEnabled(someSelected);
		mnuPuriCyto.setEnabled(someSelected);
		mnuPuriRename.setEnabled(someSelected);
		mnuPuriDispose.setEnabled(someSelected);
		mnuPuriScores.setEnabled(someSelected);
		mnuPuriMerge.setEnabled(someSelected);
		mnuPuriBaitPrey.setEnabled(someSelected);

		// now show the menu
		popPurifications.show(invoker, x, y);

	}

	private void generateNetworksPopup() {
		ActionListener listener = generateMenuListener();

		popNetworks = new JPopupMenu("Networks");
		popNetworks.add(mnuNetLoad = new JMenuItem("Load network from file"));
		popNetworks.add(mnuNetSave = new JMenuItem("Save network to file"));		
		popNetworks.add(mnuNetCyto = new JMenuItem("Export to Cytoscape"));
		popNetworks.add(new JSeparator());
		popNetworks.add(mnuNetRename = new JMenuItem("Rename network"));
		popNetworks.add(mnuNetDispose = new JMenuItem("Dispose network"));
		popNetworks.add(new JSeparator());
		popNetworks.add(mnuNetQuality = new JMenu("Quality"));
		popNetworks.add(mnuNetManipulate = new JMenu("Manipulate"));
		popNetworks.add(mnuNetRandomize = new JMenuItem("Randomize"));
		popNetworks.add(mnuNetDerivePuri = new JMenuItem("Derive purification data"));
		popNetworks.add(mnuNetMerge = new JMenuItem("Merge"));
		popNetworks.add(new JSeparator());
		popNetworks.add(mnuNetCluster = new JMenuItem("Cluster"));
		popNetworks.add(new JSeparator());
		popNetworks.add(mnuNetQuery = new JMenuItem("Query tool"));
		popNetworks.add(mnuNetHistogram = new JMenuItem("Histogram"));
		popNetworks.add(mnuNetCompare = new JMenuItem("Compare"));
		// manipulate sub menu
		mnuNetManipulate.add(mnuNetCutoff = new JMenuItem("Cut-off network"));
		mnuNetManipulate.add(mnuNetFilter = new JMenuItem("Filter network"));
		mnuNetManipulate.add(mnuNetRestrictProteins = new JMenuItem("Restrict to proteins"));
		mnuNetManipulate.add(mnuNetMultiply = new JMenuItem("Scalar multiplication"));
		// quality sub menu
		mnuNetQuality.add(mnuNetEnrichment = new JMenuItem("Complex enrichment"));
		mnuNetQuality.add(mnuNetROC = new JMenuItem("ROC curve"));

		// add all listeners
		mnuNetLoad.addActionListener(listener);
		mnuNetSave.addActionListener(listener);
		mnuNetCyto.addActionListener(listener);
		mnuNetRename.addActionListener(listener);
		mnuNetDispose.addActionListener(listener);
		mnuNetCluster.addActionListener(listener);
		mnuNetCutoff.addActionListener(listener);
		mnuNetQuery.addActionListener(listener);
		mnuNetFilter.addActionListener(listener);
		mnuNetRestrictProteins.addActionListener(listener);
		mnuNetMultiply.addActionListener(listener);
		mnuNetRandomize.addActionListener(listener);
		mnuNetDerivePuri.addActionListener(listener);
		mnuNetHistogram.addActionListener(listener);
		mnuNetMerge.addActionListener(listener);
		mnuNetEnrichment.addActionListener(listener);
		mnuNetROC.addActionListener(listener);
		mnuNetCompare.addActionListener(listener);
	}

	private JMenuBar generateMainMenus() {
		JMenuBar menuBar = new JMenuBar();

		// session menu
		JMenu session = new JMenu("Session");
		session.add(mnuMainLoadSess = new JMenuItem("Load session"));
		session.add(mnuMainSaveSess = new JMenuItem("Save session"));
		session.add(new JSeparator());
		session.add(mnuMainExit = new JMenuItem("Exit"));
		menuBar.add(session);

		// tools menu
		JMenu tools = new JMenu("Tools");
		tools.add(mnuMainProtAnno = new JMenuItem("Protein annotations"));
		tools.add(new JSeparator());
		tools.add(mnuMainNameMap = new JMenuItem("Name mappings"));
		tools.add(new JSeparator());
		tools.add(mnuMainGO = new JMenuItem("GO settings"));
		tools.add(mnuMainLoc = new JMenuItem("Localization data"));
		tools.add(new JSeparator());
		JMenu mnuPetri = new JMenu("Petri nets");
		tools.add(mnuPetri);
		mnuPetri.add(mnuMainPetri = new JMenuItem("Generate Petri net"));
		mnuPetri.add(new JSeparator());
		mnuPetri.add(mnuMainPetriXGMML = new JMenuItem("Convert to Cytoscape/XGMML"));
		mnuPetri.add(mnuMainPetriTopnet = new JMenuItem("Convert to ToPNet"));
		menuBar.add(tools);

		// options menu
		JMenu options = new JMenu("Options");
		options.add(mnuMainGZIP = new JCheckBoxMenuItem("Save data objects GZIPed"));
		options.add(mnuMainBins = new JMenuItem("Histogram bins"));
		menuBar.add(options);
		// read options from config
		mnuMainGZIP.setSelected(config.getIntVal("gzip", 0)==1);


		// help menu
		JMenu help = new JMenu("Help");
		help.add(mnuMainHelp = new JMenuItem("Help"));
		help.add(new JSeparator());
		help.add(mnuMainAbout = new JMenuItem("About"));
		help.add(mnuMainWeb = new JMenuItem("Web"));
		menuBar.add(help);

		// add all listeners
		ActionListener listener = generateMenuListener();

		mnuMainNameMap.addActionListener(listener);
		mnuMainPetri.addActionListener(listener);
		mnuMainPetriXGMML.addActionListener(listener);
		mnuMainPetriTopnet.addActionListener(listener);
		mnuMainGO.addActionListener(listener);		
		mnuMainLoc.addActionListener(listener);
		mnuMainLoadSess.addActionListener(listener);
		mnuMainSaveSess.addActionListener(listener);
		mnuMainExit.addActionListener(listener);
		mnuMainGZIP.addActionListener(listener);
		mnuMainBins.addActionListener(listener);
		mnuMainProtAnno.addActionListener(listener);
		mnuMainHelp.addActionListener(listener);
		mnuMainAbout.addActionListener(listener);
		mnuMainWeb.addActionListener(listener);
		return menuBar;

	}

	private ActionListener generateMenuListener() {
		final GUIMain window = this;
		return new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				new Thread() {

					public void run() {
						try {
							workingSetWorker(this);
							Object source = e.getSource();

							// *** MAIN MENU

							if (source == mnuMainProtAnno) {
								// protein annotations
								new ProteinAnnotationsWindow(window);
							} else if (source == mnuMainNameMap) {
								// name mappings
								new NameMappingsDialog(window, nameMappings);
							} else if (source == mnuMainPetri) {
								// generate petri net
								actionPetriNet();
							} else if (source == mnuMainPetriXGMML) {
								actionPetriNetConvert(1);
							} else if (source == mnuMainPetriTopnet) {
								actionPetriNetConvert(2);
							} else if (source == mnuMainGO) {
								// show GO settings dialog
								new GOSettingsDialog(window, goSettings);
							} else if (source == mnuMainLoc) {
								new LocSettingsDialog(window, locSettings);
							} else if (source == mnuMainExit) {
								actionExit();
								window.dispose();
							} else if (source == mnuMainSaveSess) {
								actionSaveSession();
							} else if (source == mnuMainLoadSess) {
								actionLoadSession();
							} else if (source == mnuMainGZIP) {
								// store setting
								config.setIntVal("gzip", mnuMainGZIP.isSelected() ? 1 : 0);
							} else if (source == mnuMainBins) {
								actionOptionBins();
							} else if (source == mnuMainHelp) {
								actionShowHelp();
							} else if (source == mnuMainAbout) {
								new AboutDialog(window);
							} else if (source == mnuMainWeb) {
								try {
									GUICommons.showBrowser(new URL(Tools.HOMEPAGE).toURI());
								} catch (Exception e1) {} // won't happen 
							}

							// *** NETWORKS

							if (source == mnuNetLoad) {
								// load network
								actionNetworkLoad();
							} else if (source == mnuNetSave) {
								// save network
								actionNetworkSave();
							} else if (source == mnuNetCyto) {
								// export to cytoscape
								actionNetworkCytoscape();
							} else if (source == mnuNetRename) {
								// rename
								actionNetworkRename();
							} else if (source == mnuNetDispose) {
								// dispose network
								actionNetworkDispose();
							} else if (source == mnuNetCluster) {
								// dispose network
								actionNetworkCluster();
							} else if (source == mnuNetCutoff) {
								// cutoff network
								actionNetworkCutoff();
							} else if (source == mnuNetRandomize) {
								// randomize network
								actionNetworkRandomize();
							} else if (source == mnuNetMultiply) {
								// scalar multiplication
								actionNetworkMultiply();
							} else if (source == mnuNetFilter) {
								// filter network
								actionNetworkFilter();
							} else if (source == mnuNetRestrictProteins) {
								// restrict to proteins
								actionNetworkRestrictProteins();
							} else if (source == mnuNetQuery) {
								// query tool
								actionNetworkQuery();
							} else if (source == mnuNetHistogram) {
								// histogram
								actionNetworkHistogram();
							} else if (source == mnuNetDerivePuri) {
								// derive purification data
								actionNetworkDerivePuri();
							} else if (source == mnuNetMerge) {
								// merge networks
								actionNetworksMerge();
							} else if (source == mnuNetEnrichment) {
								// complex enrichment
								actionNetworkEnrichment();
							} else if (source == mnuNetROC) {
								// ROC curve
								actionNetworkROC();
							} else if (source == mnuNetCompare) {
								// compare
								actionNetworkCompare();
							}

							// *** COMPLEX SETS
							if (source == mnuSetsLoad) {
								// load network
								actionSetsLoad();
							} else if (source == mnuSetsSave) {
								// save network
								actionSetsSave();
							} else if (source == mnuSetsCyto) {
								// save network
								actionSetsCytoscape();
							} else if (source == mnuSetsRename) {
								// rename network
								actionSetsRename();
							} else if (source == mnuSetsDispose) {
								// dispose network
								actionSetsDispose();
							} else if (source == mnuSetsInducedNet) {
								// get complex-induced network
								actionSetsInducedNet();
							} else if (source == mnuSetsRandomExchange) {
								// randomize by exchanging
								actionSetsRandomize(0);
							} else if (source == mnuSetsRandomRemap) {
								// randomize by remapping
								actionSetsRandomize(1);
							} else if (source == mnuSetsColoc) {
								// colocalization
								actionSetsColoc();
							} else if (source == mnuSetsGO) {
								// go semantic similarity
								actionSetsGO();
							} else if (source == mnuSetsSizecut) {
								// size cutoff
								actionSetsSizecut();
							} else if (source == mnuSetsRestrictProteins) {
								// restrict to proteins
								actionSetsRestrictProteins();
							} else if (source == mnuSetsDecompose) {
								actionSetsDecompose();
							} else if (source == mnuSetsShared) {
								actionSetsShared();
							} else if (source == mnuSetsBrohee) {
								actionSetsBrohee();
							} else if (source == mnuSetsShow) {
								actionSetsShow();
							} else if (source == mnuSetsHisto) {
								actionSetsHistogram();
							} else if (source == mnuSetsScorecut) {
								actionSetsScorecut();
							} else if (source == mnuSetsMap) {
								actionSetsMap();
							}

							// *** PURIFICATION DATA
							if (source == mnuPuriLoad) {
								// load data
								actionPuriLoad();
							} else if (source == mnuPuriSave) {
								// save data
								actionPuriSave();
							} else if (source == mnuPuriCyto) {
								// save data
								actionPuriCytoscape();
							} else if (source == mnuPuriRename) {
								// rename data
								actionPuriRename();
							} else if (source == mnuPuriDispose) {
								// dispose
								actionPuriDispose();
							} else if (source == mnuPuriScores) {
								// calculate some scores
								actionPuriScores();
							} else if (source == mnuPuriMerge) {
								// merge purifications
								actionPuriMerge();
							} else if (source == mnuPuriBaitPrey) {
								// get bait-prey interactions
								actionPuriBaitPrey();
							}

						} catch (Exception e) { 
							GUICommons.unexpectedError(e);

						} catch (OutOfMemoryError e) {
							outOfMemory();
						} finally {
							workingHide();
						}


					}



				}.start();
			}
		};
	}



	private void generateComplexSetPopup() {
		ActionListener listener = generateMenuListener();

		popComplexSets = new JPopupMenu("Complex sets");
		popComplexSets.add(mnuSetsLoad = new JMenuItem("Load set from file"));
		popComplexSets.add(mnuSetsSave = new JMenuItem("Save set to file"));
		popComplexSets.add(mnuSetsCyto = new JMenuItem("Export to Cytoscape"));
		popComplexSets.add(new JSeparator());
		popComplexSets.add(mnuSetsRename= new JMenuItem("Rename set"));
		popComplexSets.add(mnuSetsDispose = new JMenuItem("Dispose set"));
		popComplexSets.add(new JSeparator());

		popComplexSets.add(mnuSetsShow = new JMenuItem("Show complexes & scores"));
		popComplexSets.add(mnuSetsHisto = new JMenuItem("Show complex size histogram"));
		popComplexSets.add(new JSeparator());
		// comparison
		popComplexSets.add(mnuSetsComparison = new JMenu("Comparison"));
		mnuSetsComparison.add(mnuSetsBrohee = new JMenuItem("Brohee comparison"));
		mnuSetsComparison.add(mnuSetsMap = new JMenuItem("Map complexes"));
		// quality
		popComplexSets.add(mnuSetsQuality = new JMenu("Quality"));
		mnuSetsQuality.add(mnuSetsColoc = new JMenuItem("Colocalization"));
		mnuSetsQuality.add(mnuSetsGO = new JMenuItem("Semantic similarity (GO)"));
		// manipulate
		popComplexSets.add(mnuSetsManipulate = new JMenu("Manipulate"));
		mnuSetsManipulate.add(mnuSetsSizecut = new JMenuItem("Size cutoff"));
		mnuSetsManipulate.add(mnuSetsScorecut = new JMenuItem("Score cutoff"));
		mnuSetsManipulate.add(mnuSetsRestrictProteins = new JMenuItem("Restrict to proteins"));
		mnuSetsManipulate.add(mnuSetsDecompose = new JMenuItem("Decompose"));
		mnuSetsManipulate.add(mnuSetsShared = new JMenuItem("Add shared proteins"));
		// randomize
		popComplexSets.add(mnuSetsRandomize = new JMenu("Randomize"));
		mnuSetsRandomize.add(mnuSetsRandomExchange = new JMenuItem("by exchanging"));
		mnuSetsRandomize.add(mnuSetsRandomRemap = new JMenuItem("by remapping"));
		// rest
		popComplexSets.add(mnuSetsInducedNet = new JMenuItem("Get complex-induced network"));

		// add all listeners
		mnuSetsLoad.addActionListener(listener);
		mnuSetsSave.addActionListener(listener);
		mnuSetsCyto.addActionListener(listener);
		mnuSetsRename.addActionListener(listener);
		mnuSetsDispose.addActionListener(listener);
		mnuSetsBrohee.addActionListener(listener);
		mnuSetsShow.addActionListener(listener);
		mnuSetsHisto.addActionListener(listener);
		mnuSetsMap.addActionListener(listener);
		mnuSetsColoc.addActionListener(listener);
		mnuSetsGO.addActionListener(listener);
		mnuSetsSizecut.addActionListener(listener);
		mnuSetsScorecut.addActionListener(listener);
		mnuSetsRestrictProteins.addActionListener(listener);
		mnuSetsDecompose.addActionListener(listener);
		mnuSetsShared.addActionListener(listener);
		mnuSetsRandomExchange.addActionListener(listener);
		mnuSetsRandomRemap.addActionListener(listener);
		mnuSetsInducedNet.addActionListener(listener);
	}

	private void generatePurificationDataPopup() {
		ActionListener listener = generateMenuListener();

		popPurifications = new JPopupMenu("Purification data");
		popPurifications.add(mnuPuriLoad = new JMenuItem("Load purification data from file"));
		popPurifications.add(mnuPuriSave = new JMenuItem("Save purification data to file"));
		popPurifications.add(mnuPuriCyto = new JMenuItem("Export to Cytoscape"));
		popPurifications.add(new JSeparator());
		popPurifications.add(mnuPuriRename = new JMenuItem("Rename dataset"));
		popPurifications.add(mnuPuriDispose = new JMenuItem("Dispose dataset"));
		popPurifications.add(new JSeparator());
		popPurifications.add(mnuPuriScores = new JMenuItem("Calculate scores"));
		popPurifications.add(mnuPuriMerge = new JMenuItem("Merge"));
		popPurifications.add(mnuPuriBaitPrey = new JMenuItem("Get bait-prey interactions"));

		// add all action listeners
		mnuPuriLoad.addActionListener(listener);
		mnuPuriSave.addActionListener(listener);
		mnuPuriCyto.addActionListener(listener);
		mnuPuriRename.addActionListener(listener);
		mnuPuriDispose.addActionListener(listener);
		mnuPuriScores.addActionListener(listener);
		mnuPuriMerge.addActionListener(listener);
		mnuPuriBaitPrey.addActionListener(listener);
	}

	private void actionSetsLoad() {
		// show dialog
		File open = GUICommons.chooseFile(this, "Load complex set", "complexes", true);
		if (open != null) {

			// load complex set
			try {
				workingShow("Loading " + Tools.extractFilename(open.getAbsolutePath()) + "...");
				ComplexSetInList set = loadComplexSet(open.getAbsolutePath());
				if (set != null)
					addComplexSet(set);
			} catch (Exception e) {
				GUICommons.error("Could not load complex set from file. Reason:\n\n" + e.getMessage());
			} 
			workingHide();
		}
	}

	private void actionSetsSave() {
		if (lstComplexSets.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one complex set for saving.");
		else  {
			File open = GUICommons.chooseFile(this, "Save complex set", "complexes", false);
			if (open != null) {
				ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
				// write out
				try {
					workingShow("Saving complex set...");
					// get stream
					OutputStream out = new FileOutputStream(open.getAbsoluteFile());
					if (config.getIntVal("gzip", 0)==1)
						out = new GZIPOutputStream(out);
					ComplexSetWriter.writeComplexes(inList.set, out);
					out.close();
					workingHide();
					GUICommons.info("Complex set saved.");
				} catch (IOException e) {
					GUICommons.error("Could not save file. Reason:\n\n" + e.getMessage());
				}
			}
		}
	}

	private void actionSetsCytoscape() {
		if (lstComplexSets.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one complex set for saving.");
		else  {
			// select network
			ArrayList<String> lstNetworks = getNetworkNames();
			lstNetworks.add(0,"[none]");
			DialogSettings settings = new DialogSettings("Select network");
			settings.addInfoLabel("Select the network which will be used to annotate " +
			"the inner-complex edges with scores:");
			settings.addListParameter("Network:", lstNetworks.toArray(new String[0]));
			Object[] result = ParameterDialog.showDialog(settings);
			if (result != null) {
				ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
				// get the network
				int selNet = (Integer)result[0];
				ProteinNetwork annoNet = null;
				if (selNet > 0) 
					annoNet = networks.get(selNet-1).network;
				// do it
				if (!cytoscapeMode) {
					// export to file
					File open = GUICommons.chooseFile(this, "Export complex set", "complexes",	false);
					if (open != null) {
						// save network
						try {
							workingShow("Exporting complex set...");
							ComplexSetWriter.writeXGMML(inList.set, annoNet, open.getAbsolutePath());
							GUICommons.info("Complex set exported in XGMML format which can be imported in Cytoscape.\n\nEdge weights are stored as attribute 'weight'.");
						} catch (IOException e) {
							GUICommons.error("Could not save file. Reason:\n\n"	+ e.getMessage());
						}
						workingHide();
					}
				} else {
					// we are a plugin of cytoscape
					workingShow("Exporting complex set...");
					CytoscapeAdapter.exportComplexSet(inList.set, annoNet, inList.name);
					workingHide();
					GUICommons.info("Successfully send to Cytoscape.");
				}
			}

		}
	}

	private void actionSetsRename() {
		if (lstComplexSets.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one complex set for renaming.");
		else  {
			ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
			// ask user for new name
			DialogSettings settings = new DialogSettings("Rename complex set");
			settings.addStringParameter("New complex set name:", inList.name);
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				inList.name = (String)result[0];
				updateComplexSet(lstComplexSets.getSelectedIndex());
			}
		}
	}

	private void actionSetsDispose() {
		cursorWork();
		// delete all selected ones
		int[] toDelete = lstComplexSets.getSelectedIndices();
		for (int i=toDelete.length-1; i>=0; i--) {
			((DefaultListModel)lstComplexSets.getModel()).remove(toDelete[i]);
			complexSets.remove(toDelete[i]);
		}
		// now it would be a good time to collect the garbage
		System.gc();

		cursorNoWork();
	}

	private void actionSetsInducedNet() {
		workingShow("Calculating induced network...");
		// generate
		for (int index : lstComplexSets.getSelectedIndices()) {
			ComplexSetInList inList = complexSets.get(index);
			ProteinNetwork net = inList.set.getComplexInducedNetwork();
			// add to list
			addNetwork(net, "Induced by: " + inList.name);
		}

		workingHide();
	}

	private void actionSetsRandomize(int method) {
		workingShow("Randomizing");
		// generate
		for (int index : lstComplexSets.getSelectedIndices()) {
			ComplexSetInList inList = complexSets.get(index);
			ComplexSet random;
			if (method == 0) {
				// exchange

				random = inList.set.randomizeByExchanging();
			} else {
				// remap
				random = inList.set.randomizeByRemapping();
			}
			// add to list
			addComplexSet(random, inList.name + ", randomized");
		}
		workingHide();
	}

	private void actionSetsColoc() {

		if (lstComplexSets.getSelectedIndices().length > 1) {
			GUICommons.info("Please select only one complex set.");
		} else {
			// no data?
			if (locSettings.size() == 0) {
				if (GUICommons.yesNo("No localization data loaded.\n\nDo you want to do this now?"))
					new LocSettingsDialog(this, locSettings);
				return;
			}

			// localization data names
			ArrayList<String> labels = new ArrayList<String>();
			for (LocalizationDataSetting setting : locSettings)
				labels.add(setting.label);
			// ask user what kind of colocalization score he wants and how it is calculated
			DialogSettings settings = new DialogSettings("Colocalization score");
			settings.addListParameter("Localization data: ", labels.toArray(new String[0]));
			settings.addRadioOptions("Score type", "@Colocalization score", "PPV");
			Object[] result = ParameterDialog.showDialog(settings);
			if (result != null) { 
				cursorWork();
				LocalizationData data = locSettings.get((Integer)result[0]).data;
				ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
				new ShowComplexes(this, inList.set, inList.name,
						new GUIComplexScorer(inList.set, new Colocalization(data),
								true, this, "Colocalization")).activateScores();
				cursorNoWork();
			}
		}
	}

	private void actionSetsGO() {

		if (lstComplexSets.getSelectedIndices().length > 1) {
			GUICommons.info("Please select only one complex set.");
		} else {

			// no data?
			if (goSettings.size() == 0) {
				if (GUICommons.yesNo("No GO networks loaded.\n\nDo you want to do this now?"))
					new GOSettingsDialog(this, goSettings);
				return;
			}

			// get the network from the user
			DialogSettings settings = new DialogSettings("GO semantic similarity");
			settings.addInfoLabel("Which GO setting do you want to use?");
			// generate go setting names
			ArrayList<String> goNames = new ArrayList<String>();
			for (GOSetting setting : goSettings) {
				StringBuffer newName = new StringBuffer();
				for (int i=0; i<setting.strParameters.size(); i++) {
					newName.append(setting.strParameters.get(i));
					if (i<setting.strParameters.size()-1)
						newName.append(", ");
				}
				goNames.add(newName.toString());
			}
			settings.addListParameter("GO network:", goNames.toArray(new String[0]));
			Object[] result = ParameterDialog.showDialog(settings);

			if (result != null) {
				cursorWork();

				ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());

				// check whether any protein is annotated
				boolean somethingAnnotated = false;
				GOAnnotations annos = goSettings.get((Integer)result[0]).annos;
				for (Complex complex : inList.set) {
					for (int protein : complex) {
						Set<String> protAnnos = annos.getGOTerms(protein);
						if (protAnnos != null && protAnnos.size() > 0) {
							somethingAnnotated = true;
							break;
						}
					}
					if (somethingAnnotated) break;
				}

				// show scores
				if (somethingAnnotated) {
					FunctionalSimilarities funSim = goSettings.get((Integer)result[0]).funSim;
					GUIComplexScorer scorer = new GUIComplexScorer(inList.set, funSim, this, "GO semantic similarity");

					new ShowComplexes(this, inList.set, inList.name, scorer).activateScores();
				} else {
					GUICommons.warning("No protein of the selected complex set has a GO annotation.\n\n" +
							"Note that the identifier types in the GO annotation files and those in the complex \nset files " +
							"might differ. Consider using a name mapping set (which must be loaded before the GO data).\n\n" +
					"ProCope comes with a name mapping set for yeast in data/yeastmappings_YYMMDD.txt");
				}
				cursorNoWork();
			}
		}
	}

	private void actionSetsSizecut() {

		if (lstComplexSets.getSelectedIndices().length > 1) {
			GUICommons.info("Please select only one complex set.");
		} else {
			// get cutoff from user
			DialogSettings settings = new DialogSettings("Size cutoff");
			settings.addInfoLabel("All complexes which are larger or smaller than a given threshold " +
			"will be removed from the complex set.");
			settings.addIntegerParameter("Threshold: ", 2, new IntVerifier(0, Integer.MAX_VALUE));
			settings.addRadioOptions("Cutoff", "@below threshold", "above threshold");
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				workingShow("Cutting of complex set...");
				int cutoff= (Integer)result[0];
				boolean below = (Integer)result[1] == 0;
				ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
				boolean replace = GUICommons.yesNo("Replace existing complex set with new set?");
				String oldName = inList.name;
				String nameAdd = ", cutoff " + (below ? "below":"above")+" "+ cutoff;
				ComplexSet removed;
				if (replace) {
					removed = inList.set.removeComplexesBySize(cutoff, below);
					inList.name = oldName+nameAdd;
					updateComplexSet(lstComplexSets.getSelectedIndex());
				} else {
					ComplexSet newSet = inList.set.copy();
					removed = newSet.removeComplexesBySize(cutoff, below);
					addComplexSet(newSet, oldName+nameAdd);
				}

				if (GUICommons.yesNo("Add removed complexes to list?")) {
					String removedName = "Removed from " + oldName + nameAdd;
					addComplexSet(removed, removedName);
				}
				workingHide();
			}
		}

		//inList.set.removeComplexesBySize(cutoffSize, below)
	}


	private void actionSetsRestrictProteins() {
		if (lstComplexSets.getSelectedIndices().length > 1) {
			GUICommons.info("Please select only one complex set.");
		} else {
			// ask user from where to get the protein list
			DialogSettings settings = new DialogSettings("Restrict complex set to protein space");
			settings.addInfoLabel("This method filters out all complexes from the set whose members " +
			"are not in the given set of proteins.");
			settings.addRadioOptions("Source for proteins:", 
					"@Existing data object", "Enter manually", "Filtered protein list");
			settings.addRadioOptions("Restriction:", "@Full complex has to be contained", "Only one protein has to be contained");
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result == null) return;

			int option = (Integer)result[0];
			boolean fullCoverage = result[1].equals(0);
			// get list of proteins
			Set<Integer> proteins = null;
			switch (option) {
			case 0:
				// from existing object 
				proteins = getProteinsFromDataObject();
				break;
			case 1:
				// enter manually
				proteins = getProteinsManually();
				break;
			case 2:
				// filtered set
				proteins = getProteinsFromFilteredSet();
				break;
			}

			if (proteins != null && proteins.size() > 0) {
				workingShow("Restricting complex set...");
				//  now do the restriction
				ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
				ComplexSet newSet = inList.set.restrictToProteins(proteins,fullCoverage);

				String newName = inList.name + ", restricted";
				// replace or insert new
				boolean replace = GUICommons.yesNo("Replace existing complex set with new set?");
				if (!replace)
					addComplexSet(new ComplexSetInList(newSet, newName));
				else {
					inList.set = newSet;
					inList.name = newName;
					updateComplexSet(lstComplexSets.getSelectedIndex());
				}
				workingHide();
			}
		}
	}

	private void actionSetsDecompose() {
		if (lstComplexSets.getSelectedIndices().length > 1) {
			GUICommons.info("Please select only one complex set.");
		} else {

			ArrayList<String> networkNames = getNetworkNames();
			if (networkNames.size() == 0) {
				GUICommons.warning("No networks loaded!");
				return;
			}
			// ask user for network and score
			DialogSettings settings = new DialogSettings("Decompose complex set");
			settings.addInfoLabel("This method will decompose the complex set using a " +
					"scores network and a cutoff value. Edges within the complexes below " +
					"the given cutoff are removed, the resulting subcomplexes generate " +
			"the decomposed complex set.");
			settings.addListParameter("Scores network:", networkNames.toArray(new String[0]));
			settings.addFloatParameter("Cutoff:", 1.0f);
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				workingShow("Decomposing...");

				ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
				// get network
				ProteinNetwork net = networks.get((Integer)result[0]).network;
				// do decomposition
				ComplexSet newSet = inList.set.decompose(net, (Float)result[1]);

				// replace?
				boolean replace = GUICommons.yesNo("Replace existing complex set with new set?");
				String newName = inList.name+ ", decomposed (cutoff " + (Float)result[1] + ")"; 
				if (!replace)
					addComplexSet(new ComplexSetInList(newSet, newName));
				else {
					inList.set = newSet;
					inList.name = newName;
					updateComplexSet(lstComplexSets.getSelectedIndex());
				}
				workingHide();
			}
		}
	}

	private void actionSetsShared() {
		if (lstComplexSets.getSelectedIndices().length > 1) {
			GUICommons.info("Please select only one complex set.");
		} else {
			// get networks
			ArrayList<String> networkNames = getNetworkNames();
			if (networkNames.size() == 0) {
				GUICommons.warning("No networks loaded!");
				return;
			}
			// generate dialog
			DialogSettings settings = new DialogSettings("Add shared proteins");
			settings.addInfoLabel("This method should be applied to distinct complex sets. It adds " +
					"proteins to complexes they are not yet contained in if their average scores to the " +
			"members of the respective complex are sufficiently high.");
			settings.addListParameter("Scores network:", networkNames.toArray(new String[0]));
			settings.addRadioOptions("Method:", "@Friedel et al., 2007 (Bootstrap approach)", 
			"Pu et al., 2007");
			settings.setHelpText("Check out the documentation for information on how " +
			"these methods calculate the set of shared proteins.");
			Object[] result = ParameterDialog.showDialog(settings);

			if (result != null) {
				ProteinNetwork net = networks.get((Integer)result[0]).network;
				ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());

				// get parameters
				ComplexSet newSet = null; 
				String newName=inList.name;
				if (result[1].equals(0)) {
					// bootstrap, ask for the lambda
					DialogSettings btSettings = new DialogSettings("Parameters");
					btSettings.addFloatParameter("Lambda", 0.95f);
					Object[] btResult = ParameterDialog.showDialog(btSettings);
					if (btResult != null) {
						workingShow("Calculating shared proteins...");
						float lambda = (Float)btResult[0];
						newSet = inList.set.calculateSharedProteinsBootstrap(net, lambda);
						newName += ", shared proteins added; lambda=" + lambda; 
					}					

				} else {
					// pu
					DialogSettings puSettings = new DialogSettings("Parameters");
					puSettings.addFloatParameter("a", 1.5f);
					puSettings.addFloatParameter("b", -0.5f);
					Object[] puResult = ParameterDialog.showDialog(puSettings);
					if (puResult != null) {
						workingShow("Calculating shared proteins...");
						float a = (Float)puResult[0];
						float b = (Float)puResult[1];
						newSet = inList.set.calculateSharedProteinsPu(net, a, b);
						newName += ", shared proteins added; a=" + a+", b="+b; 
					}
				}


				if (newSet != null) {
					// replace?
					boolean replace = GUICommons.yesNo("Replace existing complex set with new set?");
					if (!replace)
						addComplexSet(new ComplexSetInList(newSet, newName));
					else {
						inList.set = newSet;
						inList.name = newName;
						updateComplexSet(lstComplexSets.getSelectedIndex());
					}
				}
				workingHide();
			}
		}
	}

	private void actionSetsBrohee() {
		// check if at least two complex sets are selected
		int[] selected = lstComplexSets.getSelectedIndices();
		if (selected.length < 2) {
			GUICommons.info("Please select at least 2 complex sets for comparison.");
		} else {
			workingShow("Comparing complex sets...");

			StringBuffer result = new StringBuffer();
			// gather complex sets
			ArrayList<ComplexSetInList> sets = new ArrayList<ComplexSetInList>();
			for (int index : selected)
				sets.add(complexSets.get(index));
			ComplexSetInList[] arrSets = sets.toArray(new ComplexSetInList[0]);  
			// iterate over all pairwise complex sets
			int count=0;
			int total = arrSets.length * (arrSets.length -1) / 2;
			for (int i=0; i<arrSets.length; i++) {
				for (int j=i+1; j<arrSets.length; j++) {
					// do the brohee
					BroheeSimilarity sim = ComplexSetComparison.broheeComparison(
							arrSets[i].set, arrSets[j].set);
					BroheeSimilarity simBack = ComplexSetComparison.broheeComparison(
							arrSets[j].set, arrSets[i].set);
					// add first direction to result
					result.append("Candidate: " + arrSets[i].name+ ", ");
					result.append("Reference: " + arrSets[j].name);
					result.append("\n");
					result.append(String.format("Sensitivity: %f, PPV: %f, Accuracy: %f", 
							sim.getSensitivity(), sim.getPPV(), sim.getAccuracy()));
					result.append("\n\n");
					result.append("Candidate: " + arrSets[j].name+ ", ");
					result.append("Reference: " + arrSets[i].name);
					result.append("\n");
					result.append(String.format("Sensitivity: %.4f, PPV: %.4f, Accuracy: %.4f", 
							simBack.getSensitivity(), simBack.getPPV(), simBack.getAccuracy()));
					result.append("\n\n");
					result.append(String.format("Average sensitivity: %f, average PPV: %f",
							(float)(Math.sqrt(sim.getSensitivity()*simBack.getSensitivity())), 
							(float)(Math.sqrt(sim.getPPV()*simBack.getPPV())))  );
					result.append("\n");
					// delimiter?
					if (count < total-1) {
						result.append("\n//\n\n");
					}

					count++;
				}
			}

			// show all results in text window
			new TextWindow(this, "Comparison results", result.toString());

			workingHide();
		}
	}

	private void actionSetsShow() {
		if (lstComplexSets.getSelectedIndices().length > 1) {
			GUICommons.info("Please select only one complex set.");
		} else {
			cursorWork();
			ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
			new ShowComplexes(this, inList.set, inList.name, new GUIComplexScorer(inList.set, this, "Choose network"));
			cursorNoWork();
		}
	}

	private void actionSetsHistogram() {
		if (lstComplexSets.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one complex set.");
		else  {
			ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
			// gather complex sizes
			double[] data = new double[inList.set.getComplexCount()];
			int index=0;
			for (Complex complex : inList.set)
				data[index++] = complex.size();

			boolean relative = GUICommons.yesNo("Show relative frequencies?");

			JFreeChart hist = ChartTools.generateHistogram("Histogram of "
					+ inList.name, data, relative, numHistBins); 
			new ChartWindow(this, "Histogram", hist, false);
		}
	}

	private void actionNetworkLoad() {
		// show dialog
		File open = GUICommons.chooseFile(this, "Load network", "networks", true);
		if (open != null) {
			// load network
			boolean directed = !GUICommons.yesNo("Load as undirected network?");

			try {
				workingShow("Loading " + Tools.extractFilename(open.getAbsolutePath())+"...");
				NetworkInList net = loadNetwork(open.getAbsolutePath(), directed);
				if (net != null)
					addNetwork(net);
			} catch (Exception e) {
				GUICommons.error("Could not load network from file. Reason:\n\n" + e.getMessage());
			} 
			workingHide();
		}
	}


	private void actionSetsScorecut() {
		if (lstComplexSets.getSelectedIndices().length > 1) {
			GUICommons.info("Please select only one complex set.");
		} else {  

			ArrayList<String> networkNames = getNetworkNames();
			if (networkNames.size() == 0) {
				GUICommons.warning("No networks loaded!");
				return;
			}
			// get network, cutoff value and ignore missing information
			DialogSettings settings = new DialogSettings("Score cutoff");
			settings.addInfoLabel("Removes all complexes from the complex set whose average edge score "
					+ "between all proteins of the complex regarding a given scores network is below the cutoff.");
			settings.addListParameter("Scores network:", networkNames.toArray(new String[0]));
			settings.addFloatParameter("Cutoff:", 1.0f);
			settings.addCheckParameter("Ignore missing scores?:", false);
			settings.setHelpText("The score of a complex is defined as the average of all " +
					"inner-complex edge scores. That is, for a complex of size n there will " +
					"be n*(n-1)/2 edge weights to be averaged.\n\n" +
					"When ignoring missing scores, edges which are not present in the network " +
			"will not get an implicit weight of 0 but will be completely ignored.");
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				workingShow("Cutting of complex set...");
				ComplexSetInList inList = complexSets.get(lstComplexSets.getSelectedIndex());
				// do it now
				ProteinNetwork scores = networks.get((Integer)result[0]).network;
				float cutoff= (Float)result[1];
				boolean replace = GUICommons.yesNo("Replace existing complex set with new set?");
				String oldName = inList.name;
				String nameAdd = ", score cutoff " + cutoff;
				ComplexSet removed;
				if (replace) {
					removed = inList.set.removeComplexesByScore(scores, cutoff);
					inList.name = oldName+nameAdd;
					updateComplexSet(lstComplexSets.getSelectedIndex());
				} else {
					ComplexSet newSet = inList.set.copy();
					removed = newSet.removeComplexesByScore(scores, cutoff);
					addComplexSet(newSet, oldName+nameAdd);
				}

				if (GUICommons.yesNo("Add removed complexes to list?")) {
					String removedName = "Removed from " + oldName + nameAdd;
					addComplexSet(removed, removedName);
				}
				workingHide();
			}
		}
	}

	private void actionSetsMap() {
		// ensure that exactly two complex sets are selected
		int[] selected = lstComplexSets.getSelectedIndices();
		if (selected.length != 2) {
			GUICommons.info("Please select exactly 2 complex sets for comparison.");
		} else {
			// get the two complex sets
			ComplexSetInList set1 = complexSets.get(selected[0]);
			ComplexSetInList set2 = complexSets.get(selected[1]);
			// get mapping settings
			DialogSettings settings = new DialogSettings("Complex mapping");
			settings.addInfoLabel("This method maps complexes of the two given complex sets by comparing their member overlaps.");
			settings.addRadioOptions("Mapping method:", 
					"@Only one mapping per complex",
					"Multiple mappings per complex",
					"Unambiguous mapping", 
			"Exact mapping (identical complexes)");
			settings.setHelpText("Only one mapping per complex - Each complex can only be mapped once. If there are multiple overlaps with complexes in the other set, the one with the largest overlap will be the mapping partner.\n\n" +
					"Multiple mappings per complex - Each complex will be mapped to all complexes in the other set for which the protein overlap is large enough.\n\n" +
					"Unambigous mapping - A complex is only mapped if it has only one mapping candidate in the other complex set.\n\n" +
			"Exact mapping (identical complexes) - Finds identical complexes between the two complex sets.");

			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				ComplexMappings mappings=null;
				int mappingChoice = (Integer)result[0];
				// what kind of mapping?
				if (mappingChoice <= 2) {
					// we need an overlap
					DialogSettings overlapSettings = new DialogSettings("Minimum overlap");
					overlapSettings.addInfoLabel("Please specify the minimum protein overlap " +
					"needed for two complexes to be mappable");
					overlapSettings.addIntegerParameter("Minimum overlap:", 2, new IntVerifier(1,Integer.MAX_VALUE));
					Object[] overlapResult = ParameterDialog.showDialog(overlapSettings);
					int minOverlap=-1;
					if (overlapResult != null) {
						workingShow("Mapping complexes...");
						// get overlap
						minOverlap = (Integer)overlapResult[0];
						// calculate
						switch (mappingChoice) {
						case 0:
							// 1:1 mapping
							mappings = ComplexSetComparison.mapComplexes(set1.set, set2.set, minOverlap);
							break;
						case 1:
							// multiple mappings
							mappings = ComplexSetComparison.mapComplexesMultiple(set1.set, set2.set, minOverlap);
							break;
						case 2:
							// consistently
							mappings = ComplexSetComparison.mapComplexesConsistently(set1.set, set2.set, minOverlap);
							break;
						}
					} else
						// user cancelled
						return;
				} else {
					workingShow("Mapping complexes...");
					// exact mapping
					mappings = ComplexSetComparison.mapComplexesExactly(set1.set, set2.set);
				}

				// generate result
				StringBuffer buffer = new StringBuffer();
				int complexCount1 = set1.set.getComplexCount();
				int complexCount2 = set2.set.getComplexCount();
				int mappingCount = mappings.size();
				int notMapped1 = mappings.getNonMappedComplexesA().size();
				int notMapped2 = mappings.getNonMappedComplexesB().size();
				// summary
				buffer.append(set1.name + ": " + complexCount1 + " complexes");
				buffer.append(String.format(", %.2f%% mapped", (float)(complexCount1-notMapped1) / (float)complexCount1 * 100f));
				buffer.append('\n');
				buffer.append(set2.name + ": " + complexCount2 + " complexes");
				buffer.append(String.format(", %.2f%% mapped", (float)(complexCount2-notMapped2) / (float)complexCount2 * 100f));
				buffer.append('\n');
				buffer.append("Total mappings: " + mappingCount + "\n");
				buffer.append('\n');
				buffer.append("Mappings:\n\n");
				// single mappings
				for (ComplexMapping mapping : mappings) {
					// get the complexes and sort them
					ArrayList<String> strComplex1 = translateComplex(set1.set.getComplex(mapping.getComplexInA()));
					ArrayList<String> strComplex2 = translateComplex(set2.set.getComplex(mapping.getComplexInB()));
					Collections.sort(strComplex1);
					Collections.sort(strComplex2);
					// write to result
					listToBuffer(strComplex1, buffer);
					buffer.append("\nto\n");
					listToBuffer(strComplex2, buffer);
					buffer.append("\nOverlap: " + mapping.getOverlap());
					buffer.append("\nJaccard-Index: " + set1.set.getComplex(
							mapping.getComplexInA()).calculateJaccardIndex(
									set2.set.getComplex(mapping.getComplexInB())) );
					buffer.append("\n\n\n");
				}
				// display in window
				new TextWindow(this, "Complex mappings", buffer.toString(), true);
				workingHide();
			}

		}
	}

	private ArrayList<String> translateComplex(Complex complex) {
		ArrayList<String> result = new ArrayList<String>();
		for (int protein : complex) 
			result.add(ProteinManager.getLabel(protein).toString());
		return result;

	}

	private void listToBuffer(ArrayList<String> list, StringBuffer buffer) {
		int index=0;
		for (String item : list) {
			buffer.append(item);
			if (index < list.size() - 1)
				buffer.append(" ");
		}
	}

	private void actionNetworkSave() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network for saving.");
		else {
			File open = GUICommons.chooseFile(this, "Save network", "networks", false);
			if (open != null) {
				NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
				// save network
				try {
					workingShow("Saving network...");
					// get stream
					OutputStream out = new FileOutputStream(open.getAbsoluteFile());
					if (config.getIntVal("gzip", 0)==1)
						out = new GZIPOutputStream(out);
					NetworkWriter.writeNetwork(inList.network, out);
					out.close();
					GUICommons.info("Network saved.");
				} catch (IOException e) {
					GUICommons.error("Could not save file. Reason:\n\n" + e.getMessage());
				} 
				workingHide();
			}
		}
	}

	private void actionNetworkCytoscape() {
		if (lstNetworks.getSelectedIndices().length > 1)
			GUICommons.info("Please select only one network for exporting.");
		else {
			if (!cytoscapeMode) {
				// export to file
				File open = GUICommons.chooseFile(this, "Export network", "networks",	false);
				if (open != null) {
					NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
					// save network
					try {
						workingShow("Exporting network...");
						NetworkWriter.writeXGMML(inList.network, open.getAbsolutePath());

						GUICommons.info("Network exported in XGMML format which can be imported in Cytoscape.\n\nEdge weights are stored as attribute 'weight'.");
					} catch (IOException e) {
						GUICommons.error("Could not save file. Reason:\n\n"	+ e.getMessage());
					}
					workingHide();
				}
			} else {
				// we are a plugin of cytoscape
				workingShow("Exporting network...");
				NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
				CytoscapeAdapter.exportNetwork(inList.network, inList.name);
				workingHide();
				GUICommons.info("Successfully send to Cytoscape.");
			}
		}
	}

	private void actionNetworkRename() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network for renaming.");
		else {
			NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
			// ask user for new name
			DialogSettings settings = new DialogSettings("Rename network");
			settings.addStringParameter("New network name:", inList.name);
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				inList.name = (String)result[0];
				updateNetwork(lstNetworks.getSelectedIndex());
			}
		}
	}

	private void actionNetworkRandomize() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network.");
		else {
			// get selected network
			NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
			int edges = inList.network.getEdgeCount();
			// get number of rewirings from dialog
			DialogSettings settings = new DialogSettings("Randomize network");
			settings.addInfoLabel("This method will randomize the network by rewiring. " +
					"Two random edges (a,b) and (c,d) are selected such that a!=b!=c!=d. " +
			"The new rewired edges (a,d) and (c,b) are inserted into the network.");
			settings.addInfoLabel("The selected network has " + edges + " edges.");
			settings.addIntegerParameter("Number of rewirings:", edges*10, new IntVerifier(1,Integer.MAX_VALUE));
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				workingShow("Randomizing network...");
				// do it
				int rewirings = (Integer)result[0];
				ProteinNetwork random = inList.network.randomizeByRewiring(rewirings);
				String newName = inList.name + ", randomized"; 
				addNetwork(new NetworkInList(random, newName));
				workingHide();
			}
		}
	}

	private void actionNetworkMultiply() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network.");
		else {
			// get multiplication factor from dialog
			DialogSettings settings = new DialogSettings("Scalar multiplication");
			settings.addInfoLabel("Each edge weight of the network will be multiplied with a given factor.");
			settings.addFloatParameter("Multiplication factor:", 1.0f);
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
				// do it
				float factor = (Float)result[0];
				String newName = inList.name + ", multiplied: " + factor; 
				// replace or insert new
				boolean replace = GUICommons.yesNo("Replace existing network with new network?");
				if (!replace) {
					// new
					workingShow("Multiplying...");
					ProteinNetwork multiplied = inList.network.copy();
					multiplied.scalarMultiplication(factor);
					addNetwork(new NetworkInList(multiplied, newName));
				} else {
					workingShow("Multiplying...");
					inList.network.scalarMultiplication(factor);
					inList.name = newName;
					updateNetwork(lstNetworks.getSelectedIndex());
				}
				workingHide();
			}
		}
	}

	private void actionNetworkFilter() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network.");
		else {
			NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
			// get filtering expression from dialog
			DialogSettings settings = new DialogSettings("Filter network");
			settings.addInfoLabel("Creates a filtered network that only contains edges which " +
			"fulfill the expression you enter below.");
			// generate attribute list
			StringBuffer buffer = new StringBuffer();
			for (String key : inList.network.getAnnotationKeys())
				buffer.append(key + ", ");
			buffer.append("@weight");
			settings.addInfoLabel("Attributes contained in the current network: " + buffer.toString());
			settings.addStringParameter("Expression:", "");
			settings.setHelpText("The function checks whether the edge annotations in the network " +
					"fulfill a given expression. Example for such a boolean expression:\n\n " +
					"value1 > 0.5 & value < 0.2\n\n" +
					"Edges which do not have the annotation 'value1' or 'value2' or do not contain " +
					"numeric values will never be matched.\n\n" +
			"Please read the documentation for detailed information on boolean expressions."); 

			// repeat until the user entered a valid expression
			boolean again=false;
			String userExpr = "";
			BooleanExpression expression = null;
			do {
				again=false;
				settings.setDefaultValue(2, userExpr);
				Object[] result = ParameterDialog.showDialog(this, settings);
				if (result != null) {
					userExpr = (String)result[0];
					// try to compile the expression
					try {
						expression = new BooleanExpression(userExpr);
					} catch (InvalidExpressionException e) {
						GUICommons.warning("Invalid expression. Problem:\n\n" + e.getMessage());
						again = true;
					}
				}
			} while (again);

			// got a valid expression?
			if (expression != null) {
				workingShow("Filtering network...");
				ProteinNetwork filtered = inList.network.getFilteredNetwork(expression);
				String newName = inList.name + ", filtered";
				// replace or insert new
				boolean replace = GUICommons.yesNo("Replace existing network with new network?");
				if (!replace)
					addNetwork(new NetworkInList(filtered, newName));
				else {
					inList.network = filtered;
					inList.name = newName;
					updateNetwork(lstNetworks.getSelectedIndex());
				}

				workingHide();
			}
		}
	}

	private void actionNetworkRestrictProteins() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network.");
		else {
			// ask user from where to get the protein list
			DialogSettings settings = new DialogSettings("Restrict network to protein space");
			settings.addInfoLabel("This method filters out all edges of the network whose nodes " +
			"are not in the given set of proteins.");
			settings.addRadioOptions("Source for proteins:", 
					"@Existing data object", "Enter manually",  "Filtered protein list");
			settings.addRadioOptions("Restriction:", "@Both nodes must be contained", "Only one node has to be contained");
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result == null) return;

			int option = (Integer)result[0];
			boolean fullCoverage = result[1].equals(0);
			// get list of proteins
			Set<Integer> proteins = null;
			switch (option) {
			case 0:
				// from existing object
				proteins = getProteinsFromDataObject();
				break;
			case 1:
				// enter manually
				proteins = getProteinsManually();
				break;
			case 2:
				// filtered set
				proteins = getProteinsFromFilteredSet();
				break;
			}

			if (proteins != null && proteins.size() > 0) {
				workingShow("Restricting network...");
				//  now do the restriction
				NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
				ProteinNetwork newNet = inList.network.restrictToProteins(proteins,fullCoverage);

				String newName = inList.name + ", restricted";
				// replace or insert new
				boolean replace = GUICommons.yesNo("Replace existing network with new network?");
				if (!replace)
					addNetwork(new NetworkInList(newNet, newName));
				else {
					inList.network = newNet;
					inList.name = newName;
					updateNetwork(lstNetworks.getSelectedIndex());	
				}

				workingHide();
			}
		}
	}

	private Set<Integer> getProteinsFromDataObject () { 
		Set<Integer> proteins=null;
		ArrayList<String> names = new ArrayList<String>();
		names.addAll(getNetworkNames());
		names.addAll(getComplexSetNames());
		names.addAll(getPurificationNames());
		DialogSettings objsettings = new DialogSettings("Select data object");
		objsettings.addListParameter("Select data object: ", names.toArray(new String[0]));
		Object[] objresult = ParameterDialog.showDialog(this, objsettings);
		if (objresult != null) {
			int index = (Integer)objresult[0];
			// get correct proteins set
			if (index < networks.size())
				proteins = networks.get(index).network.getProteins();
			else if (index >= networks.size() && index < networks.size() + complexSets.size()) {
				proteins = complexSets.get(index-networks.size()).set.getProteins();
			} else if (index >= networks.size() + complexSets.size()) {
				proteins = purifications.get(index-networks.size() - complexSets.size()).data.getProteins();
			}
		}
		return proteins;
	}

	@SuppressWarnings("unchecked")
	private Set<Integer> getProteinsFromFilteredSet() {
		Set<Integer> proteins=null;
		DialogSettings settings = new DialogSettings("Enter filter expression");
		settings.addInfoLabel("Please enter the filter expression which " +
		"protein annotations have to match in order to be in the list.");
		settings.addStringParameter("Filter expression:", "");
		Object[] result = ParameterDialog.showDialog(settings);

		if (result != null) {
			// get lists
			try {
				Object[] queryResult = ProteinAnnotationsWindow.queryProteins((String)result[0]);
				return new HashSet<Integer>( (Vector<Integer>)queryResult[0] );
			} catch (InvalidExpressionException e) {
				GUICommons.warning("Invalid expression. Problem:\n\n" + e.getMessage());
				return null;
			}
		}

		return proteins;
	}


	private Set<Integer> getProteinsManually() {
		Set<Integer> proteins=null;
		String ids = TextDialog.showDialog("Protein IDs", "Enter Protein IDs, comma seperated and/or with linebreaks:");
		if (ids == null) return null;
		try {
			proteins = new HashSet<Integer>();
			BufferedReader reader = new BufferedReader(new StringReader(ids));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split(",");
				for (String proteinID : split) {
					proteins.add(ProteinManager.getInternalID(proteinID.trim()));
				}
			}
			reader.close();
		} catch (IOException e) {
			// this is simply not going to happen
		}
		return proteins;
	}

	private void actionNetworkQuery() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network for querying.");
		else {
			NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
			new NetworkQuery(this, inList.network, inList.name);
		}
	}

	private void actionNetworkEnrichment() {

		// no data?
		if (complexSets.size() == 0) {
			GUICommons.warning("No complex sets loaded!");
			return;
		}

		DialogSettings settings = new DialogSettings("Complex enrichment");
		settings.addInfoLabel("The complex enrichment of a network is the quotient of the average " +
				"complex score of a given complex set and the average complex score of a randomized " +
		"version of that complex set.");
		settings.addListParameter("Complex set:", getComplexSetNames().toArray(new String[0]));
		settings.addIntegerParameter("Number of randomizations", 100, new IntVerifier(1, 1000000));
		settings.setHelpText("ProCope will divide the complex set score by the averaged complex set score of " +
				"the given number of randomized complex sets. More randomizations will minimize variation " +
		"in the complex enrichment calcuation result.");
		Object[] result = ParameterDialog.showDialog(settings);
		if (result != null) {
			workingShow("Calculating enrichment...");
			StringBuffer output = new StringBuffer();
			ComplexSetInList refInList = complexSets.get((Integer)result[0]);
			output.append("Reference complex set: " + refInList.name + "\n\n");
			output.append("Complex enrichment scores:\n");
			// calc enrichment for each selected network
			for (int index : lstNetworks.getSelectedIndices()) {
				NetworkInList inList = networks.get(index);
				float enrich = ComplexEnrichment.calculateComplexEnrichment(inList.network, refInList.set, (Integer)result[1], true); 
				output.append(inList.name+": " + enrich +"\n");
			}
			new TextWindow(this, "Complex enrichment", output.toString());
			workingHide();
		}
	}

	private void actionNetworkROC() {

		// no data?
		if (complexSets.size() == 0) {
			GUICommons.warning("No complex sets loaded!");
			return;
		}

		// get complex set from user
		DialogSettings settings = new DialogSettings("ROC curves");
		settings.addListParameter("Reference set:", getComplexSetNames().toArray(new String[0]));
		// reference set for negative set creation
		String[] negRefs = new String[complexSets.size()+1];
		negRefs[0] = "@[same as above]";
		int index=1;
		for (ComplexSetInList inList : complexSets)
			negRefs[index++] = inList.name;
		settings.addListParameter("Reference for negative set:", negRefs);
		// localization data
		String[] locs = new String[locSettings.size()+1];
		locs[0] = "@[none]";
		index=1;
		for (LocalizationDataSetting setting : locSettings)
			locs[index++] = setting.label;
		settings.addListParameter("Localization data:", locs);
		settings.addCheckParameter("Only use network proteins", false);
		settings.addFloatParameter("False-positive rate cutoff (empty for none):", Float.NaN);
		// help
		settings.setHelpText("Sometimes it is necessary to use a complex set for true-positive " +
				"determination in which very large complexes were removed. On the other hand, " +
				"to determine the negative edge set, all complexes should be considered. This is " +
				"why you can distinguish between the 'Reference set' and the 'Reference for negative set'\n\n" +
				"The 'Only use network proteins' option determines whether the positive and negative sets are " +
				"restricted to proteins which are present in at least one of the score networks.\n\n"+
		"For detailed information about ROC curve calculation, please read the ProCope documentation. ");

		// show the dialog
		Object[] result = ParameterDialog.showDialog(settings);
		if (result != null) {
			workingShow("Calculating ROC curve...");
			int refChoice = (Integer)result[0];
			int negChoice = (Integer)result[1];
			int locChoice = (Integer)result[2];
			boolean restrictProteins = (Boolean)result[3];
			float fpCutOff = (Float)result[4];
			if (Float.isNaN(fpCutOff)) fpCutOff = Float.POSITIVE_INFINITY;
			// get the complex sets
			ComplexSet reference = complexSets.get(refChoice).set;
			ComplexSet referenceNegative = null;
			if (negChoice > 0)
				referenceNegative = complexSets.get(negChoice-1).set;
			else
				referenceNegative = reference;
			// get the localization data
			LocalizationData locData = null;
			if (locChoice > 0)
				locData = locSettings.get(locChoice-1).data;
			// construct the vector of networks
			ArrayList<ProteinNetwork> nets = new ArrayList<ProteinNetwork>();
			ArrayList<String> netNames = new ArrayList<String>();
			for (int netIndex : lstNetworks.getSelectedIndices()) {
				nets.add(networks.get(netIndex).network);
				netNames.add(networks.get(netIndex).name);
			}
			// calculate the ROC curve
			List<ROCCurve> rocs = ROC.calculateROCCurves(nets, reference,
					referenceNegative, locData, restrictProteins);
			JFreeChart chart = ROCCurveHandler.generateChart(rocs, netNames, fpCutOff);
			new ChartWindow(this, "ROC curves", chart, true);
			workingHide();
		}
	}

	private void actionNetworkCompare() {
		if (lstNetworks.getSelectedIndices().length != 2) 
			GUICommons.info("Please select exactly two networks for comparison.");
		else {

			// ask the user what to do
			DialogSettings settings = new DialogSettings("Network comparison");
			settings.addCheckParameter("Show plot?", config.getIntVal("netcomp_plot", 1)==1);
			settings.addCheckParameter("Show correlation coefficients?", config.getIntVal("netcomp_cor", 1)==1);
			settings.addCheckParameter("Save results to file?", config.getIntVal("netcomp_file", 0)==1);
			Object[] result = ParameterDialog.showDialog(settings);

			if (result != null) { 

				workingShow("Comparing networks...");

				boolean plot = (Boolean)result[0];
				boolean cor = (Boolean)result[1];
				boolean file = (Boolean)result[2];				
				// store to config
				config.setIntVal("netcomp_plot", plot?1:0);
				config.setIntVal("netcomp_cor", cor?1:0);
				config.setIntVal("netcomp_file", file?1:0);

				int[] selected = lstNetworks.getSelectedIndices();
				// get both networks
				NetworkInList net1 = networks.get(selected[0]);
				NetworkInList net2 = networks.get(selected[1]);
				// do the comparison
				List<Point> points = NetworkComparison.weightsOverlap(net1.network, net2.network, false);

				if (plot) {
					// convert to array
					float[][] data = new float[2][points.size()];
					int index=0;
					for (Point p : points) {
						data[0][index] = p.getX();
						data[1][index] = p.getY();
						index++;
					}
					// plot it
					final NumberAxis domainAxis = new NumberAxis(net1.name);
					domainAxis.setAutoRangeIncludesZero(false);
					final NumberAxis rangeAxis = new NumberAxis(net2.name);
					rangeAxis.setAutoRangeIncludesZero(false);
					final FastScatterPlot plotObj = new CustomizableScatterPlot(data, domainAxis, rangeAxis ,2);
					plotObj.setPaint(Color.BLUE);

					final JFreeChart chart = new JFreeChart("Network comparison", plotObj);
					chart.setAntiAlias(false);
					new ChartWindow(this, "Network comparison", chart, true);
				}

				if (cor) {
					// calculate correlations
					CorrelationCoefficient pearson = new PearsonCoefficient();
					CorrelationCoefficient spearman = new SpearmanCoefficient();
					pearson.feedData(points);
					spearman.feedData(points);

					// show it
					new TextWindow(this, "Network comparison - correlation", 
							net1.name + " - " + net2.name +"\n\n" + 
							"Pearson correlation: " + pearson.getCorrelationCoefficient() + "\n" +
							"Spearman correlation: " + spearman.getCorrelationCoefficient() + "\n");
				}

				if (file) {
					try {
						File outFile = GUICommons.chooseFile(this, "Save comparison", "general", false);
						if (outFile != null) {
							PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));
							for (Point p : points) 
								writer.println(p.getX() + "\t" + p.getY());
							writer.close();
						}
					} catch (IOException e) {
						GUICommons.error("Could not save comparison data to file. Reason:\n\n" + e.getMessage());
					}
				}

				workingHide();

			}
		}

	}

	private void actionNetworkHistogram() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network.");
		else  {
			NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
			ProteinNetwork net = inList.network;
			ArrayList<String> attributes = new ArrayList<String>();
			attributes.add("@@weight");
			attributes.addAll(net.getAnnotationKeys());
			// for which attribute do we calculate the histogram?
			DialogSettings settings = new DialogSettings("Choose attribute");
			settings.addInfoLabel("Choose the attribute for which the histogram will be plotted.");
			settings.addListParameter("Attribute:", attributes.toArray(new String[0]));
			settings.addCheckParameter("Show relative frequencies:", false);
			settings.setHelpText("The @weight attribute represents the edge weights in the network.");
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				workingShow("Calculating histogram...");

				// weight or annotation?
				boolean useWeight;
				String key;
				int choice = (Integer)result[0];
				if (choice == 0) {
					useWeight = true;
					key = null;
				} else {
					useWeight = false;
					key = attributes.get(choice);
				}

				// iterate over the network and gather our data
				boolean nonNumeric=false;
				int[] edges = net.getEdgesArray();
				double[] data = new double[edges.length / 2];
				int index=0;
				for (int i=0; i<edges.length; i+=2) {
					// get score and annotations
					if (useWeight) {
						float weight = net.getEdge(edges[i], edges[i+1]);
						if (weight == weight) // not NaN
							data[index++] = weight;
					} else {
						Map<String, Object> annotations = net.getEdgeAnnotations(edges[i], edges[i+1]);
						// required value contained?
						Object value = annotations.get(key);
						if (value != null) {
							// float or integer?
							if (value instanceof Float)
								data[index++] = (Float)value;
							else if (value instanceof Integer)
								data[index++] = (Integer)value;
							else {
								// skip
								nonNumeric = true;
							}
						}
					}
				}

				if (nonNumeric) 
					// show warning
					GUICommons.warning("Attribute '" + key + "' has at least one non-numeric value in the network.\n" +
					"These values were skipped.");

				if (index == 0) {
					GUICommons.warning("No data to be shown.");
				} else {
					data = Tools.arrCopyOf(data, index);
					// show histogram with data we have collected
					JFreeChart hist = ChartTools.generateHistogram("Histogram of "
							+ inList.name, data, (Boolean) result[1], numHistBins); 
					new ChartWindow(this, "Histogram", hist, false);
				}
				workingHide();
			}
		}
	}

	private void actionNetworkDerivePuri() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network for saving.");
		else {
			NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
			// network has to be directed
			if (!inList.network.isDirected()) {
				// convert to directed?
				GUICommons.warning("Cannot derive purification data from an undirected network");
			} else {
				// ask for the pooling of baits
				DialogSettings settings = new DialogSettings("Derive purification data");
				settings.addInfoLabel("This method derives a purification data set from a directed network. " +
						"Each edge in the network is treated as one bait-prey interaction. " +
						"When pooling baits, all bait-prey interactions for a given bait will be " +
						"pooled into a single experiment. If baits are not pooled, one experiment for " +
				"each edge in the network will be derived.");
				settings.addCheckParameter("Pool baits?", true);
				Object[] result = ParameterDialog.showDialog(settings);
				if (result != null) {
					boolean pool = (Boolean)result[0];
					// derive
					workingShow("Deriving purification data...");
					PurificationData derived = inList.network.derivePurificationData(pool);
					// add to list
					String newName = "Derived from " + inList.name;
					if (!pool)
						newName += ", baits not pooled";
					addPurificationData(derived, newName);
					workingHide();
				}
			}
		}
	}

	private void actionNetworksMerge() {
		// two networks must be selected
		if (lstNetworks.getSelectedIndices().length != 2) {
			GUICommons.info("Please select exactly two networks to be merged.");
		} else {
			int[] selected = lstNetworks.getSelectedIndices();
			NetworkInList net1 = networks.get(selected[0]);
			NetworkInList net2 = networks.get(selected[1]);
			// notify user if one is directed and the other one not
			if (net1.network.isDirected() != net2.network.isDirected()) {
				GUICommons.info("Note: When merging a directed with an undirected network, " +
				"the resulting network will be undirected.");
			}
			// get all networks except the selected ones (=> mapping candidates)
			ArrayList<NetworkInList> mappingCandidates = new ArrayList<NetworkInList>(networks);
			mappingCandidates.remove(net1);
			mappingCandidates.remove(net2);
			String[] arrMapCand = new String[mappingCandidates.size() + 1];
			arrMapCand[0] = "@[no mapping]";
			int arrMapIndex=1;
			for (NetworkInList cand : mappingCandidates)
				arrMapCand[arrMapIndex++] = cand.name;
			// ask everything from the user
			DialogSettings settings = new DialogSettings("Merge networks");
			settings.addRadioOptions("Combination of proteins:", "@Merge", "Intersect");
			settings.addRadioOptions("Edge weights:",  "Average weights", "Add weights", "@Annotate weights");
			settings.addListParameter("Mapping network:", arrMapCand);
			settings.setHelpText("Merge/Intersect: Defines whether the set of nodes of both networks is merged or " +
					"if the intersection is calculated. For the latter, only edges which lie in this intersection set " +
					"will be contained in the resulting network.\n\n" +
					"Edge weights: Defines how the edge weights values are combined if a certain " +
					"edge weight is defined for both networks.\n\n" +
					"Mapping network: Nodes which are connected in the mapping network are combined into single " +
					"nodes during the combination process. This can be useful e.g. when combining networks from " +
			"different organsism.");
			Object[] result = ParameterDialog.showDialog(settings);
			if (result != null) {
				// get the options
				CombinationType combiType = result[0].equals(0) ? CombinationType.MERGE : CombinationType.INTERSECT;
				int edgeChoice = (Integer)result[1];
				WeightMergePolicy weightMerge = null;
				if (edgeChoice == 0) 
					weightMerge = WeightMergePolicy.AVERAGE;
				else if (edgeChoice == 1)
					weightMerge = WeightMergePolicy.ADD;
				else if (edgeChoice == 2)
					weightMerge = WeightMergePolicy.ANNOTATE_WEIGHTS;
				int mapChoice = (Integer)result[2];
				ProteinNetwork mapping = null;
				if (mapChoice > 0)
					mapping = mappingCandidates.get(mapChoice-1).network;
				// check if we need annotation keys
				String key1=null, key2=null;
				if (weightMerge == WeightMergePolicy.ANNOTATE_WEIGHTS) {
					DialogSettings keySettings = new DialogSettings("New attributes");
					keySettings.addInfoLabel("Please enter the names of the new attributes " +
					"under which the scores will be annotated");
					keySettings.addStringParameter("First attribute name: ", net1.name);
					keySettings.addStringParameter("Second attribute name: ", net2.name);
					Object[] keyResults = ParameterDialog.showDialog(keySettings);
					if (keyResults == null) return;
					// read out the keys
					key1 = (String)keyResults[0];
					key2 = (String)keyResults[1];

					if (key1.trim().length() == 0 || key2.trim().length() == 0) {
						GUICommons.warning("Empty attribute names are not allowed.");
						return;
					}
				}
				// construct the combination rules
				CombinationRules rules = new CombinationRules(combiType);
				if (key1 != null)
					rules.setWeightMergePolicy(weightMerge, key1, key2);
				else
					rules.setWeightMergePolicy(weightMerge);
				if (mapping != null)
					rules.setMapping(mapping);

				// do it
				workingShow("Merging networks...");
				ProteinNetwork merged = net1.network.combineWith(net2.network, rules);
				// add to list
				addNetwork(merged, "Merged: " + net1.name + " & " + net2.name);

				workingHide();

			}
		}

	}

	public ArrayList<String> getNetworkNames() {
		ArrayList<String> result = new ArrayList<String>();
		for (NetworkInList inList : networks) 
			result.add(inList.name);
		return result;
	}

	public ArrayList<String> getPurificationNames() {
		ArrayList<String> result = new ArrayList<String>();
		for (PurificationDataInList inList : purifications) 
			result.add(inList.name);
		return result;
	}

	public ProteinNetwork getNetwork(int index) {
		return networks.get(index).network;
	}

	private ArrayList<String> getComplexSetNames() {
		ArrayList<String> result = new ArrayList<String>();
		for (ComplexSetInList inList : complexSets) 
			result.add(inList.name);
		return result;
	}

	private NetworkInList loadNetwork(String file, boolean directed) {
		// load network
		try {
			// gzipped?
			boolean gzipped = Tools.isGZIPed(file);
			// open stream
			InputStream instream = new FileInputStream(file);
			if (gzipped) instream = new GZIPInputStream(instream);
			// read network and add to list
			ProteinNetwork net = NetworkReader.readNetwork(instream, directed);

			return new NetworkInList(net, GUICommons.extractBaseFilenameGZ(file));
		} catch (Exception e) {
			GUICommons.error("Could not load network from file. Reason:\n\n" + e.getMessage());
			return null;
		} finally {

		}
	}

	private ComplexSetInList loadComplexSet(String file) {
		// load network
		try {
			// gzipped?
			boolean gzipped = Tools.isGZIPed(file);
			// open stream
			InputStream instream = new FileInputStream(file);
			if (gzipped) instream = new GZIPInputStream(instream);
			// read network and add to list
			ComplexSet set = ComplexSetReader.readComplexes(file);
			if (!GUICommons.checkComplexSetSanity(set)) {
				if (JOptionPane.showConfirmDialog(
						null, file + "\ndoes not seem to be a valid complex file.\n\nLoad it anyway?",
						"Warning", JOptionPane.YES_OPTION,
						JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
					return null;
				}
			}

			return new ComplexSetInList(set, GUICommons.extractBaseFilenameGZ(file));
		} catch (Exception e) {
			GUICommons.error("Could not load complex set from file. Reason:\n\n" + e.getMessage());
			return null;
		} 
	}

	private PurificationDataInList loadPurificationData(String file) {
		// load purification data
		try {
			// gzipped?
			boolean gzipped = Tools.isGZIPed(file);
			// open stream
			InputStream instream = new FileInputStream(file);
			if (gzipped) instream = new GZIPInputStream(instream);
			// read network and add to list
			PurificationData data = PurificationDataReader.readPurifications(instream);

			return new PurificationDataInList(data, GUICommons.extractBaseFilenameGZ(file));
		} catch (Exception e) {
			GUICommons.error("Could not load purification data from file. Reason:\n\n" + e.getMessage());
			return null;
		} 
	}

	private void actionNetworkDispose() {
		cursorWork();
		// delete all selected ones
		int[] toDelete = lstNetworks.getSelectedIndices();
		for (int i=toDelete.length-1; i>=0; i--) {
			((DefaultListModel)lstNetworks.getModel()).remove(toDelete[i]);
			networks.remove(toDelete[i]);
		}
		// now it would be a good time to collect the garbage
		System.gc();

		cursorNoWork();
	}

	private void actionNetworkCluster() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network.");
		else  {
			// gather clusterers
			Vector<String> clustererNames = new Vector<String>();
			clustererNames.add("@Markov");
			clustererNames.add("Hierarchical agglomerative");
			if (userClusterers != null) {
				for (UserClusterer userClust : userClusterers)
					clustererNames.add(userClust.getName());
			}
			// determine the method
			DialogSettings settings = new DialogSettings("Clustering method");
			settings.addRadioOptions("Clustering method:", clustererNames.toArray(new String[0]));
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				// get stuff
				NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
				ProteinNetwork toCluster = inList.network;

				if ((Integer)result[0] == 0){
					// mcl
					DialogSettings mclSettings = new DialogSettings("MCL");
					mclSettings.addFloatParameter("Inflation coefficient: ", 2.0f, new FloatVerifier(1.0f,30.0f, 1));
					mclSettings.addStringParameter("Path to 'mcl' binary (optional)", "mcl"); 
					mclSettings.addIntegerParameter("Timeout (seconds):", 300, new IntVerifier(1, Integer.MAX_VALUE));
					mclSettings.addInfoLabel("Additional parameters (optional)");
					mclSettings.addFloatParameter("Pruning number:", Float.NaN);
					mclSettings.addFloatParameter("Selection number:", Float.NaN);
					mclSettings.addFloatParameter("Recover number:", Float.NaN);
					mclSettings.addFloatParameter("Mass percentage:", Float.NaN);
					mclSettings.setHelpText("If the 'mcl' binary is not in your PATH, please specifiy the full path to the file.");
					Object[] mclResult = ParameterDialog.showDialog(this, mclSettings);
					if (mclResult != null) {
						workingShow("Clustering...");
						// set parameters
						MCLParameters params = new MCLParameters();
						params.setInflation((Float)mclResult[0]);
						params.setTimeout((Integer)mclResult[2]);
						// optional parameters
						params.setP((Float)mclResult[3]);
						params.setS((Float)mclResult[4]);
						params.setR((Float)mclResult[5]);
						params.setPct((Float)mclResult[6]);
						// binary
						String bin = (String)mclResult[1];
						if (bin.length() == 0) bin = MarkovClusterer.DEFAULT_BINARY;
						MarkovClusterer.setMCLBinary(bin);
						// do the clustering
						Clusterer clusterer = new MarkovClusterer(params);
						ComplexSet clustering = null;
						try {
							clustering = clusterer.cluster(toCluster);
						} catch (ProCopeException e) {
							if (e.getMessage().contains("timed out"))
								GUICommons.warning("MCL timed out.");
							else {
								if (e.getMessage().contains("Cannot run program"))
									GUICommons.error("Could not start MCL.\n\n" +
									"Is the path to the mcl binary correct?");
								else
									GUICommons.error("Error while running mcl:\n\n" + e.getMessage());
							}
							workingHide();
							return;
						}
						workingHide();
						// add it
						addComplexSet(new ComplexSetInList(clustering, inList.name + "; mcl, inflation: " + (Float)mclResult[0]));
					}
				} else if ((Integer)result[0] == 1){
					// hcl
					DialogSettings hclSettings = new DialogSettings("Hierarchical");
					hclSettings.addListParameter("Linkage:", "@UPGMA","Single","Complete","WPGMA");
					hclSettings.addStringParameter("Cutoff(s):", "1.0");
					hclSettings.setHelpText("You can specify multiple, comma-separated cutoffs.\n\n" +
					"Further cutoffs do not increase the calculation time significantly.");
					Object[] hclResult = ParameterDialog.showDialog(this, hclSettings);
					if (hclResult != null) {
						// try to get the cutoffs
						String[] arrCutoffs = ((String)hclResult[1]).split(",");
						float[] cutoffs = new float[arrCutoffs.length];
						try {
							for (int i=0; i<arrCutoffs.length;i++) {
								cutoffs[i] = Float.parseFloat(arrCutoffs[i]);
							}
						} catch (NumberFormatException e) {
							GUICommons.error("Invalid decimal number\n\n"+e.getMessage());
							cutoffs = null;
						}

						if (cutoffs != null) {
							workingShow("Clustering...");
							// do it
							HierarchicalTreeNode hclTree = 
								HierarchicalClusteringTrees.clusterSimilarities(toCluster, LINKAGES[(Integer)hclResult[0]]);
							// iterate over cutoffs
							for (float cutoff : cutoffs) {
								ComplexSet clustering = hclTree.extractClustering(cutoff);
								addComplexSet(new ComplexSetInList(	clustering, 
										inList.name	+ "; hierarchical, "
										+ LINKAGES[(Integer) hclResult[0]] + ", cutoff: " + cutoff));
							}
							workingHide();
						}
					}
				} else {
					try {
						// user clusterer, get it from the list
						UserClusterer userClust = userClusterers.get((Integer)result[0] - 2);
						//  get parameters from the user
						Vector<Object> userInput = getUserParameters(userClust.getName(), userClust.getParameters());
						if (userInput != null) {
							// now create it
							Clusterer clusterer = userClust.generateClusterer(userInput.toArray(new Object[0]));
							// do it
							workingShow("Clustering...");
							ComplexSet clustering = clusterer.cluster(toCluster);
							// add it
							addComplexSet(new ComplexSetInList(clustering, inList.name + "; " + userClust.getName()));
						}
					} catch (ClassNotFoundException e) {
						GUICommons.warning("User clusterer class not found in current classpath:\n" + e.getMessage());
					} catch (NoSuchMethodException e) {
						GUICommons.warning("Constructor does not exist:\n" + e.getMessage());
					} catch (InvocationTargetException e) {
						GUICommons.warning("User clusterer reported a problem while initializing:\n\n" + 
								e.getTargetException().getMessage());
					} catch (Exception e) {
						GUICommons.warning("Could not create clusterer:");
						System.err.println(e.getMessage());
					}
					workingHide();
				}
			}
		}
	}

	private void actionNetworkCutoff() {
		if (lstNetworks.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one network.");
		else {
			DialogSettings settings = new DialogSettings("Cut-off network");
			settings.addInfoLabel("Removes all edges from a network whose weight is below the given cutoff.");
			settings.addFloatParameter("Cut-off score:", 1f);
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				workingShow("Cutting off network...");
				// check whether the user wants to replace the network
				NetworkInList inList = networks.get(lstNetworks.getSelectedIndex());
				ProteinNetwork cutOff = inList.network.getCutOffNetwork((Float)result[0]);
				String newName = inList.name + ", cut-off: " + (Float)result[0];
				workingHide();
				// replace or insert new
				boolean replace = GUICommons.yesNo("Replace existing network with new network?");
				if (!replace)
					addNetwork(new NetworkInList(cutOff, newName));
				else {
					inList.network = cutOff;
					inList.name = newName;
					updateNetwork(lstNetworks.getSelectedIndex());
				}

			}
		}
	}

	private void actionPuriLoad() {
		// show dialog
		File open = GUICommons.chooseFile(this, "Load purification data", "puri", true);
		if (open != null) {
			// load purification data
			try {
				workingShow("Loading " + Tools.extractFilename(open.getAbsolutePath()) + "...");
				PurificationDataInList data = loadPurificationData(open.getAbsolutePath());
				if (data != null)
					addPurificationData(data);
			} catch (Exception e) {
				GUICommons.error("Could not load purification data from file. Reason:\n\n" + e.getMessage());
			} 
			workingHide();
		}
	}

	private void actionPuriSave() {
		if (lstPurifications.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one purification data set for saving.");
		else {
			File open = GUICommons.chooseFile(this, "Save purification data", "puri", false);
			if (open != null) {
				PurificationDataInList inList = purifications.get(lstPurifications.getSelectedIndex());
				// write out
				try {
					workingShow("Saving purification data...");
					// get stream
					OutputStream out = new FileOutputStream(open.getAbsoluteFile());
					if (config.getIntVal("gzip", 0)==1)
						out = new GZIPOutputStream(out);
					PurificationDataWriter.writePurificationData(inList.data, out);
					out.close();
					GUICommons.info("Purification data saved.");
				} catch (IOException e) {
					GUICommons.error("Could not save file. Reason:\n\n" + e.getMessage());
				} 
				workingHide();					
			}
		}
	}

	private void actionPuriCytoscape() {
		if (lstPurifications.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one purification data set for saving.");
		else {
			PurificationDataInList inList = purifications.get(lstPurifications.getSelectedIndex());
			if (!cytoscapeMode) {
				// export to file
				File open = GUICommons.chooseFile(this, "Export purification data", "puri",	false);
				if (open != null) {
					// save network
					try {
						workingShow("Exporting purification data...");
						PurificationDataWriter.writeXGMML(inList.data, open.getAbsolutePath());
						GUICommons.info("Data exported in XGMML format which can be imported in Cytoscape.");
					} catch (IOException e) {
						GUICommons.error("Could not save file. Reason:\n\n"	+ e.getMessage());
					}
					workingHide();
				}
			} else {
				// we are a plugin of cytoscape
				workingShow("Exporting purification data...");
				CytoscapeAdapter.exportPurificationData(inList.data, inList.name);
				workingHide();
				GUICommons.info("Successfully send to Cytoscape.");
			}
		}
	}

	private void actionPuriRename() {
		if (lstPurifications.getSelectedIndices().length > 1) 
			GUICommons.info("Please select only one purification data set for renaming.");
		else {
			// ask user for new name
			PurificationDataInList inList = purifications.get(lstPurifications.getSelectedIndex());
			DialogSettings settings = new DialogSettings("Rename purification data set");
			settings.addStringParameter("New purification data set name:", inList.name);
			Object[] result = ParameterDialog.showDialog(this, settings);
			if (result != null) {
				inList.name = (String)result[0];
				updatePurificationSet(lstPurifications.getSelectedIndex());
			}
		}
	}

	private void actionPuriDispose() {
		cursorWork();
		// delete all selected ones
		int[] toDelete = lstPurifications.getSelectedIndices();
		for (int i=toDelete.length-1; i>=0; i--) {
			((DefaultListModel)lstPurifications.getModel()).remove(toDelete[i]);
			purifications.remove(toDelete[i]);
		}
		// now it would be a good time to collect the garbage
		System.gc();

		cursorNoWork();
	}

	private void actionPuriScores() {
		int[] selected = lstPurifications.getSelectedIndices();
//		if (selected.length == 1) {

		// gather names of score types
		Vector<String> scoreTypes = new Vector<String>();
		Vector<Object> scoreObjects = new Vector<Object>();
		if (selected.length == 1) {
			scoreTypes.add("@Socio affinity");
			scoreTypes.add("Purification enrichment (PE)");
			scoreTypes.add("Hart scores");
			scoreTypes.add("Dice coefficients");
			scoreObjects.add("socio");
			scoreObjects.add("pe");
			scoreObjects.add("hart");
			scoreObjects.add("dice");
		} else {
			scoreTypes.add("@Hart scores");
			scoreObjects.add("hart");
		}
		// user defined score calculators
		if (userCalculators != null) {
			for (UserScoresCalculator scoresCalc : userCalculators) {
				if (selected.length == 1 || scoresCalc.multiplePurifications()) {
					scoreTypes.add(scoresCalc.getName());
					scoreObjects.add(scoresCalc);
				}
			}
		}
		// create dialog
		DialogSettings settings = new DialogSettings("Calculate scores");
		settings.addInfoLabel("Calculates a scores network from " +
		"the selected purification data set(s).");
		if (selected.length > 1) settings.addInfoLabel("Note: Multiple datasets are selected");
		settings.addRadioOptions("Score type:", scoreTypes.toArray(new String[0]));
		settings.addFloatParameter("Score cutoff (empty for none):", Float.NaN, new FloatVerifier(true));
		settings.setHelpText("Scores below the 'Score cutoff' will not be inserted into the result network.");
		Object[] result = ParameterDialog.showDialog(settings);

		if (result != null) {
			// get object of the selected calculator (may be a String or a UserScoresCalculator object
			Object scoreObj = scoreObjects.get((Integer)result[0]);

			workingShow("Calculating scores...");

			float cutoff = (Float)result[1];
			if (Float.isNaN(cutoff)) cutoff = Float.NEGATIVE_INFINITY;
			// generate name addon for cutoff
			String cutoffAdd = "";
			if (!Float.isInfinite(cutoff))
				cutoffAdd = ", cutoff: " + cutoff;
			// check which scores method was select
			if (scoreObj.equals("socio")) {
				// socio scores
				PurificationDataInList inList = purifications.get(lstPurifications.getSelectedIndex());
				ProteinNetwork socios = 
					NetworkGenerator.generateNetwork(new SocioAffinityCalculator(inList.data), cutoff);
				addNetwork(socios, "Socio affinity scores from " + inList.name + cutoffAdd);

			} else if (scoreObj.equals("pe")) {
				// pe scores
				PurificationDataInList inList = purifications.get(lstPurifications.getSelectedIndex());
				DialogSettings peSettings = new DialogSettings("PE parameters");
				peSettings.addFloatParameter("r:", 0.51f, new FloatVerifier(0,1));
				peSettings.addFloatParameter("Pseudocount:", 20f, new FloatVerifier(0, Float.MAX_VALUE));
				Object[] peResult = ParameterDialog.showDialog(peSettings);
				if (peResult != null) {
					// calculate them
					float r = (Float)peResult[0];
					float pseudo = (Float)peResult[1];
					ProteinNetwork pe = 
						NetworkGenerator.generateNetwork(new PECalculator(inList.data, r, pseudo), cutoff);
					addNetwork(pe, "PE scores from " + inList.name + cutoffAdd);
				}

			} else if (scoreObj.equals("hart")) {
				// hart scores
				// generate array of purification data
				PurificationData[] puriData = new PurificationData[selected.length];
				String puriNames="";
				for (int i=0; i<selected.length; i++) {
					puriData[i] = purifications.get(selected[i]).data;
					puriNames += purifications.get(selected[i]).name;
					if (i<selected.length-1) puriNames += ", ";
				}
				// do it
				ProteinNetwork hart = NetworkGenerator.generateNetwork(new HartCalculator(puriData), cutoff);
				addNetwork(hart, "Hart scores from " + puriNames + cutoffAdd);

			} else if (scoreObj.equals("dice")) {
				// dice coefficients
				// socio scores
				PurificationDataInList inList = purifications.get(lstPurifications.getSelectedIndex());
				ProteinNetwork socios = 
					NetworkGenerator.generateNetwork(new DiceCoefficients(inList.data), cutoff);
				addNetwork(socios, "Dice coefficients from " + inList.name + cutoffAdd);

			} else {
				try {
					// generate array of purification data
					PurificationData[] puriData = new PurificationData[selected.length];
					String puriNames="";
					for (int i=0; i<selected.length; i++) {
						puriData[i] = purifications.get(selected[i]).data;
						puriNames += purifications.get(selected[i]).name;
						if (i<selected.length-1) puriNames += ", ";
					}
					// user-defined
					UserScoresCalculator userCalc = (UserScoresCalculator)scoreObj;
					// get parameters
					Vector<Object> userInput = getUserParameters(userCalc.getName(), userCalc.getParameters());
					if (userInput != null) {
						// generate it
						ScoresCalculator scoreCalc = userCalc.generateScoresCalculator(puriData, userInput.toArray(new Object[0]));
						// do it
						ProteinNetwork hart = NetworkGenerator.generateNetwork(scoreCalc, cutoff);
						addNetwork(hart, userCalc.getName() + " from " + puriNames + cutoffAdd);
					}

				} catch (ClassNotFoundException e) {
					GUICommons.warning("User scores calculator class not found in current classpath:\n" + e.getMessage());
				} catch (NoSuchMethodException e) {
					GUICommons.warning("Constructor does not exist:\n" + e.getMessage());
				} catch (InvocationTargetException e) {
					GUICommons.warning("User scores calculator reported a problem while initializing:\n\n" + 
							e.getTargetException().getMessage());
				} catch (Exception e) {
					GUICommons.warning("Could not create scores calcuator:");
					System.err.println(e.getMessage());
				}
			}

			workingHide();

		}
	}

	private void actionPuriMerge() {
		if (lstPurifications.getSelectedIndices().length < 2) 
			GUICommons.info("Please select at least 2 purification data sets for merging.");
		else {
			workingShow("Merging purification data");
			// get all selected purifications
			int[] selected = lstPurifications.getSelectedIndices();
			// construct merged set and new name
			PurificationData newData = new PurificationData();
			int count=0;
			StringBuffer allNames = new StringBuffer();
			for (int index : selected) {
				newData = newData.merge(purifications.get(index).data);
				allNames.append(purifications.get(index).name);
				if (count<selected.length-1) allNames.append("/");
				count++;
			}

			// add to the list
			addPurificationData(newData, "Merged: " + allNames);

			workingHide();
		}
	}

	private void actionPuriBaitPrey() {
		workingShow("Deriving bait-prey interactions...");
		for (int index : lstPurifications.getSelectedIndices()) { 
			PurificationDataInList inList = purifications.get(index); 
			// generate the network
			ProteinNetwork newNet = inList.data.getBaitPreyInteractions();
			// add
			addNetwork(newNet, inList.name + ", bait-prey interactions");
		}
		workingHide();
	}

	public void addNetwork(ProteinNetwork network, String name) {
		addNetwork(new NetworkInList(network, name));
	}

	private void addNetwork(NetworkInList newNet) {
		networks.add(newNet);
		((DefaultListModel)lstNetworks.getModel()).addElement(newNet.getLabel());
	}

	private void addPurificationData(PurificationData data, String name) {
		addPurificationData(new PurificationDataInList(data, name));
	}

	private void addPurificationData(PurificationDataInList newData) {
		purifications.add(newData);
		((DefaultListModel)lstPurifications.getModel()).addElement(newData.getLabel());
	}

	private void updatePurificationSet(int pos) {
		DefaultListModel model = ((DefaultListModel)lstPurifications.getModel());
		model.setElementAt(purifications.get(pos).getLabel(), pos);
	}

	private void updateNetwork(int pos) {
		DefaultListModel model = ((DefaultListModel)lstNetworks.getModel());
		model.setElementAt(networks.get(pos).getLabel(), pos);
	}

	private void updateComplexSet(int pos) {
		DefaultListModel model = ((DefaultListModel)lstComplexSets.getModel());
		model.setElementAt(complexSets.get(pos).getLabel(), pos);
	}

	public void addComplexSet(ComplexSet set, String name) {
		addComplexSet(new ComplexSetInList(set,name));
	}


	private void addComplexSet(ComplexSetInList newSet) {
		newSet.set.sortBySize(false);
		complexSets.add(newSet);
		((DefaultListModel)lstComplexSets.getModel()).addElement(newSet.getLabel());
	}


	private class NetworkInList {

		public ProteinNetwork network;
		public String name;

		public NetworkInList(ProteinNetwork network, String name) {
			this.network = network;
			this.name = name;
		}

		public String getLabel() {
			int nodes = network.getNodeCount();
			int edges = network.getEdgeCount();
			String nodeString = nodes != 1 ? "proteins" : "protein";
			String edgeString = edges != 1 ? "edges" : "edge";
			String directed = network.isDirected() ? ", directed" : "";
			return name + " - " + nodes + " " + nodeString + ", " + edges + " "	+ edgeString + directed;
		}

	}

	private class ComplexSetInList {

		public ComplexSet set;
		public String name;

		public ComplexSetInList(ComplexSet set, String name) {
			this.set = set;
			this.name = name;
		}

		public String getLabel() {
			return name + " - " +set.getComplexCount() + " complexes, " + set.getProteins().size() + " distinct proteins";
		}

	}

	private class PurificationDataInList {
		public PurificationData data;
		public String name;

		public PurificationDataInList(PurificationData data, String name) {
			this.data = data;
			this.name = name;
		}

		public String getLabel() {
			return name + " - " + data.getNumberOfExperiments() +" experiments, " + data.getProteins().size() + " distinct proteins";
		}
	}

	protected void workingShow(final String msg) {
		if (!workingDialog.isVisible())
			workingDialog.activate();
		workingDialog.setMessage(msg);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		enableGUI(false);
	}

	protected void workingHide() {
		//workingDialog.setVisible(false);
//		workingDialog.setVisibility(false);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		workingDialog.dispose();
		enableGUI(true);
	}

	private void cursorWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	private void cursorNoWork() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void enableGUI(boolean enabled) {
		lstNetworks.setEnabled(enabled);
		lstComplexSets.setEnabled(enabled);
		lstPurifications.setEnabled(enabled);
		// menus
		JMenuBar menuBar = this.getJMenuBar();
		int count = menuBar.getMenuCount();
		for (int i=0; i<count; i++) {
			JMenu menu = menuBar.getMenu(i);
			if (menu != null)
				menu.setEnabled(enabled);
		}
//		this.getMenuBar().
	}

	private void workingSetWorker(Thread t) {
		workingDialog.setWorker(t);
	}


	private void actionSaveSession() {

		// get file
		File outfile = GUICommons.chooseFile(this, "Save session", "session", false);
		if (outfile != null) {
			try {
				workingShow("Saving session...");

				OutputStream out = new GZIPOutputStream(new FileOutputStream(outfile));
				PrintWriter writer = new PrintWriter(out, true);

				// save name mappings (must be stored first)
				for (NameMapping mapping : nameMappings) {
					writer.println("x;" + mapping.label + ";" + mapping.mapNet.getEdgeCount()
							+";"+(mapping.targetFirst?"1":"0"));
					writer.flush();
					NetworkWriter.writeNetwork(mapping.mapNet, out);
				}

				// save networks
				for (NetworkInList net : networks) {
					// save type, name and number of lines
					writer.println("n;"+net.name+";"+net.network.getEdgeCount());
					writer.flush();
					NetworkWriter.writeNetwork(net.network, out);
				}
				// save complex sets
				for (ComplexSetInList set : complexSets) {
					// save type, name and number of lines
					writer.println("c;" + set.name + ";" + set.set.getComplexCount());
					writer.flush();
					ComplexSetWriter.writeComplexes(set.set, out);
				}
				// save purification data
				for (PurificationDataInList data : purifications) {
					// save type, name and number of lines
					writer.println("p;" + data.name + ";" + data.data.getPreyCount());
					writer.flush();
					PurificationDataWriter.writePurificationData(data.data, out);
				}
				// save localization data
				for (LocalizationDataSetting setting : locSettings) {
					writer.println("l;" + setting.label + ";0;" + setting.file);
					writer.flush();
				}

				// save GO settings
				for (GOSetting setting : goSettings) {
					writer.print("g;;0;");
					for (String key : setting.parameters.keySet()) {
						writer.print(key+"="+setting.parameters.get(key) + ",");
					}
					writer.println();
					writer.flush();
				}

				// save eventual protein annotations
				writer.println("a;;" + ProteinManager.getAnnotatedProteinCount());
				writer.flush();
				ProteinManager.saveProteinAnnotations(out);

				out.close();

				// notice
				GUICommons.info("Session saved to file.");
			} catch (IOException e) {
				GUICommons.error("Could not write session to file. Reason:\n\n" + e.getMessage());
			}

			workingHide();

		}
	}

	private static final String MSG_MISSING_GOLOC = "Note: Session files only contain links to "
		+ "the original files for localization data and GO settings.";
	private void actionLoadSession() {
		// get file
		File infile = GUICommons.chooseFile(this, "Load session", "session", true);
		if (infile != null) {
			workingShow("Loading session...");
			cleanUp();
			try {
				// file must be gzipped
				if (!Tools.isGZIPed(infile.getAbsolutePath()))
					// no session file, throw any exception, will be caught below
					throw new Exception();
				// get stream 
				boolean updatedManager = false;
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new GZIPInputStream(
								new FileInputStream(infile))));
				String infoLine;
				while ((infoLine = reader.readLine())!=null) {
					// get information
					String[] split = infoLine.split(";");
					char type = split[0].charAt(0);
					String name = split[1];
					int numLines = Integer.parseInt(split[2]);
					// write out dummy file
					String temp = Tools.getTempFilename();
					PrintWriter writer = new PrintWriter(new FileWriter(temp));
					for (int i=0; i<numLines; i++)
						writer.println(reader.readLine());
					writer.close();

					// if this is the first non-namemapping data object => update the protein manager now!
					if (type != 'x' && !updatedManager) {
						// update name mappings in protein manager
						NameMappingsDialog.updateManager(nameMappings);
						updatedManager = true;
					}

					// read data
					switch (type) {
					case 'n':
						// network
						ProteinNetwork net = NetworkReader.readNetwork(temp);
						addNetwork(net, name);
						break;

					case 'c':
						// complex set
						ComplexSet set = ComplexSetReader.readComplexes(temp);
						addComplexSet(set, name);
						break;

					case 'p':
						// purification data
						PurificationData data = PurificationDataReader.readPurifications(temp);
						addPurificationData(data, name);
						break;

					case 'l':
						// localization data
						String locFile = split[3];
						// check if the file still exists
						if (new File(locFile).exists()) {
							// load it
							LocalizationData locData = LocalizationDataReader.readLocalizationData(locFile);
							locSettings.add(new LocalizationDataSetting(name, locData, locFile));
						} else {
							// does not exist, warn user
							GUICommons.warning("Warning: Could not load localization data from:\n" + locFile + "\n\n" + MSG_MISSING_GOLOC);
						}
						break;

					case 'g':
						// GO settings
						// decode map
						String[] mapSplit = split[3].split(",");
						Map<String,String> parameters = new HashMap<String, String>();
						for (String keyVal : mapSplit) {
							if (keyVal.length() > 0) {
								String[] keyValSplit = keyVal.split("=");
								parameters.put(keyValSplit[0], keyValSplit[1]);
							}
						}
						// generate settings object and add to list
						GOSetting setting = GOSettingsDialog.settingFromMap(parameters);
						if (setting != null)
							goSettings.add(setting);
						else
							// could not load it
							GUICommons.warning(MSG_MISSING_GOLOC);
						break;

					case 'x':
						// name mapping
						ProteinNetwork mapNet = NetworkReader.readNetwork(temp, true);
						boolean targetFirst = split[3].equals("1");
						nameMappings.add(new NameMapping(mapNet, name, targetFirst));
						break;

					case 'a':
						// protein annotations
						ProteinManager.clearAnnotations();
						ProteinManager.loadProteinAnnotations(temp);
						break;
					}
					new File(temp).delete();

				}

			} catch (IOException e) {
				GUICommons.error("Could not read session file. Reason:\n\n" + e.getMessage());
			} catch (Exception e) {
				GUICommons.error("Could not load session from file.\n\nThe file format seems to be invalid.");
			}
			workingHide();
		}
	}

	private void actionOptionBins() {
		// ask user for number if bins in histogram
		DialogSettings settings = new DialogSettings("Histogram bins");
		settings.addInfoLabel("Set the number of bins used in all histograms.");
		settings.addIntegerParameter("Number of bins:", numHistBins, new IntVerifier(1, Integer.MAX_VALUE));
		Object[] result = ParameterDialog.showDialog(settings);
		if (result != null) {
			numHistBins = (Integer)result[0];
			config.setIntVal("histbins", numHistBins);
		}
	}

	private void actionPetriNet() {
		int[] selNetworks = lstNetworks.getSelectedIndices();
		int[] selComplexes = lstComplexSets.getSelectedIndices();
		int[] selPuri = lstPurifications.getSelectedIndices();
		// at least something has to be selected
		if (selNetworks.length == 0 && selComplexes.length == 0 && selPuri.length == 0) {
			GUICommons.info("Please mark one or more networks, complex sets and " +
			"purification data sets in the lists.");
		} else {
			GUICommons.info("This generates a Petri net which integrates the information of "
					+ "all\n selected networks, complex sets and purification data sets.\n\n"
					+ "The Petri net will be saved to the filesystem in GZIP format.");
			File out = GUICommons.chooseFile(this, "Output file", "petrinets", false);
			if (out != null) {
				try {
					workingShow("Generating Petri net...");
					PetriNetCreator netCreator = new PetriNetCreator(new GZIPOutputStream(new FileOutputStream(out)));
					// add networks
					for (int index : selNetworks)
						netCreator.addInteractionNetwork(networks.get(index).network, networks.get(index).name, true);
					// add complex sets
					for (int index : selComplexes)
						netCreator.addComplexSet(complexSets.get(index).set, complexSets.get(index).name);
					// add purification data
					for (int index : selPuri)
						netCreator.addPurificationData(purifications.get(index).data, purifications.get(index).name);
					netCreator.createPetriNet();
					netCreator.close();

					GUICommons.info("Petri net successfully generated.");
				} catch (IOException e) {
					GUICommons.error("Could not write Petri net file. Reason:\n\n" + e.getMessage());
				}
				workingHide();
			}
		}
	}

	private void actionPetriNetConvert(int mode) {
		try {
			// get input file
			File netFile = GUICommons.chooseFile(this, "Choose Petri net file", "petrinets", true);
			if (netFile != null) {
				// get output file
				File outFile = GUICommons.chooseFile(this, "Choose output XGMML file", "petrinets", false);
				if (outFile != null) {
					workingShow("Converting Petri net...");

					// get input stream (optionally gzipped)
					InputStream instream = new FileInputStream(netFile);
					if (Tools.isGZIPed(netFile.getAbsolutePath()))
						instream = new GZIPInputStream(instream);

					if (mode == 1) {
						// get the output stream, convert to XGMML
						OutputStream outstream = new FileOutputStream(outFile);
						XGMMLGenerator.convertToXGMML(instream, outstream);
						outstream.close();
					} else if (mode == 2) {
						// get output streams, convert to topnet
						OutputStream outPlaces = new FileOutputStream(outFile + ".places");
						OutputStream outInteractions = new FileOutputStream(outFile + ".interactions");
						ToPNetGenerator.convertToToPNet(instream, outPlaces, outInteractions);
						outPlaces.close();
						outInteractions.close();
					}
					instream.close();
					workingHide();

					GUICommons.info("Petri net successfully converted.");
				}
			}
		} catch (IOException e) {
			GUICommons.error("Error while converting Petri net: " + e.getMessage());
		}
	}


	private void actionShowHelp() {
		final String docs = "doc/indexgui.html";

		File doc = new File(docs);
		
		if (!doc.exists() && likelyWorkingDirectory != null)
			// try something else
			doc = new File(likelyWorkingDirectory + docs);
			
		if (!doc.exists())
			// last try
			doc = new File(System.getProperty("user.dir")
					+ System.getProperty("file.separator")
					+ docs);

		// check if it is there finally
		if (!doc.exists()) {
			if (GUICommons.yesNo("Could not find documentation files in doc/ subdirectory.\n" +
					"You are probably running the webstart version of ProCope.\n\n" +
			"Do you want to view the online documentation on the ProCope website?")) {
				try {
					GUICommons.showBrowser(new URL(Tools.HOMEPAGE_DOCS).toURI());
				} catch (Exception e) {/* won't happen */ }
			}
		} else {
			// found it, show it
			GUICommons.showBrowser(doc.toURI());
		}

	}

	private void cleanUp() {
		// empty lists
		((DefaultListModel)lstNetworks.getModel()).removeAllElements();
		((DefaultListModel)lstComplexSets.getModel()).removeAllElements();
		((DefaultListModel)lstPurifications.getModel()).removeAllElements();
		// delete data objects
		networks.clear();
		complexSets.clear();
		purifications.clear();
		locSettings.clear();
		goSettings.clear();
		nameMappings.clear();

		// good time to collect the garbage
		System.gc();
	}

	private void outOfMemory() {
		GUICommons.error("Out of memory! Please start the program with \n" +
				"a higher amount of available memory.\n\n" +
		"Will exit now.");
		cleanUp();
		System.exit(1);
	}

	private void saveWindowPos() {
		config.setIntVal("win_x", this.getX());
		config.setIntVal("win_y", this.getY());
		config.setIntVal("win_width", this.getSize().width);
		config.setIntVal("win_height", this.getSize().height);
		config.setIntVal("win_maximized",this.getExtendedState() == JFrame.MAXIMIZED_BOTH ? 1 : 0);

	}

	private void loadAndSetWindowPos() {
		int x = config.getIntVal("win_x", -1);
		if (x == -1) {
			// first time
			setLocationRelativeTo(null);
		} else {
			// load rest
			int y = config.getIntVal("win_y", -1);
			int width = config.getIntVal("win_width", -1);
			int height = config.getIntVal("win_height", -1);
			boolean maximized = config.getIntVal("win_maximized", -1) == 1;
			// set window pos&size
			this.setLocation(x, y);
			this.setSize(new Dimension(width,height));
			if (maximized)
				this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
	}

	private List<UserClusterer> loadUserClusterers() {
		// if the XML file is there => try to parse it
		File xml = new File(Tools.CLUSTERERSFILE);
		if (xml.exists()) {
			try {
				FileInputStream stream = new FileInputStream(xml);
				List<UserClusterer> clusterers = UserClusterer.parseClusterers(stream);
				stream.close();
				return clusterers;
			} catch (Exception e) {
				throw new ProCopeException(e.getMessage());
			}
		} else
			return new Vector<UserClusterer>();
	}

	private static List<UserScoresCalculator> loadUserCalculators() {
		// if the XML file is there => try to parse it
		File xml = new File(Tools.CALCULATORSFILE);
		if (xml.exists()) {
			try {
				FileInputStream stream = new FileInputStream(xml);
				List<UserScoresCalculator> calculators = UserScoresCalculator.parseCalculators(stream);
				stream.close();
				return calculators;
			} catch (Exception e) {
				throw new ProCopeException(e.getMessage());
			}
		} else
			return null;
	}

	private Vector<Object> getUserParameters(String title, List<UserParameter> parameters) {

		if (parameters.size() == 0)
			// return empty list
			return new Vector<Object>();

		DialogSettings userSettings = new DialogSettings(title);

		for (UserParameter para : parameters) {
			String def = para.getDefaultValue();
			System.out.println("def: " + def);
			switch (para.getDataType()) {
			case INTEGER:
				userSettings.addIntegerParameter(para.getName()+":", def!=null?Integer.parseInt(def):0);
				break;
			case FLOAT:
				userSettings.addFloatParameter(para.getName()+":", def!=null?Float.parseFloat(def):0f);
				break;
			case STRING:
				userSettings.addStringParameter(para.getName()+":", def!=null?def:"");
				break;
			}
		}

		// show the dialog
		Object[] result = ParameterDialog.showDialog(userSettings);
		if (result == null)
			return null;

		// assemble results
		Vector<Object> userInput = new Vector<Object>();
		for (Object o : result)
			userInput.add(o);
		return userInput;


	}

	private void questionMarkInfo() {
		// show it?
		boolean showDialog = config.getIntVal("helptip", 1)==1;

		if (showDialog) {
			DialogSettings settings = new DialogSettings("Hint");
			settings.addInfoLabel("Wherever you see the little question mark shown at the bottom left of " +
			"this window, click it to get context-related help about the ProCope GUI.");
			settings.addCheckParameter("Do not show this message again", false);
			settings.setHelpText("Yes, this is the button which shows you context-related help.");
			Object[] result = ParameterDialog.showDialog(settings);
			if (result != null) {
				config.setIntVal("helptip", (Boolean)result[0] ? 0 : 1);
			}
		}
	}

	public static void main(String[] args) {

		// first argument might contain the current path
		likelyWorkingDirectory = null;
		if (args.length > 0 && args[0].length() > 0) {
			likelyWorkingDirectory = args[0];
			// should not end with a .
			if (likelyWorkingDirectory.endsWith("."))
				likelyWorkingDirectory = likelyWorkingDirectory.substring(0,likelyWorkingDirectory.length()-1);
			// should end with a / or \
			if (!likelyWorkingDirectory.endsWith(File.separator))
				likelyWorkingDirectory += File.separator;
		}
		
		new GUIMain();

	}

}
