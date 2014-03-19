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

import ND.modules.NDModule;
import ND.parameters.ParameterSet;
import ND.parameters.Parameter;
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
public class ModuleComboParameter<ModuleType extends NDModule> implements
        UserParameter<ModuleType, ModuleComboComponent> {

        private String name, description;
        private ModuleType modules[];
        private ModuleType value;

        public ModuleComboParameter(String name, String description,
                ModuleType modules[]) {
                this.name = name;
                this.description = description;
                this.modules = modules;
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
        public ModuleComboComponent createEditingComponent() {
                return new ModuleComboComponent(modules);
        }

        public ModuleType getValue() {
                if (value == null) {
                        return null;
                }
                // First check that the module has all parameters set
                ParameterSet embeddedParameters = value.getParameterSet();
                if (embeddedParameters == null) {
                        return value;
                }
                for (Parameter p : embeddedParameters.getParameters()) {
                        if (p instanceof UserParameter) {
                                UserParameter up = (UserParameter) p;
                                Object upValue = up.getValue();
                                if (upValue == null) {
                                        return null;
                                }
                        }
                }
                return value;
        }

        @Override
        public void setValue(ModuleType value) {
                this.value = value;
        }

        @Override
        public ModuleComboParameter<ModuleType> clone() {
                ModuleComboParameter<ModuleType> copy = new ModuleComboParameter<ModuleType>(
                        name, description, modules);
                copy.setValue(this.getValue());
                return copy;
        }

        @Override
        public void setValueFromComponent(ModuleComboComponent component) {
                int index = component.getSelectedIndex();
                if (index < 0) {
                        return;
                }
                this.value = modules[index];
        }

        @Override
        public void setValueToComponent(ModuleComboComponent component,
                ModuleType newValue) {
                component.setSelectedItem(newValue);
        }

        @Override
        public void loadValueFromXML(Element xmlElement) {
                NodeList items = xmlElement.getElementsByTagName("module");

                for (int i = 0; i < items.getLength(); i++) {
                        Element moduleElement = (Element) items.item(i);
                        String name = moduleElement.getAttribute("name");
                        for (int j = 0; j < modules.length; j++) {
                                if (modules[j].toString().equals(name)) {
                                        ParameterSet moduleParameters = modules[j].getParameterSet();
                                        if (moduleParameters == null) {
                                                continue;
                                        }
                                        moduleParameters.loadValuesFromXML((Element) items.item(i));
                                }
                        }
                }
                String selectedAttr = xmlElement.getAttribute("selected");
                for (int j = 0; j < modules.length; j++) {
                        if (modules[j].toString().equals(selectedAttr)) {
                                value = modules[j];
                        }
                }
        }

        @Override
        public void saveValueToXML(Element xmlElement) {
                if (value != null) {
                        xmlElement.setAttribute("selected", value.toString());
                }
                Document parentDocument = xmlElement.getOwnerDocument();
                for (ModuleType item : modules) {
                        Element newElement = parentDocument.createElement("module");
                        newElement.setAttribute("name", item.toString());
                        ParameterSet moduleParameters = item.getParameterSet();
                        if (moduleParameters != null) {
                                moduleParameters.saveValuesToXML(newElement);
                        }
                        xmlElement.appendChild(newElement);
                }
        }

        @Override
        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
                        errorMessages.add(name + " is not set properly");
                        return false;
                }
                ParameterSet moduleParameters = value.getParameterSet();
                return moduleParameters.checkUserParameterValues(errorMessages);
        }
}
