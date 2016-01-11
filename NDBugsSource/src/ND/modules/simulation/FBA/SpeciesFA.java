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
        if (this.ants.isEmpty() && !this.isInside(ant)) {
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
}
