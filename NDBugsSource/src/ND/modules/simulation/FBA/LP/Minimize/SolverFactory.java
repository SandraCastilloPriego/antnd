package ND.modules.simulation.FBA.LP.Minimize;

import ND.modules.simulation.FBA.LP.*;


public class SolverFactory
{	
	/**
	 * Create a solver capable of linear optimizations
	 * @param algorithm FBA or GDBB only
	 * @return A linear optimizer
	 * @see edu.rutgers.MOST.Analysis.FBA
	 */
	public static LinearSolver createFBASolver()
	{
		LinearSolver solver = null;
                solver = new LinearGLPKSolver();
		
		
		return solver;
	}
	
}
