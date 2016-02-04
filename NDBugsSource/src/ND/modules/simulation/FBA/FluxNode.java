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
    List<ReactionFA> outReactions;
    List<ReactionFA> inReactions;

    FluxNode(String id, String reaction, Double flux) {
        this.flux = new HashMap<>();
        this.flux.put(reaction, flux);
        this.id = id;
        ReactionFA initReaction = new ReactionFA(reaction);
        initReaction.addProduct(id, 1.0);
        initReaction.setFlux(flux);

        this.outReactions = new ArrayList<>();
        this.inReactions = new ArrayList<>();
        this.inReactions.add(initReaction);
    }

    FluxNode(String id) {
        this.flux = new HashMap<>();
        this.id = id;
        this.outReactions = new ArrayList<>();
        this.inReactions = new ArrayList<>();
    }

    public void setOutReactions(List<ReactionFA> outReactions) {
        for (ReactionFA r : outReactions) {
            this.outReactions.add(r);
        }
    }

    public void setOutReactions(ReactionFA outReaction) {
        this.outReactions.add(outReaction);
    }

    public List<ReactionFA> getOutReactions() {
        return this.outReactions;
    }

    public void setInReactions(List<ReactionFA> inReactions) {
        for (ReactionFA r : inReactions) {
            this.inReactions.add(r);
        }
    }

    public void setInReactions(ReactionFA inReaction) {
        this.inReactions.add(inReaction);
    }

    /* void setFlux(String reaction, double flux) {
     if (this.flux.containsKey(reaction)) {
     if (this.flux.get(reaction) > flux) {
     this.flux.put(reaction, flux);
     }
     } else {
     this.flux.put(reaction, flux);
     }
     }*/
    void updateFlux(boolean verbose) {
        double Flux = -1.0;
        double sumFlux = this.getFlux();

        double outFlux = 0.0;

        for (ReactionFA r : this.outReactions) {
            outFlux += Math.abs(r.getStoichiometry(id));
        }
        for (ReactionFA r : this.outReactions) {
            Flux = (sumFlux / outFlux) * r.getStoichiometry(id);
            if (Flux > 0.0 && (r.getFlux() > Flux || r.getFlux() < 0)) {
                r.setFlux(Flux);
            }
        }
        if (verbose) {

            System.out.print(this.id + ":" + this.inReactions.size() + " / " + this.outReactions.size() + " : " + Flux + "  -->");
            for (ReactionFA r : this.inReactions) {
                System.out.print(r.getId() + ">>" + r.getFlux() + " ,");
            }
            System.out.print(" <-- ");
            for (ReactionFA r : this.outReactions) {
                System.out.print(r.getId() + ">>" + r.getFlux() + ",");
            }
            System.out.print("\n");
        }
    }

    double getFlux() {
        double sumFlux = 0.0;

        for (ReactionFA r : this.inReactions) {
            if (r.getFlux() > 0.0) {
                sumFlux += r.getFlux() * Math.abs(r.getStoichiometry(id));
            } else {
                return -1.0;
            }
        }
        return sumFlux;

    }
//    double getFlux(String outReaction) {
//        double Flux = -1.0;
//        double sumFlux = 0.0;
//        ReactionFA outR = null;
//        if (FluxContainsAllInReactions()) {
//            for (String f : flux.keySet()) {
//                if (flux.get(f) > -1) {
//                    if (getInReaction(f) == null) {
//                        sumFlux += flux.get(f);
//                    } else {
//                        sumFlux += flux.get(f) * Math.abs(getInReaction(f).getStoichiometry(id));
//                    }
//                }
//            }
//            if (!flux.containsKey("cofactor")) {
//                double outFlux = 0.0;
//
//                for (ReactionFA r : this.outReactions) {
//                    outFlux += Math.abs(r.getStoichiometry(id));
//                    // outFlux++;
//                    if (r.getId().equals(outReaction)) {
//                        outR = r;
//                    }
//                }
//                if (outR != null) {
//                    Flux = (sumFlux / outFlux) /* outR.getStoichiometry(id)*/;
//                } else {
//                    Flux = -2;
//                }
//            }
//        }
//
//        if (flux.containsKey("cofactor")) {
//            return sumFlux;
//        }
//        /*  System.out.print(this.id + ":" + this.inReactions.size() + " / " + this.outReactions.size() + " : " + Flux + "  -->");
//         for (ReactionFA r : this.inReactions) {
//         System.out.print(r.getId() + ",");
//         }
//         System.out.print(" <-- ");
//         for (ReactionFA r : this.outReactions) {
//         System.out.print(r.getId() + ",");
//         }
//         System.out.print("\n");*/
//        return Flux;
//        // return sumFlux;
//    }
//
//    double getFlux() {
//        double Flux = -1.0;
//        if (FluxContainsAllInReactions()) {
//            Flux = 0.0;
//            for (String f : flux.keySet()) {
//                try {
//                    Flux += flux.get(f) * Math.abs(getInReaction(f).getStoichiometry(id));
//                } catch (Exception e) {
//                    Flux += flux.get(f);
//                }
//            }
//        }
//        return Flux;
//    }

    private ReactionFA getInReaction(String f) {
        for (ReactionFA inReaction : this.inReactions) {
            if (inReaction.getId().equals(f)) {
                return inReaction;
            }
        }
        return null;
    }

    private boolean FluxContainsAllInReactions() {
        if (flux.containsKey("cofactor")) {
            return true;
        }
        for (ReactionFA r : this.inReactions) {
            if (this.flux.get(r.getId()) == null /*|| this.flux.get(r.getId()) <= 0.0*/) {
                return false;
            }
        }
        return true;
    }

    /* void print() {
     System.out.print(this.id + ":" + this.inReactions.size() + " / " + this.outReactions.size() + " : " + this.getFlux() + "  -->");
     for (ReactionFA r : this.inReactions) {
     System.out.print(r.getId() + ",");
     }
     System.out.print(" <-- ");
     for (ReactionFA r : this.outReactions) {
     System.out.print(r.getId() + ",");
     }
     System.out.print("\n");
     }*/
}
