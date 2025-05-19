package it.unimib.disco.essere.main.systemreconstructor;

import java.util.*;

import it.unimib.disco.essere.main.ExTimeLogger;
import it.unimib.disco.essere.main.graphmanager.ClassFilter;
import org.apache.bcel.util.Repository;
import org.apache.bcel.classfile.JavaClass;

public abstract class SystemBuilder {
	private List<JavaClass> classes;
	private List<String> packages;
	private Set<String> extClasses;
	private Set<String> extPackages;
	protected Repository repo;
	protected ExTimeLogger exTimeLogger;


	protected ClassFilter classFilter;

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

	public Set<String> getExtClasses() {
		return extClasses;
	}

	public Set<String> getExtPackages() {
		return extPackages;
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
		extClasses = new HashSet<>();
		extPackages = new HashSet<>();
	}

	protected SystemBuilder(ClassFilter classFilter, ExTimeLogger exTimeLogger, Repository repo) {
		this();
		this.classFilter = classFilter;
		this.exTimeLogger = exTimeLogger;
		this.repo = repo;
	}

	/**
	 * Reads all classes in the provided classpath and returns the list of
	 * classes and the list of packages of the analyzed system
	 * @param url TODO
	 */
	public abstract void readClass(String url);

}
