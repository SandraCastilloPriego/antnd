/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.sbml.jsbml.Species;

/**
 *
 * @author scsandra
 */
public class FBAAnalysisDataset implements Dataset {

    String datasetName, path;
    protected DatasetType type;
    StringBuffer infoDataset;
    private int ID;
    private SBMLDocument document;
    private final JTextArea textArea;
    private List<String> sources;
    private Graph graph;
    private boolean isCluster = false;
    private HashMap<String, ReactionFA> reactions;
    private HashMap<String, SpeciesFA> compounds;
    private List<String> cofactors;
    private Map<String, Double[]> sourcesMap;
    private String selectedReaction, selectedMetabolite;

    /**
     *
     * @param datasetName Name of the data set
     * @param path
     */
    public FBAAnalysisDataset(String datasetName, String path) {
        this.datasetName = datasetName;
        this.infoDataset = new StringBuffer();
        this.path = path;
        type = DatasetType.FBAAnalysis;
        this.textArea = new JTextArea();
    }

    public FBAAnalysisDataset() {
        type = DatasetType.FBAAnalysis;
        this.infoDataset = new StringBuffer();
        this.textArea = new JTextArea();
    }

    @Override
    public void setNodes(List<Node> nodes) {
        
    }

    @Override
    public void setEdges(List<Edge> edges) {
        
    }

    @Override
    public List<Node> getNodes() {
        return null;
    }

    @Override
    public List<Edge> getEdges() {
       return null;
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
    public List<String> getSources() {
        return this.sources;
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
    public FBAAnalysisDataset clone() {
        FBAAnalysisDataset newDataset = new FBAAnalysisDataset(this.datasetName, this.path);
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
    public boolean isReactionSelected(Reaction reaction) {
        if (this.selectedReaction.equals(reaction.getId())) {
            return true;
        }
        return false;
    }

    @Override
    public void setReactionSelectionMode(String reaction) {
        this.selectedReaction = reaction;
    }
    
    @Override
    public boolean isMetaboliteSelected(Species metabolite) {
        if (this.selectedMetabolite.equals(metabolite.getId())) {
            return true;
        }
        return false;
    }

    @Override
    public void setMetaboliteSelectionMode(String metabolite) {
        this.selectedMetabolite = metabolite;
    }

    @Override
    public String getParent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setParent(String dataset) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isParent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setIsParent(boolean isParent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
