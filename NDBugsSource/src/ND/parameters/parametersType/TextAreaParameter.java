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
 * AntND; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package ND.parameters.parametersType;

import ND.parameters.UserParameter;
import java.util.Collection;
import javax.swing.JTextArea;



import org.w3c.dom.Element;

/**
 * Simple Parameter implementation
 *
 *
 */
/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 */
public class TextAreaParameter implements UserParameter<String, JTextArea> {

	private String name, description, value;

	public TextAreaParameter(String name, String description) {
		this(name, description, null);
	}

	public TextAreaParameter(String name, String description, String defaultValue) {
		this.name = name;
		this.description = description;
		this.value = defaultValue;
	}

	
	@Override
	public String getName() {
		return name;
	}

	
	@Override
	public String getDescription() {
		return description;
	}


	public JTextArea createEditingComponent() {
		return new JTextArea();
	}

	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public StringParameter clone() {
		StringParameter copy = new StringParameter(name, description);
		copy.setValue(this.getValue());
		return copy;
	}

	@Override
	public void setValueFromComponent(JTextArea component) {
		value = component.getText();
	}

	@Override
	public void setValueToComponent(JTextArea component, String newValue) {
		component.setText(newValue);
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		value = xmlElement.getTextContent();
	}

	@Override
	public void saveValueToXML(Element xmlElement) {
		if (value == null)
			return;
		xmlElement.setTextContent(value);
	}

        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
			errorMessages.add(name + " is not set");
			return false;
		}
		return true;
        }

}
