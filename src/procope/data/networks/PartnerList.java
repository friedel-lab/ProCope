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
class PartnerList {

	public int[][] keys;
	private int bucketSizes[];
	private int bucketMaxSizes[];
	private int resizeStep = 50;
	
	private static int mainResizeStep = 5000;
	private int mainSize=0;
	
	
	private Set<Integer> firstPartners;
	
	/**
	 * Creates a new PartnerList
	 */
	PartnerList() {
		// initialize
		keys = new int[0][];
		bucketSizes = new int[0];
		bucketMaxSizes = new int[0];
		
		firstPartners = new HashSet<Integer>();
	}
	
	/**
	 * Sets the partner flag for two given proteins
	 */
	public void set(int x, int y)  {
		
		if (x >= mainSize || y >= mainSize) 
			resizeMain(Math.max(x, y));
		
		if (x==y) {
			internalSet(x,y);
		} else {
			internalSet(x,y);
			internalSet(y,x);
			
		}
	}
	
	private void internalSet(int x, int y)  {
		
		// already existing?
		int index = findIndex(x, y);
		if (index < 0) {
			
			// new
			// need to enlarge bucket?
			if (bucketSizes[x]+1 > bucketMaxSizes[x]) 
				enlargeBucket(x);
			// insert value
			insertIntoBucket(x, y, -index-1);  
			bucketSizes[x]++;
			
			firstPartners.add(x);

		}
	
	}
	
	public boolean get(int x, int y) {
		
		if (x >= mainSize || y >= mainSize) 
			return false;
		
		int index = findIndex(x, y);
		return (index >= 0); 
	}
	
	private void enlargeBucket(int bucket) {
		
		// new size
		bucketMaxSizes[bucket] += resizeStep;
		// copy or create new?
		int[] newKeys;
		if (keys[bucket] != null) {
			// copy
			newKeys = Tools.arrCopyOf(keys[bucket], bucketMaxSizes[bucket]);
		} else {
			// create new
			newKeys = new int[bucketMaxSizes[bucket]];
		}
		// set new references
		keys[bucket] = newKeys;
	}
	
	// do kind of insertion sort step, has to copy part of the array :-(
	private void insertIntoBucket(int bucket, int key, int insertion) {
		// iterate over bucket to find correct position
				// shift rest
				for (int j=bucketSizes[bucket]; j>insertion; j--) {
					keys[bucket][j] = keys[bucket][j-1];
				}
				// set new
				keys[bucket][insertion] = key;
	
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

	public int[] getPartners(int firstPartner) {
		if (mainSize <= firstPartner || bucketSizes[firstPartner] == 0)
			return new int[0];
		else
			return Tools.arrCopyOf(keys[firstPartner], bucketSizes[firstPartner]);
	}
	
	public Set<Integer> getFirstPartners() {
		return firstPartners;
	}
	
	public boolean delete(int x, int y) {
		
		if (x >= mainSize || y >= mainSize)
			return false;
		
		
		if (x == y) {
			boolean deleted = internalDelete(x, y);
			return deleted;
		} else {
			boolean deleted = internalDelete(x, y);
			if (deleted) {
				internalDelete(y, x);
			} 
			return deleted;
		}
	}
	
	private boolean internalDelete(int i, int j) {
		// search index
		int index = findIndex(i, j);

		if (index >= 0) {
			// shift array beyond that index
			for (int k=index; k<bucketSizes[i]-1; k++) {
				keys[i][k] = keys[i][k+1];
			}
			// reduce bucketsize
			bucketSizes[i]--;
			
			// downsize array?
			if (bucketSizes[i] % resizeStep == 0) {
				keys[i] = Tools.arrCopyOf(keys[i], bucketSizes[i]);
				bucketMaxSizes[i] = bucketSizes[i];
			}
			return true;
		} else
			return false;
	}
	
	private void resizeMain(int max) {
		 int newMainSize = (max+1+mainResizeStep)/mainResizeStep*mainResizeStep;
		 keys = Tools.arrCopyOf(keys, newMainSize);
		 bucketSizes = Tools.arrCopyOf(bucketSizes, newMainSize);
		 bucketMaxSizes = Tools.arrCopyOf(bucketMaxSizes, newMainSize);
		 mainSize = newMainSize;
	}	
	
}
