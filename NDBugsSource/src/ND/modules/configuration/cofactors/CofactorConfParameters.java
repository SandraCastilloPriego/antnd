/*
 * Copyright 2007-2013 VTT Biotechnology
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
package ND.modules.configuration.cofactors;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.StringParameter;

public class CofactorConfParameters extends SimpleParameterSet {

        public static final StringParameter NAD = new StringParameter("NAD+ id", "The corresponding id of NAD+ in the model", "C00003");
        public static final StringParameter NADH = new StringParameter("NADH id", "The corresponding id of NADH in the model", "C00004");
        public static final StringParameter NADP = new StringParameter("NADP+ id", "The corresponding id of NADP+ in the model", "C00006");
        public static final StringParameter NADPH = new StringParameter("NADPH id", "The corresponding id of NADPH in the model", "C00005");
        public static final StringParameter ADP = new StringParameter("ADP id", "The corresponding id of ADP in the model", "C00008");
        public static final StringParameter ATP = new StringParameter("ATP id", "The corresponding id of ATP in the model", "C00002");

        public CofactorConfParameters() {
                super(new Parameter[]{NAD, NADH, NADP, NADPH, ADP, ATP});
        }
}
