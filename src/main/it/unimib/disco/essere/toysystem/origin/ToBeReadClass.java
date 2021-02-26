package it.unimib.disco.essere.toysystem.origin;

public class ToBeReadClass extends SuperClass{
   
    
    public ToBeReadClass(){
        super(5,"string of ToBeReadClass");
    }
    
    public int sumMethodTest(int a, int b){
        //Class1 instanceClass1 = new Class1(2,3);
        //return instanceClass1.sum();
        return new Class1(2,3).sum();
        
    }
    
    @Override
    public void printTest(){
        super.printTest();
        System.out.println("Hello from ToBeReadClass too!");
    }
    
}
