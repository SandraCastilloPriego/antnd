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
package ND.data.impl.datasets;

import ND.data.Dataset;
import ND.data.DatasetType;
import ND.data.Row;
import ND.data.impl.SampleDescription;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.sbml.jsbml.SBMLDocument;

/**
 * Basic data set implementation.
 *
 * @author SCSANDRA
 */
public class SimpleBasicDataset implements Dataset {

        String datasetName;
        List<Row> peakList;
        List<String> columnNames;
        protected DatasetType type;
        String infoDataset = "";
        private HashMap<String, SampleDescription> parameters;
        private List<String> parameterNames;
        private int ID;
        private int numberRows = 0;
        private List<Color> rowColor;
        private SBMLDocument document;

        /**
         *
         * @param datasetName Name of the data set
         */
        public SimpleBasicDataset(String datasetName) {
                this.datasetName = datasetName;
                this.peakList = new ArrayList<>();
                this.columnNames = new ArrayList<>();
                this.parameters = new HashMap<>();
                this.parameterNames = new ArrayList<>();
                this.rowColor = new ArrayList<>();
                type = DatasetType.MODELS;
        }

        /**
         *
         * @param datasetName Name of the data set
         */
        public SimpleBasicDataset() {
                this.peakList = new ArrayList<>();
                this.columnNames = new ArrayList<>();
                this.parameters = new HashMap<>();
                this.parameterNames = new ArrayList<>();
                this.rowColor = new ArrayList<>();
                type = DatasetType.MODELS;
        }

        /**
         * Returns true when the list of column names contain the parameter
         * <b>columnName</b>.
         *
         * @param columnName Name of the column to be searched
         * @return true or false depending if the parameter <b>columnName</b> is
         * in the name of any column
         */
        public boolean containtName(String columnName) {
                for (String name : this.columnNames) {
                        if (name.compareTo(columnName) == 0) {
                                return true;
                        }
                        if (name.matches(".*" + columnName + ".*")) {
                                return true;
                        }
                        if (columnName.matches(".*" + name + ".*")) {
                                return true;
                        }
                }
                return false;
        }

        /**
         * Returns true when any row of the data set contains the String
         * <b>str</b> into a column called "Name"
         *
         * @param str
         * @return true or false depending if the parameter <b>str</b> is in the
         * column called "Name"
         */
        public boolean containRowName(String srt) {
                for (Row row : this.getRows()) {
                        if (((String) row.getPeak("Name")).contains(srt) || srt.contains((CharSequence) row.getPeak("Name"))) {
                                return true;
                        }
                }
                return false;
        }

        @Override
        public void setID(int ID) {
                this.ID = ID;
        }

        @Override
        public int getID() {
                return ID;
        }

        @Override
        public void addParameterValue(String experimentName, String parameterName, String parameterValue) {
                if (parameters.containsKey(experimentName)) {
                        SampleDescription p = parameters.get(experimentName);
                        p.addParameter(parameterName, parameterValue);
                } else {
                        SampleDescription p = new SampleDescription();
                        p.addParameter(parameterName, parameterValue);
                        parameters.put(experimentName, p);
                }
                if (!this.parameterNames.contains(parameterName)) {
                        parameterNames.add(parameterName);
                }
        }

        @Override
        public void deleteParameter(String parameterName) {
                for (String experimentName : columnNames) {
                        if (parameters.containsKey(experimentName)) {
                                SampleDescription p = parameters.get(experimentName);
                                p.deleteParameter(parameterName);
                        }
                }
                this.parameterNames.remove(parameterName);
        }

        @Override
        public String getParametersValue(String experimentName, String parameterName) {
                if (parameters.containsKey(experimentName)) {
                        SampleDescription p = parameters.get(experimentName);
                        return p.getParameter(parameterName);
                } else {
                        return null;
                }
        }

        @Override
        public List<String> getParameterAvailableValues(String parameter) {
                List<String> availableParameterValues = new ArrayList<>();
                for (String rawDataFile : this.getAllColumnNames()) {
                        String paramValue = this.getParametersValue(rawDataFile, parameter);
                        if (!availableParameterValues.contains(paramValue)) {
                                availableParameterValues.add(paramValue);
                        }
                }
                return availableParameterValues;
        }

        @Override
        public List<String> getParametersName() {
                return parameterNames;
        }

        @Override
        public String getDatasetName() {
                return this.datasetName;
        }

        @Override
        public void setDatasetName(String datasetName) {
                this.datasetName = datasetName;
        }

        @Override
        public void addRow(Row peakListRow) {
                this.peakList.add(peakListRow);
        }

        @Override
        public void addColumnName(String nameExperiment) {
                this.columnNames.add(nameExperiment);
        }

        @Override
        public Row getRow(int i) {
                return (Row) this.peakList.get(i);
        }

        @Override
        public List<Row> getRows() {
                return this.peakList;
        }

        @Override
        public int getNumberRows() {
                return this.peakList.size();
        }

        public int getNumberRowsdb() {
                return this.numberRows;
        }

        @Override
        public void setNumberRows(int numberRows) {
                this.numberRows = numberRows;
        }

        @Override
        public int getNumberCols() {
                return this.columnNames.size();
        }

        @Override
        public List<String> getAllColumnNames() {
                return this.columnNames;
        }

        public void setNameExperiments(List<String> experimentNames) {
                this.columnNames = experimentNames;
        }

        @Override
        public DatasetType getType() {
                return type;
        }

        @Override
        public void setType(DatasetType type) {
                this.type = type;
        }

        @Override
        public void removeRow(Row row) {
                try {
                        this.peakList.remove(row);

                } catch (Exception e) {
                        System.out.println("No row found");
                }
        }

        @Override
        public void addColumnName(String nameExperiment, int position) {
                this.columnNames.set(position, datasetName);
        }

        @Override
        public String getInfo() {
                return infoDataset;
        }

        @Override
        public void setInfo(String info) {
                this.infoDataset = info;
        }

        @Override
        public SimpleBasicDataset clone() {
                SimpleBasicDataset newDataset = new SimpleBasicDataset(this.datasetName);
                for (String experimentName : this.columnNames) {
                        newDataset.addColumnName(experimentName);
                }
                for (Row peakListRow : this.peakList) {
                        newDataset.addRow(peakListRow.clone());
                }
                newDataset.setType(this.type);
                return newDataset;
        }

        @Override
        public List<Row> getSelectedRows() {
                List<Row> selectedRows = new ArrayList<>();
                for (Row row : this.getRows()) {
                        if (row.isSelected()) {
                                selectedRows.add(row);
                        }
                }
                return selectedRows;
        }

        @Override
        public void removeSampleNames() {
                this.columnNames.clear();
        }

        @Override
        public Color[] getRowColor() {
                return this.rowColor.toArray(new Color[0]);
        }

        @Override
        public void addRowColor(Color rowColor) {
                this.rowColor.add(rowColor);
        }

        @Override
        public Color getCellColor(int row, int column) {
                return this.getRow(row).getColor(column - 2);
        }

        @Override
        public void setCellColor(Color cellColor, int row, int column) {
                this.getRow(row).setColor(cellColor, column);
        }

        @Override
        public SBMLDocument getDocument() {
                return this.document;
        }

        @Override
        public void setDocument(SBMLDocument document) {
                this.document = document;
        }
}
