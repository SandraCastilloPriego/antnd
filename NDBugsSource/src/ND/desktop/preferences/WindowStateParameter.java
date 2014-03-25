/*
 * Copyright 2007-2012 
 * 
 * This file is part of AntND.
 * 
 * AntND is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * AntND is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * AntND; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package ND.desktop.preferences;

import ND.desktop.impl.MainWindow;
import ND.main.NDCore;
import ND.parameters.Parameter;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.util.Collection;



import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Proxy server settings
 */
public class WindowStateParameter implements Parameter {

	@Override
	public String getName() {
		return "MM window state";
	}

	@Override
	public WindowStateParameter clone() {
		return this;
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {

		MainWindow mainWindow = (MainWindow) NDCore.getDesktop();

		// Set window position
		NodeList posElement = xmlElement.getElementsByTagName("position");
		if (posElement.getLength() == 1) {
			String posString = posElement.item(0).getTextContent();
			String posArray[] = posString.split(":");
			int posX = Integer.valueOf(posArray[0]);
			int posY = Integer.valueOf(posArray[1]);
			mainWindow.setLocation(posX, posY);
		}

		// Set window size
		NodeList sizeElement = xmlElement.getElementsByTagName("size");
		if (sizeElement.getLength() == 1) {
			String sizeString = sizeElement.item(0).getTextContent();
			String sizeArray[] = sizeString.split(":");

			int newState = Frame.NORMAL;

			int width = 800, height = 600;
			if (sizeArray[0].equals("maximized"))
				newState |= Frame.MAXIMIZED_HORIZ;
			else
				width = Integer.parseInt(sizeArray[0]);

			if (sizeArray[1].equals("maximized"))
				newState |= Frame.MAXIMIZED_VERT;
			else
				height = Integer.parseInt(sizeArray[1]);

			mainWindow.setSize(width, height);
			mainWindow.setExtendedState(newState);
		}

	}

	@Override
	public void saveValueToXML(Element xmlElement) {

		Document doc = xmlElement.getOwnerDocument();

		// Get window properties
		MainWindow mainWindow = (MainWindow) NDCore.getDesktop();
		Point position = mainWindow.getLocation();
		int state = mainWindow.getExtendedState();
		Dimension size = mainWindow.getSize();
		String mainWindowWidth, mainWindowHeight;
		if ((state & Frame.MAXIMIZED_HORIZ) != 0)
			mainWindowWidth = "maximized";
		else
			mainWindowWidth = String.valueOf(size.width);
		if ((state & Frame.MAXIMIZED_VERT) != 0)
			mainWindowHeight = "maximized";
		else
			mainWindowHeight = String.valueOf(size.height);

		// Add elements
		Element positionElement = doc.createElement("position");
		xmlElement.appendChild(positionElement);
		positionElement.setTextContent(position.x + ":" + position.y);

		Element sizeElement = doc.createElement("size");
		xmlElement.appendChild(sizeElement);
		sizeElement.setTextContent(mainWindowWidth + ":" + mainWindowHeight);

	}

        public Object getValue() {
                return null;
        }

        public void setValue(Object newValue) {
                
        }

        public boolean checkValue(Collection errorMessages) {
                return true;
        }

}
