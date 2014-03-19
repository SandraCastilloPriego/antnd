/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.Network3;

import java.util.ArrayList;
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
public class Node {

      
        private double weight;
        private Reaction r;
        private int ants;

        public Node(Reaction r) {
                this.r = r;
        }

        public void setWeight(double weight) {
                this.weight = weight;
        }

        public String getId() {
                return r.getId();
        }

        public String getName() {
                return r.getName();
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((this.getId() == null) ? 0 : this.getId().hashCode());
                return result;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj) {
                        return true;
                }
                if (obj == null) {
                        return false;
                }
                if (getClass() != obj.getClass()) {
                        return false;
                }
                Node other = (Node) obj;
                if (this.getId() == null) {
                        if (other.getId() != null) {
                                return false;
                        }
                } else if (!this.getId().equals(other.getId())) {
                        return false;
                }
                return true;
        }

        @Override
        public String toString() {
                return this.getName();
        }

        public double getWeight() {
                return weight;
        }

        public int getAnts(){
                return this.ants;
        }

        public void reduceAnts() {
                this.ants--;
        }
        
        public void augmentAnts(){
                this.ants++;
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

        public List<Species> getPossibleEdges() {
                List<Species> edges = new ArrayList<>();
                KineticLaw k = r.getKineticLaw();
                LocalParameter lP = k.getLocalParameter("LOWER_BOUND");
                LocalParameter uP = k.getLocalParameter("UPPER_BOUND");
                if (uP.getValue() > 0.0) {
                        for (SpeciesReference s : r.getListOfReactants()) {
                                edges.add(s.getSpeciesInstance());
                        }                        
                }
                if (lP.getValue() < 0.0) {
                        for (SpeciesReference s : r.getListOfProducts()) {
                                 edges.add(s.getSpeciesInstance());
                        }
                }
                return edges;
        }

}
