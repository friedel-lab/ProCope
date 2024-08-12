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
package procope.userinterface.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import procope.tools.Tools;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

// Class for handling configuration XML files

public class Config extends DefaultHandler {
	
	private static Config instance=null;
	private HashMap<String,String> settings;
	
	static {
		// create the instance
		try {
			instance = new Config();
		} catch (Exception e) {
			// create Config instance without any backing XML file
			instance = new Config(true);
		}
	}
	
	
	public static Config getInstance() {
		
		return instance;
	}

	
	public String getStringVal(String name, String defval) {
		String temp = settings.get(name);
		if (temp != null)
			return temp;
		else
			return defval;
	}
	
	public void deleteVal(String name) {
		settings.remove(name);
	}
	
	public int getIntVal(String name, int defval) {
		String temp = settings.get(name);
		if (temp != null)
			return Integer.parseInt(temp);
		else
			return defval;
			
	}
	
	public void setIntVal(String name, int val) {
		setVal(name, val+"");
	}
	
	public void setVal(String name, String val) {
		settings.remove(name);
		settings.put(name,val);
	}
	
	public void storeConfig() throws FileNotFoundException {
		// create directory if needed
		new File(Tools.CONFIGPATH).mkdirs();
		// open output file
		PrintStream p = new PrintStream(
                new FileOutputStream(Tools.CONFIGFILE));
		// write initial line
		p.println("<settings>");
		// iterate through hashmap
		Iterator<String> iterator = settings.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			String value = settings.get(key);

			p.println("\t<setting name=\"" + key + "\" value=\"" + value + "\" />");
		}
		// write ending line
		p.println("</settings>");
		
		p.close();

	}
	
	private Config() throws Exception {
		// create XML parser
		SAXParser p = SAXParserFactory.newInstance().newSAXParser();
		// set object itsself as content handler
		settings = new HashMap<String,String>();
		// start parsing
		p.parse(new File(Tools.CONFIGFILE), this);
	}
	
	private Config(boolean noFile) {
		// constructor with overloading dummy variable
		
		// create hashmap
		settings = new HashMap<String,String>();
	}
	
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts)  {
		if (qName.toLowerCase().equals("setting")) {
			// add to hashmap
			settings.put(atts.getValue("name"), atts.getValue("value"));
		}
	}
}
