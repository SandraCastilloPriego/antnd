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

        public Ant() {
                this.path = new ArrayList<>();
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
                Ant ant = new Ant();
                for (String p : path) {
                        ant.addReactionInPath(p);
                }
                return ant;
        }
}
