/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.Network3;


import java.util.List;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 *
 * @author scsandra
 */
public class Edge {

        private String id;
        private List<Node> source;
        private List<Node> destination;
        private double weight;
        private Species s;
        private double feromone = 1;
        private int ants;

        public Edge(Species s) {
                this.id = s.getId();
                this.s = s;
        }

        public Edge(String id, List<Node> source, List<Double> sourceAmount, List<Node> destination, double weight) {
                this.id = id;
                this.source = source;
                this.destination = destination;
                this.weight = weight;
        }

        public String getId() {
                return id;
        }

        public List<Node> getDestination() {
                return destination;
        }

        public List<Node> getSource() {
                return source;
        }

        public double getWeight() {
                return weight;
        }

        @Override
        public String toString() {
                return source + " " + destination;
        }

        public void setSource(Node source) {
                this.source.add(source);
        }

        public void setDestination(Node destination) {
                this.destination.add(destination);
        }

        public void setId(String id) {
                this.id = id;
        }

        public void setWeight(double weight) {
                this.weight = weight;
        }

        
        public void addAnt() {
                this.ants++;
               // this.feromone++;
        }

        public void cycle() {
                this.feromone--;
        }

        public double getFeromoneLevel() {
                return this.feromone;
        }

        public int getAnts() {
                return this.ants;
        }

        
}
