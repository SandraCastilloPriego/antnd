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
