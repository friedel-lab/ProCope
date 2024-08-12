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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import procope.data.purifications.PurificationData;
import procope.methods.scores.ScoresCalculator;


/**
 * Represents a scores calculator defined by the user. Used internally by
 * ProCope and thus not very well documented.
 * 
 * @author Jan Krumsiek
 */
public class UserScoresCalculator {
	
	private String name;
	private String className;
	private boolean multiPuri;
	private List<UserParameter> parameters;
	private Class<?>[] paraClasses;
	
	/**
	 * Creates a new user scores calculator.
	 */
	public UserScoresCalculator(String name, String className,
			boolean multiplePurificationDatasets, List<UserParameter> parameters) {
		
		this.name = name;
		this.className = className;
		this.multiPuri = multiplePurificationDatasets;
		this.parameters = parameters;
		
		// get classes for the parameters
		paraClasses = new Class[parameters.size()+1];
		paraClasses[0] = multiPuri ?
				(new PurificationData[0]).getClass() :
				PurificationData.class;
				
		int index=1;
		for (UserParameter para : parameters) {
			paraClasses[index++] = para.getDataClass();
		}
		
	}
	
	/**
	 * Returns the name of this scores calcuator
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the parameters of this scores calculator.
	 */
	public List<UserParameter> getParameters() {
		return parameters;
	}

	/**
	 * Returns whether this scores calculator accepts multiple
	 * purification data sets.
	 */
	public boolean multiplePurifications() {
		return multiPuri;
	}
	
	/**
	 * Generates the actual {@link ScoresCalculator} object for this user scores
	 * calculator.
	 */
	public ScoresCalculator generateScoresCalculator(PurificationData[] data,
			Object... additionalParameters) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, ClassNotFoundException {
		// get correct constructor
		Class<?> classObject = Class.forName(className);
		Constructor<?> constr = classObject.getConstructor(paraClasses);

		// generate real array
		Object[] parameters = new Object[additionalParameters.length+1];
		parameters[0] = multiPuri ? data : data[0];
		for (int i=0; i<additionalParameters.length; i++)
			parameters[i+1] = additionalParameters[i];
		// generate the object
		return (ScoresCalculator) constr.newInstance(parameters);
	}
	
	
	/**
	 * Parse user scores calculators from a given XML input stream.
	 */
	public static List<UserScoresCalculator> parseCalculators(InputStream stream) throws SAXException, IOException {
			
			// create the handler
			
			final ArrayList<UserScoresCalculator> parsedCalculators = new ArrayList<UserScoresCalculator>();
			
			DefaultHandler handler = new DefaultHandler() {
				private boolean gotRoot=false;
				private boolean inCalculator=false;
				private String curName, curClass;
				private boolean curMultiPuri;
				private ArrayList<UserParameter> curParameters;
				
				public void startElement(String namespaceURI, String localName,
						String qName, Attributes atts) throws UserClassSpecException  {

					// do we still need a root?
					if (gotRoot == false) {
						if (!qName.equals("scorescalculators")) 
							throw new UserClassSpecException("Root entry must be <scorescalculators>");

						gotRoot = true;
					} else {
						// inside a calculator?
						if (!inCalculator) {
							// no
							if (!qName.equals("scorescalculator"))
								throw new UserClassSpecException("Invalid entry: " + qName);
							// store information about this calculator
							curName = atts.getValue("name");
							curClass = atts.getValue("class");
							curMultiPuri = (atts.getValue("multipuri")!=null && atts.getValue("multipuri").equals("1"));
							curParameters = new ArrayList<UserParameter>();
							// check if they were really givne
							if (curName == null || curClass == null)
								throw new UserClassSpecException("The attributes 'name' and 'class' must be specified for every scores calculator");


							inCalculator = true;
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
					 if (name.equals("scorescalculator")) {
						 // end of a calculator, store it
						 parsedCalculators.add(new UserScoresCalculator(curName, curClass, curMultiPuri, curParameters));
						 inCalculator = false;
					 }
				}
			};
			
			// do the parsing
	        SAXParser p=null;
			try {
				p = SAXParserFactory.newInstance().newSAXParser();
			} catch (ParserConfigurationException e) {
			}
	        p.parse(stream, handler);

			return parsedCalculators;
			
	}
	
}
