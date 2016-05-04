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
package ND.util.components;

import ND.data.Dataset;
import ND.data.impl.datasets.SimpleBasicDataset;
import ND.data.models.MetaboliteDataModel;
import ND.data.models.ReactionsDataModel;
import ND.util.Tables.DataTableModel;

/**
 *
 * @author scsandra
 */
public class FileUtils {
    

    public static Dataset getDataset(Dataset dataset, String Name) {
        Dataset newDataset = null;
        switch (dataset.getType()) {            
            case MODELS:
                newDataset = new SimpleBasicDataset(Name + dataset.getDatasetName(), dataset.getPath());
                break;            
        }
        newDataset.setType(dataset.getType());
        return newDataset;
    }
    
    public static DataTableModel getTableModel(Dataset dataset) {            
        return new ReactionsDataModel(dataset);
       
    }

    public static DataTableModel getTableModelMet(Dataset dataset) {
        return new MetaboliteDataModel(dataset);
    }
   
}
