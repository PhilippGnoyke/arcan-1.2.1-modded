package it.unimib.disco.essere.main.graphmanager;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class ClassFilter
{
    private Set<String> sharedClasses;
    private Set<String> sharedPackages;

    public ClassFilter(String filePath) throws IOException
    {
        getSharedClasses(filePath);
    }

    private void getSharedClasses(String filePath) throws IOException
    {
        String header = "fullyQualifiedName";
        CSVParser records = CSVFormat.DEFAULT.withIgnoreHeaderCase().
            withHeader(header).withFirstRecordAsHeader().parse(new FileReader(filePath));
        sharedClasses = new HashSet<>((int) records.getRecordNumber() * 2);
        sharedPackages = new HashSet<>((int) records.getRecordNumber());
        for (CSVRecord record : records)
        {
            String className = record.get(header);
            sharedClasses.add(className);
            int lastDot = className.lastIndexOf('.');
            String packageName = (lastDot != -1) ? className.substring(0, lastDot) : "";
            sharedPackages.add(packageName);
        }
    }

    public boolean isSharedClass(String fullyQualifiedName)
    {
        return sharedClasses.contains(fullyQualifiedName);
    }

    public boolean isSharedPackage(String fullyQualifiedPName)
    {
        return sharedPackages.contains(fullyQualifiedPName);
    }

    public boolean isSharedComponent(String fullyQualifiedName)
    {
        return isSharedClass(fullyQualifiedName) || isSharedPackage(fullyQualifiedName);
    }

}
