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

import ND.parameters.ParameterSet;
import ND.parameters.Parameter;

import ND.parameters.UserParameter;
import java.util.Collection;
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
public class OptionalModuleParameter implements
		UserParameter<Boolean, OptionalModuleComponent> {

	private String name, description;
	private ParameterSet embeddedParameters;
	private Boolean value;

	public OptionalModuleParameter(String name, String description,
			ParameterSet embeddedParameters) {
		this.name = name;
		this.description = description;
		this.embeddedParameters = embeddedParameters;
	}

	public ParameterSet getEmbeddedParameters() {
		return embeddedParameters;
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
	public OptionalModuleComponent createEditingComponent() {
		return new OptionalModuleComponent(embeddedParameters);
	}

	@Override
	public Boolean getValue() {
		// If the option is selected, first check that the module has all
		// parameters set
		if ((value != null) && (value)) {
			for (Parameter p : embeddedParameters.getParameters()) {
				if (p instanceof UserParameter) {
					UserParameter up = (UserParameter) p;
					Object upValue = up.getValue();
					if (upValue == null)
						return null;
				}
			}
		}
		return value;
	}

	@Override
	public void setValue(Boolean value) {
		this.value = value;
	}

	@Override
	public OptionalModuleParameter clone() {
		OptionalModuleParameter copy = new OptionalModuleParameter(name,
				description, embeddedParameters.clone());
		copy.setValue(this.getValue());
		return copy;
	}

	@Override
	public void setValueFromComponent(OptionalModuleComponent component) {
		this.value = component.isSelected();
	}

	@Override
	public void setValueToComponent(OptionalModuleComponent component,
			Boolean newValue) {
		component.setSelected(newValue);
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		embeddedParameters.loadValuesFromXML(xmlElement);
		String selectedAttr = xmlElement.getAttribute("selected");
		this.value = Boolean.valueOf(selectedAttr);
	}

	@Override
	public void saveValueToXML(Element xmlElement) {
		if (value != null)
			xmlElement.setAttribute("selected", value.toString());
		embeddedParameters.saveValuesToXML(xmlElement);
	}

        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
			errorMessages.add(name + " is not set");
			return false;
		}
		return true;
        }
}
