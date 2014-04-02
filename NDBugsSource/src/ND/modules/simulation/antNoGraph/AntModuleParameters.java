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
package ND.modules.simulation.antNoGraph;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.FileNameParameter;
import ND.parameters.parametersType.IntegerParameter;
import ND.parameters.parametersType.StringParameter;

public class AntModuleParameters extends SimpleParameterSet {

        public static final FileNameParameter exchangeReactions = new FileNameParameter(
                "ExchangeReactions", "Define the exchange reactions", null);
        public static final FileNameParameter bounds = new FileNameParameter(
                "Reaction bounds", "Define the bounds of the reactions", null);
        public static final StringParameter objectiveReaction = new StringParameter(
                "Compound ID", "ID of the compound you want to find");        
        public static final IntegerParameter numberOfIterations = new IntegerParameter(
                "Number of Iterations", "Number of Iterations", 5000);
        public static final IntegerParameter addPheromones = new IntegerParameter(
                "Pheromones", "Pheromones added when a valid path is found", 100);        
        public static final IntegerParameter removePheromones = new IntegerParameter(
                "Pheromones evaporation", "Pheromones removed with time", 20);

        public AntModuleParameters() {
                super(new Parameter[]{exchangeReactions, bounds, objectiveReaction, numberOfIterations, addPheromones, removePheromones});
        }
}
