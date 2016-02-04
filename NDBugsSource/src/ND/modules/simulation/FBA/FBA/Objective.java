package ND.modules.simulation.FBA.FBA;


import parsebionet.biodata.BioEntity;

/**
 * 
 * This class represents the objective function of the FBA.
 * 
 * @author lmarmiesse 11 mars 2013
 * 
 */

public class Objective {

	/**
	 * 
	 * Name of the objective function.
	 * 
	 */
	private String name = "Objective value";

	/**
	 * 
	 * If true, it maximizes the function, if false it minimizes.
	 * 
	 */
	private boolean maximize;

	/**
	 * 
	 * Entities composing the function.
	 * 
	 */
	BioEntity[] entities;

	/**
	 * 
	 * Their coefficients.
	 * 
	 */
	double[] coeffs;

	public Objective() {
		maximize = true;
		entities = new BioEntity[0];
		coeffs = new double[0];
	}

	public Objective(BioEntity[] entities, double[] coeffs, String name,
			boolean maximize) {
		this.name = name;
		this.coeffs = coeffs;
		this.maximize = maximize;
		this.entities = entities;

	}

	public String getName() {
		return name;
	}

	public boolean getMaximize() {
		return maximize;
	}

	public BioEntity[] getEntities() {
		return entities;
	}

	public double[] getCoeffs() {
		return coeffs;
	}

	public String toString() {

		String res = "";

		if (maximize) {
			res += "maximize : ";
		} else {
			res += "minimize : ";
		}

		for (int i = 0; i < entities.length; i++) {

			res += coeffs[i] + "*" + entities[i].getId() + " ";

		}

		return res;

	}

}
