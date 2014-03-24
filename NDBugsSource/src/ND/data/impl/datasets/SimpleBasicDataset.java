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
import org.sbml.jsbml.SBMLDocument;

/**
 * Basic data set implementation.
 *
 * @author SCSANDRA
 */
public class SimpleBasicDataset implements Dataset {
        
        String datasetName;
        protected DatasetType type;
        String infoDataset = "";
        private int ID;
        private SBMLDocument document;

        /**
         *
         * @param datasetName Name of the data set
         */
        public SimpleBasicDataset(String datasetName) {
                this.datasetName = datasetName;
                type = DatasetType.MODELS;
        }
        
        public SimpleBasicDataset() {                
                type = DatasetType.MODELS;
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
        public String getDatasetName() {
                return this.datasetName;
        }
        
        @Override
        public void setDatasetName(String datasetName) {
                this.datasetName = datasetName;
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
                newDataset.setType(this.type);
                newDataset.setDocument(this.getDocument());
                return newDataset;
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
