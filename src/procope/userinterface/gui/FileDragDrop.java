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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * This file is part of the ProCope GUI and not very well documented
 * @author Jan Krumsiek
 */

// file drag and drop listener which should work on all platforms 
public class FileDragDrop extends JList implements DropTargetListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7818363179826736749L;
	
	DropTarget dropTarget = null;
	private GUIMain main;
	private static final String URI_LIST_MIME_TYPE = "text/uri-list;class=java.lang.String";

	private static List<File> textURIListToFileList(String data) {
		List<File> list = new ArrayList<File>();
		// iterate over data, line by line
		for (StringTokenizer st = new StringTokenizer(data, "\r\n"); st
				.hasMoreTokens();) {
			String s = st.nextToken();
			if (s.startsWith("#")) {
				// the line is a comment 
				continue;
			}
			try {
				// try to turn the URI into a file
				URI uri = new URI(s);
				if (uri.isAbsolute()) {
					File file = new File(uri);
					list.add(file);
				}
			} catch (Exception e) {
				// can't do anything about it
			}
		}
		return list;
	}

	public FileDragDrop(GUIMain main) {
		this.main = main;
	}


	public void dragEnter(DropTargetDragEvent event) {
		event.acceptDrag(DnDConstants.ACTION_MOVE);
	}
	public void dragOver(DropTargetDragEvent event) {
	}
	public void dropActionChanged(DropTargetDragEvent event) {
	}
	public void dragExit(DropTargetEvent event) {
	}

	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent event) {
		Transferable transferable = event.getTransferable();
		DefaultListModel model = new DefaultListModel();

		event.acceptDrop(DnDConstants.ACTION_MOVE);

		DataFlavor uriListFlavor = null;
		try {
			uriListFlavor = new DataFlavor(URI_LIST_MIME_TYPE);
		} catch (ClassNotFoundException e) {
			// can't do anything about it
		}

		try {
			if (transferable
					.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				// file list
				List<Object> data = (List<Object>) transferable
						.getTransferData(DataFlavor.javaFileListFlavor);
				for (Object o : data) {
					model.addElement(o);
				}
				reportFiles(data);
			} else if (transferable.isDataFlavorSupported(uriListFlavor)) {
				// different list
				String data = (String) transferable
						.getTransferData(uriListFlavor);
				List files = textURIListToFileList(data);
				for (Object o : files) {
					model.addElement(o);
				}
				reportFiles(files);
			}
		} catch (Exception e) {
			// can't do anything about it
		}

		setModel(model);
	}

	private void reportFiles(List<Object> list) {
		// report this list of files to the main GUI
		ArrayList<String> fileNames = new ArrayList<String>();
		// if these are files => cast to string
		for (Object obj : list) {
			if (obj instanceof String)
				fileNames.add((String) obj);
			else if (obj instanceof File)
				fileNames.add(((File) obj).getAbsolutePath());
			else
				// neither String nor File => can't do anything with it
				;
		}

		main.reportFileDragDrop(fileNames, this);
	}
}
