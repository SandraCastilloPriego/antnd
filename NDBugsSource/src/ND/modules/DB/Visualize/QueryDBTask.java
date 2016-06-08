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

import ND.modules.DB.Visualize.QueryDBParameters;
import ND.main.NDCore;
import ND.modules.configuration.db.DBConfParameters;
import ND.parameters.SimpleParameterSet;
import ND.taskcontrol.AbstractTask;
import ND.taskcontrol.TaskStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.ArrayList;
import java.util.List;
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

    public QueryDBTask(SimpleParameterSet parameters) {
        this.cypherQuery = parameters.getParameter(QueryDBParameters.cypher).getValue();
        this.URI = NDCore.getDBParameters().getParameter(DBConfParameters.URI).getValue();
        if (!URI.contains("http")) {
            this.URI = "http://" + URI + ":7474/db/data/";
        } else {
            this.URI = URI + ":7474/db/data/";
        }
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

    private List<String> getDBQuery(String URI, String queryString) {
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
        //     System.out.println(responsestring);
        JSONObject obj;
        try {
            obj = new JSONObject(responsestring);
            JSONArray array = obj.getJSONArray("data");
            List<String> nodes = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONArray a = array.getJSONArray(i);
                nodes.add(a.getJSONObject(0).getJSONObject("data").toString());
                //  System.out.println(a.getJSONObject(0).getJSONObject("data").get("BioledgeBagId").toString());
            }
            return nodes;
        } catch (JSONException ex) {
        }

        return null;
    }

}
