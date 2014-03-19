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

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 *
 * This class represents a range of doubles.
 */
public class Range {

        private double min, max;

        /**
         * Creates a range with only one value, representing both minimum and
         * maximum. Such range can later be extended using extendRange().
         *
         * @param minAndMax Range minimum and maximum
         */
        public Range(double minAndMax) {
                this(minAndMax, minAndMax);
        }

        /**
         * Creates a range from min to max.
         *
         * @param min Range minimum
         * @param max Range maximum
         */
        public Range(double min, double max) {
                if (min > max) {
                        throw (new IllegalArgumentException(
                                "Range minimum must be <= maximum"));
                }
                this.min = min;
                this.max = max;
        }

        /**
         * Copy constructor.
         *
         * @param range Range to copy
         */
        public Range(Range range) {
                this(range.getMin(), range.getMax());
        }

        /**
         * Creates a range from String where the numbers are separated by "-".
         * Example: "343.3 - 455.4"
         *
         * @param range String with two numbers separated by "-"
         */
        public Range(String range) {
                try {
                        String[] value = range.split("-");
                        this.min = Double.parseDouble(value[0]);
                        this.max = Double.parseDouble(value[1]);
                        if (min > max) {
                                throw (new IllegalArgumentException(
                                        "Range minimum must be <= maximum"));
                        }

                } catch (Exception e) {
                }
        }

        /**
         * @return Range minimun
         */
        public double getMin() {
                return min;
        }

        /**
         * @return Range maximum
         */
        public double getMax() {
                return max;
        }

        /**
         * Returns true if this range contains given value.
         *
         * @param value Value to check
         * @return True if range contains this value
         */
        public boolean contains(double value) {
                return ((min <= value) && (max >= value));
        }

        /**
         * Returns true if this range contains the whole given range as a subset.
         *
         * @param checkMin Minimum of given range
         * @param checkMax Maximum of given range
         * @return True if this range contains given range
         */
        public boolean containsRange(double checkMin, double checkMax) {
                return ((checkMin >= min) && (checkMax <= max));
        }

        /**
         * Returns true if this range contains the whole given range as a subset.
         *
         * @param checkRange Given range
         * @return True if this range contains given range
         */
        public boolean containsRange(Range checkRange) {
                return containsRange(checkRange.getMin(), checkRange.getMax());
        }

        /**
         * Returns true if this range lies within the given range.
         *
         * @param checkMin Minimum of given range
         * @param checkMax Maximum of given range
         * @return True if this range lies within given range
         */
        public boolean isWithin(double checkMin, double checkMax) {
                return ((checkMin <= min) && (checkMax >= max));
        }

        /**
         * Returns true if this range lies within the given range.
         *
         * @param checkRange Given range
         * @return True if this range lies within given range
         */
        public boolean isWithin(Range checkRange) {
                return isWithin(checkRange.getMin(), checkRange.getMax());
        }

        /**
         * Extends this range (if necessary) to include the given value
         *
         * @param value Value to extends this range
         */
        public void extendRange(double value) {
                if (min > value) {
                        min = value;
                }
                if (max < value) {
                        max = value;
                }
        }

        /**
         * Extends this range (if necessary) to include the given range
         *
         * @param extension Range to extends this range
         */
        public void extendRange(Range extension) {
                if (min > extension.getMin()) {
                        min = extension.getMin();
                }
                if (max < extension.getMax()) {
                        max = extension.getMax();
                }
        }

        /**
         * Returns the size of this range.
         *
         * @return Size of this range
         */
        public double getSize() {
                return (max - min);
        }

        /**
         * Returns the average point of this range.
         *
         * @return Average
         */
        public double getAverage() {
                return ((min + max) / 2);
        }

        /**
         * Returns the String representation
         *
         * @return This range as string
         */
        public String toString() {
                return String.valueOf(min) + " - " + String.valueOf(max);
        }
}
