/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.data.models;

import ND.data.Dataset;
import ND.data.DatasetType;
import ND.data.MetColumnName;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.util.Tables.DataTableModel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.xml.XMLNode;

/**
 *
 * @author scsandra
 */
public class MetaboliteDataModel extends AbstractTableModel implements DataTableModel {

    private Dataset dataset;
    private List<MetColumnName> columns;
    private Color[] rowColor;

    public MetaboliteDataModel(Dataset dataset) {
        this.dataset = (SimpleBasicDataset) dataset;
        rowColor = new Color[dataset.getDocument().getModel().getNumReactions()];
        columns = new ArrayList<>();
        columns.addAll(Arrays.asList(MetColumnName.values()));
    }

    @Override
    public int getRowCount() {
        return this.dataset.getDocument().getModel().getNumSpecies();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public Object getValueAt(final int row, final int column) {
        try {
            Model m = this.dataset.getDocument().getModel();
            Species sp = m.getSpecies(row);

            String value = columns.get(column).getColumnName();
            switch (value) {
                case "Number":
                    return row + 1;
                case "Id":
                    return sp.getId();
                case "Metabolite Name":
                    return sp.getName();
                case "Notes":
                    String notes = sp.getNotesString();
                    return notes;
                case "Compartment":
                    return sp.getCompartment();
               /* case "Reactions":
                    return this.getPossibleReactions(sp.getId(), m);*/
            }
            return value;

        } catch (Exception e) {
            return null;
        }
    }

    public String getPossibleReactions(String compound, Model m) {
        String reactions = null;
        Species sp = m.getSpecies(compound);
        for (Reaction r : m.getListOfReactions()) {
            if (r.hasReactant(sp) || r.hasProduct(sp)) {
                if (reactions != null) {
                    reactions = reactions + ", " + r.getId();
                } else {
                    reactions = r.getId();
                }
            }
        }
        return reactions;
    }

    @Override
    @SuppressWarnings("fallthrough")
    public void setValueAt(Object aValue, int row, int column) {
        try {
            Species r = this.dataset.getDocument().getModel().getSpecies(row);
            String info = "";

            String value = columns.get(column).getColumnName();
            switch (value) {
                case "Number":
                    return;
                case "Id":
                    if (aValue == null || aValue.toString().isEmpty() || aValue.equals("NA")) {
                        info = info + "\n- The compound " + r.getId() + " - " + r.getName() + " has been removed";
                        dataset.addInfo(info);
                        this.dataset.getDocument().getModel().removeSpecies(r);
                        Graph g = this.dataset.getGraph();
                        Node n = g.getNode(r.getId());
                        if (n != null) {
                            g.removeNode(n.getId());
                        }
                    } else {
                        info = info + "\n- Id of the compound " + r.getId() + " - " + r.getName() + " has changed to " + aValue.toString();
                        dataset.addInfo(info);
                        r.setId(aValue.toString());
                    }
                    return;
                case "Name":
                    info = info + "\n- Name of the compound " + r.getId() + " - " + r.getName() + " has changed to " + aValue.toString();
                    dataset.addInfo(info);
                    r.setName(aValue.toString());
                    return;
                case "Notes":
                    info = info + "\n- Notes of the compound " + r.getId() + " - " + r.getName() + " has changed to " + aValue.toString();
                    dataset.addInfo(info);
                    r.setNotes(XMLNode.convertStringToXMLNode(aValue.toString()));
                    return;
                case "Compartment":
                    info = info + "\n- Compartment of the compound " + r.getId() + " - " + r.getName() + " has changed to " + aValue.toString();
                    dataset.addInfo(info);
                    r.setCompartment(aValue.toString());
                    return;    
                    
              /*  case "Reactions":
                     
                    return;*/
            }

            fireTableCellUpdated(row, column);
        } catch (Exception e) {

        }

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return (String) this.columns.get(columnIndex).toString();
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

    @Override
    public Class<?> getColumnClass(int c) {
        if (getValueAt(0, c) != null) {
            return getValueAt(0, c).getClass();
        } else {
            return Object.class;
        }
    }

    @Override
    public boolean isTransport(int row) {
        return false;
    }

}
