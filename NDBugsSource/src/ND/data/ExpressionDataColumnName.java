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

/**
 *
 * @author scsandra
 */
public enum ExpressionDataColumnName {

        SELECTION("Selection", "isSelected", "setSelectionMode", "Selection", ParameterType.BOOLEAN),
        NAME("featurenames", "getName", "setName", ".*featurenames.*|.*name.*", ParameterType.STRING),
        P("P-value", "getPValue", "setPValue", ".*p-value.*|.*P-value.*", ParameterType.DOUBLE),
        Q("Q-value", "getQValue", "setQValue", ".*q-value.*|.*Q-value.*", ParameterType.DOUBLE);
        private final String columnName;
        private final String getFunctionName, setFunctionName;
        private final String regExp;
        private final ParameterType type;

        ExpressionDataColumnName(String columnName, String getFunctionName,
                String setFunctionName, String regExp, ParameterType type) {
                this.columnName = columnName;
                this.getFunctionName = getFunctionName;
                this.setFunctionName = setFunctionName;
                this.regExp = regExp;
                this.type = type;
        }

        public String getColumnName() {
                return this.columnName;
        }

        public String getGetFunctionName() {
                return this.getFunctionName;
        }

        public String getSetFunctionName() {
                return this.setFunctionName;
        }

        public String getRegularExpression() {
                return this.regExp;
        }

        public ParameterType getType() {
                return this.type;
        }

        @Override
        public String toString() {
                return this.columnName;
        }
}
