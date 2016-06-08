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
package ND.modules.DB.Visualize;

import ND.data.network.Edge;
import ND.data.network.Graph;
import ND.data.network.Node;
import ND.desktop.impl.PrintPaths;
import ND.main.NDCore;
import ND.modules.configuration.db.DBConfParameters;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.ws.rs.core.MediaType;
import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONException;
import org.neo4j.shell.util.json.JSONObject;

/**
 *
 * @author scsandra
 */
public class QueryDBTask extends AbstractTask {

    private final String cypherQuery;
    private double finishedPercentage = 0.0f;
    private String URI;
    List<Node> nodes;
    List<Edge> edges;
    Map<String, Node> matches;

    public QueryDBTask(SimpleParameterSet parameters) {
        this.cypherQuery = parameters.getParameter(QueryDBParameters.cypher).getValue();
        this.URI = NDCore.getDBParameters().getParameter(DBConfParameters.URI).getValue();
        if (!URI.contains("http")) {
            this.URI = "http://" + URI + ":7474/db/data/";
        } else {
            this.URI = URI + ":7474/db/data/";
        }
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.matches = new HashMap<>();
    }

    @Override
    public String getTaskDescription() {
        return "Retreiving data from the graph database... ";
    }

    @Override
    public double getFinishedPercentage() {
        return finishedPercentage;
    }

    @Override
    public void cancel() {
        setStatus(TaskStatus.CANCELED);
    }

    @Override
    public void run() {
        try {
            setStatus(TaskStatus.PROCESSING);
            this.getDBQuery(URI, cypherQuery);

            finishedPercentage = 1.0f;
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }

    private void getDBQuery(String URI, String queryString) {
        final String nodeEntryPointUri = URI + "cypher";

        String query = "{\"query\" : \"" + queryString + "\",\n \"params\":  {} \n}";
        //System.out.println(query);
        WebResource resource = Client.create()
            .resource(nodeEntryPointUri);
        ClientResponse response = resource.accept(MediaType.APPLICATION_JSON)
            .type(MediaType.APPLICATION_JSON)
            .entity(query)
            .post(ClientResponse.class);

        String responsestring = response.getEntity(String.class);
        response.close();
        // System.out.println(responsestring);
        JSONObject obj;
        try {
            obj = new JSONObject(responsestring);

            JSONArray columns = obj.getJSONArray("columns");
            String[] col = columns.toString().replace("[", "").replace("]", "").split(",");
            JSONArray array = obj.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONArray a = array.getJSONArray(i);
                List<MiniEdge> mes = new ArrayList<>();
                for (int j = 0; j < a.length(); j++) {
                    String metanode = a.getJSONObject(j).getJSONObject("metadata").toString();
                    String node = null;                    
                    if (metanode.contains("type")) {
                        node = "-{" + a.getJSONObject(j).getJSONObject("metadata").get("type").toString() + "}->";
                        String start =  a.getJSONObject(j).get("start").toString() ;
                        String end =  a.getJSONObject(j).get("end").toString() ;
                        MiniEdge me = new MiniEdge();
                        me.type = node;
                        me.start = start;
                        me.end = end;
                        mes.add(me);               
                        
                    } else {
                        JSONObject nod = a.getJSONObject(j);
                        node = nod.getJSONObject("data").toString();
                        String self  = nod.get("self").toString();
                        Node n  = new Node(node);                        
                        if(!matches.containsKey(self)){
                            this.matches.put(self, n);
                            this.nodes.add(n);
                        }
                    }
                    System.out.print(node);
                }
                
                for(MiniEdge me : mes){
                    Edge  e = new Edge (me.type + " - "+uniqueId.nextId(), this.matches.get(me.start), this.matches.get(me.end) );
                    this.edges.add(e);
                }
                System.out.println();
                //  System.out.println(a.getJSONObject(0).getJSONObject("data").get("BioledgeBagId").toString());
            }
            Graph g = new Graph(this.nodes, this.edges);
            JInternalFrame frame = new JInternalFrame("Graph DB", true, true, true, true);
            JPanel pn = new JPanel();
            JScrollPane panel = new JScrollPane(pn);

            frame.setSize(new Dimension(700, 500));
            frame.add(panel);
            NDCore.getDesktop().addInternalFrame(frame);

            PrintPaths print = new PrintPaths(null);
            try {

                System.out.println("Visualize");
                pn.add(print.printPathwayInFrame(g));

            } catch (NullPointerException ex) {
                System.out.println(ex.toString());
            }

        } catch (JSONException ex) {
            System.out.println(ex.toString());
        }

    }

}
