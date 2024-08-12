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

import java.awt.Component;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import procope.data.complexes.Complex;
import procope.data.complexes.ComplexSet;
import procope.tools.Tools;
import procope.tools.namemapping.ProteinManager;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

public class GUICommons {

	private static String ERROR_REPORT_SCRIPT = "http://www.krumsiek.com/dev/procopeerr.php";
	private static String ERROR_REPORT_VARIABLE = "msg";
	
	private static Config config = Config.getInstance();
	
	public static int readUShort(InputStream in) throws IOException {
		int b = readUByte(in);
		return ((int)readUByte(in) << 8) | b;
	}

	private static int readUByte(InputStream in) throws IOException {
		int b = in.read();
		if (b == -1) {
			throw new EOFException();
		}
		if (b < -1 || b > 255) {
			// Report on this.in, not argument in; see read{Header, Trailer}.
			throw new IOException(".read() returned value out of range -1..255: " + b);
		}
		return b;
	}

	public static void error(String msg) {
		JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void warning(String msg) {
		JOptionPane.showMessageDialog(null, msg, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public static void info(String msg) {
		JOptionPane.showMessageDialog(null, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	
	public static String extractBaseFilenameGZ(String fullPath) {
		if (fullPath.endsWith(".gz")) 
			// do it twice
			return Tools.extractBaseFilename(Tools.extractBaseFilename(fullPath));
		else
			// just once
			return Tools.extractBaseFilename(fullPath);
	}
	
	public static boolean yesNo(String question) {
		return JOptionPane.showConfirmDialog(null, question, "ProCope",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
	}


	public static File chooseFile(Component parent, String title, String dirTag, boolean open) {
		// check for an eventual last accessed directory
		String dir = config.getStringVal("lastdir_" + dirTag, "");
		// create and show the dialog
		JFileChooser chooser = new JFileChooserAskOverwrite(dir);
		chooser.setDialogTitle(title);
		int choice;
		if (open)
			choice = chooser.showOpenDialog(parent);
		else
			choice = chooser.showSaveDialog(parent);
		// save the path
		config.setVal("lastdir_" + dirTag, chooser.getCurrentDirectory().getAbsolutePath());
		// return the result
		if (choice == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		else 
			return null;
	}
	

	public static void showBrowser(URI uri) {
		boolean worked=true;
		try {
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Method getDesktop = desktopClass.getMethod("getDesktop");
			Method browse = desktopClass.getMethod("browse", uri.getClass());
			Object desktop = getDesktop.invoke(null);
			browse.invoke(desktop, uri);
		} catch (Exception e) {
			worked = false;
		} catch (Error e) {
			worked = false;
		}
		if (!worked) {
			// could not open browser, did the user specify a browser?
			String browser = config.getStringVal("userbrowser", null);
			if (browser == null) {
				if (GUICommons.yesNo("Could not open browser. Do you want to select the binary for your browser manually?")) {

					if (
					JOptionPane
					.showOptionDialog(
							null,
							"Do you want to select the browser executable in a file dialog or enter the command directly?",
							"Binary", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null,
							new String[] { "Select file",
							"Enter command" }, null) == JOptionPane.YES_OPTION) {	
						// let the user select it
						File fileBrowser = chooseFile(null, "Select browser", "browser", true);
						if (fileBrowser != null) 
							browser = fileBrowser.getAbsolutePath();
					} else {
						String result = JOptionPane.showInputDialog("Browser command (e.g. 'firefox'):");
						if (result != null)
							browser = result;
					}
				}
			}
			// did he select one now?
			if (browser != null) {
				// try to run the browser
				try {
					Runtime.getRuntime().exec(browser + " " + uri.toString());
					config.setVal("userbrowser", browser);
				} catch (IOException e1) {
					warning("Could not start used-defined browser:\n\n" + e1.getMessage());
					config.deleteVal("userbrowser");
				}
			}
		}
	}
	
	public static boolean sendReport(String report, String reportID) {
		try {
			// construct URL
			String url = ERROR_REPORT_SCRIPT + "?" + URLEncoder.encode(ERROR_REPORT_VARIABLE, "UTF-8") + 
				"=" + URLEncoder.encode("Report: " + reportID + "\n\n" + report, "UTF-8");
			// send HTTP request
			new URL(url).getContent();
			
			return true;
	        
		} catch (Exception e) {
			return false;
		}
	}
	
	public static void unexpectedError(Exception e) {
		// get stack trace as string, construct message
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		// construct message
		String msg = "An unexpected error occured.\n\n";
		msg += "May I send a bug report to the developer? No further data will be submitted!";
		
		// output error
		if (JOptionPane.showConfirmDialog(null, msg, "Error",
				JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
			// send it
			String reportid = md5(System.currentTimeMillis() + "" + (Math.random()*238293));
			if (sendReport("ProCope bug report:\n\n" + sw.toString(), reportid)) {
				if (JOptionPane.showConfirmDialog(null, "Report send!\nWould you like to help improving ProCope by writing an " +
						"additional message\n (which might include a description of how the error occured or your email " +
						"address for further contact)?", "Bug Report",	
						JOptionPane.YES_NO_OPTION , JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					new BugReportDialog(reportid);
				}
				
			} else
				warning("Could not send data. No internet connection?");
		}
		

	}
	
	private static String md5(String in) {
		// create ID
		MessageDigest md5=null;
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				// so what shall we do? the JRE must be broken
			}
		
		String baseID = System.currentTimeMillis() + "" + (Math.random()*238293);
		// create MD5 digest
		md5.update(baseID.getBytes());
		byte[] digest = md5.digest();
		// convert to hex string
		String sessionID="";
		for (int j=0; j<digest.length; j++) {
			String hexPiece = Integer.toHexString(digest[j] & 0xFF);
			if (hexPiece.length() == 1) hexPiece = "0" + hexPiece;
			sessionID += hexPiece;
		}
		
		return sessionID;
	}
	
	public static boolean checkComplexSetSanity(ComplexSet set) {
		final int upperThreshold = 30;
		final int lowerThreshold = 2;
		// check if there are complex names with strange names
		for (Complex c : set) {
			for (int protein : c)  {
				int len = ProteinManager.getLabel(protein).length();
				if (len < lowerThreshold || len > upperThreshold)
					return false;
			}
		}
		return true;
	}
}
