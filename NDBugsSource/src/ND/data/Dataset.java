/*
 * Copyright 2007-2012 
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
package ND.data;

import java.awt.Color;
import java.util.List;
import org.sbml.jsbml.SBMLDocument;

/**
 * Interface for data set
 *
 * @author scsandra
 */
public interface Dataset {
        
        public SBMLDocument getDocument();
        
        public void setDocument(SBMLDocument document);

        /**
         * Constructs an exact copy of it self and returns it.
         *
         * @return Exact copy of itself
         */
        public Dataset clone();

        /**
         * Sets data set ID
         *
         */
        public void setID(int ID);

        /**
         * Retruns data set ID
         *
         * @return data set ID
         */
        public int getID();

        /**
         * Every dataset has a name to allow the user to identify it
         * Returns the name of the data set.
         *
         * @return Name of the data set
         */
        public String getDatasetName();

        /**
         * Returns the name of each sample or column into the data set depending on what
         * kind of data set is it. In the case of LC-MS data they will be only the name
         * of the samples or experiments, but in the case of "Other dataset" it will
         * correspond to all the columns into the dataset.
         *
         * @return Column's name
         */
        public List<String> getAllColumnNames();

        /**
         * Returns number of columns. It doesn't take into account the columns corresponding
         * to a parameter of the data such as "m/z" or "retention time".
         *
         * @return Number of columns
         */
        public int getNumberCols();

        /**
         * Returns the number of rows in the data set.
         *
         * @return Number of rows
         */
        public int getNumberRows();

        /**
         * Sets the number or rows in the data set.
         *
         * @param numberRows Number of rows
         */
        public void setNumberRows(int numberRows);

        /**
         * Sets the name of the dataset.
         *
         * @param Name of the dataset
         */
        public void setDatasetName(String name);

        /**
         * The type of the data set can be LC-MS, GCxGC-Tof or others.
         * @see ND.data.DatasetType
         *
         * @return DatasetType class
         */
        public DatasetType getType();

        /**
         * Sets the type of the data set. It can be LC-MS, GCxGC-Tof or others.
         * @see ND.data.DatasetType
         *
         * @param type DatasetType
         */
        public void setType(DatasetType type);

        /**
         * Returns the row of the data set indicated by the user with its index into the
         * list of rows.
         *
         * @param row Row index
         * @return Row
         */
        public Row getRow(int row);

        /**
         * Removes the row of the data set indicated by the user with its index into the
         * list of rows.
         *
         * @param row Row index
         */
        public void removeRow(Row row);

        /**
         * Adds a new name to the list of columns names.
         *
         * @param columnName New column name
         */
        public void addColumnName(String columnName);

        /**
         * Adds a new name to the list of columns names in the position indicated.
         *
         * @param columnName New column name
         * @param position Position of this colmun in the list
         */
        public void addColumnName(String columnName, int position);

        /**
         * Returns all the rows in the data set.
         *
         * @return List with all the rows
         */
        public List<Row> getRows();

        /**
         * Returns all the rows in the data set.
         *
         * @return List with all the rows
         */
        public List<Row> getSelectedRows();

        /**
         * Adds a new row into the data set.
         *
         * @param Row row
         */
        public void addRow(Row row);

        /**
         * Returns general information about the data set.
         * It will be written by the user.
         *
         * @return General information about the data set
         */
        public String getInfo();

        /**
         * Adds general information about the data set.
         *
         * @param info Information about the data set
         */
        public void setInfo(String info);

        /**
         * Adds a new parameter value for one column. The parameters define the
         * metadata related with the columns.
         *
         * @param columnName Column name
         * @param parameterName Parameter name
         * @param parameterValue Value of the parameter
         */
        public void addParameterValue(String columnName, String parameterName, String parameterValue);

        /**
         * Deletes the value of on parameter for all the columns.
         *
         * @param parameterName Name of the parameter
         */
        public void deleteParameter(String parameterName);

        /**
         * Returns the name of all the parameters defined.
         *
         * @return List of every parameters name
         */
        public List<String> getParametersName();

        /**
         * Returns the value of one concrete parameter in one column.
         *
         * @param columnName Column's name
         * @param parameterName Name of the parameter
         * @return String with the parameter value in the columnName
         */
        public String getParametersValue(String columnName, String parameterName);

        /**
         * Returns a list of all possible values of one parameter in the columns.
         *
         * @param parameter Parameter's name
         * @return Vector of All possible values of this parameter
         */
        public List<String> getParameterAvailableValues(String parameter);

        /**
         * Remove the sample names.
         */
        public void removeSampleNames();       

        public Color[] getRowColor();
        
        public void addRowColor(Color rowColor);

        public Color getCellColor(int row, int column);

        public void setCellColor(Color cellColor, int row, int column);
}
