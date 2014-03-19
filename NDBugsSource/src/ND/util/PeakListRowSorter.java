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

import ND.data.Row;
import java.util.Comparator;

/**
 * @author Taken from MZmine2 http://mzmine.sourceforge.net/
 *
 * Compare peak list rows either by ID, average m/z or median area of peaks
 *
 */
public class PeakListRowSorter implements Comparator<Row> {

        private SortingProperty property;
        private SortingDirection direction;

        public PeakListRowSorter(SortingProperty property,
                SortingDirection direction) {
                this.property = property;
                this.direction = direction;
        }

        public int compare(Row row1, Row row2) {

                Double row1Value = getValue(row1);
                Double row2Value = getValue(row2);

                if (direction == SortingDirection.Ascending) {
                        return row1Value.compareTo(row2Value);
                } else {
                        return row2Value.compareTo(row1Value);
                }

        }

        private double getValue(Row row) {
                switch (property) {
                        case Intensity:
                                Object[] intensityPeaks = row.getPeaks(null);
                                double[] peakIntensities = new double[intensityPeaks.length];
                                for (int i = 0; i < intensityPeaks.length; i++) {
                                        try {
                                                peakIntensities[i] = (Double) intensityPeaks[i];

                                        } catch (Exception e) {
                                        }
                                }
                                double medianIntensity = MathUtils.calcQuantile(peakIntensities,
                                        0.5);
                                return medianIntensity;

                        case Height:
                                Object[] heightPeaks = row.getPeaks(null);
                                double[] peakHeights = new double[heightPeaks.length];
                                for (int i = 0; i < peakHeights.length; i++) {
                                        try {
                                                peakHeights[i] = (Double) heightPeaks[i];

                                        } catch (Exception e) {
                                        }
                                }
                                double medianHeight = MathUtils.calcQuantile(peakHeights, 0.5);
                                return medianHeight;
                        case ID:
                                return row.getID();
                }

                // We should never get here, so throw exception
                throw (new IllegalStateException());
        }
}
