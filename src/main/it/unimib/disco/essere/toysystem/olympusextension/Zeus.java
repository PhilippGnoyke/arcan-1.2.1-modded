package it.unimib.disco.essere.toysystem.olympusextension;

import it.unimib.disco.essere.toysystem.origin.SuperClass;

public class Zeus {
    private String name = "Zeus";
    protected int power = 100;
    private int rage;
    
    public Zeus(int rage){
        this.rage = rage;
    }
    
    protected void launchThunder(){
        System.out.println("Booom!");
    }
    
    public void talk(){
        System.out.println("Hi, I am " + name);
    }
    
    public int fight(){
        int damage = power * rage;
        return damage;
    }
    
    public void testAfference(){
        SuperClass sc = new SuperClass(1, "Zeus");
        sc.printTest();
    }
    
}
