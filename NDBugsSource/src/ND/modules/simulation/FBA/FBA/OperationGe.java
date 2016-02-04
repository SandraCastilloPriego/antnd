package ND.modules.simulation.FBA.FBA;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import parsebionet.biodata.BioEntity;

/**
 * 
 * Class representing the operation "Greater or equal to" : >=
 * 
 * @author lmarmiesse 7 mars 2013
 * 
 */
public class OperationGe extends Operation {

	public String toString() {
		return " >= ";
	}
	
	public String toFormula() {

		return (" >= ");
	}

	public List<Constraint> makeConstraint(BioEntity entity, double value) {

		List<Constraint> constraints = new ArrayList<Constraint>();

		Map<BioEntity, Double> constraintMap = new HashMap<BioEntity, Double>();
		constraintMap.put(entity, 1.0);
		Constraint c = new Constraint(constraintMap, value, Double.MAX_VALUE);
		constraints.add(c);

		return constraints;

	}

	public boolean isTrue(Constraint cons, double value) {

		return cons.getLb() >= value;
		
	}

	public boolean isInverseTrue(Constraint cons, double value) {
		
		return cons.getUb() < value;
	}


}
