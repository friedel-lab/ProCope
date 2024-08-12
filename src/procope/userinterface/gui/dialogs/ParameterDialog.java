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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import procope.userinterface.gui.GUICommons;
import procope.userinterface.gui.HelpButton;
import procope.userinterface.gui.JMultilineLabel;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class ParameterDialog extends JDialog {

	private static final long serialVersionUID = -3736312659434968022L;

	private ArrayList<SingleSetting> settingsList;
	private ArrayList<Object> inputComponents;

	private int nonInfoSettings=0;

	private static final int MINIMAL_COMPONENT_WIDTH = 150;

	private static final int MAX_INFO_WIDTH = 450;

	private boolean accepted=false;

	Component parent;

	JButton btnOK, btnCancel;

	private DialogSettings settings;

	public static Object[] showDialog(Frame parent, DialogSettings settings) {
		ParameterDialog dialog = new ParameterDialog(parent, settings);
		return dialog.getUserInput();
	}

	public static Object[] showDialog(DialogSettings settings) {
		ParameterDialog dialog = new ParameterDialog((Frame)null, settings);
		return dialog.getUserInput();
	}

	private ParameterDialog(Frame parent,  DialogSettings settings) {
		super(parent,settings.getTitle(), true);
		this.parent = parent;
		initialize(settings);
	}

	private ParameterDialog(DialogSettings settings) {
		super((Frame)null, settings.getTitle(), true);
		initialize(settings);
	}


	private void initialize(DialogSettings settings) {

		this.settings = settings;
		this.settingsList = settings.getSettings();
		inputComponents = new ArrayList<Object>();

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		setResizable(false);
		initializeGUI();
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
		
		

	}

	private void initializeGUI() {

		KeyListener keyListener = getKeyListener();
		MouseListener fileClickListener = getFileClickListener();

		Insets infoInsets = new Insets(4,2,4,2);

		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; 
		c.gridy = 0;

		c.insets = new Insets(4,8 ,4,8);

		// iterate over all settings
		for (SingleSetting setting : settingsList) {
			c.gridx = 0;
			c.weightx = 1;

			c.fill = GridBagConstraints.NONE;

			if (setting.getType() == SettingType.INFOLABEL) {
				c.gridwidth = 2;
				c.anchor = GridBagConstraints.NORTHWEST;

				JMultilineLabel label = new JMultilineLabel();
				label.setMaxWidth(MAX_INFO_WIDTH); 
				label.setText(setting.getLabel());
				label.setMargin(infoInsets);
				pane.add(label, c);
				Font f = label.getFont();
				label.setFont(f.deriveFont(Font.BOLD));

			} else {
				nonInfoSettings++;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.NORTHEAST;
				pane.add(new JLabel(setting.getLabel()),c);

				// create the input component
				c.gridx = 1;
				c.weightx = 50;

				c.anchor = GridBagConstraints.NORTHWEST;
				c.fill = GridBagConstraints.HORIZONTAL;
				Object newComponent=null; 
				if (setting.getType() == SettingType.STRING) {
					// string input field
					JTextField field;
					pane.add(field = new JTextField((String)setting.getValue()),c);
					newComponent = field;
				} else if (setting.getType() == SettingType.INTEGER) {
					// integer input field
					JTextField field;
					pane.add(field = new JTextField((String)setting.getValue()),c);
					newComponent = field;
				} else if (setting.getType() == SettingType.FLOAT) {
					// float input field
					JTextField field;
					float value = (Float)setting.getValue();
					pane.add(field = new JTextField(),c);
					if (!Float.isNaN(value))
						field.setText(value+"");
					newComponent = field;
				} else if (setting.getType() == SettingType.LIST) {
					// list
					String[] list = (String[])setting.getValue();
					// find preselected item
					int preselected=0;
					for (int i=0; i<list.length; i++) {
						if (list[i].charAt(0) == '@') {
							list[i] = list[i].substring(1);
							preselected = i;
							break;
						}
					}
					// create and add component
					JComboBox combo = new LongItemComboBox(list);
					pane.add(combo,c);
					combo.setSelectedIndex(preselected);

					newComponent = combo;
				} else if (setting.getType() == SettingType.INFOLABEL) {
					// no component
				} else if (setting.getType() == SettingType.RADIO) {
					// create new panel which contains the buttons
					JPanel subPane = new JPanel();
					subPane.setLayout(new BoxLayout(subPane, BoxLayout.PAGE_AXIS));
					// add all buttons
					String[] list = (String[])setting.getValue();
					ButtonGroup grp = new ButtonGroup();
					for (int i=0; i<list.length; i++) {
						// preselected one?
						boolean preselected=false;
						if (list[i].charAt(0) == '@') {
							list[i] = list[i].substring(1);
							preselected = true;
						}
						// create button
						JRadioButton dummy = new JRadioButton(list[i]);
						dummy.setMargin(new Insets(0,0,0,0));
						subPane.add(dummy);
						grp.add(dummy);
						dummy.setSelected(preselected);
						// add key listeners
						dummy.addKeyListener(keyListener);
					}
					pane.add(subPane,c);
					newComponent = grp;
				} else if (setting.getType() == SettingType.CHECK) {
					JCheckBox check;
					pane.add(check = new JCheckBox(),c);
					check.setSelected((Boolean)setting.getValue());
					newComponent = check;
				} else if (setting.getType() == SettingType.FILE) {
					JTextField txtFile;
					pane.add(txtFile = new JTextField(),c);
					newComponent = txtFile;
					if (setting.getValue() != null)
						txtFile.setText((String)setting.getValue());
					else {
						txtFile.setText("[click to select a file]");
						txtFile.setForeground(Color.DARK_GRAY);
					}
					txtFile.setEditable(false);
					txtFile.setBackground(Color.WHITE);
					// add click listener
					txtFile.addMouseListener(fileClickListener);
					txtFile.setPreferredSize(new Dimension(350, txtFile.getPreferredSize().height));
				} 
				
				if (newComponent != null)
					// add to list
					inputComponents.add(newComponent);

				if (newComponent != null && newComponent instanceof JComponent) {
					JComponent comp = (JComponent)newComponent;

					// set minimal width
					int myHeight = comp.getPreferredSize().height;
					comp.setPreferredSize(new Dimension(MINIMAL_COMPONENT_WIDTH, myHeight));

					// if a verifier came with this component => set it now
					InputVerifier verifier = setting.getVerifier();
					if (verifier != null)
						comp.setInputVerifier(verifier);

					// add focus listener which will selected the text of text fields automatically on focus 
					comp.addFocusListener(new FocusListener() {
						public void focusGained(FocusEvent e) {
							Object source = e.getSource();
							if (source instanceof JTextField) {
								JTextField field = (JTextField)source; 
								field.setSelectionStart(0);
								field.setSelectionEnd(field.getText().length());
							}
						}
						public void focusLost(FocusEvent e) {}
					});
					// add key listener
					comp.addKeyListener(keyListener);
				}
			}



			// go to next cell
			c.gridy++;
		}

		
		
		// create buttons panel
		GridBagConstraints buttonC = new GridBagConstraints();
		buttonC.gridx = 0;
		buttonC.gridy = 0;
		
		JPanel buttonsPane = new JPanel(new GridBagLayout());
		// help button?
		buttonC.anchor = GridBagConstraints.WEST;
		if (settings.getHelpText() != null) 
			buttonsPane.add(new HelpButton(settings.getHelpText()), buttonC);
		// spacer
		buttonC.gridx++;
		buttonC.weightx = 1;
		buttonC.fill = GridBagConstraints.BOTH;
		buttonsPane.add(new JLabel(" "),buttonC);
		
		// buttons
		buttonC.fill = GridBagConstraints.NONE;
		buttonC.weightx = 0;
		buttonC.anchor = GridBagConstraints.EAST;
		buttonC.insets = new Insets(2,6,2,2);
		buttonC.gridx++;
		buttonsPane.add(btnOK = new JButton("OK"), buttonC);
		buttonC.gridx++;
		buttonsPane.add(btnCancel = new JButton("Cancel"), buttonC);
		btnOK.setPreferredSize(btnCancel.getPreferredSize());
		
		// add to main panel
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
		pane.add(buttonsPane,c);

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

		this.addKeyListener(keyListener);

		setContentPane(pane);
	}


	private MouseAdapter getFileClickListener() {
		final ParameterDialog dialog = this;
		return new MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				// show dialog
				File open = GUICommons.chooseFile(dialog, "Choose file", "general", true);
				if (open != null) {
					((JTextField)e.getSource()).setText(open.getAbsolutePath());
				}
			}
			
		};
	}
	
	/**
	 * Creates a listener which presses OK if enter is typed 
	 * and Cancel if escape is types
	 * @return
	 */
	private KeyListener getKeyListener() {
		final ParameterDialog dialog = this;
		return new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER) {
					boolean guiOK = true;
					// need to verify the focused control
					Component focusOwner = dialog.getFocusOwner();
					if (focusOwner instanceof JComponent) {
						JComponent comp = (JComponent)focusOwner;
						InputVerifier verifier = comp.getInputVerifier();
						if (verifier != null) {
							if (!verifier.verify(comp))
								guiOK = false;
						}
					}
					// accept ?
					if (guiOK) {
						accepted = true;
						dispose();
					}
				} else if (keyCode == KeyEvent.VK_ESCAPE)
					dispose();

			}
		};
	}

	public Object[] getUserInput() {

		if (!accepted) return null;

		Object[] result = new Object[nonInfoSettings];

		// get those result
		int index=0;
		for (SingleSetting setting : settingsList) {
			if (setting.getType() != SettingType.INFOLABEL) {
				if (setting.getType() == SettingType.STRING) {
					// string 
					result[index] = ((JTextField)inputComponents.get(index)).getText();
				} else if (setting.getType() == SettingType.INTEGER) {
					// integer
					result[index] = (Integer)Integer.parseInt(((JTextField)inputComponents.get(index)).getText());
				} else if (setting.getType() == SettingType.FLOAT) {
					// float
					String text = ((JTextField)inputComponents.get(index)).getText();
					if (text.length() == 0)
						result[index] = Float.NaN;
					else
						result[index] = (Float)Float.parseFloat(text);
				} else if (setting.getType() == SettingType.LIST) {
					// list, return the index
					result[index] = (Integer)((JComboBox)inputComponents.get(index)).getSelectedIndex();
				} else if (setting.getType() == SettingType.RADIO) {
					// set of radio buttons, return selected index
					ButtonGroup grp = (ButtonGroup)inputComponents.get(index);
					Enumeration<AbstractButton> buttons = grp.getElements();
					int btnIndex=0;
					while (buttons.hasMoreElements()) {
						if (buttons.nextElement().isSelected()) {
							result[index] = btnIndex;
							break;
						}
						btnIndex++;
					}
				} else if (setting.getType() == SettingType.CHECK) {
					// checkbox, return whether it is selected or not
					result[index] = ((JCheckBox)inputComponents.get(index)).isSelected();
				} else if (setting.getType() == SettingType.FILE) {
					// file
					String txt = ((JTextField)inputComponents.get(index)).getText();
					// if no file was selected => return null
					if (txt.startsWith("["))
						result[index] = null;
					else 
						result[index] = txt;
				}
				
				index++;
			}
		}

		return result;
	}




}
