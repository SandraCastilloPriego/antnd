package ND.modules.simulation.FBA.FBA;



import java.util.List;

import parsebionet.biodata.BioEntity;

/**
 * 
 * Superclass of all operations.
 * 
 * @author lmarmiesse 7 mars 2013
 * 
 */
public abstract class Operation {

	public abstract String toString();

	/**
	 * 
	 * Make constraint from an entity and a value.
	 * 
	 * <p>
	 * Only used when interactions are not in the solver.
	 * 
	 * @param entity
	 *            The entity concerned.
	 * @param value
	 *            The value to use.
	 * @return A list of constraints.
	 */
	public abstract List<Constraint> makeConstraint(BioEntity entity,
			double value);

	/**
	 * 
	 * Checks if the combination of the constraint, the operation and the value
	 * is true or not.
	 * 
	 * <p>
	 * Only used when interactions are not in the solver.
	 * 
	 * @param cons
	 *            The constraint to check.
	 * @param value
	 *            The value to check.
	 * @return If the combination is true.
	 */
	public abstract boolean isTrue(Constraint cons, double value);

	public abstract boolean isInverseTrue(Constraint cons, double value);

	public abstract String toFormula();

}
