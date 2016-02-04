package ND.modules.simulation.FBA.FBA;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parsebionet.biodata.BioEntity;

/**
 * 
 * Class representing the operation "Equal to" : =
 * 
 * 
 * @author lmarmiesse 7 mars 2013
 * 
 */
public class OperationEq extends Operation {

	public String toString() {

		return (" = ");
	}
	
	public String toFormula() {

		return (" == ");
	}

	public List<Constraint> makeConstraint(BioEntity entity, double value) {

		List<Constraint> constraints = new ArrayList<Constraint>();

		Map<BioEntity, Double> constraintMap = new HashMap<BioEntity, Double>();
		constraintMap.put(entity, 1.0);
		Constraint c = new Constraint(constraintMap, value, value);
		constraints.add(c);

		return constraints;

	}

	public boolean isTrue(Constraint cons, double value) {

		return cons.getLb() == value && cons.getUb() == value;
	}

	public boolean isInverseTrue(Constraint cons, double value) {
		
		if (cons.getLb() > value && cons.getUb() > value){
			return true;
		}
		else if (cons.getLb() < value && cons.getUb() < value){
			return true;
		}
		
		return false;
	}

}
