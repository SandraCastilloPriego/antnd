package ND.modules.simulation.FBA.FBA;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parsebionet.biodata.BioEntity;

/**
 * 
 * 
 * Class representing the operation "Not equal to" : !=
 * 
 * @author lmarmiesse 25 avr. 2013
 * 
 */
public class OperationNotEq extends Operation {

	public String toString() {
		return (" != ");
	}

	public String toFormula() {

		return (" != ");
	}

	public boolean isTrue(Constraint cons, double value) {
		if (cons.getLb() > value && cons.getUb() > value) {
			return true;
		} else if (cons.getLb() < value && cons.getUb() < value) {
			return true;
		}

		return false;
	}

	public boolean isInverseTrue(Constraint cons, double value) {
		return cons.getLb() == value && cons.getUb() == value;
	}

	public List<Constraint> makeConstraint(BioEntity entity, double value) {

		List<Constraint> constraints = new ArrayList<Constraint>();

		// Map<BioEntity, Double> constraintMap = new HashMap<BioEntity,
		// Double>();
		// constraintMap.put(entity, 1.0);
		// if (Vars.cheat) {
		//
		// Constraint c = new Constraint(constraintMap, -Double.MAX_VALUE,
		// value - Vars.epsilon);
		// c.setLazy(lazy);
		// constraints.add(c);
		//
		// } else {
		// Constraint c = new Constraint(constraintMap, -Double.MAX_VALUE,
		// value);
		// c.setLazy(lazy);
		// constraints.add(c);
		//
		// Constraint c2 = new Constraint(constraintMap, value, true);
		// c2.setLazy(lazy);
		// constraints.add(c2);
		//
		// }

		Map<BioEntity, Double> constraintMap = new HashMap<BioEntity, Double>();
		constraintMap.put(entity, 1.0);

		if (Vars.cheat) {

			Constraint c = new Constraint(constraintMap, value + Vars.epsilon,
					Double.MAX_VALUE);
			constraints.add(c);

		} else {
			Constraint c = new Constraint(constraintMap, value,
					Double.MAX_VALUE);
			constraints.add(c);
			Constraint c2 = new Constraint(constraintMap, value, true);
			constraints.add(c2);

		}

		return constraints;
	}

	

}
