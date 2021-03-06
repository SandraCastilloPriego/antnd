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


import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.StringParameter;

public class QueryDBParameters extends SimpleParameterSet {     
   

        public static final StringParameter cypher = new StringParameter(
                "Cypher query", "Write here the cypher query", "match(n:BioledgeBag)-[:DBLINK*]->(m:KEGG)<-[:DBLINK*]-(met:MetaNetX),(n:BioledgeBag)-[:DBLINK*]->(j:ChEBI)<-[:DBLINK*]-(met2:MetaNetX) WITH count(met) as c, n as n, met as met, m as m, j as j, met2 as met2 where c=1  return n,met,m,j,met2");
       

        public QueryDBParameters() {
                super(new Parameter[]{cypher});
        }
}
