
package ND.modules.simulation.geneticalgorithm.testing;


import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.StringParameter;



public class StartTestingGAParameters extends SimpleParameterSet { 
  
        public static final StringParameter reactions = new StringParameter(
                "Tested Reactions", "Reactions to test","");

        public StartTestingGAParameters() {
                super(new Parameter[]{reactions});
        }
}
