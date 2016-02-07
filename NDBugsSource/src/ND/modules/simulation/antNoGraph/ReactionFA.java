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
package ND.modules.simulation.antNoGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author scsandra
 */
public class ReactionFA {

    private final String id;
    private final List<String> reactants, products;
    private final Map<String, String> names;
    private final HashMap<String, Double> stoichiometry;
    private double ub = 1000;
    private double lb = -1000;
    private int pheromones = 0;
    private Map<String, Double> flux;

    public ReactionFA(String id) {
        this.id = id;
        this.reactants = new ArrayList<>();
        this.products = new ArrayList<>();
        this.stoichiometry = new HashMap<>();
        this.names = new HashMap<>();
        this.flux = new HashMap<>();
    }

    public void addReactant(String r, Double sto) {
        this.reactants.add(r);
        /*  if (sto < 1.0) {
         sto = 1.0;
         }*/
        this.stoichiometry.put(r, sto);
    }

    public void addReactant(String r, String name, Double sto) {
        this.reactants.add(r);
        /*   if (sto < 1.0) {
         sto = 1.0;
         }*/
        this.stoichiometry.put(r, sto);
        this.names.put(r, name);
    }

    public void addProduct(String p, Double sto) {
        this.products.add(p);
        /*  if (sto < 1.0) {
         sto = 1.0;
         }*/
        this.stoichiometry.put(p, sto);
    }

    public void addProduct(String p, String name, Double sto) {
        this.products.add(p);
        /* if (sto < 1.0) {
         sto = 1.0;
         }*/
        this.stoichiometry.put(p, sto);
        this.names.put(p, name);
    }

//        public double getFlux(){
//            return flux;
//        }
//        
//       public void setFlux(double flux){
//           this.flux = flux;
//       }
    public Double getStoichiometry(String specie) {
        if (this.stoichiometry.containsKey(specie)) {
            return this.stoichiometry.get(specie);
        }
        return 0.0;
    }

    public void setName(String specie, String name) {
        this.names.put(specie, name);
    }

    public String getName(String specie) {
        return this.names.get(specie);
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
        if (lb < -1000) {
            this.lb = -1000;
        } else {
            this.lb = lb;
        }
        if (ub > 1000) {
            this.ub = 1000;
        } else {
            this.ub = ub;
        }
    }

    public String getId() {
        return this.id;
    }

    public boolean hasReactant(String node) {
        return this.reactants.contains(node);
    }

    public boolean hasProduct(String node) {
        return this.products.contains(node);
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
        return this.reactants.contains(edge) || this.products.contains(edge);
    }

    public void resetPheromones() {
        this.pheromones = 0;
    }

    public void setFlux(String specie, Double flux) {
      // if ((this.flux.containsKey(specie) && this.flux.get(specie) < flux && this.flux.get(specie)>=0.0) || !this.flux.containsKey(specie)) {
            this.flux.put(specie, flux);
      // }
    }

    public double getFlux() {
        double min = Double.MAX_VALUE;
        for (String sp : this.flux.keySet()) {
            Double spflux = this.flux.get(sp);
            if (spflux != -1 && spflux < min) {
                min = spflux;
            }
        }
        return min;
    }
}
