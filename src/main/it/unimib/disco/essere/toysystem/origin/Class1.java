package it.unimib.disco.essere.toysystem.origin;

public class Class1 extends Thread{
    private int a;
    private int b;
    
    public Class1(int a, int b){
        this.a = a;
        this.b = b;
    }
    
    public void testInstanceOf(SuperClass clazz){
        if(clazz instanceof ToBeReadClass){
            //do nothing
        }
    }
    
    public int sum(){
        return this.a + this.b;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
}