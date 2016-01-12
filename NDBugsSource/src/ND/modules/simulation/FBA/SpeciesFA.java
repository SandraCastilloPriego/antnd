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
package ND.modules.simulation.FBA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author scsandra
 */
public class SpeciesFA {

    private final String id;
    private final List<String> reactions;
    private List<Ant> ants;
    private double pool = 0;
    private final String name;

    public SpeciesFA(String id, String name) {
        this.ants = new ArrayList<>();
        this.id = id;
        this.name = name;
        this.reactions = new ArrayList<>();
    }

    public Ant getAnt() {

        if (!ants.isEmpty()) {
            return ants.get(0);
        }
        return null;
    }

    public List<Ant> getAnts() {
        return ants;
    }

    public String getName() {
        return this.name;
    }

    public void addAnt(Ant ant) {
        if (!this.isInside(ant)) {
            if (ants.size() < 30) {
                this.ants.add(ant);
            } else {
                Collections.sort(ants, new Comparator<Ant>() {
                    public int compare(Ant o1, Ant o2) {
                        return o1.getPathSize() < o2.getPathSize() ? -1 : o1.getPathSize() > o2.getPathSize() ? 1 : 0;
                    }
                });
                int size = this.ants.get(this.ants.size() - 1).getPathSize();
                if (size > ant.getPathSize()) {
                    this.ants.set(this.ants.size() - 1, ant);
                }
            }
        }
    }

    public void addSingleAnt(Ant ant) {
        if (this.ants.isEmpty() || this.ants.get(0).getPathSize() > ant.getPathSize()) {
            this.ants.clear();
            this.ants.add(ant);
        }
    }

    public void addReaction(String id) {
        this.reactions.add(id);
    }

    public String getId() {
        return this.id;
    }

    public List<String> getReactions() {
        return this.reactions;
    }

    public void clearAnts() {
        this.ants.clear();
    }

    public double getPool() {
        return this.pool;
    }

    public void setPool(double pool) {
        this.pool = pool;
    }

    private boolean isInside(Ant ant) {
        for (Ant a : ants) {
            if (a.getPathSize() == ant.getPathSize()) {
                if (a.toString().equals(ant.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> combinePahts() {
        List<String> combined = new ArrayList<>();
        for(Ant ant: ants){
            for(String path: ant.getPath()){
                if(!combined.contains(path)){
                    combined.add(path);
                }
            }
            
        }
        return combined;
    }
}
