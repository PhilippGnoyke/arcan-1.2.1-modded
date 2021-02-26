package it.unimib.disco.essere.main.terminal;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;


public class ParametersDetectionArchitecturalSmell {
	@Parameter(names = { "-CycleDependency", "-CD", "-cd" }, description = "Calculates the class and package cycles of the graph. If the graph is already written and it is specified the project folder it will be read from the db folder.", descriptionKey="as")
	public boolean _cycle = false;

	@Parameter(names = { "-UnstableDependencies", "-UD", "-ud" }, description = "Search for Unstable dependencies", descriptionKey="as")
	public boolean _UnstableDependencies = false;

	@Parameter(names = { "-HubLikeDependencies", "-HL", "-hl" }, description = "Search for Hub-Like dependencies", descriptionKey="as")
	public boolean _HubLikeDependencies = false;

//	@ParametersDelegate
//	static ParametersComputeMetricsTerminal _parMetrics = ParametersComputeMetricsTerminal.getInstance();
}
