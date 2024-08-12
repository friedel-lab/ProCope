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

import java.util.regex.Pattern;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * This file is part of the ProCope GUI and not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class RegexVerifier extends InputVerifier {

	private Pattern pattern;
	private String errorMessage;

	public RegexVerifier (String regex, String errorMessage) {
		this.pattern = Pattern.compile(regex);
		this.errorMessage = errorMessage;
	}
	
	@Override
	public boolean verify(JComponent input) {
		
		if (!(input instanceof JTextField))
			throw new IllegalArgumentException("Only JTextField objects allowed for this verifier.");
		
		// get text
		String contents = ((JTextField)input).getText();
		
		if (!pattern.matcher(contents).matches()) {
			// pattern did not match
			JOptionPane.showMessageDialog(input.getParent(), errorMessage,
						"Invalid value", JOptionPane.WARNING_MESSAGE);
			return false;
		} else
			return true;
			
	}

}
