package ND.modules.simulation.FBA.LP;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkCallbackListener;
import org.gnu.glpk.glp_tree;


public class LinearGLPKSolver extends GLPKSolver implements  GlpkCallbackListener
{

	public LinearGLPKSolver()
	{
		super();
	}
	
	private void fillSoln()
	{
		// get the solution columns
		soln.clear();
		int columnCount = GLPK.glp_get_num_cols( problem_tmp );
		for( int i = 1; i <= columnCount; ++i)
			// soln.add( GLPK.glp_get_col_prim( problem, i ) );
			soln.add( GLPK.glp_mip_col_val( problem_tmp, i ) );

		// objval = GLPK.glp_get_obj_val( problem );
		objval = GLPK.glp_mip_obj_val( problem_tmp );
	}

	
	@Override
	public void callback( glp_tree tree )
	{
		int reason = GLPK.glp_ios_reason( tree );
		if( aborted() )
		{
			GLPK.glp_ios_terminate( tree );
		}
		else if( reason == GLPKConstants.GLP_IBINGO )
		{
			fillSoln();
		}
	}
	
	
	@Override
	public void postCheck()
	{
		if( soln.size() == 0 )
			fillSoln();
	}

    
}
