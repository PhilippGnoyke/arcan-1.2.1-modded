package it.unimib.disco.essere.main.terminal;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class PositiveInteger implements IParameterValidator {
	public void validate(String name, String value) throws ParameterException {
		int n = Integer.parseInt(value);
		if (n < 0) {
			throw new ParameterException("Parameter " + name + " should be positive (found " + value + ")");
		}
	}
}