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
        private HashMap<String, Double> stoReactants, stoProducts;
        private double ub = 1000;
        private double lb = -1000;
        private int pheromones = 0;

        public ReactionFA(String id) {
                this.id = id;
                this.reactants = new ArrayList<>();
                this.products = new ArrayList<>();
                this.stoReactants = new HashMap<>();
                this.stoProducts = new HashMap<>();
        }

        public void addReactant(String r, Double sto) {
                this.reactants.add(r);
                if(sto < 1.0) sto = 1.0;
                this.stoReactants.put(r, sto);
        }

        public void addProduct(String p, Double sto) {
                this.products.add(p);
                if(sto < 1.0) sto = 1.0;
                this.stoProducts.put(p, sto);
        }

        public Double getStoichiometryReactant(String reactant) {
                return this.stoReactants.get(reactant);
        }

        public Double getStoichiometryProduct(String product) {
                return this.stoProducts.get(product);
        }

        public Double getStoichiometry(String specie) {
                if (this.stoReactants.containsKey(specie)) {
                        return this.stoReactants.get(specie);
                } else {
                        return this.stoProducts.get(specie);
                }
        }

        public List<String> getReactants() {
                return this.reactants;
        }

        public List<String> getProducts() {
                return this.products;
        }

        public void addPheromones() {
                this.pheromones++;
        }

        public void removePheromones() {
                if (this.pheromones > 0) {
                        this.pheromones--;
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

        boolean hasSpecies(String edge) {
                if (this.reactants.contains(edge) || this.products.contains(edge)) {
                        return true;
                }
                return false;
        }
}
