/*
 * Copyright 2013-2014 VTT Biotechnology
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
package ND.modules.reactionOP.fluxAnalysis;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.FileNameParameter;

public class FluxAnalysisParameters extends SimpleParameterSet {

        public static final FileNameParameter fluxes = new FileNameParameter(
                "Fluxes", "Define the fluxes ", null);
        public static final FileNameParameter exchange = new FileNameParameter(
                "Source", "Define the uptaken compounds", null);

        public FluxAnalysisParameters() {
                super(new Parameter[]{fluxes, exchange});
        }
}