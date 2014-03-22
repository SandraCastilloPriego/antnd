/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.antNoGraph.network;


/**
 *
 * @author scsandra
 */
public class Edge {

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
      
             
}
