package ND.modules.simulation.geneticalgorithmDirections;

import ND.parameters.Parameter;
import ND.parameters.SimpleParameterSet;
import ND.parameters.parametersType.FileNameParameter;
import ND.parameters.parametersType.IntegerParameter;
import ND.parameters.parametersType.StringParameter;

public class StartSimulationDirectionsParameters extends SimpleParameterSet {

    public static final StringParameter objective = new StringParameter(
        "Objective", "Reaction that will be optimized", "");
    public static final IntegerParameter bugLife = new IntegerParameter("Life of the Bugs",
        "Minimum number of cicles that a bug can live", new Integer(300));

    public static final FileNameParameter reactions = new FileNameParameter("Reactions file",
        "File containing the reaction ids separated by comma", null);
    public static final FileNameParameter output = new FileNameParameter("Output file",
        "Output file", null);

    public StartSimulationDirectionsParameters() {
        super(new Parameter[]{objective, bugLife, reactions, output});
    }
}
