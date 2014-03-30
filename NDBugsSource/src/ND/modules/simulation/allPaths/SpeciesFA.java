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
package ND.modules.simulation.allPaths;

import ND.modules.simulation.antNoGraph.Ant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author scsandra
 */
public class SpeciesFA {

        private String id;
        private List<String> reactions;
        List<Ant> ant;

        public SpeciesFA(String id) {
                this.id = id;
                this.reactions = new ArrayList<>();
                this.ant = new ArrayList();
        }

        public Ant getAnt(Random rand) {
                return sortestAnt();
        }

        public void addAnt(Ant ant) {
                if (!isHere(ant)) {
                                               
                        this.ant.add(ant);
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

        private boolean isHere(Ant ant) {
                for (Ant a : this.ant) {
                        if (a.compare(ant)) {
                                return true;
                        }
                }
                return false;
        }

        private Ant sortestAnt() {
                Ant shortest = null;
                int size = Integer.MAX_VALUE;
                for(Ant a : this.ant){
                        if(a.getPathSize() < size){
                                shortest = a;
                                size = a.getPathSize();
                        }
                }
                return shortest;
        }
}
