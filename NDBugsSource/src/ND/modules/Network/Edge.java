/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.Network;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author scsandra
 */
public class Edge {

        private String id;
        private Node source;
        private Node destination;
        private double weight;
        private Map<Node, Integer> steq;
        
        
        public Edge(){}

        public Edge(String id, Node source, Node destination, double weight) {
                this.id = id;
                this.source = source;
                this.destination = destination;
                this.weight = weight;
                this.steq = new HashMap<>();
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

        public double getWeight() {
                return weight;
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
        
        public void setWeight(double weight){
                this.weight = weight;
        }
             
}
