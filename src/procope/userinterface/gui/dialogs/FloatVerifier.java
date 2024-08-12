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

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class FloatVerifier extends InputVerifier {

	private float min, max;

	private String format = "%,f";
	private boolean allowEmpty = false;

	public FloatVerifier(float min, float max, int digits) {
		this(min, max);
		this.format = "%,." + digits + "f";
	}
		
	public FloatVerifier(float min, float max) {
		this.min = min;
		this.max = max;		
	}
	
	public FloatVerifier() {
		this.min = -Float.MAX_VALUE;
		this.max = Float.MAX_VALUE;		
	}
	
	public FloatVerifier(boolean allowEmpty) {
		this();
		this.allowEmpty = allowEmpty;
	}

	public boolean verify(JComponent input) {
		if (!(input instanceof JTextField))
			throw new IllegalArgumentException("Only JTextField objects allowed for this verifier.");
		
		// get text
		String contents = ((JTextField)input).getText();

		// maybe allow an empty text
		if (allowEmpty && contents.length() == 0)
			return true;
		
		// assume valid input
		boolean valueok=true;
		// check if text field contains a valid integer between m_min and m_max
		try {
			float value = Float.parseFloat(contents);
			if ((value < min) || (value > max)) {
				valueok = false;
				System.out.println(min);
				System.out.println(max);
				System.out.println(value); 
				System.out.println(value<min);
				System.out.println(value>max); 
			}
		} catch (NumberFormatException nfe) {
			valueok = false;
		}
		
		if (valueok == false)
			if (min != -Float.MAX_VALUE || max != Float.MAX_VALUE) {
				// show warning message
				JOptionPane.showMessageDialog(input.getParent(),"Please enter a number integer between "
						+String.format(format,min)+" and " + String.format(format, max),
						"Invalid value", JOptionPane.WARNING_MESSAGE);
			} else {
				// generic warning message
				JOptionPane.showMessageDialog(input.getParent(),"Please enter a valid number.",
						"Invalid value", JOptionPane.WARNING_MESSAGE);
	
			}
		return valueok;
	}

}
