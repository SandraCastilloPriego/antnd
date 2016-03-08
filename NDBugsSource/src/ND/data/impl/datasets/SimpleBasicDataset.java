/*
 * Copyright 2007-2012 
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
package ND.data.impl.datasets;

import ND.data.Dataset;
import ND.data.DatasetType;
import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.modules.simulation.FBA.SpeciesFA;
import ND.modules.simulation.antNoGraph.ReactionFA;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTextArea;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;

/**
 * Basic data set implementation.
 *
 * @author SCSANDRA
 */
public class SimpleBasicDataset implements Dataset {

    String datasetName, path;
    protected DatasetType type;
    StringBuffer infoDataset;
    private int ID;
    private SBMLDocument document;
    private final JTextArea textArea;
    private List<Node> nodes;
    private List<Edge> edges;
    private List<String> sources;
    private Graph graph;
    private String biomassId;
    private boolean isCluster = false;
    private HashMap<String, ReactionFA> reactions;
    private HashMap<String, SpeciesFA> compounds;
    private List<String> cofactors;
    private Map<String, Double[]> sourcesMap;
    private String selectedReaction;

    /**
     *
     * @param datasetName Name of the data set
     * @param path
     */
    public SimpleBasicDataset(String datasetName, String path) {
        this.datasetName = datasetName;
        this.infoDataset = new StringBuffer();
        this.path = path;
        type = DatasetType.MODELS;
        this.textArea = new JTextArea();
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public SimpleBasicDataset() {
        type = DatasetType.MODELS;
        this.infoDataset = new StringBuffer();
        this.textArea = new JTextArea();
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    @Override
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    @Override
    public List<Node> getNodes() {
        return this.nodes;
    }

    @Override
    public List<Edge> getEdges() {
        return this.edges;
    }

    @Override
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    @Override
    public void addSource(String source) {
        if (this.sources == null) {
            this.sources = new ArrayList<>();
        }
        this.sources.add(source);
    }

    @Override
    public void setBiomass(String biomassId) {
        this.biomassId = biomassId;
    }

    @Override
    public List<String> getSources() {
        return this.sources;
    }

    @Override
    public String getBiomassId() {
        return this.biomassId;
    }

    @Override
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public Graph getGraph() {
        return this.graph;
    }

    @Override
    public void setID(int ID) {
        this.ID = ID;
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public String getDatasetName() {
        return this.datasetName;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    @Override
    public DatasetType getType() {
        return type;
    }

    @Override
    public void setType(DatasetType type) {
        this.type = type;
    }

    @Override
    public JTextArea getInfo() {
        return this.textArea;
    }

    @Override
    public void addInfo(String info) {
        this.infoDataset.append(info).append("\n");
        this.textArea.setText(infoDataset.toString());
    }

    @Override
    public SimpleBasicDataset clone() {
        SimpleBasicDataset newDataset = new SimpleBasicDataset(this.datasetName, this.path);
        newDataset.setType(this.type);
        newDataset.setDocument(this.getDocument());
        newDataset.setGraph(this.graph);
        return newDataset;
    }

    @Override
    public SBMLDocument getDocument() {
        return this.document;
    }

    @Override
    public void setDocument(SBMLDocument document) {
        this.document = document;
    }

    @Override
    public void setInfo(String info) {
        this.infoDataset.delete(0, this.infoDataset.length());
        this.infoDataset.append(info);
    }

    @Override
    public void SetCluster(boolean isCluster) {
        this.isCluster = isCluster;
    }

    @Override
    public boolean isCluster() {
        return this.isCluster;
    }

    @Override
    public void setPaths(Map<String, SpeciesFA> paths) {
        this.compounds = (HashMap<String, SpeciesFA>) paths;
    }

    @Override
    public HashMap<String, SpeciesFA> getPaths() {
        return this.compounds;
    }

    @Override
    public void setReactionsFA(Map<String, ReactionFA> reactions) {
        this.reactions = (HashMap<String, ReactionFA>) reactions;
    }

    @Override
    public HashMap<String, ReactionFA> getReactionsFA() {
        return this.reactions;
    }

    @Override
    public void setSourcesMap(Map<String, Double[]> sources) {
        this.sourcesMap = sources;

    }

    @Override
    public Map<String, Double[]> getSourcesMap() {
        return this.sourcesMap;
    }

    @Override
    public void setCofactors(List<String> cofactor) {
        this.cofactors = cofactor;
    }

    @Override
    public List<String> getCofactors() {
        return this.cofactors;
    }

    @Override
    public boolean isSelected(Reaction reaction) {
        if (this.selectedReaction.equals(reaction.getId())) {
            return true;
        }
        return false;
    }

    public void setSelectionMode(String reaction) {
        this.selectedReaction = reaction;
    }

}
