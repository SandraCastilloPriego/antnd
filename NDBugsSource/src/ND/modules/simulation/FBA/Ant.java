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

import ND.modules.simulation.antNoGraph.ReactionFA;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scsandra
 */
public class Ant {

    List<String> path;
    String location;
    boolean lost = false;
    int pathsize = 0;
    double flux = 0.0;

    public Ant(String location) {
        this.path = new ArrayList<>();
        this.location = location;
    }

    public void initAnt(double flux) {
        this.path.add(location);
        this.flux = flux;
    }

    public void removePath() {
        this.path.clear();
    }

    public List<String> getPath() {
        return this.path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    @Override
    public Ant clone() {
        Ant ant = new Ant(this.location);
        ant.setPath(path);
        ant.setPathSize(this.pathsize);
        ant.setFlux(this.flux);
        return ant;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;

    }

    public void setLocation(String location, ReactionFA rc) {
        this.location = location;
        this.setFlux(this.flux * rc.getStoichiometry(location));
    }

    public void print() {
        System.out.print("size: " + this.getPathSize() + " - location: " + this.location + "//");
        for (String p : this.path) {
            System.out.print(" - " + p.split(" - ")[0]);
        }
        System.out.print("\n");
    }

    public String toString() {
        String path = null;
        for (String p : this.path) {
            path += " - " + p.split(" - ")[0];
        }
        return path;
    }

    public void joinGraphs(String reactionChoosen, List<Ant> combinedAnts, ReactionFA rc) {
        for (Ant ant : combinedAnts) {
            List<String> localPath = ant.getPath();
            for (String reaction : localPath) {
                if (!this.path.contains(reaction)) {
                    this.path.add(reaction);
                    this.pathsize++;

                }
            }
        }
                
        this.pathsize++;
        this.path.add(reactionChoosen);
        if (this.getPathSize() > 500) {
            this.lost = true;
        }

    }

    public boolean isLost() {
        return this.lost;
    }

    public boolean contains(String id) {
        for(String p : this.path){
            if(p.equals(id)) return true;
        }
        return false;
    }

    public void setPathSize(int pathsize) {
        this.pathsize = pathsize;
    }

    public int getPathSize() {
        return this.pathsize;
    }

    public boolean compare(Ant ant) {
        for (String p : this.getPath()) {
            for (String op : ant.getPath()) {
                if (!p.split(" - ")[0].equals(op.split(" - ")[0])) {
                    return false;
                }
            }
        }
        return true;
    }

    public double getFlux() {
        return this.flux;
    }

    public void setFlux(double flux) {
        this.flux = flux;
    }

}
