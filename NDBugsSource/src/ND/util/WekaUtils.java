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
package ND.util;

import ND.data.Dataset;
import ND.data.Row;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

public class WekaUtils {

    public static Instances getWekaDataset(Dataset dataset, boolean samples) {
        try {
            if (samples) {
                FastVector attributes = new FastVector();
                int cont = 1;
                for (Row row : dataset.getRows()) {
                    String rowName = "";
                    rowName = row.getName();

                    if (rowName == null || rowName.isEmpty()) {
                        rowName = "Var";
                    }
                    rowName += cont++;

                    Attribute var = new Attribute(rowName);
                    attributes.addElement(var);
                }

                //Creates the dataset
                Instances data = new Instances(dataset.getDatasetName(), attributes, 0);

                for (int i = 0; i < dataset.getNumberCols(); i++) {

                    String sampleName = dataset.getAllColumnNames().get(i);

                    double[] values = new double[data.numAttributes()];
                    cont = 0;
                    for (Row row : dataset.getRows()) {
                        values[cont++] = (Double) row.getPeak(sampleName);
                    }

                    Instance inst = new SparseInstance(1.0, values);
                    data.add(inst);
                }
                return data;
            } else {
                FastVector attributes = new FastVector();
                int cont = 1;
                for (String column : dataset.getAllColumnNames()) {
                    Attribute var = new Attribute(column);
                    attributes.addElement(var);
                }

                //Creates the dataset
                Instances data = new Instances(dataset.getDatasetName(), attributes, 0);

                for (Row row : dataset.getRows()) {
                    double[] values = new double[data.numAttributes()];
                    cont = 0;
                    for (int i = 0; i < dataset.getNumberCols(); i++) {
                        String sampleName = dataset.getAllColumnNames().get(i);
                        values[cont++] = (Double) row.getPeak(sampleName);
                    }

                    Instance inst = new SparseInstance(1.0, values);
                    data.add(inst);
                }
                return data;

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }
}
