
package ND.data;

public enum ColumnName {

        /**
         * Fix columns for Models files. Each column has:
         * Column name, getVar function, setVar function, regular Expresion to parse files and type of data. *
         */
        SELECTION("Number", "isSelected", "setSelectionMode", "Selection", ParameterType.BOOLEAN),
        ID("Id", "getID", "setID", "^ID.*|^Id|.*row ID.*", ParameterType.STRING),
        NAME("Name", "getName", "setName", ".*|^Name.*|^name.*|^Name", ParameterType.STRING),
        REACTION("Reaction", "getReaction", "setReaction", ".*Reaction.*", ParameterType.STRING),
        REACTIONEXT("Reaction extended", "getReactionExtended", "setReactionExtended", ".*ReactionExtended.*", ParameterType.STRING),
        LB("Lower bound", "getLowerBound", "setLowerBound", ".*lb.*", ParameterType.STRING),
        UB("Upper bound", "getUpperBound", "setUpperBound", ".*ub.*", ParameterType.STRING),
        GENERULES("Notes", "getGeneRules", "setGeneRules", ".*Gene rules.*", ParameterType.STRING),
        OBJECTIVE("Objective", "getObjective", "setObjective", ".*Objective function.*", ParameterType.STRING),
        FLUXES("Fluxes", "", "e", "", ParameterType.DOUBLE);
        
        private final String columnName;
        private final String getFunctionName, setFunctionName;
        private final String regExp;
        private final ParameterType type;

        ColumnName(String columnName,
                String getFunctionName, String setFunctionName,
                String regExp, ParameterType type) {
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
