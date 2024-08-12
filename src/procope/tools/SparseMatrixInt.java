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
import java.util.List;

/**
 * Implementation of a matrix (2-dimensional array) of integers. Optimized
 * for sparse matrices, i.e. only relatively few values in the matrix actually
 * have a value.
 * <p>This implementation reduces the space complexity of a matrix with <i>n*n
 * </i> elements from <i>O(n<sup>2</sup>)</i> to <i>O(l)</i>, where <i>l</i> is
 * the number of nonzero items in the matrix.
 * 
 * @author Jan Krumsiek
 */
public class SparseMatrixInt {

	/*
	 * data and state variables
	 */
	private int[][] keys;
	private int[][] values;
	private int bucketSizes[];
	private int bucketMaxSizes[];
	private int resizeStep = 50;
	private static int mainResizeStep = 5000;
	private int mainSize=0;
	private boolean symmetrical;
	
	/**
	 * Creates an empty matrix
	 * 
	 * @param symmetrical if {@code true}, the values {@code (a,b)} and 
	 *                    {@code (b,a)} will always be equal and only
	 *                    consume memory once
	 */
	public SparseMatrixInt(boolean symmetrical) { 
		// initialize
		keys = new int[0][];
		values = new int[0][]; 
		bucketSizes = new int[0];
		bucketMaxSizes = new int[0];
		this.symmetrical = symmetrical;
	}
	
	/**
	 * Set a cell in the matrix to a given value. If the cell is already set
	 * the old value will be overwritten and returned
	 * 
	 * @param x first index
	 * @param y second index
	 * @param value value
	 * @return the old value of that cell or {@link Integer#MIN_VALUE} if the 
	 *         cell was not set before
	 */
	public int set(int x, int y , int value)  {
		
		if (x >= mainSize || y >= mainSize) 
			resizeMain(Math.max(x, y));
		
		int i,j;
		if (symmetrical) {
			i = Math.min(x, y);
			j = Math.max(x, y);
		} else {
			i = x;
			j = y;
		}
		
		// already existing?
		int index = findIndex(i, j);
		if (index >= 0) {
			// already existing
			int old = values[i][index];
			values[i][index] = value;
			return old;
		} else {
			// new
			// need to enlarge bucket?
			if (bucketSizes[i]+1 > bucketMaxSizes[i]) 
				enlargeBucket(i);
			// insert value
			insertIntoBucket(i, j, value, -index-1);  
			bucketSizes[i]++;

			return Integer.MIN_VALUE;
		}
	
	}
	/**
	 * Retrieves a value from the matrix.
	 * 
	 * @param x first index
	 * @param y second index
	 * @return value of that cell or {@link Integer#MIN_VALUE} if the cell is
	 *         not set
	 */
	public int get(int x, int y) {
		
		int i,j;
		if (symmetrical) {
			i = Math.min(x, y);
			j = Math.max(x, y);
		} else {
			i = x;
			j = y;
		}
		
		if (i >= mainSize) 
			return Integer.MIN_VALUE;
		
		int index = findIndex(i, j);
		if (index >= 0) 
			return values[i][index];
		else
			return Integer.MIN_VALUE;
		
	}
	
	/**
	 * Enlarges a given bucket by creating a larger array and copying the old
	 * one to the new one
	 */
	private void enlargeBucket(int bucket) {
		
		// new size
		bucketMaxSizes[bucket] += resizeStep;
		// copy or create new?
		int[] newValues;
		int[] newKeys;
		if (values[bucket] != null) {
			// copy
			newValues = Tools.arrCopyOf(values[bucket], bucketMaxSizes[bucket]);
			newKeys = Tools.arrCopyOf(keys[bucket], bucketMaxSizes[bucket]);
		} else {
			// create new
			newValues = new int[bucketMaxSizes[bucket]];
			newKeys = new int[bucketMaxSizes[bucket]];
		}
		// set new references
		values[bucket] = newValues;
		keys[bucket] = newKeys;
	}
	
	/**
	 * Insert an item into the bucket, index was already determined before
	 * using binary search
	 */
	private void insertIntoBucket(int bucket, int key, int value, int insertion) {
		// shift rest
		for (int j=bucketSizes[bucket]; j>insertion; j--) {
			keys[bucket][j] = keys[bucket][j-1];
			values[bucket][j] = values[bucket][j-1];
		}
		// set new
		keys[bucket][insertion] = key;
		values[bucket][insertion] = value;

	}
	
	/**
	 * perform binary search to find a key in the given bucket, returns
	 * -insertion pos if the key was not found
	 */
	private int findIndex(int bucket, int key) {
		int[] keysub = keys[bucket];
		if (keysub == null) return -1;
		int low = 0;
		int high = bucketSizes[bucket] - 1;
		int mid;

		while( low <= high ) {
			mid = (low + high) / 2;

			if(key > keysub[mid])
				low = mid + 1;
			else if(key < keysub[mid])
				high = mid - 1;
			else
				return mid;
		}

		return -(low+1);     
	}

	/**
	 * Increase the value of a given cell. If the cell is not set yet, the value
	 * 0 is assumed
	 * 
	 * @param x first index
	 * @param y second index
	 * @param value value by which the cell will be increased, can be negative
	 */
	public void add(int x, int y, int value) {
		
		if (x >= mainSize || y >= mainSize) 
			resizeMain(Math.max(x, y));
		
		int i,j;
		if (symmetrical) {
			i = Math.min(x, y);
			j = Math.max(x, y);
		} else {
			i = x;
			j = y;
		}
		
		int index = findIndex(i, j);
		
		if (index < 0) {
			// insert new value
			
			// need to enlarge bucket?
			if (bucketSizes[i]+1 > bucketMaxSizes[i]) 
				enlargeBucket(i);
			// insert value
			insertIntoBucket(i, j, value, -index-1);  
			bucketSizes[i]++;

		} else 
			// increase value
			values[i][index] += value;
	}
	
	/**
	 * Returns the number of elements set in this matrix
	 * 
	 * @return number of elements in the matrix
	 */
	public int getElementCount() {
		int count=0;
		for (int i=0; i<keys.length; i++) 
			count += bucketSizes[i];
		return count;
	}
	
	/**
	 * Get the list of indices which are set as a first index
	 * 
	 * @return list of indices which are used a first index
	 */
	public List<Integer> getFirstIndices() {
		List<Integer> result = new ArrayList<Integer>();
		for (int i=0; i<mainSize; i++) {
			if (bucketSizes[i] > 0)
				result.add(i);
		}
		return result;
	}
	
	/**
	 * Return array of items which have a value associated with the given index
	 * 
	 * @param first index for which to retrieve neighbors
	 * @return array of neighbors for the given index
	 */
	public int[] getNeighbors(int first) {
		return Tools.arrCopyOf(keys[first], bucketSizes[first]);
	}
	
	/**
	 * Return array of values associated with a given index. Only makes sense
	 * in combination with {@link #getNeighbors(int)}.
	 * 
	 * @param first index for which to retrieve values
	 * @return array of values for the given index
	 */
	public int[] getValues(int first) {
		return Tools.arrCopyOf(values[first], bucketSizes[first]);
	}
	
	/**
	 * Resize the main array to hold more items
	 */
	private void resizeMain(int max) {
		 int newMainSize = (max+1+mainResizeStep)/mainResizeStep*mainResizeStep;
		 keys = Tools.arrCopyOf(keys, newMainSize);
		 values = Tools.arrCopyOf(values, newMainSize);
		 bucketSizes = Tools.arrCopyOf(bucketSizes, newMainSize);
		 bucketMaxSizes = Tools.arrCopyOf(bucketMaxSizes, newMainSize);
		 mainSize = newMainSize;
	}
	
}
