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

package ND.desktop.numberFormat;

import ND.parameters.UserParameter;
import java.text.DecimalFormat;
import java.util.Collection;

import javax.swing.JTextField;


import org.w3c.dom.Element;

/**
 * Simple Parameter implementation
 * 
 * 
 */
public class NumberFormatParameter implements
		UserParameter<DecimalFormat, JTextField> {

	private String name, description;
	private DecimalFormat value;

	public NumberFormatParameter(String name, String description,
			DecimalFormat defaultValue) {

		assert defaultValue != null;

		this.name = name;
		this.description = description;
		this.value = defaultValue;
	}

	/**
	 * @see net.sf.mzmine.data.Parameter#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see net.sf.mzmine.data.Parameter#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public JTextField createEditingComponent() {
		JTextField editor = new JTextField();
		editor.setColumns(8);
		editor.setText(value.toPattern());
		return editor;
	}

	public DecimalFormat getValue() {
		return value;
	}

	@Override
	public void setValue(DecimalFormat value) {
		assert value != null;
		this.value = value;
	}

	@Override
	public NumberFormatParameter clone() {
		NumberFormatParameter copy = new NumberFormatParameter(name, description,
				value);
		copy.setValue(this.getValue());
		return copy;
	}

	@Override
	public void setValueFromComponent(JTextField component) {
		value.applyPattern(component.getText());
	}

	@Override
	public void setValueToComponent(JTextField component, DecimalFormat newValue) {
		component.setText(newValue.toPattern());
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		String newPattern = xmlElement.getTextContent();
		value.applyPattern(newPattern);
	}

	@Override
	public void saveValueToXML(Element xmlElement) {
		xmlElement.setTextContent(value.toPattern());
	}

        public boolean checkValue(Collection<String> errorMessages) {
                return true;
        }

}
