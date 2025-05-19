package it.unimib.disco.essere.main.systemreconstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import it.unimib.disco.essere.main.ETLE;
import it.unimib.disco.essere.main.ExTimeLogger;
import it.unimib.disco.essere.main.graphmanager.ClassFilter;
import org.apache.bcel.util.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SystemBuilderByFolderOfJars extends SystemBuilder {
    private static final Logger logger = LogManager.getLogger(SystemBuilderByFolderOfJars.class);

    private SystemBuilderByJar jarSys;

    public SystemBuilderByFolderOfJars(ClassFilter classFilter, ExTimeLogger exTimeLogger, Repository repo) {
        super(classFilter,exTimeLogger,repo);
        jarSys = new SystemBuilderByJar(classFilter, exTimeLogger,repo);
    }

    public SystemBuilderByFolderOfJars() {
        super();
        jarSys = new SystemBuilderByJar();
    }

    @Override
    public void readClass(String url) {
        exTimeLogger.logEventStart(ETLE.Event.READ_WALK_JARS);
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
                        this.getExtClasses().addAll(jarSys.getExtClasses());
                        this.getExtPackages().addAll(jarSys.getExtPackages());
                    }
                }
            });
            stream.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
        exTimeLogger.logEventEnd(ETLE.Event.READ_WALK_JARS);
        ETLE.Event[] toBeSubtracted = new ETLE.Event[]{
            ETLE.Event.READ_READ_BYTES,
            ETLE.Event.READ_INIT_IN_STREAMS,
            ETLE.Event.READ_GET_JAR_ENTRY,
            ETLE.Event.READ_INIT_OUT_STREAMS,
            ETLE.Event.READ_PARSE_CLASS,
            ETLE.Event.READ_ADD_TO_REPO,
            ETLE.Event.READ_ADD_TO_LISTS};
        exTimeLogger.subtractEventsFromEvent(ETLE.Event.READ_WALK_JARS,toBeSubtracted);
    }
}
