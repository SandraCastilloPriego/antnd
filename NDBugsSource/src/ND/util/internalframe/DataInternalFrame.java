/*
 * Copyright 2007-2012 
 * This file is part of AntND.
 *
 * AntND is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * AntND is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * AntND; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.util.internalframe;

import java.awt.Dimension;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

/**
 * Internal frame which will contain a table with the data set.
 *
 * @author scsandra
 */
public class DataInternalFrame extends JInternalFrame {

    JTable table, metTable;
    JTabbedPane jtp;

    public DataInternalFrame(String name, JTable table, JTable metTable, Dimension size) {
        super(name, true, true, true, true);
        jtp = new JTabbedPane();
        this.table = table;
        this.metTable = metTable;
        setSize(size);
        setTable(table);
        setMetTable(metTable);
        this.add(jtp);
    }

    public JTable getTable() {
        return table;
    }

    public JTable getMetTable() {
        return metTable;
    }

    public void setTable(JTable table) {
        try {
            JScrollPane scrollPanel = new JScrollPane(table);
            scrollPanel.setPreferredSize(new Dimension(this.getWidth() - 330, this.getHeight() - 90));
            jtp.addTab("Reactions", scrollPanel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMetTable(JTable metTable) {
        try {
            JScrollPane scrollPanel = new JScrollPane(metTable);
            scrollPanel.setPreferredSize(new Dimension(this.getWidth() - 330, this.getHeight() - 90));
            jtp.addTab("Metabolites", scrollPanel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
