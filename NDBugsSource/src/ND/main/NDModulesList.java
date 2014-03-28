/*
 * Copyright 2007-2012 
 *
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
package ND.main;

import ND.modules.file.openfile.OpenBasicFileModule;
import ND.modules.reactionOP.addReaction.AddReactionModule;
import ND.modules.reactionOP.fluxAnalysis.FluxAnalysisModule;
import ND.modules.reactionOP.removeReaction.RemoveReactionModule;
import ND.modules.reactionOP.showCompound.ShowCompoundModule;
import ND.modules.reactionOP.showReaction.ShowReactionModule;
import ND.modules.simulation.loopAnalizer.LoopAnalyzerModule;
import ND.modules.simulation.superAnt.SuperAntModule;
import ND.modules.simulation.superAntAdvanced.SuperAntAdvancedModule;

/**
 * List of modules included in MM
 */
public class NDModulesList {

        public static final Class<?> MODULES[] = new Class<?>[]{
                OpenBasicFileModule.class,
               // AntModule.class,
                SuperAntModule.class,
                SuperAntAdvancedModule.class,
                AddReactionModule.class,
                RemoveReactionModule.class,
                ShowReactionModule.class,
                ShowCompoundModule.class,
                FluxAnalysisModule.class,
                LoopAnalyzerModule.class
        };
}
