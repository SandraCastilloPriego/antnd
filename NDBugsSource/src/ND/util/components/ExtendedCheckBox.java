/*
 * Copyright 2013-2014 VTT Biotechnology
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

package ND.util.components;

import java.awt.Font;

import javax.swing.JCheckBox;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 * 
 * Checkbox wrapper class
 */
public class ExtendedCheckBox<Type> extends JCheckBox {

        static final Font checkBoxFont = new Font("SansSerif", Font.PLAIN, 11);
        private Type object;

        public ExtendedCheckBox(Type object) {
                this(object, false);
        }

        public ExtendedCheckBox(Type object, boolean selected) {
                super(object.toString(), selected);
                this.object = object;
                setOpaque(false);
                setFont(checkBoxFont);
        }

        /**
         * @return Returns the dataFile.
         */
        public Type getObject() {
                return object;
        }

        public int getPreferredWidth() {
                return ((int) getPreferredSize().getWidth()) + 30;
        }
}
