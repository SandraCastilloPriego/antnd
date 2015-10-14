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
package ND.modules.simulation.PathsBetweenReactions;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.StringParameter;

public class AntBetweenModuleParameters extends SimpleParameterSet {

        public static final StringParameter sourceReaction = new StringParameter(
                "Compound ID From:", "ID of the initial compound");       
        public static final StringParameter objectiveReaction = new StringParameter(
                "Compound ID To:", "ID of the compound you want to find"); 
        public static final StringParameter excluded = new StringParameter(
                "Excluded compounds:", "ID of the compounds that will be excluded"); 
        public AntBetweenModuleParameters() {
                super(new Parameter[]{sourceReaction, objectiveReaction, excluded});
        }
}
