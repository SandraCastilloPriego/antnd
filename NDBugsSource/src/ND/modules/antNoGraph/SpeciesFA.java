/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.antNoGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author scsandra
 */
class SpeciesFA {

        private String id;
        private List<String> reactions;
        private List<Ant> ants;

        public SpeciesFA(String id) {
                this.id = id;
                this.reactions = new ArrayList<>();
                this.ants = new ArrayList<>();
        }

        public Ant getAnt(Random rand) {
                if (this.ants.size() > 0) {
                        Ant selected = this.ants.get(rand.nextInt(this.ants.size()));
                        this.ants.remove(selected);
                        return selected;
                } else {
                        return null;
                }
        }

        public void addAnt(Ant ant) {
                this.ants.add(ant);
        }

        public List<Ant> getAnts() {
                return this.ants;
        }

        public int getNumberOfAnts() {
                return this.ants.size();
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
}
