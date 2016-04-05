
package ND.modules.simulation.geneticalgorithm.reducing;


import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.StringParameter;



public class StartReducingGAParameters extends SimpleParameterSet { 
  
        public static final StringParameter reactions = new StringParameter(
                "Tested Reactions", "Reactions to test","");
        public static final StringParameter objective = new StringParameter(
                "Objective reaction", "Reaction that will be optimized", "");
        public static final StringParameter biomass = new StringParameter(
                "Biomass reaction", "Biomass reaction", "");

        public StartReducingGAParameters() {
                super(new Parameter[]{reactions, objective, biomass});
        }
}
