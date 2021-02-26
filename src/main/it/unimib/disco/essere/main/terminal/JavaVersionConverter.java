package it.unimib.disco.essere.main.terminal;

import java.io.File;
import java.nio.file.Paths;

import com.beust.jcommander.IStringConverter;

public class JavaVersionConverter implements IStringConverter<String> {
	public final static String _j8 = "1.8";
	public final static String _j7 = "1.7";
	public final static String _j6 = "1.6";
	public final static String _j5 = "1.5";
	@Override
	public String convert(final String value) {
		if("8".equals(value)||_j8.equals(value)){
			return _j8;
		}else if("7".equals(value)||_j7.equals(value)){
			return _j7;
		}else if("6".equals(value)||_j6.equals(value)){
			return _j6;
		}else if("5".equals(value)||_j5.equals(value)){
			return _j5;
		}else 
		return _j8;
	}
}