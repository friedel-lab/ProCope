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
package procope.methods.clustering;

/**
 * Represents a set of parameters for {@link MarkovClusterer MCL clustering}.
 * <p>The <i>inflation coefficient</i> is the most important parameter and
 * affects the granularity of the clustering. For a more detailed description
 * of all parameters consult the MCL manual at 
 * <a target="_blank" href="http://micans.org/mcl/">
 * http://micans.org/mcl/</a>
 * 
 * @author Jan Krumsiek
 */
public class MCLParameters {
	
	private float i=2.0f;
	private float p=Float.NaN;
	private float pct=Float.NaN;
	private float r=Float.NaN;
	private float s=Float.NaN;
	private int scheme=-1;
	int timeoutSeconds=Integer.MAX_VALUE;
	
	/**
	 * Creates a set of MCL parameters with default settings. By default the
	 * inflation coefficient is 2.0, all other parameters are not set. No
	 * timeout value is set (see also: {@link #setTimeout(int)})
	 */
	public MCLParameters() {
		
	}
	
	/**
	 * Set the inflation coefficient
	 * 
	 * @param i inflation coefficient
	 */
	public void setInflation(float i) {
		this.i = i;
	}
	
	/**
	 * Set the pruning number 
	 * 
	 * @param p pruning number
	 */
	public void setP(float p) {
		if (scheme != -1)
			System.err.println("Warning: Setting a parameter is disabling the scheme");
		scheme = -1;
		this.p = p;
	}
	
	/**
	 * Set the selection number
	 * 
	 * @param s selection number
	 */
	public void setS(float s) {
		if (scheme != -1)
			System.err.println("Warning: Setting a parameter is disabling the scheme");
		scheme = -1;
		this.s = s;
	}
	
	/**
	 * Set the recover number
	 * 
	 * @param r recover number
	 */
	public void setR(float r) {
		if (scheme != -1)
			System.err.println("Warning: Setting a parameter is disabling the scheme");
		scheme = -1;
		this.r = r;
	}

	/**
	 * Set <i>mass percentage below which to apply recovery</i>
	 * 
	 * @param pct mass percentage
	 */
	public void setPct(float pct) {
		if (scheme != -1)
			System.err.println("Warning: Setting a parameter is disabling the scheme");
		scheme = -1;
		this.pct = pct;
	}
	
	/**
	 * Set the timeout (in seconds) after which the mcl call will be 
	 * terminated. This setting can be used to avoid too long or non-converging
	 * calculations.
	 * 
	 * @param timeoutSeconds
	 */
	public void setTimeout(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}
	
	/**
	 * Sets a scheme for the parameters p,s,r and pct. Any values set for these
	 * parameters will be unset.
	 * 
	 * @param scheme scheme to be used
	 */
	public void setScheme(int scheme) {
		// output warning if one of the others is != NaN
		if (!Float.isNaN(p) || !Float.isNaN(s) || !Float.isNaN(r) || !Float.isNaN(pct))
			System.err.println("Warning: Setting a scheme is disabling the other parameters p,s,r and pct");
		// set all others to NaN
		p = s = r = pct = Float.NaN;
		
		this.scheme = scheme;
		
	}

	/**
	 * Assemble command line parameters, directly used in command call
	 * 
	 * @return command line parameters string
	 */
	protected String getCommandLineParameters() {
		StringBuffer cmd = new StringBuffer();
		
		if (!Float.isNaN(i))
			cmd.append(" -I " + i);
		
		if (!Float.isNaN(p))
			cmd.append(" -P " + p);
		if (!Float.isNaN(s))
			cmd.append(" -S " + s);
		if (!Float.isNaN(r))
			cmd.append(" -R " + r);
		if (!Float.isNaN(pct))
			cmd.append(" -pct " + pct);
		
		if (scheme != -1)
			cmd.append(" -scheme " + scheme);
		
		cmd.append(" ");
		
		return cmd.toString();
		
	}

}

