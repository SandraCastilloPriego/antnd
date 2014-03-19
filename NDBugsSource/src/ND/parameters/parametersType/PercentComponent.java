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

package ND.parameters.parametersType;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 */
public class PercentComponent extends JPanel {

	private JFormattedTextField percentField;

	public PercentComponent() {

		percentField = new JFormattedTextField(NumberFormat.getNumberInstance());
		percentField.setColumns(4);
		add(percentField);

		add(new JLabel("%"));

	}

	public void setValue(double value) {
		percentField.setValue(value * 100);
	}

	public Double getValue() {
		Number value = (Number) percentField.getValue();
		if (value == null)
			return null;
		return value.doubleValue() / 100;
	}

}
