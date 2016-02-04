package ND.modules.simulation.FBA.FBA;


/**
 * 
 * Class that creates the right type of Operation.
 * 
 * @author lmarmiesse 7 mars 2013
 * 
 */
public class OperationFactory {

	public Operation makeEq() {
		return new OperationEq();
	}

	public Operation makeGe() {
		return new OperationGe();
	}

	public Operation makeGt() {
		return new OperationGt();
	}

	public Operation makeLe() {
		return new OperationLe();
	}

	public Operation makeLt() {
		return new OperationLt();
	}

	public Operation makeNotEq() {
		return new OperationNotEq();
	}

	/**
	 * 
	 * Create the right type of Operation given a string.
	 * 
	 * @param s
	 *            The string to check.
	 * @return The right type of Operation.
	 */
	public Operation makeOperationFromString(String s) {

		if (s.contains("<=")) {
			return makeLe();
		} else if (s.contains(">=")) {
			return makeGe();
		} else if (s.contains("=")) {
			return makeEq();
		} else if (s.contains(">")) {
			return makeGt();
		} else if (s.contains("<")) {
			return makeLt();
		} else if (s.contains("*")) {
			return makeLt();
		}
		System.err.println("error in the interaction file");

		return null;
	}

}
