/*
 * Copyright 2007-2013 VTT Biotechnology
 * This file is part of Guineu.
 *
 * Guineu is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * Guineu is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Guineu; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.data.models;

import ND.data.ColumnName;
import ND.data.Dataset;
import ND.data.DatasetType;
import ND.data.ParameterType;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.util.Tables.DataTableModel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;

public class DatasetDataModel extends AbstractTableModel implements DataTableModel {

    private Dataset dataset;
    private int fixNumberColumns = 9;
    private List<ColumnName> columns;
    private Color[] rowColor;

    public DatasetDataModel(Dataset dataset) {
        this.dataset = (SimpleBasicDataset) dataset;
        rowColor = new Color[dataset.getDocument().getModel().getNumReactions()];
        this.setParameters();
        this.writeData();
    }

    public Color getRowColor(int row) {
        if (row < rowColor.length) {
            return rowColor[row];
        } else {
            return null;
        }
    }

    public void addRowColor(Color[] color) {
        this.rowColor = color;
    }

    /**
     * @see guineu.data.LCMSColumnName The function isColumnShown() in the enum
     * class says whether each column has to be shown in the table or not.
     *
     */
    public void setParameters() {
        this.columns = new ArrayList<ColumnName>();
        fixNumberColumns = 0;

        for (ColumnName column : ColumnName.values()) {
            columns.add(column);
            fixNumberColumns++;
        }

    }

    /**
     * Sets the rest of the data from the dataset object, which contains the all
     * the rows.
     *
     */
    private void writeData() {
        /*PeakListRow peakListRow;
         for (int i = 0; i < dataset.getNumberRows(); i++) {
         peakListRow = this.dataset.getRow(i);
         if (peakListRow.getID() == -1) {
         peakListRow.setID(i);
         }
         }*/

    }

    /**
     * @see guineu.util.Tables.DataTableModel
     */
    public void removeRows() {
        Model model = dataset.getDocument().getModel();
        List<Reaction> toBeRemoved = new ArrayList();
        for (Reaction reaction : model.getListOfReactions()) {
            if (dataset.isSelected(reaction)) {
                toBeRemoved.add(reaction);
                this.fireTableDataChanged();
                this.removeRows();
                break;
            }
        }
        for (Reaction reaction : toBeRemoved) {
            model.removeReaction(reaction);
        }
    }

    public int getColumnCount() {
        return this.fixNumberColumns;
    }

    public int getRowCount() {
        return this.dataset.getDocument().getModel().getNumReactions();
    }

    public Object getValueAt(final int row, final int column) {
        try {
            Reaction r = this.dataset.getDocument().getModel().getReaction(row);

            String value = columns.get(column).getColumnName();
            switch (value) {
                case "Selection":
                    return this.dataset.isSelected(r);
                case "Id":
                    return r.getId();
                case "Name":
                    return r.getName();
                case "Reaction":
                    return getReaction(r);
                case "Reaction extended":
                    return getReactionExt(r);
                case "Lower bound":
                    return r.getKineticLaw().getLocalParameter("LOWER_BOUND").getValue();
                case "Upper bound":
                    return r.getKineticLaw().getLocalParameter("UPPER_BOUND").getValue();
                case "Gene rules":
                    return r.getNotes().toXMLString();
                case "Objective":
                    return r.getKineticLaw().getLocalParameter("OBJECTIVE_COEFFICIENT").getValue();
                case "Fluxes":
                    return r.getKineticLaw().getLocalParameter("FLUX_VALUE").getValue();
            }
            return value;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        return (String) this.columns.get(columnIndex).toString();
    }

    @Override
    public Class<?> getColumnClass(int c) {
        if (getValueAt(0, c) != null) {
            return getValueAt(0, c).getClass();
        } else {
            return Object.class;
        }
    }

    @Override
    @SuppressWarnings("fallthrough")
    public void setValueAt(Object aValue, int row, int column) {
        try {
            Reaction r = this.dataset.getDocument().getModel().getReaction(row);

            String value = columns.get(column).getColumnName();
            switch (value) {
                case "Selection":
                    this.dataset.setSelectionMode(r.getId());
                case "Id":
                    r.setId(aValue.toString());
                case "Name":
                    r.setName(aValue.toString());
                case "Reaction":

                case "Reaction extended":

                case "Lower bound":
                    r.getKineticLaw().getLocalParameter("LOWER_BOUND").setValue(Double.valueOf(aValue.toString()));
                case "Upper bound":
                    r.getKineticLaw().getLocalParameter("UPPER_BOUND").setValue(Double.valueOf(aValue.toString()));
                case "Gene rules":

                case "Objective":
                    r.getKineticLaw().getLocalParameter("OBJECTIVE_COEFFICIENT").setValue(Double.valueOf(aValue.toString()));
                case "Fluxes":
                    r.getKineticLaw().getLocalParameter("FLUX_VALUE").setValue(Double.valueOf(aValue.toString()));
            }

            fireTableCellUpdated(row, column);
        } catch (Exception e) {

        }

        /* SimplePeakListRowLCMS peakRow = (SimplePeakListRowLCMS) this.dataset.getRow(row);
         if (column < this.fixNumberColumns) {
         if (columns.get(column) == LCMSColumnName.IDENTIFICATION) {
         if (aValue.toString().contains("NA")) {
         peakRow.setVar(this.columns.get(column).getSetFunctionName(), IdentificationType.UNKNOWN.toString());
         } else {
         peakRow.setVar(this.columns.get(column).getSetFunctionName(), aValue);
         }
         } else if (columns.get(column) == LCMSColumnName.STANDARD) {
         if ((Boolean) aValue == false) {
         peakRow.setVar(this.columns.get(column).getSetFunctionName(), 0);
         }
         if ((Boolean) aValue == true) {
         peakRow.setVar(this.columns.get(column).getSetFunctionName(), 1);
         }
         } else {
         peakRow.setVar(this.columns.get(column).getSetFunctionName(), aValue);
         }
         } else {
         peakRow.setPeak(this.dataset.getAllColumnNames().get(column - this.fixNumberColumns), (Double) aValue);
         }
         fireTableCellUpdated(row, column);*/
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    /**
     * @see guineu.util.Tables.DataTableModel
     */
    public DatasetType getType() {
        return this.dataset.getType();
    }

    /**
     * @see guineu.util.Tables.DataTableModel
     */
    public int getFixColumns() {
        return this.fixNumberColumns;
    }

    /**
     * @see guineu.util.Tables.DataTableModel
     */
    public void addColumn(String columnName) {

    }

    @Override
    public Color getCellColor(int row, int column) {
        return Color.white;
    }

    private Object getReaction(Reaction r) {
        String reaction = "";
        for (SpeciesReference reactants : r.getListOfReactants()) {
            reaction += reactants.getStoichiometry() + " " + reactants.getSpecies() + " + ";
        }
        reaction = reaction.substring(0, reaction.lastIndexOf(" + "));
        reaction += " <=> ";
        for (SpeciesReference product : r.getListOfProducts()) {
            reaction += product.getStoichiometry() + " " + product.getSpecies() + " + ";
        }
        reaction = reaction.substring(0, reaction.lastIndexOf(" + "));
        return reaction;
    }

    private Object getReactionExt(Reaction r) {
        String reaction = "";
        for (SpeciesReference reactants : r.getListOfReactants()) {
            reaction += reactants.getStoichiometry() + " " + reactants.getSpeciesInstance().getName() + " + ";
        }
        reaction = reaction.substring(0, reaction.lastIndexOf(" + "));
        reaction += " <=> ";
        for (SpeciesReference product : r.getListOfProducts()) {
            reaction += product.getStoichiometry() + " " + product.getSpeciesInstance().getName() + " + ";
        }
        reaction = reaction.substring(0, reaction.lastIndexOf(" + "));
        return reaction;
    }
}
