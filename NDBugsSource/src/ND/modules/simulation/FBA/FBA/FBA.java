/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.simulation.FBA.FBA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sbml.jsbml.SBMLDocument;
import parsebionet.biodata.BioChemicalReaction;
import parsebionet.biodata.BioEntity;
import parsebionet.biodata.BioNetwork;
import parsebionet.io.JSBMLToBionetwork;

/**
 *
 * @author scsandra
 */
public class FBA {

//    public boolean checkInteractionNetwork = true;
//
//    /**
//     * Used for pareto analysis and conditionComparison, if set to false, the
//     * objective in the constraints file is ignored.
//     */
//    private boolean loadObjective = true;
//
//    /**
//     * Objective of the problem.
//     */
//    protected Objective obj;
//
//    /**
//     * List used when several objectives are given in the constraints file.
//     */
//    public List<Objective> constraintObjectives = new ArrayList<Objective>();
//
//    /**
//     * Will permit to create the right operations.
//     */
//    protected OperationFactory operationFactory;
//    /**
//     * To create the right relations.
//     */
//    protected RelationFactory relationFactory;
//
//    /**
//     * The interaction network of the problem.
//     */
//    protected InteractionNetwork intNet = new InteractionNetwork();
//
//    /**
//     * The metabolic network of the problem.
//     */
//    protected BioNetwork bioNet;
//
//    /**
//     * All problem's constraints.
//     */
//    protected List<Constraint> constraints = new ArrayList<Constraint>();
//
//    /**
//     * List of dead reactions.
//     */
//    protected Collection<BioChemicalReaction> deadReactions = new ArrayList<BioChemicalReaction>();
//
//    /**
//     * Contains the results of the last FBA performed.
//     */
//    protected Map<String, Double> lastSolve = new HashMap<String, Double>();
//
//    FBA() {
//    }
//
//    public void loadSbmlNetwork(SBMLDocument sBMLdoc) {
//        JSBMLToBionetwork parser = new JSBMLToBionetwork(null, false);
//        parser.setSBMLdoc(sBMLdoc);
//        setNetworkAndConstraints(parser.getBioNetwork());
//
//    }
//    /*
//     * 
//     * Sets the problem's network and adds constraints and interactions included
//     * in it.
//     * 
//     * @param network
//     *            Network to give the problem.
//     */
//
//    public void setNetworkAndConstraints(BioNetwork network) {
//
//        if (network == null) {
//
//            System.err.println("Error : could not load sbml file");
//            System.exit(0);
//        }
//
//        this.bioNet = network;
//        deadReactions = bioNet.trim();
//
//        simpleConstraints.clear();
//        constraints.clear();
//        intNet.clear();
//        clear();
//
//        // we add the trimed reactions as set to 0
//        for (BioChemicalReaction trimed : deadReactions) {
//
//            intNet.addNumEntity(trimed);
//
//            Map<BioEntity, Double> constMap = new HashMap<BioEntity, Double>();
//            constMap.put(trimed, 1.0);
//            constraints.add(new Constraint(constMap, 0.0, 0.0));
//        }
//
//        solverPrepared = false;
//
//        // adds networks entites
//        intNet.addNetworkEntities(bioNet);
//
//        // creates the steady state constraints
//        makeSteadyStateConstraints();
//
//        // sets the reaction bounds
//        setReactionsBounds();
//
//        // GPR to interaction
//        intNet.gprInteractions(bioNet, relationFactory, operationFactory);
//
//    }
//
//    /**
//     * Creates interactions from the interaction file.
//     *
//     * @param path Path to the interaction file.
//     */
//    public void loadRegulationFile(String path) {
//
//        intNet = SBMLQualReader.loadSbmlQual(path, intNet, relationFactory);
//
//    }
//
//    /**
//     *
//     * Adds an entity to the problem.
//     *
//     * @param b Entity to add.
//     * @param integer If it is an integer entity.
//     * @param binary If it is a binary entity.
//     */
//    public void addRightEntityType(BioEntity b, boolean integer, boolean binary) {
//        if (binary) {
//            intNet.addBinaryEntity(b);
//            return;
//        }
//        if (integer && !binary) {
//            intNet.addIntEntity(b);
//            return;
//        }
//        if (!integer && !binary) {
//            intNet.addNumEntity(b);
//            return;
//        }
//    }
}
