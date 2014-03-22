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
public class RandomSelector {

        private List<String> reactions = new ArrayList<>();

        public void Add(String reaction, int pheromone) {
                for (int i = 0; i < pheromone + 1; i++) {
                        reactions.add(reaction);
                }
        }

        public String GetRandom(Random rand) {
                return reactions.get(rand.nextInt(reactions.size()));
        }
}
