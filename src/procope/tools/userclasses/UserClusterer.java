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
package procope.tools.userclasses;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import procope.methods.clustering.Clusterer;


/**
 * Represents a clusterer defined by the user. Used internally by ProCope and
 * thus not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class UserClusterer {
	
	private List<UserParameter> parameters;
	private String name;
	private String className;
	private Class<?>[] paraClasses;
	
	private UserClusterer(String name, String className, List<UserParameter> parameters) {
		this.name = name;
		this.className = className;
		this.parameters = parameters;
		
		// get classes for the parameters
		paraClasses = new Class[parameters.size()];
		int index=0;
		for (UserParameter para : parameters) {
			paraClasses[index++] = para.getDataClass();
		}
		
	}
	
	/**
	 * Returns the name of this clusterer
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parameters of this clusterer
	 */
	public List<UserParameter> getParameters() {
		return parameters;
	}
	
	/**
	 * Parses user clusterers from a given XML input stream
	 */
	public static List<UserClusterer> parseClusterers(InputStream stream) throws SAXException, IOException {
		
		// create the handler
		
		final ArrayList<UserClusterer> parsedClusterers = new ArrayList<UserClusterer>();
		
		DefaultHandler handler = new DefaultHandler() {
			private boolean gotRoot=false;
			private boolean inClusterer=false;
			private String curName, curClass;
			private ArrayList<UserParameter> curParameters;
			
			public void startElement(String namespaceURI, String localName,
					String qName, Attributes atts) throws UserClassSpecException  {

				// do we still need a root?
				if (gotRoot == false) {
					if (!qName.equals("clusterers")) 
						throw new UserClassSpecException("Root entry must be <clusterers>");

					gotRoot = true;
				} else {
					// inside a clusterer?
					if (!inClusterer) {
						// no
						if (!qName.equals("clusterer"))
							throw new UserClassSpecException("Invalid entry: " + qName);
						// store information about this clusterer
						curName = atts.getValue("name");
						curClass = atts.getValue("class");
						curParameters = new ArrayList<UserParameter>();
						// check if they were really givne
						if (curName == null || curClass == null)
							throw new UserClassSpecException("The attributes 'name' and 'class' must be specified for every clusterer");


						inClusterer = true;
					} else {
						// then this must be a parameter
						if (!qName.equals("parameter"))
							throw new UserClassSpecException("Invalid entry: " + qName + ", awaiting: parameter");

						// parse the parameter
						curParameters.add(UserParameter.getFromSAX(atts));
					}
				}

			}

			 @Override
			public void endElement(String uri, String localName, String name)
					throws SAXException {
				 if (name.equals("clusterer")) {
					 // end of a clusterer, store it
					 parsedClusterers.add(new UserClusterer(curName, curClass, curParameters));
					 inClusterer = false;
				 }
			}
		};
		
		// do the parsing
        SAXParser p=null;
		try {
			p = SAXParserFactory.newInstance().newSAXParser();
		} catch (ParserConfigurationException e) {}
        
        p.parse(new InputSource(stream), handler);

		return parsedClusterers;
		
	}
	
	/**
	 * Generates the actual Clusterer object from this user clusterer.
	 */
	public Clusterer generateClusterer(Object... parameters)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException {
		// get correct constructor
		Class<?> classObject = Class.forName(className);
		Constructor<?> constr = classObject.getConstructor(paraClasses);
		// generate the object
		return (Clusterer)constr.newInstance(parameters);
	}
	
}
