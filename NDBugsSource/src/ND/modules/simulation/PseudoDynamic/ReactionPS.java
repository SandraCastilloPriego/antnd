package ND.modules.simulation.PseudoDynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author scsandra
 */
public class ReactionPS {

    private double fluxForward, fluxBackwards;
    private final List<CompoundPS> reactants;
    private final List<CompoundPS> products;
    private final Map<CompoundPS, Double> stoichiometry;
    double lb, ub;
    private final String id, name;

    public ReactionPS(String id, String name) {
        this.fluxForward = 0;
        this.fluxBackwards = 0;
        this.id = id;
        this.name = name;
        reactants = new ArrayList<>();
        products = new ArrayList<>();
        stoichiometry = new HashMap<>();
    }

    void setBounds(double lb, double ub) {
        this.lb = lb;
        this.ub = ub;
    }

    public void addReactant(CompoundPS reactant) {
        this.reactants.add(reactant);
    }

    public void setStoichiometry(CompoundPS compound, Double sto) {
        this.stoichiometry.put(compound, sto);
    }

    public void addProduct(CompoundPS product) {
        this.products.add(product);
    }

    List<CompoundPS> getReactants() {
        return this.reactants;
    }

    boolean isPossible(CompoundPS cps) {
        if (lb == 0 && !this.reactants.contains(cps)) {
            return false;
        }
        if (ub == 0 && !this.products.contains(cps)) {
            return false;
        }

        boolean direction = getDirection();
        if (direction == true && !this.reactants.contains(cps)) {
            return false;
        }
        if (direction == false && !this.products.contains(cps)) {
            return false;
        }

        if (direction == true && (this.getReactantsPool() <= 0 || this.getProductsPool() > 1000)) {
            return false;
        }
        if (direction == false && (this.getProductsPool() <= 0 || this.getReactantsPool() > 1000)) {
            return false;
        }

        return true;
    }

    boolean getDirection() {
        if (lb == 0 && ub > 0) {
            return true;
        } else if (ub == 0 && lb < 0) {
            return false;
        }
        double poolProducts = getProductsPool();
        double poolReactants = getReactantsPool();
        if (poolReactants >= poolProducts) {
            return true;
        } else {
            return false;
        }
    }

    public double getProductsPool() {
        double pool = 0;
        for (CompoundPS c : this.products) {
            if (c.getPool() <= 0) {
                return 0;
            } else {
                pool += c.getPool();
            }
        }
        return pool;
    }

    public double getReactantsPool() {
        double pool = 0;
        for (CompoundPS c : this.reactants) {
            if (c.getPool() <= 0) {
                return 0;
            } else {
                pool += c.getPool();
            }
        }
        return pool;
    }

    boolean contains(CompoundPS compound) {
        for (CompoundPS c : this.products) {
            if (c.equals(compound)) {
                return true;
            }
        }

        for (CompoundPS c : this.reactants) {
            if (c.equals(compound)) {
                return true;
            }
        }
        return false;
    }

    void update() {
        if (this.reactants.size() == 1) {
            if ((this.name.contains("transport") || this.name.contains("uniport") || this.name.contains("diffusion"))
                && !this.reactants.get(0).getCompartment().contains("extracellular") 
                && !this.products.get(0).getCompartment().contains("extracellular")) {
                updateTransporter();
                return;
            }
        }

        boolean direction = this.getDirection();

        // Check no pool is 0
        boolean possible = true;
        if (direction == true) {
            for (CompoundPS ps : this.reactants) {
                if (ps.getPool() < this.stoichiometry.get(ps)) {
                    possible = false;
                }
            }

            if (possible) {
                for (CompoundPS ps : this.reactants) {
                    ps.setPool(ps.getPool() - this.stoichiometry.get(ps));
                }

                for (CompoundPS ps : this.products) {
                    ps.setPool(ps.getPool() + this.stoichiometry.get(ps));
                }
                this.fluxForward++;
            }
        }

        if (direction == false) {
            for (CompoundPS ps : this.products) {
                if (ps.getPool() < this.stoichiometry.get(ps)) {
                    possible = false;
                }
            }

            if (possible) {
                for (CompoundPS ps : this.products) {
                    ps.setPool(ps.getPool() - this.stoichiometry.get(ps));
                }

                for (CompoundPS ps : this.reactants) {
                    ps.setPool(ps.getPool() + this.stoichiometry.get(ps));
                }
                this.fluxBackwards++;
            }
        }
    }

    String getId() {
        return this.id;
    }

    Double getFFlux() {
        return this.fluxForward;
    }

    Double getBFlux() {
        return this.fluxBackwards;
    }

    String getName() {
        return this.name;
    }

    private void updateTransporter() {
        boolean direction = this.getDirection();
        double amount = 0;
        for (CompoundPS ps : this.reactants) {
            double pool = 0;
            if (ps.getPool() > 0) {
                pool = ps.getPool();
            }
            amount += pool;
        }
        for (CompoundPS ps : this.products) {
            double pool = 0;
            if (ps.getPool() > 0) {
                pool = ps.getPool();
            }
            amount += pool;
        }
        amount /= 2;

        if (direction == true) {
            for (CompoundPS ps : this.reactants) {
                ps.setPool(ps.getPool() - amount);
            }

            for (CompoundPS ps : this.products) {
                ps.setPool(ps.getPool() + amount);
            }
            this.fluxForward += amount;

        }

        if (direction == false) {

            for (CompoundPS ps : this.products) {
                ps.setPool(ps.getPool() - amount);
            }

            for (CompoundPS ps : this.reactants) {
                ps.setPool(ps.getPool() + amount);
            }
            this.fluxBackwards += amount;

        }
    }

    private double getUpperLimitProducts() {
        double limit = 0;
        for (CompoundPS c : this.products) {
            if (!c.getId().equals("s_1438")
                && !c.getId().equals("s_1468")
                && !c.getId().equals("s_0796")
                && !c.getId().equals("s_1277")
                && !c.getId().equals("s_1374")
                && !c.getId().equals("s_1324")
                && !c.getId().equals("s_0925")
                && !c.getId().equals("s_0420")) {
                if (c.getPool() > limit) {
                    limit = c.getPool();
                }
            }
        }
        return limit;
    }

    private double getUpperLimitReactants() {
        double limit = 0;
        for (CompoundPS c : this.reactants) {
            if (!c.getId().equals("s_1438")
                && !c.getId().equals("s_1468")
                && !c.getId().equals("s_0796")
                && !c.getId().equals("s_1277")
                && !c.getId().equals("s_1374")
                && !c.getId().equals("s_1324")
                && !c.getId().equals("s_0925")
                && !c.getId().equals("s_0420")) {
                if (c.getPool() > limit) {
                    limit = c.getPool();
                }
            }
        }
        return limit;
    }

}
