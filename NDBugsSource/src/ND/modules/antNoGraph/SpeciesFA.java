/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.antNoGraph;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scsandra
 */
class SpeciesFA {

        private String id;
        private List<String> reactions;
        private List<Ant> ants;
        private int counter = 0;
        
        public SpeciesFA(String id) {
                this.id = id;
                this.reactions = new ArrayList<>();
                this.ants = new ArrayList<>();
        }

        public Ant getAnt() {
                if (this.ants.size() > 0) {
                        return getShortest();
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
        
        public int getCount(){
                return this.counter;
        }
        
        public void addCount(){
                this.counter++;
                if(counter > 10){
                        this.removeAnts();
                        this.counter = 0;
                }
        }
      
        public void removeCount(){
                this.counter = 0;
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

        public void removeAnts() {
                this.ants.clear();
        }

        void removeAnt(Ant a) {
                this.ants.remove(a);
        }

        private Ant getShortest() {
                int shortest = Integer.MAX_VALUE;
                Ant ant = null;
                for(Ant a : this.ants){
                        if(a.getPathSize() < shortest){
                                shortest = a.getPathSize();
                                ant = a;
                        }
                }
                return ant;
        }
}
