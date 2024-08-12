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
class BooleanTree {
	
	
	private SingleExpression exp=null;
	private Condition op=null;
	
	public BooleanTree leftChild=null, rightChild=null;

	
	public static BooleanTree createAndNode(BooleanTree leftChild, BooleanTree rightChild) {
		return new BooleanTree(Condition.AND, leftChild, rightChild);
	}
	public static BooleanTree createOrNode(BooleanTree leftChild, BooleanTree rightChild) {
		return new BooleanTree(Condition.OR, leftChild, rightChild);
	}
	public static BooleanTree createXorNode(BooleanTree leftChild, BooleanTree rightChild) {
		return new BooleanTree(Condition.XOR, leftChild, rightChild);
	}
	public static BooleanTree createNotNode(BooleanTree child) {
		return new BooleanTree(Condition.NOT, child, null);
	}
	public static BooleanTree createLeafNode(SingleExpression exp) {
		return new BooleanTree(exp);
	}
	
	private BooleanTree(Condition op, BooleanTree leftChild, BooleanTree rightChild) {
		// inner node
		this.op = op;
		this.leftChild = leftChild;
		this.rightChild = rightChild;
	}
	
	private BooleanTree(SingleExpression exp) {
		// leaf
		this.exp = exp;
	}
	
	
	public boolean evaluate() {
		if (op != null) {
			// inner node
			if (op == Condition.OR) {
				// or operator
				boolean left = leftChild.evaluate();
				// short-circuiting: ignore right branch if left already true
				if (left) 
					return true;
				else
					return rightChild.evaluate();
			} else if (op == Condition.AND) {
				// and operator
				boolean left = leftChild.evaluate();
				// short-circuiting: ignore right branch if left already false
				if (!left) 
					return false;
				else
					return rightChild.evaluate();
			} else if (op == Condition.XOR) {
				// xor operator, no short-circuting possible
				boolean left = leftChild.evaluate();
				boolean right = rightChild.evaluate();
				if (left != right)
					return true;
				else
					return false;
			} else if (op == Condition.NOT) {
				return !leftChild.evaluate();
			} else
				throw new ProCopeException("Something went terribly wrong!");
			
		} else {
			// leaf
			return exp.evaluate();
		}
	}

}
