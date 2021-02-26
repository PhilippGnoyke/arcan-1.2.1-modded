package it.unimib.disco.essere.test;

public class regexPackageTest {
    public static void main(String [] args){
        String s = "Ljavax/naming/InitialContext";
        
        int slashIndex = s.lastIndexOf('/');
        String result = s.substring(1, slashIndex);
        String result2 = result.replace('/', '.');
        System.out.println(result2);
        
        String className = s.substring(slashIndex + 1);
        System.out.println(className);

    }
    
    
}
