package it.unimib.disco.essere.main.systemreconstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimib.disco.essere.main.graphmanager.GraphUtils;

public class SystemBuilderByJar extends SystemBuilder {
    private static final Logger logger = LogManager.getLogger(SystemBuilderByJar.class);

    public SystemBuilderByJar() {
        super();
    }

    @Override
    public void readClass(String url) {
        this.getClasses().clear();
        this.getPackages().clear();
        
        JarFile jarFile;
        try {
            jarFile = new JarFile(url);
            logger.debug("jar file "+jarFile.getName()+" size: "+jarFile.size());
            ClassParser cParser = null;
            InputStream inputStream = null;

            Enumeration<JarEntry> jarEnum = jarFile.entries();

            while (jarEnum.hasMoreElements()) {
                JarEntry entry = jarEnum.nextElement();
                logger.debug("jar entry "+entry);
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    try {
                        inputStream = jarFile.getInputStream(entry);
                        cParser = new ClassParser(inputStream, entry.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (cParser != null) {
                        JavaClass clazz = cParser.parse();
                        Repository.addClass(clazz);
                        this.getClasses().add(clazz);
                        this.getPackages().add(GraphUtils.getPackageName(clazz.getClassName()));
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
