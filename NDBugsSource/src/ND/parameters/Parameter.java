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
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package ND.parameters;

import java.util.Collection;

import org.w3c.dom.Element;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 */
/**
 * Parameter interface, represents parameters or variables used in the project
 */
public interface Parameter<ValueType> {

	/**
	 * Returns this parameter's name. The name must be unique within one
	 * ParameterSet.
	 *
	 * @return Parameter name
	 */
	public String getName();

	public ValueType getValue();

	public void setValue(ValueType newValue);

	public boolean checkValue(Collection<String> errorMessages);

	public void loadValueFromXML(Element xmlElement);

	public void saveValueToXML(Element xmlElement);

	public Parameter clone();

}
