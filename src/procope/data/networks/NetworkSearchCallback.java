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

/**
 * This interface must be implemented by classes which function as callback
 * adapter for the network search functions 
 * {@link ProteinNetwork#depthFirstSearch(int, NetworkSearchCallback)} and
 * {@link ProteinNetwork#breadthFirstSearch(int, NetworkSearchCallback)}. A 
 * callback adapter has two functions: (1) It receives the nodes passed during
 * the network search in the order they are visited and (2) it can stop the
 * network search at any point.
 * 
 * <p><font size="+1">Example</font>
 * <p>The interface can be implemented by any class or inner class. A quick way
 * to write a callback adapter is to use an anonymous inner class:
 * 
 * <pre>net.depthFirstSearch(1, new NetworkSearchCallback() {
 *     public boolean reportNode(int protein) {
 *         System.out.println("Passed node: " + protein);
 *         if (/&#42;we want to go on&#42;/)
 *             return true;
 *         else // stop the search
 *             return false;
 *     }
 * });</pre>
 * 
 * @author Jan Krumsiek
 */

public interface NetworkSearchCallback {

	/**
	 * Implemented by network search callback adapters. Receives each protein
	 * passed during the search and returns whether the search should go on.
	 * 
	 * @param protein protein which was just passed in the network search
	 * @return {@code true} if the search should go on, {@code false} to stop
	 *         the search
	 */
	public boolean reportNode(int protein);
	
}
