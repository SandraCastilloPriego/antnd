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
package ND.modules.ants;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.FileNameParameter;
import ND.parameters.parametersType.StringParameter;

public class AntModuleParameters extends SimpleParameterSet {

        public static final FileNameParameter exchangeReactions = new FileNameParameter(
                "Objective function", "Define the objective function", null);
        public static final StringParameter objectiveReaction = new StringParameter(
                "Reaction ID", "ID of the reaction you want to maximize");
        

        public AntModuleParameters() {
                super(new Parameter[]{exchangeReactions, objectiveReaction});
        }
}
