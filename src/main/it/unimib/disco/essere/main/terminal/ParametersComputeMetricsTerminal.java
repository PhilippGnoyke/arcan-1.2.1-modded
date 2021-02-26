package it.unimib.disco.essere.main.terminal;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;


public class ParametersComputeMetricsTerminal {
	@Parameter(names = { "-PackageMetrics", "-PM", "-pm" }, description = "Compute Robert C. Martin metrics on packages", descriptionKey="metrics")
	public boolean _PackageMetrics = false;

	@Parameter(names = { "-ClassMetrics", "-CM", "-cm" }, description = "Compute the metrics on classes", descriptionKey="metrics")
	public boolean _ClassMetrics = false;
}
