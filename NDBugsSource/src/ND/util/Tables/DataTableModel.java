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
package ND.util.Tables;

import ND.data.DatasetType;
import java.awt.Color;
import javax.swing.table.TableModel;

/**
 * interface of a model
 *
 * @author scsandra
 */
public interface DataTableModel extends TableModel {

    /**
     * Removes selected rows from the table. It only removes the rows which are
     * selected in the "Selection" column.
     *
     */
    public void removeRows();

    /**
     * Returns the type of data set which correspons to this data model
     *
     * @return Data set type
     */
    public DatasetType getType();

    /**
     * Adds a new column in the table.
     *
     * @param columnName Name of the new column
     */
    public void addColumn(String columnName);

    public void addRowColor(Color[] color);

    public Color getRowColor(int row);

    public Color getCellColor(int row, int column);

    public boolean isExchange(int row);
}
