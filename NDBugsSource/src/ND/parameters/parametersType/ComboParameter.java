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
import java.util.Collection;
import javax.swing.JComboBox;


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
public class ComboParameter<ValueType> implements
        UserParameter<ValueType, JComboBox> {

        private String name, description;
        private ValueType choices[], value;

        public ComboParameter(String name, String description, ValueType choices[]) {
                this(name, description, choices, null);
        }

        public ComboParameter(String name, String description, ValueType choices[],
                ValueType defaultValue) {
                this.name = name;
                this.description = description;
                this.choices = choices;
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
        public JComboBox createEditingComponent() {
                return new JComboBox(choices);
        }

        @Override
        public ValueType getValue() {
                return value;
        }

        public ValueType[] getChoices() {
                return choices;
        }

        public void setChoices(ValueType newChoices[]) {
                this.choices = newChoices;
        }

        @Override
        public void setValue(ValueType value) {
                this.value = value;
        }

        @Override
        public ComboParameter<ValueType> clone() {
                ComboParameter<ValueType> copy = new ComboParameter<ValueType>(name,
                        description, choices);
                copy.value = this.value;
                return copy;
        }

        @Override
        public void setValueFromComponent(JComboBox component) {
                int index = component.getSelectedIndex();
                if (index < 0) {
                        return;
                }
                value = choices[index];
        }

        @Override
        public void setValueToComponent(JComboBox component, ValueType newValue) {
                component.setSelectedItem(newValue);
        }

        @Override
        public void loadValueFromXML(Element xmlElement) {
                String elementString = xmlElement.getTextContent();
                if (elementString.length() == 0) {
                        return;
                }
                for (ValueType option : choices) {
                        if (option.toString().equals(elementString)) {
                                value = option;
                                break;
                        }
                }
        }

        @Override
        public void saveValueToXML(Element xmlElement) {
                if (value == null) {
                        return;
                }
                xmlElement.setTextContent(value.toString());
        }

        @Override
        public String toString() {
                return name;
        }

        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
                        errorMessages.add(name + " is not set");
                        return false;
                }
                return true;
        }
}
