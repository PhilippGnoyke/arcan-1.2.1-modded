package it.unimib.disco.essere.main.systemreconstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SystemBuilderByFolderOfJars extends SystemBuilder {
    private static final Logger logger = LogManager.getLogger(SystemBuilderByFolderOfJars.class);

    private SystemBuilderByJar jarSys;

    public SystemBuilderByFolderOfJars() {
        super();
        jarSys = new SystemBuilderByJar();
    }

    @Override
    public void readClass(String url) {
        this.getClasses().clear();
        this.getPackages().clear();
        
        Path systemPath = Paths.get(url);

        Stream<Path> stream;
        try {
            stream = Files.walk(systemPath);

            stream.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    if ("jar".equals(FilenameUtils.getExtension(filePath.toString()))) {
                        jarSys.readClass(filePath.toString());
                        this.getClasses().addAll(jarSys.getClasses());
                        this.getPackages().addAll(jarSys.getPackages());
                    }
                }
            });
            stream.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }
}
