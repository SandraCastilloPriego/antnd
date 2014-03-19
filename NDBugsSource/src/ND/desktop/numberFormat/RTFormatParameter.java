/*
 * Copyright 2007-2012 
 * This file is part of MetModels.
 *
 * MetModels is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MetModels is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package ND.desktop.numberFormat;


import ND.parameters.UserParameter;
import java.util.Collection;
import org.w3c.dom.Element;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 */
/**
 * Simple Parameter implementation
 * 
 * 
 */
public class RTFormatParameter implements
		UserParameter<RTFormatter, RTFormatComponent> {

	private String name, description;
	private RTFormatter value;

	public RTFormatParameter(String name, String description,
			RTFormatter defaultValue) {

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
	public RTFormatComponent createEditingComponent() {
		RTFormatComponent editor = new RTFormatComponent();
		if (value != null)
			editor.setValue(value);
		return editor;
	}

	public RTFormatter getValue() {
		return value;
	}

	@Override
	public void setValue(RTFormatter value) {
		assert value != null;
		this.value = value;
	}

	@Override
	public RTFormatParameter clone() {
		RTFormatParameter copy = new RTFormatParameter(name,
				description, value);
		copy.setValue(this.getValue());
		return copy;
	}

	@Override
	public void setValueFromComponent(RTFormatComponent component) {
		value.setFormat(component.getType(), component.getPattern());
	}

	@Override
	public void setValueToComponent(RTFormatComponent component,
			RTFormatter newValue) {
		component.setValue(newValue);
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		String attrValue = xmlElement.getAttribute("type");
		if (attrValue.length() == 0)
			return;
		RTFormatterType newType = RTFormatterType.valueOf(attrValue);
		String newPattern = xmlElement.getTextContent();
		value.setFormat(newType, newPattern);
	}

	@Override
	public void saveValueToXML(Element xmlElement) {
		xmlElement.setAttribute("type", value.getType().name());
		xmlElement.setTextContent(value.getPattern());
	}

        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
			errorMessages.add(name + " is not set");
			return false;
		}
		return true;
        }

}
