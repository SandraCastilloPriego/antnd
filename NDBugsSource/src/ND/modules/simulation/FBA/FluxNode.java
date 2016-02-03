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

import ND.modules.simulation.antNoGraph.ReactionFA;
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
    List<ReactionFA> outReactions;
    List<ReactionFA> inReactions;

    FluxNode(String id, String reaction, Double flux) {
        this.flux = new HashMap<>();
        this.flux.put(reaction, flux);
        this.id = id;
    }

    FluxNode(String id) {
        this.flux = new HashMap<>();
        this.id = id;
    }

    public void setOutReactions(List<ReactionFA> outReactions) {
        this.outReactions = outReactions;
    }

    public void setInReactions(List<ReactionFA> inReactions) {
        this.inReactions = inReactions;
    }

    void setFlux(String reaction, double flux) {
        if (this.flux.containsKey(reaction)) {
            if (this.flux.get(reaction) > flux) {
                this.flux.put(reaction, flux);
            }
        } else {
            this.flux.put(reaction, flux);
        }
    }

    double getFlux(String outReaction) {
        double Flux = -1.0;
        ReactionFA outR = null;
        if (FluxContainsAllInReactions()) {
            double sumFlux = 0.0;
            for (String f : flux.keySet()) {
                //System.out.println(f + " - " + id);
                if (getInReaction(f) == null) {
                    sumFlux += flux.get(f);
                } else {
                    sumFlux += flux.get(f) * getInReaction(f).getStoichiometry(id);
                }
            }
            double outFlux = 0.0;

            for (ReactionFA r : this.outReactions) {
                outFlux += r.getStoichiometry(id);
                if (r.getId().equals(outReaction)) {
                    outR = r;
                }
            }
            if (outR != null) {
                Flux = (sumFlux / outFlux) * outR.getStoichiometry(id);
            } else {
                Flux = -2;
            }
        }
        System.out.println(this.id +":"+ this.inReactions.size()+ " / "+ this.outReactions.size()+ " : "+ Flux);
        return Flux;
    }

    double getFlux() {
        double Flux = -1.0;
        if (flux.size() == this.inReactions.size()) {
            Flux = 0.0;
            for (String f : flux.keySet()) {
                Flux += flux.get(f) * getInReaction(f).getStoichiometry(id);
            }
        }
        return Flux;
    }

    private ReactionFA getInReaction(String f) {
        for (ReactionFA inReaction : this.inReactions) {
            if (inReaction.getId().equals(f)) {
                return inReaction;
            }
        }
        return null;
    }

    private boolean FluxContainsAllInReactions() {
        for (ReactionFA r : this.inReactions) {
            if (this.flux.get(r.getId()) == null || this.flux.get(r.getId()) <= 0.0) {
                return false;
            }
        }
        return true;
    }

}
