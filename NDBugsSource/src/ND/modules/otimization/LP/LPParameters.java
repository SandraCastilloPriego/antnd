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
package ND.modules.otimization.LP;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.BooleanParameter;
import ND.parameters.parametersType.FileNameParameter;
import ND.parameters.parametersType.StringParameter;

public class LPParameters extends SimpleParameterSet {

        public static final FileNameParameter bounds = new FileNameParameter(
                "Bounds", "Define the reaction bounds ", null);
        public static final FileNameParameter exchange = new FileNameParameter(
                "Source", "Define the uptaken compounds", null);
        public static final StringParameter objective = new StringParameter(
                "Objective", "Reaction that will be optimized", "");
        public static final BooleanParameter maximize = new BooleanParameter(
                "Maximize", "If this option is not selected the objective fluxes will be minimized ", true);
         public static final BooleanParameter steadyState = new BooleanParameter(
                "Steady state", "NAD/NADH, ADP/ATP and NADP/NADPH will be balanced", true);

        public LPParameters() {
                super(new Parameter[]{bounds, exchange, objective, maximize, steadyState});
        }
}
