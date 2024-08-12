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
package procope.data.networks;

import java.util.HashSet;
import java.util.Set;

import procope.tools.Tools;

/**
 * Internal class, used by ProteinNetwork
 */
class SparseMatrixFloat {

	public int[][] keys;
	private float[][] values;
	private int bucketSizes[];
	private int bucketMaxSizes[];
	private static int resizeStep = 50;
	private boolean symmetrical;
	
	private static int mainResizeStep = 5000;
	private int mainSize=0;
	
	int count=0;
	private Set<Integer> firstPartners;
	
	SparseMatrixFloat(boolean symmetrical) { 
		// initialize
		keys = new int[0][];
		values = new float[0][]; 
		bucketSizes = new int[0];
		bucketMaxSizes = new int[0];
		
		firstPartners = new HashSet<Integer>();
		
		this.symmetrical = symmetrical;
	}
	
	public float set(int x, int y , float value)  {
		
		int i,j;
		if (symmetrical) {
			i = Math.min(x, y);
			j = Math.max(x, y);
		} else {
			i = x;
			j = y;
		}
		
		if (i >= mainSize || j >= mainSize) 
			resizeMain(Math.max(i, j));
		
		// already existing?
		int index = findIndex(i, j);
		if (index >= 0) {
			// already existing
			float old = values[i][index];
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
			
			count++;
			firstPartners.add(i);

			return Float.NaN;
		}
	
	}
	
	private void resizeMain(int max) {
		 int newMainSize = (max+1+mainResizeStep)/mainResizeStep*mainResizeStep;
		 keys = Tools.arrCopyOf(keys, newMainSize);
		 values = Tools.arrCopyOf(values, newMainSize);
		 bucketSizes = Tools.arrCopyOf(bucketSizes, newMainSize);
		 bucketMaxSizes = Tools.arrCopyOf(bucketMaxSizes, newMainSize);
		 mainSize = newMainSize;
	}

	public float get(int x, int y) {
		
		if (x >= mainSize || y >= mainSize) 
			return Float.NaN;
		
		int i,j;
		if (symmetrical) {
			i = Math.min(x, y);
			j = Math.max(x, y);
		} else {
			i = x;
			j = y;
		}	
		
		int index = findIndex(i, j);
		if (index >= 0) 
			return values[i][index];
		else
			return Float.NaN;
		
	}
	
	public boolean delete(int x, int y) {
		
		if (x >= mainSize || y >= mainSize)
			return false;
		
		int i,j;
		if (symmetrical) {
			i = Math.min(x, y);
			j = Math.max(x, y);
		} else {
			i = x;
			j = y;
		}	
		
		// search index
		int index = findIndex(i, j);
		
		if (index < 0) 
			return false;
		else {
			
			// shift array beyond that index
			for (int k=index; k<bucketSizes[i]-1; k++) {
				keys[i][k] = keys[i][k+1];
				values[i][k] = values[i][k+1];
			}
			// reduce bucketsize
			bucketSizes[i]--;
			
			// downsize array?
			if (bucketSizes[i] % resizeStep == 0) {
				keys[i] = Tools.arrCopyOf(keys[i], bucketSizes[i]);
				values[i] = Tools.arrCopyOf(values[i], bucketSizes[i]);
				bucketMaxSizes[i] = bucketSizes[i];
			}
			
			return true;
		}
	}
	
	private void enlargeBucket(int bucket) {
		
		// new size
		bucketMaxSizes[bucket] += resizeStep;
		// copy or create new?
		float[] newValues;
		int[] newKeys;
		if (values[bucket] != null) {
			// copy
			newValues = Tools.arrCopyOf(values[bucket], bucketMaxSizes[bucket]);
			newKeys = Tools.arrCopyOf(keys[bucket], bucketMaxSizes[bucket]);
		} else {
			// create new
			newValues = new float[bucketMaxSizes[bucket]];
			newKeys = new int[bucketMaxSizes[bucket]];
		}
		// set new references
		values[bucket] = newValues;
		keys[bucket] = newKeys;
	}
	
	// do kind of insertion sort step, has to copy part of the array :-(
	private void insertIntoBucket(int bucket, int key, float value, int insertion) {
		// iterate over bucket to find correct position
//		for (int i=0; i<=bucketSizes[bucket]; i++) {
//			if (keys[bucket][i] > key || i == bucketSizes[bucket]) {
				// shift rest
				for (int j=bucketSizes[bucket]; j>insertion; j--) {
					keys[bucket][j] = keys[bucket][j-1];
					values[bucket][j] = values[bucket][j-1];
				}
				// set new
				keys[bucket][insertion] = key;
				values[bucket][insertion] = value;
				// do not compare any further
//				break;
//			}
//		}
	
	}
	
	// do binary search to find key, return -insertion pos otherwise
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
	 * increase value, if not existing => assume 0
	 * @param i
	 * @param j
	 * @param value
	 */
	public void add(int x, int y, float value) {
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
			
			count++;
			firstPartners.add(i);


		} else 
			// increase value
			values[i][index] += value;
	}
	
//	public int getElementCount() {
//		return count;
//	}
	
	public int[] getPartners(int firstPartner) {
		if (keys[firstPartner] == null) return null;
		return Tools.arrCopyOf(keys[firstPartner], bucketSizes[firstPartner]);
	}
	
	public float[] getValues(int firstPartner) {
		return Tools.arrCopyOf(values[firstPartner], bucketSizes[firstPartner]);
	}
	
	public Set<Integer> getFirstPartners() {
		return firstPartners;
	}
}
