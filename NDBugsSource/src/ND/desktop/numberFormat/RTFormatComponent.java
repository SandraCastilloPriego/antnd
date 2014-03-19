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

package ND.desktop.numberFormat;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 */
public class RTFormatComponent extends JPanel {

	private JComboBox formatTypeCombo;
	private JTextField formatField;

	public RTFormatComponent() {

		super(new BorderLayout());

		formatTypeCombo = new JComboBox(RTFormatterType.values());
		add(formatTypeCombo, BorderLayout.WEST);

		formatField = new JTextField();
		formatField.setColumns(8);
		add(formatField, BorderLayout.CENTER);

	}

	public void setValue(RTFormatter format) {
		formatTypeCombo.setSelectedItem(format.getType());
		formatField.setText(format.getPattern());
	}

	public RTFormatterType getType() {
		if ((formatTypeCombo == null)
				|| (formatTypeCombo.getSelectedItem() == null))
			return null;
		return (RTFormatterType) formatTypeCombo.getSelectedItem();
	}

	public String getPattern() {
		return formatField.getText();
	}

}
