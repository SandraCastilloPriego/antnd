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
package ND.modules.reactionOP.changebounds;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.DoubleParameter;
import ND.parameters.parametersType.StringParameter;

public class ChangeBoundsParameters extends SimpleParameterSet {     
   

        public static final StringParameter reactionName = new StringParameter(
                "Reaction name", "Name of the reaction that will be removed", "");
        public static final DoubleParameter lb = new DoubleParameter(
                "Lower bound", "Lower Bound", 0.0);
        public static final DoubleParameter ub = new DoubleParameter(
                "Upper bound", "Upper Bound", 1000.0);
       

        public ChangeBoundsParameters() {
                super(new Parameter[]{reactionName,lb,ub});
        }
}
