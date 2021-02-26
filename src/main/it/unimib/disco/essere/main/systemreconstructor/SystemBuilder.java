package it.unimib.disco.essere.main.systemreconstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.bcel.classfile.JavaClass;

public abstract class SystemBuilder {
	private List<JavaClass> classes;
	private List<String> packages;

	public HashMap<String,JavaClass> getClassesHashMap() {
		HashMap<String,JavaClass> p = new HashMap<String,JavaClass>();
		for(JavaClass c : classes){
			p.put(c.getClassName(), c);
		}
		return p;
	}
	public List<JavaClass> getClasses() {
		return classes;
	}

	public List<String> getPackages() {
		return packages;
	}
	
	public HashMap<String,String> getPackagesHashMap() {
		HashMap<String,String> p = new HashMap<String,String>();
		for(String c : packages){
			p.put(c, c);
		}
		return p;
	}
	
	protected SystemBuilder() {
		classes = new ArrayList<>();
		packages = new ArrayList<>();
	}

	/**
	 * Reads all classes in the provided classpath and returns the list of
	 * classes and the list of packages of the analyzed system
	 * @param url TODO
	 */
	public abstract void readClass(String url);

}
