package ND.modules.simulation.FBA.FBA;


public class Vars {

	
	public static Boolean verbose=false;

	
	
	
	/**
	 * Maximum lower bound and upper bounds
	 */	
	public static double minLowerBound = -999999;
	public static double maxUpperBound = +999999;
	
	/**
	 * Maximum number of threads created.
	 */
	public static int maxThread = Runtime.getRuntime().availableProcessors()/2;

	/**
	 * Determines if FlexFlux uses epsilon.
	 */
	public static boolean cheat = true;
	
	
	/**
	 * Value used to approximate inequalities.
	 */
	public static double epsilon = 1e-10;

	/**
	 * Keyword for the sum of all fluxes.
	 */
	public static String FluxSumKeyWord = "FluxSum";

	public static String absolute = "FlexFluxAbs";

	/**
	 * Percentage of liberty for constraints created by objective functions.
	 */
	public static double libertyPercentage = 0;

	/**
	 * Number of decimals of precision of the calculations.
	 */
	public static int decimalPrecision = 6;
	
	/**
	 * Maximal number of iterations to find a steady state in the interaction network.
	 */
	public static int steadyStatesIterations = 100;
	
	
	/**
	 * Whether or not the calculated interaction network steady states must be saved to a file;
	 */
	public static boolean writeInteractionNetworkStates = false;
	

	/**
	 * 
	 * Rounds number to the decimal precision.
	 * 
	 * @param value
	 *            Initial value.
	 * @return The rounded value.
	 */
	static public double round(double value) {
		if(Double.isNaN(value)){
			return Double.NaN;
		}
		
		double r = (Math.round(value * Math.pow(10, decimalPrecision)))
				/ (Math.pow(10, decimalPrecision));
		return r;
	}

}
