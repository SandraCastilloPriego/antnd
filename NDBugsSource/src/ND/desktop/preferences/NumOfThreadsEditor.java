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

package ND.desktop.preferences;

import java.awt.BorderLayout;
import java.text.NumberFormat;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

/**
 */
public class NumOfThreadsEditor extends JPanel {

	private static final String options[] = {
			"Set to the number of CPU cores ("
					+ Runtime.getRuntime().availableProcessors() + ")",
			"Set manually" };

	private JComboBox optionCombo;
	private JFormattedTextField numField;

	public NumOfThreadsEditor() {

		super(new BorderLayout());

		optionCombo = new JComboBox(options);
		add(optionCombo, BorderLayout.WEST);

		numField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		numField.setColumns(3);
		add(numField, BorderLayout.CENTER);

	}

	public void setValue(boolean automatic, int numOfThreads) {
		if (automatic) {
			optionCombo.setSelectedIndex(0);
		} else {
			optionCombo.setSelectedIndex(1);
		}
		numField.setValue(numOfThreads);
		numField.setEnabled(!automatic);
	}

	public boolean isAutomatic() {
		int index = optionCombo.getSelectedIndex();
		return index <= 0;
	}

	public Number getNumOfThreads() {
		return (Number) numField.getValue();
	}

}
