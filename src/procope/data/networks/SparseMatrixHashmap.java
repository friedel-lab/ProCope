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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import procope.tools.Tools;

/**
 * Internal class, used by ProteinNetwork
 */
class SparseMatrixHashmap {

	public int[][] keys;
	private HashMap<String, Object>[][] values;
	private int bucketSizes[];
	private int bucketMaxSizes[];
	private int resizeStep = 50;
	private boolean symmetrical;
	
	private static int mainResizeStep = 5000;
	private int mainSize=0;
	
	int count=0;
	private Set<Integer> firstPartners;
	
	@SuppressWarnings("unchecked")
	SparseMatrixHashmap(boolean symmetrical) { 
		// initialize
		keys = new int[0][];
		values = new HashMap[0][]; 
		bucketSizes = new int[0];
		bucketMaxSizes = new int[0];
		
		firstPartners = new HashSet<Integer>();
		
		this.symmetrical = symmetrical;
	}
	
	public boolean add(int x, int y, String key, Object value)  {
		
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
		boolean isNew;
		HashMap<String, Object> map;
		int index = findIndex(i, j);
		if (index >= 0) {
			// add to existing
			map = values[i][index];
			isNew = false;
		} else {
			// create new
			// need to enlarge bucket?
			if (bucketSizes[i]+1 > bucketMaxSizes[i]) 
				enlargeBucket(i);
			// insert value
			insertIntoBucket(i, j, map = new HashMap<String, Object>(), -index-1);  
			bucketSizes[i]++;
			
			count++;
			firstPartners.add(i);
			isNew = true;
		}
		
		map.put(key, value);
		return isNew;
	
	}
	
	private void resizeMain(int max) {
		 int newMainSize = (max+1+mainResizeStep)/mainResizeStep*mainResizeStep;
		 keys = Tools.arrCopyOf(keys, newMainSize);
		 values = Tools.arrCopyOf(values, newMainSize);
		 bucketSizes = Tools.arrCopyOf(bucketSizes, newMainSize);
		 bucketMaxSizes = Tools.arrCopyOf(bucketMaxSizes, newMainSize);
		 mainSize = newMainSize;
	}

	
	public Object get(int x, int y, String key) {
		
		if (x >= mainSize || y >= mainSize) 
			return null;
		
		int i,j;
		if (symmetrical) {
			i = Math.min(x, y);
			j = Math.max(x, y);
		} else {
			i = x;
			j = y;
		}	
		
		int index = findIndex(i, j);
		
//		System.out.println("index of " + i + ","+j+": " +index);
//		System.out.println("key bucket of " +i + ": " + Arrays.toString(keys[i]));
		
		if (index >= 0) 
			return values[i][index].get(key);
		else
			return null;
		
	}
	

	public Map<String, Object> getAll(int x, int y) {

		if (x >= mainSize || y >= mainSize) 
			return null;
		
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
			return null;
	}
	
	@SuppressWarnings("unchecked")
	private void enlargeBucket(int bucket) {
		
		// new size
		bucketMaxSizes[bucket] += resizeStep;
		// copy or create new?
		HashMap<String, Object>[] newValues;
		int[] newKeys;
		if (values[bucket] != null) {
			// copy
			newValues = Tools.arrCopyOf(values[bucket], bucketMaxSizes[bucket]);
			newKeys = Tools.arrCopyOf(keys[bucket], bucketMaxSizes[bucket]);
		} else {
			// create new
			newValues = new HashMap[bucketMaxSizes[bucket]];
			newKeys = new int[bucketMaxSizes[bucket]];
		}
		// set new references
		values[bucket] = newValues;
		keys[bucket] = newKeys;
	}
	
	// do kind of insertion sort step, has to copy part of the array :-(
	private void insertIntoBucket(int bucket, int key, HashMap<String, Object> value, int insertion) {
		
		// shift rest
		for (int j=bucketSizes[bucket]; j>insertion; j--) {
			keys[bucket][j] = keys[bucket][j-1];
			values[bucket][j] = values[bucket][j-1];
		}
		// set new
		keys[bucket][insertion] = key;
		values[bucket][insertion] = value;
		// do not compare any further
		 
		
	//	System.out.println("Inserting into bucket at " + insertion);
		
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

	public Set<Integer> getFirstPartners() {
		return firstPartners;
	}
	
	public boolean removeSingle(int x, int y, String key) {
		Map<String, Object> annotations = getAll(x,y);
		Object removed = annotations.remove(key);
		// map empty?
		if (annotations.size() == 0) 
			delete(x,y);
		
		return (removed != null);
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
	
}
