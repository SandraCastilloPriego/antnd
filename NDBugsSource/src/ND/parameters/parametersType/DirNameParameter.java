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
package ND.parameters.parametersType;

import ND.parameters.UserParameter;
import java.io.File;
import java.util.Collection;
import org.w3c.dom.Element;

/**
 *
 * @author scsandra
 */
public class DirNameParameter implements UserParameter<File, DirNameComponent> {

	private String name, description;
	private File value;
	private String extension;

	public DirNameParameter(String name, String description) {
		this(name, description, null);
	}

	public DirNameParameter(String name, String description, String extension) {
		this.name = name;
		this.description = description;
		this.extension = extension;
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
	public DirNameComponent createEditingComponent() {
		return new DirNameComponent();
	}

	@Override
	public File getValue() {
		return value;
	}

	@Override
	public void setValue(File value) {
		this.value = value;
	}

	@Override
	public DirNameParameter clone() {
		DirNameParameter copy = new DirNameParameter(name, description);
		copy.setValue(this.getValue());
		return copy;
	}

	@Override
	public void setValueFromComponent(DirNameComponent component) {
		File compValue = component.getValue();
		if (extension != null) {
			if (!compValue.getName().endsWith(extension))
				compValue = new File(compValue.getPath() + "." + extension);
		}
		this.value = compValue;
	}

	@Override
	public void setValueToComponent(DirNameComponent component, File newValue) {
		component.setValue(newValue);
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		String fileString = xmlElement.getTextContent();
		if (fileString.length() == 0)
			return;
		this.value = new File(fileString);
	}

	@Override
	public void saveValueToXML(Element xmlElement) {
		if (value == null)
			return;
		xmlElement.setTextContent(value.getPath());
	}

        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
			errorMessages.add(name + " is not set");
			return false;
		}
		return true;
        }
}

