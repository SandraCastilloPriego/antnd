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
package ND.modules.analysis.KStepMarkov;


import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.IntegerParameter;

public class KStepMarkovParameters extends SimpleParameterSet {     
   

        public static final IntegerParameter K = new IntegerParameter(
                "K", "positive integer parameter which controls the relative tradeoff between a distribution \"biased\" towards R and the steady-state distribution which is independent of where the Markov-process started. Generally values between 4-8 are reasonable", 6);
       
       

        public KStepMarkovParameters() {
                super(new Parameter[]{K});
        }
}
