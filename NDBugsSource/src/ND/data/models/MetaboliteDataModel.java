/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;

/**
 *
 * @author scsandra
 */
public class MetaboliteDataModel extends AbstractTableModel implements DataTableModel {

    private Dataset dataset;
    private int fixNumberColumns = 9;
    private List<String> columns;
    private Color[] rowColor;

    public MetaboliteDataModel(Dataset dataset) {
        this.dataset = (SimpleBasicDataset) dataset;
        rowColor = new Color[dataset.getDocument().getModel().getNumReactions()];
        columns = new ArrayList<>();
        columns.add("Number");
        columns.add("Id");
        columns.add("Metabolite Name");
        columns.add("Reactions");
    }

    @Override
    public int getRowCount() {
        return this.dataset.getDocument().getModel().getNumSpecies();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(final int row, final int column) {
        try {
            Species sp = this.dataset.getDocument().getModel().getSpecies(row);

            String value = columns.get(column);
            switch (value) {
                case "Number":
                    return row + 1;
                case "Id":
                    return sp.getId();
                case "Metabolite Name":
                    return sp.getName();
                case "Reactions":
                    return null;
            }
            return value;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("fallthrough")
    public void setValueAt(Object aValue, int row, int column) {
        try {
            Species r = this.dataset.getDocument().getModel().getSpecies(row);

            String value = columns.get(column);
            switch (value) {
                case "Number":
                    return;
                case "Id":
                    if (aValue == null || aValue.toString().isEmpty() || aValue.equals("NA")) {
                        this.dataset.getDocument().getModel().removeSpecies(r);
                        Graph g = this.dataset.getGraph();
                        Node n = g.getNode(r.getId());
                        if (n != null) {
                            g.removeNode(n.getId());
                        }
                    } else {
                        r.setId(aValue.toString());
                    }
                    return;
                case "Name":
                    r.setName(aValue.toString());
                    return;
                case "Reaction":

                    return;
            }

            fireTableCellUpdated(row, column);
        } catch (Exception e) {

        }

    }

    @Override
    public String getColumnName(int columnIndex) {
        return (String) this.columns.get(columnIndex);
    }

    @Override
    public void removeRows() {
        Model model = dataset.getDocument().getModel();
        List<Species> toBeRemoved = new ArrayList();
        for (Species metabolite : model.getListOfSpecies()) {
            if (dataset.isMetaboliteSelected(metabolite)) {
                toBeRemoved.add(metabolite);
                this.fireTableDataChanged();
                this.removeRows();
                break;
            }
        }
        for (Species metabolite : model.getListOfSpecies()) {
            model.removeSpecies(metabolite);
        }
    }

    @Override
    public DatasetType getType() {
        return this.dataset.getType();
    }

    @Override
    public void addColumn(String columnName) {

    }

    @Override
    public void addRowColor(Color[] color) {
        this.rowColor = color;
    }

    @Override
    public Color getRowColor(int row) {
        if (row < rowColor.length) {
            return rowColor[row];
        } else {
            return null;
        }
    }

    @Override
    public Color getCellColor(int row, int column) {
        return null;
    }

    @Override
    public boolean isExchange(int row) {
       return false;
    }

}
