package it.unimib.disco.essere.main.terminal;

import java.io.File;
import java.nio.file.Paths;

import com.beust.jcommander.IStringConverter;

public class FileConverter implements IStringConverter<File> {
	@Override
	public File convert(final String value) {
		return Paths.get(value).toFile();
	}
}