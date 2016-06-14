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
package ND.modules.DB.ShortPathway;

import ND.modules.DB.Visualize.*;
import ND.data.network.Edge;
import ND.data.network.Node;
import ND.main.NDCore;
import ND.modules.configuration.db.DBConfParameters;
import ND.modules.simulation.antNoGraph.uniqueId;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import edu.uci.ics.jung.visualization.VisualizationViewer;
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
public class ShortestPathDBTask extends AbstractTask {

    private final String From, To;
    private double finishedPercentage = 0.0f;
    private String URI;
    List<Node> nodes;
    List<Edge> edges;
    Map<String, Node> matches;

    public ShortestPathDBTask(SimpleParameterSet parameters) {
        this.From = parameters.getParameter(ShortestPathDBParameters.From).getValue();
        this.To = parameters.getParameter(ShortestPathDBParameters.To).getValue();
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
            String query = "MATCH(n:Compound {BioledgeBagId:'" + From + "'}),(j:Compound {BioledgeBagId:'" + To + "'}),p=allShortestPaths((n)-[*]-(j)) return p";
            this.getDBQuery(URI, query);

            finishedPercentage = 1.0f;
            setStatus(TaskStatus.FINISHED);
        } catch (Exception e) {
            setStatus(TaskStatus.ERROR);
            errorMessage = e.toString();
        }
    }

    private void getDBQuery(String URI, String queryString) {

        //match(n:BioledgeBag)-[:DBLINK*]->(m:KEGG)<-[:DBLINK*]-(met:MetaNetX),(n:BioledgeBag)-[:DBLINK*]->(j:ChEBI)<-[:DBLINK*]-(met2:MetaNetX) WITH count(met) as c, n as n, met as met, m as m, j as j, met2 as met2 where c=1  return n,met,m,j,met2 
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
        System.out.println(responsestring);
        JSONObject obj;
        try {
            obj = new JSONObject(responsestring);

            JSONArray array = obj.getJSONArray("data");

            for (int i = 0; i < array.length(); i++) {
                JSONArray a = array.getJSONArray(i);
                for (int j = 0; j < a.length(); j++) {
                    String path = a.getJSONObject(j).get("nodes").toString();
                    if (!path.contains("/node/687197") && !path.contains("/node/693359") && !path.contains("/node/694004") && !path.contains("/node/687229")&& !path.contains("/node/691642")
                        && !path.contains("/node/687219")) {
                        String[] nodes = path.replace("[", "").replace("]", "").replace("\"", "").split(",");
                        String newQuery1 = "MATCH";
                        String newQuery2 = " WHERE ";
                        String newQuery3 = " RETURN ";
                        int n = 1;
                        for (String node : nodes) {
                            node = node.substring(node.lastIndexOf("/")+1);
                            newQuery1 += "(node" + n + ")-[rel"+n+"]-";
                            newQuery2 += "id(node" + n + ")=" + node + " and ";
                            newQuery3 += "node" + n+",rel" + n + ",";
                            n++;
                        }
                        n--;
                        newQuery1 = newQuery1.substring(0, newQuery1.lastIndexOf("-[rel"+n+"]-"));
                        newQuery2 = newQuery2.substring(0, newQuery2.lastIndexOf("and"));
                        newQuery3 = newQuery3.substring(0, newQuery3.lastIndexOf(",rel"+n));
                        String q = newQuery1 + newQuery2 + newQuery3;
                         System.out.println(q);
                       
                        getDBQueryp(URI,q);
                    }
                }

            }         
        } catch (JSONException ex) {
            System.out.println(ex.toString());
        }

    }
    
      private void getDBQueryp(String URI, String queryString) {
        
        //match(n:BioledgeBag)-[:DBLINK*]->(m:KEGG)<-[:DBLINK*]-(met:MetaNetX),(n:BioledgeBag)-[:DBLINK*]->(j:ChEBI)<-[:DBLINK*]-(met2:MetaNetX) WITH count(met) as c, n as n, met as met, m as m, j as j, met2 as met2 where c=1  return n,met,m,j,met2 
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
            String[] col = columns.toString().replace("[", "").replace("]", "").replace("\"", "").split(",");
            JSONArray array = obj.getJSONArray("data");
            List<String[]> values = new ArrayList<>();
            List<MiniGraph> graphs = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONArray a = array.getJSONArray(i);
                List<MiniEdge> mes = new ArrayList<>();
                List<MiniNode> mns = new ArrayList<>();
                String[] value = new String[col.length];
                for (int j = 0; j < a.length(); j++) {
                    String metanode = a.getJSONObject(j).getJSONObject("metadata").toString();
                    String node = null;
                    if (metanode.contains("type")) {
                        node = a.getJSONObject(j).getJSONObject("metadata").get("type").toString();
                        String start = a.getJSONObject(j).get("start").toString();
                        String end = a.getJSONObject(j).get("end").toString();
                        String self = a.getJSONObject(j).get("self").toString();
                        MiniEdge me = new MiniEdge();
                        me.type = node;
                        me.start = start;
                        me.end = end;
                        me.self = self;
                        mes.add(me);

                    } else {
                        JSONObject nod = a.getJSONObject(j);
                        String[] knode = nod.getJSONObject("data").toString().replace("\"", "").replace("{", "").replace("}", "").split(",");
                        for (String k : knode) {
                            if (k.contains("Id")) {
                                node = k;
                            }
                        }
                        String labels = a.getJSONObject(j).getJSONObject("metadata").get("labels").toString();
                        String self = nod.get("self").toString();
                        MiniNode mn = new MiniNode();
                        mn.Id = node;
                        mn.self = self;
                        mn.labels = labels;
                        mns.add(mn);
                    }
                    value[j] = node;
                }
                MiniGraph mg = new MiniGraph(mns, mes);
                graphs.add(mg);

                values.add(value);
                for (MiniEdge me : mes) {
                    Edge e = new Edge(me.type + " - " + uniqueId.nextId(), this.matches.get(me.start), this.matches.get(me.end));
                    this.edges.add(e);
                }
            }

            MiniGraph mg = graphs.get(0);
            PrintGraph pg = new PrintGraph();
            JInternalFrame frame = new JInternalFrame("Graph DB", true, true, true, true);
            JPanel pn = new JPanel();
            JScrollPane panel = new JScrollPane(pn);

            frame.setSize(new Dimension(500, 500));
            frame.add(panel);
            NDCore.getDesktop().addInternalFrame(frame);
            try {

                System.out.println("Visualize");
                VisualizationViewer vv =pg.printPathwayInFrame(mg);
                vv.setPreferredSize(new Dimension(1000, 1000));
                pn.add(vv);

            } catch (NullPointerException ex) {
                System.out.println(ex.toString());
            }

        } catch (JSONException ex) {
            System.out.println(ex.toString());
        }

    }

}
