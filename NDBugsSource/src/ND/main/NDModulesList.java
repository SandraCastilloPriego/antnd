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

import ND.modules.DB.Visualize.QueryDBModule;
import ND.modules.analysis.ClusteringBetweenness.ClusteringBetweennessModule;
import ND.modules.analysis.ClusteringKmeans.ClusteringModule;
import ND.modules.analysis.CompareModels.CompareModule;
import ND.modules.analysis.kNeighborhood.KNeighborhoodModule;
import ND.modules.configuration.db.DBConfModule;
import ND.modules.configuration.sources.SourcesConfModule;
import ND.modules.file.openProject.OpenProjectModule;
import ND.modules.file.openfile.OpenBasicFileModule;
import ND.modules.file.saveProject.SaveProjectModule;
import ND.modules.reactionOP.Layout.ExtractLayoutModule;
import ND.modules.reactionOP.Layout.ExtractLayoutTask;
import ND.modules.simulation.FBAreal.LPModule;
import ND.modules.reactionOP.addReactions.AddReactionsModule;
import ND.modules.reactionOP.addReaction.AddReactionModule;
import ND.modules.reactionOP.changebounds.ChangeBoundsModule;
import ND.modules.reactionOP.compoundFlux.FluxCalcModule;
import ND.modules.reactionOP.deadends.DeadEndsModule;
import ND.modules.reactionOP.fluxAnalysis.FluxAnalysisModule;
import ND.modules.reactionOP.removeLipids.RemoveLipidsModule;
import ND.modules.reactionOP.removeReaction.RemoveReactionModule;
import ND.modules.reactionOP.setBounds.SetBoundsModule;
import ND.modules.reactionOP.showAllCompoundList.ShowAllCompoundsModule;
import ND.modules.reactionOP.showAllReactionList.ShowAllReactionsModule;
import ND.modules.reactionOP.showCompound.ShowCompoundModule;
import ND.modules.reactionOP.showPathways.ShowPathwaysModule;
import ND.modules.reactionOP.showReaction.ShowReactionModule;
import ND.modules.simulation.Dynamic.DynamicModule;
import ND.modules.simulation.PathsBetweenReactions.AntBetweenModule;
import ND.modules.simulation.geneticalgorithm.StartSimulationModule;
import ND.modules.simulation.geneticalgorithm.reducing.StartReducingGAModule;
import ND.modules.simulation.geneticalgorithm.testing.StartTestingGAModule;
import ND.modules.simulation.geneticalgorithmDirections.StartSimulationDirectionsModule;
import ND.modules.simulation.somePaths.SomePathsModule;
import ND.modules.simulation.superAnt.SuperAntModule;

/**
 * List of modules included in MM
 */
public class NDModulesList {

    /**
     *
     */
    public static final Class<?> MODULES[] = new Class<?>[]{
        OpenBasicFileModule.class,
        OpenProjectModule.class,
        SaveProjectModule.class,
        DBConfModule.class,
        // SourcesConfModule.class,
        //CofactorConfModule.class,
        // AntModule.class,
        AntBetweenModule.class,
        SuperAntModule.class,
        //SuperAntAdvancedModule.class,
        //AntFBAModule.class,
        AddReactionModule.class,
        AddReactionsModule.class,
        RemoveReactionModule.class,
        ChangeBoundsModule.class,
        SetBoundsModule.class,
        ShowReactionModule.class,
        ShowAllReactionsModule.class,
        ShowCompoundModule.class,
        ShowAllCompoundsModule.class,
        DeadEndsModule.class,
        //AddInfoModule.class, 
        SomePathsModule.class,
        ShowPathwaysModule.class,
        // LoopAnalyzerModule.class,
        //  AntFluxMaxModule.class,
        //  AntFluxMinModule.class,
        LPModule.class,
        // LPNCModule.class,
        FluxAnalysisModule.class,
        FluxCalcModule.class,
        DynamicModule.class,
        RemoveLipidsModule.class,
        ExtractLayoutModule.class,
        ClusteringModule.class,
        ClusteringBetweennessModule.class,
        KNeighborhoodModule.class,
        //KStepMarkovModule.class,
        //CycleDetectorModule.class,
        CompareModule.class,
        //FluxVisualizationModule.class,
        StartSimulationModule.class,
        StartTestingGAModule.class,
        StartReducingGAModule.class,
        StartSimulationDirectionsModule.class,
        QueryDBModule.class,};
}
