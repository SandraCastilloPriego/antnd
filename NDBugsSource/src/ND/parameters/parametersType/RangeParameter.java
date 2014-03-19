/*
 * Copyright 2007-2012 
 * 
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
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package ND.parameters.parametersType;

import ND.parameters.UserParameter;
import ND.util.Range;
import java.text.NumberFormat;
import java.util.Collection;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Simple Parameter implementation
 * 
 * 
 */
/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 */
public class RangeParameter implements UserParameter<Range, RangeComponent> {

	private String name, description;
	private NumberFormat format;
	private Range value;

	public RangeParameter(String name, String description, NumberFormat format) {
		this(name, description, format, null);
	}

	public RangeParameter(String name, String description, NumberFormat format,
			Range defaultValue) {
		this.name = name;
		this.description = description;
		this.format = format;
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

	@Override
	public RangeComponent createEditingComponent() {
		return new RangeComponent(format);
	}

	public Range getValue() {
		return value;
	}

	@Override
	public void setValue(Range value) {
		this.value = value;
	}

	@Override
	public RangeParameter clone() {
		RangeParameter copy = new RangeParameter(name, description, format);
		copy.setValue(this.getValue());
		return copy;
	}

	@Override
	public void setValueFromComponent(RangeComponent component) {
		value = component.getValue();
	}

	@Override
	public void setValueToComponent(RangeComponent component, Range newValue) {
		component.setValue(newValue);
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		NodeList minNodes = xmlElement.getElementsByTagName("min");
		if (minNodes.getLength() != 1)
			return;
		NodeList maxNodes = xmlElement.getElementsByTagName("max");
		if (maxNodes.getLength() != 1)
			return;
		String minText = minNodes.item(0).getTextContent();
		String maxText = maxNodes.item(0).getTextContent();
		double min = Double.valueOf(minText);
		double max = Double.valueOf(maxText);
		value = new Range(min, max);
	}

	@Override
	public void saveValueToXML(Element xmlElement) {
		if (value == null)
			return;
		Document parentDocument = xmlElement.getOwnerDocument();
		Element newElement = parentDocument.createElement("min");
		newElement.setTextContent(String.valueOf(value.getMin()));
		xmlElement.appendChild(newElement);
		newElement = parentDocument.createElement("max");
		newElement.setTextContent(String.valueOf(value.getMax()));
		xmlElement.appendChild(newElement);
	}

        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
			errorMessages.add(name + " is not set");
			return false;
		}
		return true;
        }

}
