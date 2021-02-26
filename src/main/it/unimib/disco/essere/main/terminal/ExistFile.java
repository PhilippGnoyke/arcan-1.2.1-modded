package it.unimib.disco.essere.main.terminal;

import java.io.File;
import java.nio.file.Paths;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ExistFile implements IParameterValidator {
	public void validate(final String name, final String value) throws ParameterException {
		//System.out.printf("%s %s\n",name, value); // Modded (removed)
		if ("-projectFolder".equals(name) || "-p".equals(name)) {
//			File n = Paths.get(value.substring(0, value.length() - 2)).toFile();
			File n = Paths.get(value).toFile();
			if (!n.exists()) {
				throw new ParameterException(
						"Parameter " + name + " should be a real folder (found " + value + ")");
			}
			if (!n.canWrite()) {
				throw new ParameterException(
						"Parameter " + name + " you don't have the rights to write in this folder (found " + value + ")");
			}
			if (!n.canRead()) {
				throw new ParameterException(
						"Parameter " + name + " you don't have the rights to read in this folder (found " + value + ")");
			}
		} else if ("-classpathFolder".equals(name) || "-c".equals(name)) {
			if (value != null) {
				File n = Paths.get(value).toFile();
				if (!n.exists()) {
					throw new ParameterException(
							"Parameter " + name + " should be a real folder (found " + value + ")");
				}
				if (!n.canWrite()) {
					throw new ParameterException(
							"Parameter " + name + " you don't have the rights to write in this folder (found " + value + ")");
				}
				if (!n.canRead()) {
					throw new ParameterException(
							"Parameter " + name + " you don't have the rights to read in this folder (found " + value + ")");
				}
			}
		} else if ("-libraryFolder".equals(name) || "-l".equals(name)) {
			if (value != null) {
				File n = Paths.get(value).toFile();
				if (!n.exists()) {
					throw new ParameterException(
							"Parameter " + name + " should be a real folder (found " + value + ")");
				}
				if (!n.canWrite()) {
					throw new ParameterException(
							"Parameter " + name + " you don't have the rights to write in this folder (found " + value + ")");
				}
				if (!n.canRead()) {
					throw new ParameterException(
							"Parameter " + name + " you don't have the rights to read in this folder (found " + value + ")");
				}
			}
		} else if ("-gitFolder".equals(name) || "-g".equals(name)) {
			if (value != null) {
				File n = Paths.get(value).toFile();
				if (!n.exists()) {
					throw new ParameterException(
							"Parameter " + name + " should be a real folder (found " + value + ")");
				}
				if (!n.canWrite()) {
					throw new ParameterException(
							"Parameter " + name + " you don't have the rights to write in this folder (found " + value + ")");
				}
				if (!n.canRead()) {
					throw new ParameterException(
							"Parameter " + name + " you don't have the rights to read in this folder (found " + value + ")");
				}
			}
		}else if ("-outputDir".equals(name) || "-out".equals(name)) {
			if (value != null) {
				File n = Paths.get(value).toFile();
				if (n.exists()&&!n.canWrite()) {
					throw new ParameterException(
							"Parameter " + name + " you don't have the rights to write in this folder (found " + value + ")");
				}
			}
		}
	}
}