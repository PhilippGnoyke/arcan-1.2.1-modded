package it.unimib.disco.essere.main.systemreconstructor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import it.unimib.disco.essere.main.ETLE;
import it.unimib.disco.essere.main.ExTimeLogger;
import it.unimib.disco.essere.main.graphmanager.ClassFilter;
import org.apache.bcel.util.Repository;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimib.disco.essere.main.graphmanager.GraphUtils;

public class SystemBuilderByJar extends SystemBuilder
{
    private static final Logger logger = LogManager.getLogger(SystemBuilderByJar.class);

    public SystemBuilderByJar()
    {
        super();
    }

    public SystemBuilderByJar(ClassFilter classFilter, ExTimeLogger exTimeLogger, Repository repo)
    {
        super(classFilter, exTimeLogger, repo);
    }

    @Override
    public void readClass(String url)
    {

        this.getClasses().clear();
        this.getPackages().clear();

        try
        {


            exTimeLogger.logEventStart(ETLE.Event.READ_READ_BYTES);
            InputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(url)));
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            byte[] data = new byte[8192];  // 8 KB buffer
            int bytesRead;
            while ((bytesRead = in.read(data, 0, data.length)) != -1)
            {
                buffer.write(data, 0, bytesRead);
            }

            byte[] jarBytes = buffer.toByteArray();

            //byte[] jarBytes = Files.readAllBytes(Paths.get(url));
            exTimeLogger.logEventEnd(ETLE.Event.READ_READ_BYTES);
            exTimeLogger.logEventStart(ETLE.Event.READ_INIT_IN_STREAMS);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(jarBytes);
                 JarInputStream jis = new JarInputStream(bais))
            {
                JarEntry entry;
                exTimeLogger.logEventEnd(ETLE.Event.READ_INIT_IN_STREAMS);

                while ((entry = getNextJarEntry(jis)) != null)
                {
                    if (!entry.isDirectory() && entry.getName().endsWith(".class"))
                    {
                        exTimeLogger.logEventStart(ETLE.Event.READ_INIT_OUT_STREAMS);
                        logger.debug("Parsing class from entry: " + entry.getName());

                        ByteArrayOutputStream classOut = new ByteArrayOutputStream();
                        byte[] bufferOut = new byte[4096];
                        int len;
                        while ((len = jis.read(bufferOut)) != -1)
                        {
                            classOut.write(bufferOut, 0, len);
                        }

                        byte[] classBytes = classOut.toByteArray();
                        exTimeLogger.logEventEnd(ETLE.Event.READ_INIT_OUT_STREAMS);
                        exTimeLogger.logEventStart(ETLE.Event.READ_PARSE_CLASS);
                        try (ByteArrayInputStream classInput = new ByteArrayInputStream(classBytes))
                        {
                            ClassParser cParser = new ClassParser(classInput, entry.getName());
                            JavaClass clazz = cParser.parse();
                            String className = clazz.getClassName();
                            exTimeLogger.logEventEnd(ETLE.Event.READ_PARSE_CLASS);
                            if (classFilter == null || classFilter.isSharedClass(className))
                            {
                                exTimeLogger.logEventStart(ETLE.Event.READ_ADD_TO_REPO);
                                repo.storeClass(clazz);
                                exTimeLogger.logEventEnd(ETLE.Event.READ_ADD_TO_REPO);
                                exTimeLogger.logEventStart(ETLE.Event.READ_ADD_TO_LISTS);
                                this.getClasses().add(clazz);
                                this.getPackages().add(GraphUtils.getPackageName(className));
                                exTimeLogger.logEventEnd(ETLE.Event.READ_ADD_TO_LISTS);
                            }
                            else
                            {
                                exTimeLogger.logEventStart(ETLE.Event.READ_ADD_TO_LISTS);
                                this.getExtClasses().add(className);
                                this.getExtPackages().add(GraphUtils.getPackageName(className));
                                exTimeLogger.logEventEnd(ETLE.Event.READ_ADD_TO_LISTS);
                            }
                        }
                    }
                }
            }
        } catch (IOException e)
        {
            logger.error("Error reading JAR file: " + e.getMessage());
        }
    }

    //Modded
    private JarEntry getNextJarEntry(JarInputStream jis) throws IOException
    {
        exTimeLogger.logEventStart(ETLE.Event.READ_GET_JAR_ENTRY);
        JarEntry entry = jis.getNextJarEntry();
        exTimeLogger.logEventEnd(ETLE.Event.READ_GET_JAR_ENTRY);
        return entry;
    }

}