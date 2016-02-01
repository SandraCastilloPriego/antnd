/*
 * Copyright 2007-2013 VTT Biotechnology
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sandra Castillo <Sandra.castillo@vtt.fi>
 */
public class FluxNode {

    Map<String, Double> flux;
    String id;
    double outReactions;

    FluxNode(String id, String reaction, Double flux) {
        this.flux = new HashMap<>();
        this.flux.put(reaction, flux);
        this.id = id;
        this.outReactions= 1;
    }
    
    public void setOutReactions(double outReactions){
        this.outReactions = outReactions;
    }

    void setFlux(String reaction, double bound) {
        if (this.flux.containsKey(reaction)) {
            if (this.flux.get(reaction) > bound) {
                this.flux.put(reaction, bound);
            }
        } else {
            this.flux.put(reaction, bound);
        }
    }


    double getFlux() {
        double Flux = 0;
        for(String reaction : flux.keySet()){
            Flux+=this.flux.get(reaction);
        }
        Flux = Flux / this.outReactions;
        return Flux ;
    }   
   
}
