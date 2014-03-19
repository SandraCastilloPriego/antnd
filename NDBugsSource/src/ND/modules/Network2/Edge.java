/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.Network2;

import java.util.List;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
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
        private Reaction r;
        private double feromone = 1;
        private int ants;

        public Edge(Reaction r) {
                this.id = r.getId();
                this.r = r;
        }

        public Edge(String id, List<Node> source, List<Double> sourceAmount, List<Node> destination, List<Double> destinationAmount, double weight) {
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

        public boolean isReversible() {
                return this.r.getReversible();
        }

        boolean contains(String node) {
                KineticLaw k = r.getKineticLaw();
                LocalParameter lP = k.getLocalParameter("LOWER_BOUND");
                LocalParameter uP = k.getLocalParameter("UPPER_BOUND");
                if (uP.getValue() > 0.0) {
                        for (SpeciesReference s : r.getListOfReactants()) {
                                if (s.getSpecies().equals(node)) {
                                        return true;
                                }
                        }
                }

                if (lP.getValue() < 0.0) {
                        for (SpeciesReference s : r.getListOfProducts()) {
                                if (s.getSpecies().equals(node)) {
                                        return true;
                                }
                        }
                }
                return false;
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

        public boolean isInProducts(String nodeId) {
                for (SpeciesReference s : this.r.getListOfProducts()) {
                        if (s.getSpeciesInstance().getId() == null ? nodeId == null : s.getSpecies().equals(nodeId)) {
                                return true;
                        }
                }
                return false;
        }

        public ListOf<SpeciesReference> getProducts() {
                return r.getListOfProducts();
        }

        public ListOf<SpeciesReference> getSubstrates() {
                return r.getListOfReactants();
        }

        public double getNumProducts() {
                return r.getNumProducts();
        }

        public double getNumSubstrates() {
                return r.getNumReactants();
        }

        public String getName() {
                return r.getName();
        }
}
