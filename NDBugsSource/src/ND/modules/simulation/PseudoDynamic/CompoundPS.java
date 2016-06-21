/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ND.modules.simulation.PseudoDynamic;

/**
 *
 * @author scsandra
 */
public class CompoundPS {
    private double pool = 10;
    private final String compartment;
    private final String id, name;
    
    
    public CompoundPS(String id, String name, String compartment){
         this.id = id;
         this.name = name;
         this.compartment = compartment;
    }

    public double getPool() {
        return this.pool;
    }

    String getId() {
        return this.id;
    }
    
    public void setPool(double pool){
        this.pool = pool;
    }

    String getName() {
       return this.name;
    }

    String getCompartment() {
       return this.compartment;
    }
}
