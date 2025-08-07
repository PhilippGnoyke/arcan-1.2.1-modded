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
        for (CSVRecord record : records)
        {
            sharedClasses.add(record.get(header));
        }
    }

    public boolean isSharedClass(String fullyQualifiedName)
    {
        return sharedClasses.contains(fullyQualifiedName);
    }


}
