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
import java.io.File;
import java.util.Collection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author bicha
 */
public class FileNamesParameter implements UserParameter<File[], FileNameComponent> {

        private File value[];
        private String name, description;
        private String extension;

        public FileNamesParameter(String name, String description) {
                this(name, description, null);
        }

        public FileNamesParameter(String name, String description, String extension) {
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
        public FileNameComponent createEditingComponent() {
                return new FileNameComponent(true);
        }

        @Override
        public FileNamesParameter clone() {
                FileNamesParameter copy = new FileNamesParameter(name, description);
                copy.setValue(this.getValue());
                return copy;
        }

        @Override
        public void loadValueFromXML(Element xmlElement) {
                NodeList list = xmlElement.getElementsByTagName("file");
                File newFiles[] = new File[list.getLength()];
                for (int i = 0; i < list.getLength(); i++) {
                        Element nextElement = (Element) list.item(i);
                        newFiles[i] = new File(nextElement.getTextContent());
                }
                this.value = newFiles;
        }

        @Override
        public void saveValueToXML(Element xmlElement) {
                if (value == null) {
                        return;
                }
                Document parentDocument = xmlElement.getOwnerDocument();
                for (File f : value) {
                        Element newElement = parentDocument.createElement("file");
                        newElement.setTextContent(f.getPath());
                        xmlElement.appendChild(newElement);
                }
        }

        public File[] getValue() {
                return value;
        }

        public void setValue(File[] newValue) {
                this.value = newValue;
        }

        public void setValueToComponent(FileNameComponent component, File[] newValue) {
                component.setValues(newValue);
        }

        public void setValueFromComponent(FileNameComponent component) {
                this.value = component.getValues();
        }

        public boolean checkValue(Collection<String> errorMessages) {
                if (value == null) {
			errorMessages.add(name + " is not set");
			return false;
		}
		return true;
        }
}
