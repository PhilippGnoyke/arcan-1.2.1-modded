package it.unimib.disco.essere.main.asengine.udutils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;

public class UDPrinter {
    private static final Logger logger = LogManager.getLogger(UDPrinter.class);
    private UnstableDependencyDetector d;
    private PrintWriter writer;
    private File projectFolder;
    private CSVFormat formatter;
    private CSVPrinter printer;
    private String[] header = { "UnstableDependenciesPackage", "InstabilityUnstableDependenciesPackage",
            "CorrelatedPackage", "InstabilityCorrelatedPackage" };

    private String fileCSVName = "UnstableDependencies.csv";
    
    public UDPrinter(File projectFolder, UnstableDependencyDetector d) {
        try {
            this.projectFolder = projectFolder;
            this.d = d;
            writer = new PrintWriter(
                    Paths.get(this.projectFolder.getAbsolutePath(), this.fileCSVName).toAbsolutePath().toFile());

            formatter = CSVFormat.EXCEL.withHeader(header);
            printer = new CSVPrinter(writer, formatter);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    
    
    
    public UDPrinter(File projectFolder, UnstableDependencyDetector d, String fileCSVName) {
    	if(fileCSVName!=null){
    		this.fileCSVName=fileCSVName;
    	}
    	try {
            this.projectFolder = projectFolder;
            this.d = d;
            logger.debug(projectFolder);
            writer = new PrintWriter(
                    Paths.get(this.projectFolder.getAbsolutePath(), this.fileCSVName).toAbsolutePath().toFile());

            formatter = CSVFormat.EXCEL.withHeader(header);
            printer = new CSVPrinter(writer, formatter);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void print(Map<String, List<String>> smellMap) throws IOException {
        logger.debug("MAP: " + smellMap);
        if (smellMap != null) {
            Map<String, Double> instabilityMap = d.getInstabilityMap();
            for (Entry<String, List<String>> entry : smellMap.entrySet()) {
                for (String correlatedPackage : entry.getValue()) {
                    logger.debug("key: " + entry.getKey());
                    printer.print(entry.getKey());
                    printer.print(instabilityMap.get(entry.getKey()));
                    printer.print(correlatedPackage);
                    printer.print(instabilityMap.get(correlatedPackage));
                    printer.println();
                }
            }
        }else{
            logger.info("***No unstable dependency smell detected, nothing to print***");
        }
    }

    public void closeAll() throws IOException {
        printer.close();
        writer.close();
    }
}
