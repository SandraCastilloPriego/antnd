/*
 * Copyright 2007-2010 VTT Biotechnology
 * This file is part of NDBugs.
 *
 * NDBugs is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * NDBugs is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NDBugs; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.modules.simulation.control;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.DoubleParameter;
import ND.parameters.parametersType.FileNameParameter;
import ND.parameters.parametersType.IntegerParameter;

public class StartSimulationParameters extends SimpleParameterSet {     
   

        public static final FileNameParameter weights = new FileNameParameter(
                "Weights", "File where the weights are", null);
        public static final IntegerParameter numberOfNodes = new IntegerParameter(
                "Maximum Number of Nodes",
                "Introduce the maximum number of nodes", new Integer(10));
        public static final IntegerParameter numberOfBugs = new IntegerParameter(
                "Maximum Number of Bugs",
                "Introduce the maximum number of Bugs (possible solutions)", new Integer(10000));
        public static final IntegerParameter numberOfcycles = new IntegerParameter(
                "Number of cycles",
                "Stopping criteria: number of cycles where the best result didn't change", new Integer(100));
        public static final DoubleParameter mutationRate = new DoubleParameter(
                "Mutation rate",
                "Mutation rate", new Double(0.1));

        public StartSimulationParameters() {
                super(new Parameter[]{weights, numberOfNodes, numberOfBugs, numberOfcycles, mutationRate});
        }
}
