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
import java.util.Map;
import java.util.Stack;

import procope.data.networks.ProteinNetwork;
import procope.tools.namemapping.ProteinManager;


/**
 * Used to evaluate boolean expressions. An example for such an expression
 * would be:
 * <p><i>(source='Y2H' OR source='TAP') AND score >= 0.5</i> 
 * <p>You can use the boolean expression objects to filter edges in a 
 * {@link ProteinNetwork network} or proteins in the {@link ProteinManager}.
 * <p>For detailed examples on how to use this class check out the manual
 * of the library.
 * 
 * @author Jan Krumsiek
 */

public class BooleanExpression {
	
	private SingleExpressionList expressionlist;
	private BooleanTree root;
	
	/**
	 * Create expression object from a given expression string. The expression
	 * will be parsed and an internal tree representation for rapid evaluation 
	 * will be compiled.
	 * 
	 * @param expression the expression string
	 * @throws InvalidExpressionException if the expression is invalid
	 */
	public BooleanExpression(String expression) throws InvalidExpressionException {

		try {
			expressionlist = new SingleExpressionList();
			char[] arr = preprocess(expression).toCharArray();
			root = parseStatement(arr, 0, arr.length);
		} catch (InvalidExpressionException e) {
			throw e;
		} catch (Exception e) {
			// if any other exception occurs (should not happen actually) => expression must be invalid
			throw new InvalidExpressionException("Invalid expression: " + expression);
		}
	}
	
	/**
	 * Evaluate a given set of <i>variable name</i> =&#62; <i>value</i> 
	 * mappings using the expression.
	 * 
	 * @param values variables and their annotations, for non-numeric object
	 *               type the String representation of that object will be used
	 * @return {@code true} or {@code false} depending on whether the variables
	 *         let the expression evaluate to {@code true} or {@code false}
	 */
	
	public boolean evaluate(Map<String, Object> values) {
		expressionlist.updateValues(values);
		return root.evaluate();
	}
	
	/**
	 * Preprocess pattern, check brackets levels and add missing brackets
	 */
	private String preprocess(String expression) throws InvalidExpressionException {
		
		// check if brackets are OK
		int open=0;
		for (int i=0; i<expression.length(); i++) {
			char c = expression.charAt(i);
			// deeper?
			if (c == '(') 
				open++;
			else if (c == ')') 
				open--;
		}
		// throw exception if something went wrong
		if (open > 0)
			throw new InvalidExpressionException("Missing closing brackets in: " + expression);
		else if (open < 0)
			throw new InvalidExpressionException("Missing opening brackets in: " + expression);
		
		StringBuffer buffer = new StringBuffer(expression);
		// insert brackets at beginning and end, they are needed and don't hurt
		buffer.insert(0, '(');
		buffer.insert(buffer.length(), ')');
		
		// first level starts at beginning
		Stack<Integer> startPos = new Stack<Integer>();
		startPos.push(-1);
		Stack<ArrayList<Integer>> operatorPos = new Stack<ArrayList<Integer>>();
		ArrayList<Integer> currentOpPos = new ArrayList<Integer>();
		operatorPos.push(currentOpPos);
		
		// iterate over string
		for (int i=0; i<buffer.length(); i++) {
			char c = buffer.charAt(i);
			
			// deeper?
			if (c == '(') {
				// one level deeper
				startPos.push(i);
				currentOpPos = new ArrayList<Integer>();
				operatorPos.push(currentOpPos);
			} else if (c == ')' || i==buffer.length()-1) {
				// evaluate this level
				int start = startPos.pop();
				// if there was more than one operator: we need brackets
				if (currentOpPos.size() > 1) {
					System.out.println("inserting, stack height: " + startPos.size() + ", operators: " + currentOpPos);
					for (int j=1; j<currentOpPos.size(); j++) {
						buffer.insert(start+1, '(');
						buffer.insert((int)currentOpPos.get(j)+(j*2)-1, ')');
						// skip the new chars
						i+=2;
					}
				}
				
				// get level before
				operatorPos.pop();
				currentOpPos = operatorPos.peek();

				
			} else if (c == '&' || c == '|' || c == '^') {
				// store position of this operator
				currentOpPos.add(i);
			}
			
		}
		
		return buffer.toString();
	}
		
	/**
	 * Parse tree from a given string statement
	 */
	private BooleanTree parseStatement(char[] arr, int from, int to) throws InvalidExpressionException {
		
		// find surrounding brackets, if there are none: expression
		// opening bracket
		int openBracket=-1;
		for (int i=from; i<to; i++) {
			if (arr[i] == '(') { 
				openBracket = i;
				break;
			}
			else if (arr[i] != ' ') 
				break;
		}
		// closing bracket
		int closeBracket=-1;
		for (int i=to-1; i>=from; i--) {
			if (arr[i] == ')') { 
				closeBracket = i;
				break;
			}
			else if (arr[i] != ' ') break;
		}

		if ((openBracket == -1 && closeBracket >= 0) || (closeBracket ==-1 && openBracket>=0))
			throw new InvalidExpressionException("Syntax error for sub-expression: " + new String(arr,from,to));

		if (openBracket == -1) {
			// expression 
			return BooleanTree.createLeafNode(parseExpression(arr, from, to));
			
		} else {
			// need further parsing
			from = openBracket+1;
			to = closeBracket;

			// find outtermost conditional operator
			Condition cond = null;
			int depth=0;
			for (int i=from; i<to; i++) {
				// change depths
				if (arr[i] == '(')
					depth++;
				else if (arr[i] == ')')
					depth--;
				// only examine the outside
				if (depth == 0) {
					// is there an operator ?
					if (arr[i] == '&') cond = Condition.AND;
					else if (arr[i] == '|') cond = Condition.OR;
					else if (arr[i] == '^') cond = Condition.XOR;
					if (cond != null) {
						// here is an operator, call recursively for both sides
						BooleanTree left = parseStatement(arr, from, i);
						BooleanTree right = parseStatement(arr, i+1, to);
						// return it
						if (cond == Condition.AND)
							return BooleanTree.createAndNode(left, right);
						else if (cond == Condition.OR)
							return BooleanTree.createOrNode(left, right);
						else if (cond == Condition.XOR)
							return BooleanTree.createXorNode(left, right);
					}

				}
			}				
		}
		
		// if we are here there was no operator and it was no expression => unnecessary brackets
		return parseStatement(arr, openBracket+1, closeBracket );

	}
	
	/**
	 * Parse a single expression [literal][operator][literal]
	 */
	@SuppressWarnings("unchecked")
	private SingleExpression parseExpression(char[] arr, int from, int to) throws InvalidExpressionException {

		// check if first character is a ! => NOT
		boolean NOT=false;
		for (int i=from; i<to; i++) {
			if (arr[i] == '!') {
				NOT = true;
				from=i+1;
				break;
			}
			if (arr[i] != ' ')
				break;
		}
		
		// find comparison operator
		Operator op = null;
		int opPos=-1,opLen=1;
		for (int i=from; i<to; i++) {
			
			if (arr[i] == '=') { // equality
				// must be ==
				if (i==to-1 || arr[i+1] != '=')
					throw new InvalidExpressionException("Invalid operator: " + new String(arr,i,2));
				op = Operator.EQUALS;
				opLen = 2;
			} else if (arr[i] == '!') { // inequality
				// must be != 
				if (i==to-1 || arr[i+1] != '=')
					throw new InvalidExpressionException("Invalid operator: " + new String(arr,i,2));
				op = Operator.NOTEQUALS;
				opLen = 2;
			} else if (arr[i] == '>') { // greater or greater-or-equal
				if (i<to && arr[i+1] == '=') {
					// >=
					op = Operator.GREATEROREQUAL;
					opLen = 2;
				} else {
					op = Operator.GREATER;
				}
			} else if (arr[i] == '<') { // less or less-or-equal
				if (i<to && arr[i+1] == '=') {
					// <=
					op = Operator.LESSOREQUAL;
					opLen = 2;
				} else {
					op = Operator.LESS;
				}
			}
			
			
			if (op != null) {
				opPos = i;
				break;
			}
		}
		
		if (op == null) {
			// no operator found
			throw new InvalidExpressionException("Missing operator in: " + new String(arr,from,to-from));
		} else {
			// parse literals
			Literal left = parseLiteral(arr, from, opPos);
			Literal right = parseLiteral(arr, opPos+opLen, to);
			
			SingleExpression exp = new SingleExpression(op, NOT);
			// add to list
			String key1 = left.variable ? (String)left.value : null;
			String key2 = right.variable ? (String)right.value : null;
			expressionlist.addExpression(exp, key1, key2);
			// set constant literals
			if (!left.variable)
				exp.updateValue1((Comparable)left.value);
			if (!right.variable)
				exp.updateValue2((Comparable)right.value);
			
			
			return exp;
		}
		
	
		
	}

	/**
	 * Parse a literal, find out if it is a variable or a constant value and get
	 * the corresponding object
	 */
	private Literal parseLiteral(char[] arr, int from, int to) throws InvalidExpressionException {
		
		// check for the first character
		int firstPos=-1;
		char firstChar = 0;
		for (int i=from; i<to; i++) {
			if (arr[i] != ' ') {
				firstPos = i;
				firstChar = arr[i];
				break;
			}
		}
		
		if (Character.isDigit(firstChar) || firstChar=='.' || firstChar=='-') {
			// try to parse float
			String toParse = new String(arr,from,to-from).trim();
			Object value = null;
			try {
				value = Float.parseFloat(toParse);
			} catch (NumberFormatException e) {
				throw new InvalidExpressionException("Invalid numeric expression: " + toParse);
			}
			
			return new Literal(value, false);

		} else if (firstChar == '\'') {
			// find closing '
			int closing=-1;
			for (int i=to-1; i>=from; i--) {
				if (arr[i] == '\'') {
					closing = i;
					break;
				}
			}
			// valid?
			if (closing == firstPos) 
				throw new InvalidExpressionException("Invalid String expression: " + new String(arr,from,to-from));
			Object value = (String)new String(arr,firstPos+1,closing-firstPos-1);
			return new Literal(value, false);
			
		} else {
			// this is a variable
			String variable = new String(arr,from,to-from).trim();
			validateVariableName(variable);
			return new Literal(variable, true);
		}
	}


	/**
	 * Check if a variable name is OK, throw exception if not
	 */
	private void validateVariableName(String variable) throws InvalidExpressionException {
		if (variable.indexOf("<") >= 0 || variable.indexOf(">") >= 0
				|| variable.indexOf("!") >= 0 || variable.indexOf("=") >= 0
				|| variable.indexOf("&") >= 0 || variable.indexOf("|") >= 0
				|| variable.indexOf("^") >= 0) {
			throw new InvalidExpressionException("Invalid variable name: " +  variable);
		}
	}
	
	/**
	 * a literate, might be a variable or a constant value (string or numeric)
	 */
	private class Literal {
		private boolean variable;
		private Object value;

		public Literal(Object value, boolean variable) {
			this.value = value;
			this.variable = variable;
		}
		@Override
		public String toString() {
			if (variable)
				return "variable: " + value;
			else
				return "value of type " + value.getClass() + ": " + value;
		}
	}
	

}
