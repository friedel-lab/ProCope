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
package procope.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class SingleExpressionList {
	
	HashMap<String, ArrayList<SingleExpression>> keyMapping1 = new HashMap<String, ArrayList<SingleExpression>>();
	HashMap<String, ArrayList<SingleExpression>> keyMapping2 = new HashMap<String, ArrayList<SingleExpression>>();
	
	@SuppressWarnings("unchecked")
	public void updateValues(Map<String, Object> values) {
		
		// reset values
		for (ArrayList<SingleExpression> list : keyMapping1.values()) {
			for (SingleExpression exp : list) { 
				exp.updateValue1(null);
			}
		}
		for (ArrayList<SingleExpression> list : keyMapping2.values()) {
			for (SingleExpression exp : list) { 
				exp.updateValue2(null);
			}
		}
			
		
		for (String key : values.keySet()) {
			Object value = values.get(key);
			if (value instanceof Integer) // we have to cast integers to floats
				value = ((Integer)value).floatValue();
			
			ArrayList<SingleExpression> list1 = keyMapping1.get(key); 
			if (list1 != null) {
				for (SingleExpression exp : list1) {
					exp.updateValue1((Comparable)value);
				}
			}
			// update all expressions which have this key as value 1
			ArrayList<SingleExpression> list2 = keyMapping2.get(key); 
			if (list2 != null) {
				for (SingleExpression exp : list2)
					exp.updateValue2((Comparable)value);
			}
		}
	}
	
	public void addExpression(SingleExpression exp, String key1, String key2) {
		
		
		// store this expression for the keys
		if (key1 != null)
			addToMap(keyMapping1, key1, exp);
		if (key2 != null)
			addToMap(keyMapping2, key2, exp);
		
	}
	
	private void addToMap(HashMap<String, ArrayList<SingleExpression>> map, String key, SingleExpression exp) {
		ArrayList<SingleExpression> list = map.get(key);
		if (list == null) {
			list = new ArrayList<SingleExpression>();
			map.put(key, list);
		}
		list.add(exp);
	}

}
