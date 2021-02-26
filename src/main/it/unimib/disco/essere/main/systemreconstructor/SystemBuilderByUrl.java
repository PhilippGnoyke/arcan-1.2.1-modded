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

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;

public class SystemBuilderByUrl extends SystemBuilder {
    private static final Logger logger = LogManager.getLogger(SystemBuilderByUrl.class);

    public SystemBuilderByUrl() {
        super();
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
                    try {

                        if ("class".equals(FilenameUtils.getExtension(filePath.toString()))) {

                            try(FileInputStream inputStream = new FileInputStream(filePath.toFile());) {
                                ClassParser cParser = new ClassParser(inputStream, filePath.getFileName().toString());
                                try {
                                    JavaClass clazz = cParser.parse();
                                    Repository.addClass(clazz);
                                    this.getClasses().add(clazz);
                                    this.getPackages().add(GraphUtils.getPackageName(clazz.getClassName()));
                                } catch (Exception e) {
                                    logger.debug(e.getMessage());;
                                }
                            } catch (IOException e) {
                                logger.debug(e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        logger.debug(e.getMessage());;
                    }
                }
            });
            stream.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }

    }

}
