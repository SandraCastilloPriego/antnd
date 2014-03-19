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

import ND.util.Range;
import ND.util.components.GridBagPanel;
import java.awt.GridBagConstraints;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;



/**
 */
public class RangeComponent extends GridBagPanel {

	private JFormattedTextField minTxtField, maxTxtField;

	public RangeComponent(NumberFormat format) {

		minTxtField = new JFormattedTextField(format);
		minTxtField.setColumns(8);
		
		maxTxtField = new JFormattedTextField(format);
		maxTxtField.setColumns(8);
		
		add(minTxtField, 0, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
		add(new JLabel(" - "), 1, 0, 1, 1, 0, 0);
		add(maxTxtField, 2, 0, 1, 1, 1, 0, GridBagConstraints.HORIZONTAL);
	}

	public Range getValue() {
		Number minValue = (Number) minTxtField.getValue();
		Number maxValue = (Number) maxTxtField.getValue();
		if ((minValue == null) || (maxValue == null))
			return null;
		return new Range(minValue.doubleValue(), maxValue.doubleValue());
	}
	
	public void setNumberFormat(NumberFormat format) {
		DefaultFormatterFactory fact = new DefaultFormatterFactory(
				new NumberFormatter(format));
		minTxtField.setFormatterFactory(fact);
		maxTxtField.setFormatterFactory(fact);
	}

	public void setValue(Range value) {
		minTxtField.setValue(value.getMin());
		maxTxtField.setValue(value.getMax());
	}

}
