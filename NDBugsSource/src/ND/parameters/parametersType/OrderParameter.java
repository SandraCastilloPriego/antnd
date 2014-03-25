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
public class OrderParameter<ValueType> implements
		UserParameter<ValueType[], OrderComponent> {

	private String name, description;
	private ValueType value[];

	public OrderParameter(String name, String description, ValueType value[]) {

		assert value != null;

		this.name = name;
		this.description = description;
		this.value = value;
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
	public ValueType[] getValue() {
		return value;
	}

	@Override
	public OrderComponent createEditingComponent() {
		return new OrderComponent();
	}

	@Override
	public OrderParameter<ValueType> clone() {
		OrderParameter<ValueType> copy = new OrderParameter<ValueType>(name,
				description, value);
		copy.setValue(this.getValue());
		return copy;
	}

	@Override
	public void setValueFromComponent(OrderComponent component) {
		Object newOrder[] = component.getValues();
		System.arraycopy(newOrder, 0, this.value, 0, newOrder.length);
	}

	@Override
	public void setValueToComponent(OrderComponent component,
			ValueType[] newValue) {
		component.setValues(newValue);
	}

	@Override
	public void setValue(ValueType[] newValue) {
		assert newValue != null;
		this.value = newValue;
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		NodeList items = xmlElement.getElementsByTagName("item");
		ValueType newValues[] = value.clone();
		for (int i = 0; i < items.getLength(); i++) {
			String itemString = items.item(i).getTextContent();
			for (int j = i + 1; j < newValues.length; j++) {
				if (newValues[j].toString().equals(itemString)) {
					ValueType swap = newValues[i];
					newValues[i] = newValues[j];
					newValues[j] = swap;
				}
			}
		}
		value = newValues;
	}

	@Override
	public void saveValueToXML(Element xmlElement) {
		if (value == null)
			return;
		Document parentDocument = xmlElement.getOwnerDocument();
		for (ValueType item : value) {
			Element newElement = parentDocument.createElement("item");
			newElement.setTextContent(item.toString());
			xmlElement.appendChild(newElement);
		}
	}

        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
			errorMessages.add(name + " is not set");
			return false;
		}
		return true;
        }

}
