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
package procope.examples;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import procope.tools.BooleanExpression;
import procope.tools.InvalidExpressionException;
import procope.tools.namemapping.ProteinManager;

public class ProtManager {
	
	// note we just through exceptions out of the main as this code
	// is not intended to demonstrate error handling
	public static void main(String[] args) throws InvalidExpressionException, IOException {
		
		// first we get the internal IDs for some string protein identifiers
		ProteinManager.setCaseSensitivity(true);
		int id1 = ProteinManager.getInternalID("PROTID_A");
		int id2 = ProteinManager.getInternalID("PROTID_B");
		int id3 = ProteinManager.getInternalID("PROTID_A");
		int id4 = ProteinManager.getInternalID("protid_b");
		// note that id1==id3 as they refer to the same protein
		// id2 != id4 as case sensitivity enabled
		System.out.println(id1+", " + id2 + ", " + id3 + ", " + id4);
		
		// next we use a regular expression to extract a certain part of an identifer
		ProteinManager.setRegularExpression("\\s*(.*)\\s+");
		int id5 = ProteinManager.getInternalID("     PROTID_C     ");
		// deactivate regular expression again
		ProteinManager.unsetRegularExpression();
		
		// map pack an internal ID to a string identifier, we will also see 
		// that the regular expression worked and all whitespaces are stripped
		System.out.println("String for " + id5 + ": " +
				ProteinManager.getLabel(id5));
		
		// now we annotate some arbitrary numeric values to some of the proteins
		ProteinManager.addAnnotation(id1, "value", 0.2f);
		ProteinManager.addAnnotation(id2, "value", 0.4f);
		ProteinManager.addAnnotation(id5, "value", 0.6f);
		
		// next we store the annotations to the file system, delete them
		// and reload them from the file we've just written
		ProteinManager.saveProteinAnnotations("annotations");
		ProteinManager.clearAnnotations();
		ProteinManager.loadProteinAnnotations("annotations");
		// clean up
		new File("annotations").delete();
		
		
		// filter out all proteins which have a "value" >= 0.3
		Set<Integer> filtered = 
			ProteinManager.getFilteredProteins(new BooleanExpression("value >= 0.3"));
		// note that all proteins which have no "value" never match this expression
		
		// output these proteins
		for (int protein : filtered) {
			System.out.println(ProteinManager.getLabel(protein));
		}
		
	}

}
