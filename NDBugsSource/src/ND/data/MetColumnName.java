/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.data;

/**
 *
 * @author scsandra
 */
public enum MetColumnName {

    /**
     * Fix columns for Models files. Each column has: Column name, getVar
     * function, setVar function, regular Expresion to parse files and type of
     * data. *
     */
    SELECTION("Number", "isSelected", "setSelectionMode", "Selection", ParameterType.BOOLEAN),
    ID("Id", "getID", "setID", "^ID.*|^Id|.*row ID.*", ParameterType.STRING),
    NAME("Metabolite Name", "getName", "setName", ".*|^Name.*|^name.*|^Name", ParameterType.STRING),    
    NOTES("Notes", "getNotes", "setNotes", ".*Notes.*", ParameterType.STRING),
    COMPARTMENT("Compartment", "getCompartment", "setCompartment", ".*ompartment.*", ParameterType.STRING);
   // REACTION("Reactions", "getReaction", "setReaction", ".*Reaction.*", ParameterType.STRING);

    private final String columnName;
    private final String getFunctionName, setFunctionName;
    private final String regExp;
    private final ParameterType type;

    MetColumnName(String columnName,
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
