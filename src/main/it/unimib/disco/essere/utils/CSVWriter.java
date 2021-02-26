package it.unimib.disco.essere.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CSVWriter {
   /* public static void main(String args[]) {
       SystemBuilder sys = new SystemBuilderByJar(
                "C:/Users/Ilaria/Downloads/quartz-1.8.6/quartz-all-1.8.6.jar");

        sys.readClass();
        List<String> myLines = new ArrayList<>();
        for(JavaClass clazz : sys.getClasses()){
            myLines.add(clazz.getClassName());
        }

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("C:/Users/Ilaria/Desktop/infusionClasses.csv"));

            List<String> infusionLines = new ArrayList<>();
            String line = null;

            while ((line = reader.readLine()) != null) {
                infusionLines.add(line);
            }
            infusionLines = CSVFormatter(infusionLines);
            
            List<String> comparison = new ArrayList<>();
            List<String> comparison2 = new ArrayList<>();
              
            comparison = compare(myLines, infusionLines);
            comparison2 = compare(infusionLines, myLines);
            
            writeOnCSV(infusionLines, "C:/Users/Ilaria/Desktop/infusionClassesModified.csv");
            writeOnCSV(myLines, "C:/Users/Ilaria/Desktop/MyClassesModified.csv");
            writeOnCSV(comparison,"C:/Users/Ilaria/Desktop/Comparison.csv");
            writeOnCSV(comparison2,"C:/Users/Ilaria/Desktop/Comparison2.csv");
            
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
       
    }*/
    
    public static List<String> CSVFormatter(List<String> rows){
        List<String> lines = new ArrayList<>();
        for(String r : rows){
            int lastIndex = r.lastIndexOf('/');
            r = r.substring(lastIndex + 1);
            lines.add(r);
        }
        return lines;
    }
    
    public static void writeOnCSV(List<String> rows, String file){
        try {
            PrintWriter pw = new PrintWriter(new File(file));
            for(String r : rows){
                pw.write(r + "\n");
            }
            pw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static List<String> compare(List<String> myLines, List<String> otherLines){
        List<String> comparison = new ArrayList<>();
        for(String s : myLines){
            if(!otherLines.contains(s)){
                comparison.add(s);
            }
        }
        return comparison;
    }
 
}
