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


/**
 * Internal class needed by BooleanExpression
 */
class SingleExpression {
	
	private Comparable<Object> value1=null, value2=null;

	private Operator operator;

	private boolean not;
	
	public SingleExpression(Operator operator, boolean not) {
		this.operator = operator;
		this.not = not;
	}
	
	public void updateValue1(Comparable<Object> value1){
		this.value1 = value1;
	}
	
	public void updateValue2(Comparable<Object> value2){
		this.value2 = value2;
	}

	
	// javadoc und doku: wenn wert nicht vorhanden => false, auch wenn not
	public boolean evaluate() {
		
		if (value1 == null || value2 == null)
			return false;
		
		// do not compare different types of classes
		if (value1.getClass() != value2.getClass())
			return false;
			
		boolean ret;
		
		if (operator == Operator.EQUALS)
			ret = value1.equals(value2);
		else if (operator == Operator.NOTEQUALS)
			ret = !value1.equals(value2);
		else if (operator == Operator.GREATER)
			ret = value1.compareTo(value2) > 0;
		else if (operator == Operator.GREATEROREQUAL)
			ret = value1.compareTo(value2) >= 0;
		else if (operator == Operator.LESS)
			ret = value1.compareTo(value2) < 0;
		else if (operator == Operator.LESSOREQUAL)
			ret = value1.compareTo(value2) <= 0;
		else
			throw new ProCopeException("Something went terribly wrong!");
		
		if (not)
			return !ret;
		else
			return ret;
	}

}
