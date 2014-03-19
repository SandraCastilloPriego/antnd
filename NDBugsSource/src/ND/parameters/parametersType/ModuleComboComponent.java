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

import ND.modules.NDModule;
import ND.parameters.ParameterSet;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 */
public class ModuleComboComponent extends JPanel implements ActionListener {

	private JComboBox comboBox;
	private JButton setButton;

	public ModuleComboComponent(NDModule modules[]) {

		super(new BorderLayout());

		assert modules != null;
		assert modules.length > 0;

		comboBox = new JComboBox(modules);
		comboBox.addActionListener(this);
		add(comboBox, BorderLayout.CENTER);

		setButton = new JButton("...");
		setButton.addActionListener(this);
		boolean buttonEnabled = (modules[0].getParameterSet() != null);
		setButton.setEnabled(buttonEnabled);
		add(setButton, BorderLayout.EAST);

	}

	public int getSelectedIndex() {
		return comboBox.getSelectedIndex();
	}

	public void setSelectedItem(Object selected) {
		comboBox.setSelectedItem(selected);
	}

	public void actionPerformed(ActionEvent event) {

		Object src = event.getSource();

		if (src == comboBox) {
			NDModule selected = (NDModule) comboBox.getSelectedItem();
			if (selected == null) {
				setButton.setEnabled(false);
				return;
			}
			ParameterSet parameterSet = selected.getParameterSet();
			setButton.setEnabled(parameterSet != null);
		}

		if (src == setButton) {
			NDModule selected = (NDModule) comboBox.getSelectedItem();
			if (selected == null)
				return;
			ParameterSet parameterSet = selected.getParameterSet();
			if (parameterSet == null)
				return;
			parameterSet.showSetupDialog();
		}

	}

}
