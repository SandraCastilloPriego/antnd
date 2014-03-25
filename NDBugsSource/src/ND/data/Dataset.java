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
         * Every dataset has a name to allow the user to identify it Returns the
         * name of the data set.
         *
         * @return Name of the data set
         */
        public String getDatasetName();
        
        /**
         * Every dataset has a path to allow the user to identify it Returns the
         * name of the data set.
         *
         * @return path of the data set
         */
        public String getPath();
        
        public void setPath(String path);

        /**
         * Sets the name of the dataset.
         *
         * @param Name of the dataset
         */
        public void setDatasetName(String name);

        /**
         * The type of the data set can be LC-MS, GCxGC-Tof or others.
         *
         * @see ND.data.DatasetType
         *
         * @return DatasetType class
         */
        public DatasetType getType();

        /**
         * Sets the type of the data set. It can be LC-MS, GCxGC-Tof or others.
         *
         * @see ND.data.DatasetType
         *
         * @param type DatasetType
         */
        public void setType(DatasetType type);

        /**
         * Returns general information about the data set. It will be written by
         * the user.
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
}
