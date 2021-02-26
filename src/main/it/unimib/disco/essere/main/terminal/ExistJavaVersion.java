package it.unimib.disco.essere.main.terminal;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ExistJavaVersion implements IParameterValidator {
	public void validate(final String name, final String value) throws ParameterException {
		// System.out.printf("%s %s\n",name, value); // Modded (removed)
		if ("-javaversion".equals(name) || "-jv".equals(name)) {
			if (!(JavaVersionConverter._j8.equals(value)||"8".equals(value)
					||JavaVersionConverter._j7.equals(value)||"7".equals(value)
					||JavaVersionConverter._j6.equals(value)||"6".equals(value)
					||JavaVersionConverter._j5.equals(value)||"5".equals(value))) {
				throw new ParameterException(
						"Parameter " + name + " should be a version of java (found {" + value + "})");
			} 
		} 
	}
}