/*
 * Copyright 2007-2016 VTT Biotechnology
 * This file is part of AntND.
 *
 * AntND is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * AntND is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * AntND; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.modules.simulation.FBAMinimize;

/**
 *
 * @author scsandra
 */
import ND.modules.simulation.FBA.LP.Minimize.Analysism;
import ND.modules.simulation.FBA.LP.Minimize.LinearSolver;
import ND.modules.simulation.FBA.LP.Minimize.Solver;
import ND.modules.simulation.FBA.LP.Minimize.SolverFactory;


public class FBAm extends Analysism
{
	protected LinearSolver linearSolver = SolverFactory.createFBASolver();
	
	public FBAm()
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