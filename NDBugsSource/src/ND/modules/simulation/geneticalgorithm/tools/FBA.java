package ND.modules.simulation.geneticalgorithm.tools;

import ND.modules.simulation.FBA.LP.LinearSolver;
import ND.modules.simulation.FBA.LP.Solver;
import ND.modules.simulation.FBA.LP.SolverFactory;


public class FBA extends Analysis
{
	protected LinearSolver linearSolver = SolverFactory.createFBASolver();
	
	public FBA()
	{
		super();
	}
	
	public void disableSolverErrors()
	{
		linearSolver.disableErrors();
	}
	
	//@Override
 	/*public ArrayList< Double > run() throws Exception
 	{
		
		try
		{			
	 		
	 		this.setSolverParameters();
	 		this.maxObj = linearSolver.optimize();
	 		
	 		
	 		ArrayList< Double > soln = linearSolver.getSoln();
	 		return soln;
		}
		catch( Exception e )
		{
			throw e;
		}
 	}
*/
	public void solve() throws Exception
	{
		this.setSolverParameters();
		this.maxObj = linearSolver.optimize();
	}
	
	@Override
	public Solver getSolver()
	{
		return linearSolver;
	}
}
