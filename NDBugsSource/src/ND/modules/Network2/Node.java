/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.Network2;

import org.sbml.jsbml.Species;

/**
 *
 * @author scsandra
 */
public class Node {

      
        private double weight;
        private Species s;
        private int ants;

        public Node(Species s) {
                this.s = s;
        }

        public void setWeight(double weight) {
                this.weight = weight;
        }

        public String getId() {
                return s.getId();
        }

        public String getName() {
                return s.getName();
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
}
