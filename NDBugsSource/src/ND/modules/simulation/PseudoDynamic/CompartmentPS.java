/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.simulation.PseudoDynamic;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author scsandra
 */
public class CompartmentPS {
    private List<CompoundPS> compounds;
    final private String name;
    
    public CompartmentPS(String name){
        this.name = name;
        this.compounds = new ArrayList<>();
    }
    
    public void addCompound(CompoundPS compound){
        this.compounds.add(compound);
    }
    
    public boolean isThisCompoundSaturated(CompoundPS compound){
        if(compound.getPool()>1000) return true;
        return false;
    }
}
