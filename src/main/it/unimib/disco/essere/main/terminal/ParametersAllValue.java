package it.unimib.disco.essere.main.terminal;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

@Parameters(commandDescription="Architectural smells detection")
public class ParametersAllValue {
	@Parameter( names="all" ,description = "Calculates all metrics and all type of architectural smells")
	public boolean _all = false;

}
