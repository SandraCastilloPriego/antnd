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
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.util.Tables.DataTableModel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;

public class ReactionsDataModel extends AbstractTableModel implements DataTableModel {

    private Dataset dataset;
    private List<ColumnName> columns;
    private Color[] rowColor;

    public ReactionsDataModel(Dataset dataset) {
        this.dataset = (SimpleBasicDataset) dataset;
        rowColor = new Color[dataset.getDocument().getModel().getNumReactions()];
        columns = new ArrayList<>();
        columns.addAll(Arrays.asList(ColumnName.values()));
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
     * @see guineu.util.Tables.DataTableModel
     */
    public void removeRows() {
        Model model = dataset.getDocument().getModel();
        List<Reaction> toBeRemoved = new ArrayList();
        for (Reaction reaction : model.getListOfReactions()) {
            if (dataset.isReactionSelected(reaction)) {
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
        return 10;
    }

    public int getRowCount() {
        return this.dataset.getDocument().getModel().getNumReactions();
    }

    public Object getValueAt(final int row, final int column) {
        try {
            Reaction r = this.dataset.getDocument().getModel().getReaction(row);

            String value = columns.get(column).getColumnName();
            switch (value) {
                case "Number":
                    return row + 1;
                case "Id":
                    return r.getId();
                case "Name":
                    return r.getName();
                case "Reaction":
                    return getReaction(r);
                case "Reaction extended":
                    return getReactionExt(r);
                case "Lower bound":
                    LocalParameter lp = r.getKineticLaw().getLocalParameter("LOWER_BOUND");
                    if (lp == null) {
                        lp = r.getKineticLaw().getLocalParameter("LB_" + r.getId());
                    }
                    return lp.getValue();
                case "Upper bound":
                    lp = r.getKineticLaw().getLocalParameter("UPPER_BOUND");
                    if (lp == null) {
                        lp = r.getKineticLaw().getLocalParameter("UB_" + r.getId());
                    }
                    return lp.getValue();
                case "Notes":
                    String notes = r.getNotesString();
                    return notes;
                case "Objective":
                    lp = r.getKineticLaw().getLocalParameter("OBJECTIVE_COEFFICIENT");
                    if (lp == null) {
                        lp = r.getKineticLaw().getLocalParameter("OBJ_" + r.getId());
                    }
                    return lp.getValue();
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
            String info = "";
            Reaction r = this.dataset.getDocument().getModel().getReaction(row);

            String value = columns.get(column).getColumnName();
            switch (value) {
                case "Number":
                    return;
                case "Id":
                    if (aValue == null || aValue.toString().isEmpty() || aValue.equals("NA")) {
                        info = info + "\n- The reaction " + r.getId() + " - " + r.getName() + " has been removed : \n" + getReaction(r) + "\n" + getReactionExt(r) + "\n------------------";
                        dataset.addInfo(info);

                        this.dataset.getDocument().getModel().removeReaction(r);
                        Graph g = this.dataset.getGraph();
                        Node n = g.getNode(r.getId());
                        if (n != null) {
                            g.removeNode(n.getId());
                        }

                    } else {
                        info = info + "\n- Id of the reaction " + r.getId() + " - " + r.getName() + " has changed to " + aValue.toString();
                        dataset.addInfo(info);
                        r.setId(aValue.toString());
                    }
                    return;
                case "Name":
                    r.setName(aValue.toString());
                    info = info + "\n- Name of the reaction " + r.getId() + " - " + r.getName() + " has changed to " + aValue.toString();
                    dataset.addInfo(info);
                    return;
                case "Reaction":
                    changeReaction(r, aValue.toString());
                    return;
                case "Reaction extended":
                    changeReaction(r, aValue.toString());
                    dataset.addInfo(info);
                    return;
                case "Lower bound":
                    LocalParameter parameter = r.getKineticLaw().getLocalParameter("LOWER_BOUND");
                    if (parameter == null) {
                        parameter = r.getKineticLaw().getLocalParameter("LB_" + r.getId());
                    }
                    info = info + "\n- Lower bound of the reaction " + r.getId() + " - " + r.getName() + " has changed from " + parameter.getValue() + " to " + aValue.toString();
                    dataset.addInfo(info);

                    parameter.setValue(Double.valueOf(aValue.toString()));
                    return;
                case "Upper bound":
                    parameter = r.getKineticLaw().getLocalParameter("UPPER_BOUND");
                    if (parameter == null) {
                        parameter = r.getKineticLaw().getLocalParameter("UB_" + r.getId());
                    }
                    info = info + "\n- Upper bound of the reaction " + r.getId() + " - " + r.getName() + " has changed from " + parameter.getValue() + " to " + aValue.toString();
                    dataset.addInfo(info);

                    parameter.setValue(Double.valueOf(aValue.toString()));
                    return;
                case "Notes":
                    info = info + "\n- Notes of the reaction " + r.getId() + " - " + r.getName() + " have changed from " + r.getNotes().toXMLString() + " to " + aValue.toString();
                    dataset.addInfo(info);
                    r.setNotes(value);
                    return;
                case "Objective":
                    parameter = r.getKineticLaw().getLocalParameter("OBJECTIVE_COEFFICIENT");
                    if (parameter == null) {
                        parameter = r.getKineticLaw().getLocalParameter("OBJ_" + r.getId());
                    }
                    if (parameter == null) {
                        parameter = r.getKineticLaw().createLocalParameter("OBJECTIVE_COEFFICIENT");
                    }

                    info = info + "\n- Objective coefficient of the reaction " + r.getId() + " - " + r.getName() + " has changed from " + parameter.getValue() + " to " + aValue.toString();
                    dataset.addInfo(info);

                    parameter.setValue(Double.valueOf(aValue.toString()));

                    return;
                case "Fluxes":
                    parameter = r.getKineticLaw().getLocalParameter("FLUX_VALUE");
                    if (parameter == null) {
                        parameter = r.getKineticLaw().createLocalParameter("FLUX_VALUE");
                    }

                    info = info + "\n- Flux of the reaction " + r.getId() + " - " + r.getName() + " has changed from " + parameter.getValue() + " to " + aValue.toString();
                    dataset.addInfo(info);

                    parameter.setValue(Double.valueOf(aValue.toString()));

                    return;
            }

            fireTableCellUpdated(row, column);
        } catch (Exception e) {

        }

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
    public void addColumn(String columnName) {

    }

    @Override
    public Color getCellColor(int row, int column) {
        return null;
    }

    private Object getReaction(Reaction r) {
        String reaction = "";
        for (SpeciesReference reactants : r.getListOfReactants()) {
            reaction += reactants.getStoichiometry() + " " + reactants.getSpecies() + " + ";
        }
        reaction = reaction.substring(0, reaction.lastIndexOf(" + "));
        reaction += " <=> ";
        if (!r.getListOfProducts().isEmpty()) {
            for (SpeciesReference product : r.getListOfProducts()) {
                reaction += product.getStoichiometry() + " " + product.getSpecies() + " + ";
            }
            reaction = reaction.substring(0, reaction.lastIndexOf(" + "));
        }
        return reaction;
    }

    private Object getReactionExt(Reaction r) {
        String reaction = "";
        for (SpeciesReference reactants : r.getListOfReactants()) {
            reaction += reactants.getStoichiometry() + " " + reactants.getSpeciesInstance().getName() + " + ";
        }
        reaction = reaction.substring(0, reaction.lastIndexOf(" + "));
        reaction += " <=> ";
        if (!r.getListOfProducts().isEmpty()) {
            for (SpeciesReference product : r.getListOfProducts()) {
                reaction += product.getStoichiometry() + " " + product.getSpeciesInstance().getName() + " + ";
            }
            reaction = reaction.substring(0, reaction.lastIndexOf(" + "));
        }
        return reaction;
    }

    private void changeReaction(Reaction r, String value) {
        /*if (value == null) {
         this.dataset.getDocument().getModel().removeReaction(r);
         } else {
         //1.0 s_3713 <=> 1.0 s_1524
         try {
         String[] sides = value.split(" <=> ");
         String[] reactants = sides[0].split(" + ");
         String[] products = sides[1].split(" + ");
         r.getListOfReactants()
         } catch (Exception e) {
         }

         }*/

    }

    @Override
    public boolean isExchange(int row) {
        Reaction r = this.dataset.getDocument().getModel().getReaction(row);
        if (r.getName().contains("exchange")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isTransport(int row) {
        Reaction r = this.dataset.getDocument().getModel().getReaction(row);
        if (r.getName().contains("port")) {
            return true;
        }
        return false;
    }
}
