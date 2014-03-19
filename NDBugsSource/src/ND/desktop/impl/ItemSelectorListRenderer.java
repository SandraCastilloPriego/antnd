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
package ND.desktop.impl;

import java.awt.Component;
import java.awt.Dimension;


import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

class ItemSelectorListRenderer extends DefaultListCellRenderer {

        /**
         * @author Taken from MZmine2
         * http://mzmine.sourceforge.net/
         *
         * Main rendering method
         */
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean hasFocus) {

                // First get original rendered component
                final Component component = super.getListCellRendererComponent(list,
                        value, index, isSelected, hasFocus);

                // This is necessary, although it doesn't make much sense.
                // If we don't set the preferred size here, the JList occasionally
                // becomes blank when the ListModel is updated
                component.setPreferredSize(new Dimension(100, 15));
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                if (value.toString().contains("LC-MS")) {
                        label.setIcon(new ImageIcon("icons/lcms.png"));
                } else if (value.toString().contains("Text File")) {
                        label.setIcon(new ImageIcon("icons/others.png"));
                } else if (value.toString().contains("GCxGC-MS File")) {
                        label.setIcon(new ImageIcon("icons/gcgcfiles.png"));
                } else if (value.toString().contains("GCxGC")) {
                        label.setIcon(new ImageIcon("icons/gcgc.png"));
                }else{
                         label.setIcon(new ImageIcon("icons/openfile.png"));
                }

                return component;
        }
}
