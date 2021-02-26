package it.unimib.disco.essere.toysystem.origin;
import org.apache.bcel.Repository;

public class SuperClass extends Repository{
    private int i;
    private String s;
    
    public SuperClass(int i, String s){
        this.i = i;
        this.s = s;
    }
    
    public void printTest(){
        System.out.println("Hello BCEL from SuperClass");
    }
    
    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }


    

}
