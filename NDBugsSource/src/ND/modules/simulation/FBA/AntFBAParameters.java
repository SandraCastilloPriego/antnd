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
package ND.modules.simulation.FBA;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.IntegerParameter;
import ND.parameters.parametersType.StringParameter;

public class AntFBAParameters extends SimpleParameterSet {

      /*  public static final StringParameter objectiveReaction = new StringParameter(
                "Compound ID", "ID of the compound you want to maximize"); */
        public static final IntegerParameter iterations = new IntegerParameter(
                "Iterations", "Number of iterations", 20); 
        public static final StringParameter cofactors = new StringParameter(
                "Cofactors", "ID of the model cofactors separated by comma."); 
       /* public static final StringParameter cofactors2 = new StringParameter(
                "Cofactors2", "ID of the model cofactors separated by comma."); */
       

        public AntFBAParameters() {
                super(new Parameter[]{/*objectiveReaction,*/ cofactors, /*cofactors2,*/ iterations});
        }
}
