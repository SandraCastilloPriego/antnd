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
package ND.data.network;

import ND.modules.simulation.antNoGraph.uniqueId;
import org.jgrapht.graph.DefaultEdge;


/**
 *
 * @author scsandra
 */
public class Edge extends DefaultEdge{

        private String id;
        private Node source;
        private Node destination;
        
        
        public Edge(){}

        public Edge(String id, Node source, Node destination) {
                this.id = id;
                this.source = source;
                this.destination = destination;
        }

        public String getId() {
                return id;
        }

        public Node getDestination() {
                return destination;
        }

        public Node getSource() {
                return source;
        }

        @Override
        public String toString() {
                return source + " " + destination;
        }
        
        public void setSource(Node source){
                this.source = source;
        }
        
        public void setDestination(Node destination){
                this.destination = destination;
        }
        
        public void setId(String id){
                this.id = id;
        }       
      
        @Override
        public Edge clone(){
                Edge e = new Edge(this.id, this.source, this.destination);
                return e;
        }
             
}
