/*
 * Copyright 2013-2014 VTT Biotechnology
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
package ND.modules.reactionOP.addReaction;


import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.DoubleParameter;
import ND.parameters.parametersType.StringParameter;

public class AddReactionParameters extends SimpleParameterSet {     
   

        public static final StringParameter reactionName = new StringParameter(
                "Reaction name", "Name of the reaction that will be added.", "");
        public static final StringParameter compounds = new StringParameter(
                "Compound Id", "Id of all the compounds separated by comma.", "");
        public static final StringParameter stoichiometry = new StringParameter(
                "Stoichiometry", "Stoichiometry of all the compounds separated by comma", "");        
        public static final DoubleParameter lb = new DoubleParameter(
                "Lower bound", "Lower bound.", -1000.0);
        public static final DoubleParameter ub = new DoubleParameter(
                "Upper bound", "Upper bound.", 1000.0);


        public AddReactionParameters() {
                super(new Parameter[]{reactionName, compounds, stoichiometry, lb, ub});
        }
}
