/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.antNoGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author scsandra
 */
public class ReactionFA {

        private String id;
        private List<String> reactants, products;
        private HashMap<String, Double> stoichiometry;
        private double ub = 1000;
        private double lb = -1000;
        private int pheromones = 0;

        public ReactionFA(String id) {
                this.id = id;
                this.reactants = new ArrayList<>();
                this.products = new ArrayList<>();
                this.stoichiometry = new HashMap<>();
        }

        public void addReactant(String r, Double sto) {
                this.reactants.add(r);
                if (sto < 1.0) {
                        sto = 1.0;
                }
                this.stoichiometry.put(r, sto);
        }

        public void addProduct(String p, Double sto) {
                this.products.add(p);
                if (sto < 1.0) {
                        sto = 1.0;
                }
                this.stoichiometry.put(p, sto);
        }

        public Double getStoichiometry(String specie) {
                if (this.stoichiometry.containsKey(specie)) {
                        return this.stoichiometry.get(specie);
                }
                return Double.MAX_VALUE;
        }

        public List<String> getReactants() {
                return this.reactants;
        }

        public List<String> getProducts() {
                return this.products;
        }

        public void addPheromones(int number) {
                this.pheromones = this.pheromones + number;
        }

        public void removePheromones(int number) {
                this.pheromones = this.pheromones - number;
                if (pheromones < 0) {
                        this.pheromones = 0;
                }
        }

        public int getPheromones() {
                return this.pheromones;
        }

        public void setBounds(double lb, double ub) {
                this.lb = lb;
                this.ub = ub;
        }

        public String getId() {
                return this.id;
        }

        public boolean hasReactant(String node) {
                if (this.reactants.contains(node)) {
                        return true;
                }
                return false;
        }

        public boolean hasReactant(List<String> nodes) {
                for (String node : nodes) {
                        if (this.reactants.contains(node)) {
                                return true;
                        }
                }
                return false;
        }

        public double getub() {
                return this.ub;
        }

        public double getlb() {
                return this.lb;
        }

        List<String> getSources(List<String> nodes) {
                List<String> sources = new ArrayList<>();
                for (String node : nodes) {
                        if (this.reactants.contains(node) || this.products.contains(node)) {
                                sources.add(node);
                        }
                }
                return sources;
        }

        boolean hasSourcesInProducts(List<String> nodes) {
                for (String node : nodes) {
                        if (this.products.contains(node)) {
                                return true;
                        }
                }
                return false;
        }

        boolean hasSourcesInReactants(List<String> nodes) {
                for (String node : nodes) {
                        if (this.reactants.contains(node)) {
                                return true;
                        }
                }
                return false;
        }

        boolean hasSpecies(String edge) {
                if (this.reactants.contains(edge) || this.products.contains(edge)) {
                        return true;
                }
                return false;
        }

        void resetPheromones() {
                this.pheromones = 0;
        }
}
