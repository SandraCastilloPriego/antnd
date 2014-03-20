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
public class Ant {

        List<String> path;
        String location;
       // boolean moved = false;
        public Ant(String location) {
                this.path = new ArrayList<>();
                this.location = location;
        }

        public void addReactionInPath(String reaction) {
                if (!this.path.contains(reaction)) {
                        this.path.add(reaction);
                }
        }

        public void removePath() {
                this.path.clear();
        }

        public List<String> getPath() {
                return this.path;
        }

        @Override
        public Ant clone() {
                Ant ant = new Ant(this.location);
                for (String p : this.path) {
                        ant.addReactionInPath(p);
                }
                return ant;
        }
        
        public String getLocation(){
                return location;
        }
        
        public void setLocation(String location){
                this.location = location;
        }
        
       /* public boolean isMoved(){
                return this.moved;
        }
        
        public void setMoved(boolean moved){
                this.moved = moved;
        }
        */
       
}
