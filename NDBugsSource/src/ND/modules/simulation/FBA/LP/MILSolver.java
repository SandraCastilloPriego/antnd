package ND.modules.simulation.FBA.LP;



public interface MILSolver extends LinearSolver
{
	/**
	 * Returns the SolverComponent
	 * @return The SolverComponent
	 * @see SolverComponent
	 */
	public abstract SolverComponent getSolverComponent();
	
	public abstract void postCheck();
}
