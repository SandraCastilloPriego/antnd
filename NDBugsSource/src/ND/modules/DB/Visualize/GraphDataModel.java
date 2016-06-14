/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.DB.Visualize;

import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author scsandra
 */
public class GraphDataModel extends AbstractTableModel implements TableModel {

    private final List<MiniGraph> graphs;
    private final List<String[]> values;
    private final String[] columns;

    public GraphDataModel(List<MiniGraph> graphs, String[] columns, List<String[]> values) {
        this.graphs = graphs;
        this.columns = columns;
        this.values = values;
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public int getColumnCount() {
        return this.columns.length;
    }

    @Override
    public Object getValueAt(int i, int j) {
        String[] value = this.values.get(i);
        return value[j];
    }

    @Override
    public String getColumnName(int i) {
        return this.columns[i];
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    @Override
    public void setValueAt(Object o, int i, int i1) {
    }

    public MiniGraph getGraph(int i) {
        return this.graphs.get(i);
    }

}
