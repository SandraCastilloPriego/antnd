package ND.modules.simulation.FBA.FBA;


import java.util.HashMap;
import java.util.Map;

import parsebionet.biodata.BioEntity;

/**
 * 
 * This class represents a constraints for the problem. A constraint represents
 * a linear equation. It is composed of entities, their coefficients, a lower
 * bound and an upper bound.
 * 
 * @author lmarmiesse 6 mars 2013
 * 
 */
public class Constraint {

	/**
	 * map containing entities and their coefficients.
	 */

	protected Map<BioEntity, Double> entities;
	/**
	 * Lower bound.
	 */
	protected Double lb;
	/**
	 * Upper bound.
	 */
	protected Double ub;

	/**
	 * If it is true, the map must not be equal to lb.
	 */
	protected boolean not = false;

	/**
	 * 
	 * Creates a constraint;
	 * 
	 * @param entities
	 *            Entities with their coefficients.
	 * @param lb
	 *            Lower bound.
	 * @param ub
	 *            Upper bound.
	 */
	public Constraint(Map<BioEntity, Double> entities, Double lb, Double ub) {

		if (lb <= ub) {
			this.entities = entities;
			this.lb = lb;
			this.ub = ub;
		} else {
			this.entities = entities;
			this.lb = lb;
			this.ub = lb;
		}

	}
	
	public Constraint(BioEntity ent, Double lb, Double ub) {

		Map<BioEntity, Double> entitiesMap  = new HashMap<BioEntity, Double>();
		entitiesMap.put(ent, 1.0);
		
		if (lb <= ub) {
			this.entities = entitiesMap;
			this.lb = lb;
			this.ub = ub;
		} else {
			this.entities = entitiesMap;
			this.lb = lb;
			this.ub = lb;
		}

	}

	public boolean getNot() {
		return not;
	}
//
//	public void setOverWritesBounds(boolean b) {
//		this.overWritesBounds = b;
//	}
//
//	public boolean getOverWritesBounds() {
//		return overWritesBounds;
//	}

	/**
	 * Creates an inequality constraint.
	 * 
	 * @param entities
	 *            Entities and their coefficients.
	 * @param lb
	 *            Lower and Upper bound.
	 * @param not
	 *            Determines if the contraint in an inequality.
	 */
	public Constraint(Map<BioEntity, Double> entities, Double lb, boolean not) {

		this.entities = entities;
		this.lb = lb;
		this.ub = lb;
		this.not = not;

	}

	/**
	 * 
	 * @return Entities and their coefficients.
	 */
	public Map<BioEntity, Double> getEntities() {
		return entities;

	}

	/**
	 * 
	 * @return Entities's names and their coefficients.
	 */
	public Map<String, Double> getEntityNames() {
		Map<String, Double> names = new HashMap<String, Double>();

		for (BioEntity ent : entities.keySet()) {

			names.put(ent.getId(), entities.get(ent));
		}

		return names;

	}

	public double getLb() {
		return lb;
	}

	public double getUb() {
		return ub;
	}

	public void setLb(double lb) {
		this.lb = lb;
	}

	public void setUb(double ub) {
		this.ub = ub;
	}

	public String toString() {

		String result = "";

		if (not) {
			result += "NOT : ";
		}

		result += lb + " <= ";

		for (BioEntity entity : entities.keySet()) {
			result += entities.get(entity) + " " + entity.getId() + " + ";
		}

		return result.subSequence(0, result.length() - 3) + " <= " + ub;

	}
	
}
