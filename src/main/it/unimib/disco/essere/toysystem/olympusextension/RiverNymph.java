package it.unimib.disco.essere.toysystem.olympusextension;

public class RiverNymph implements Nymph {
    private Athena athena = new Athena(4, 500);
    
    public void call(){
        athena.doSomeCalculation(2, 2);
    }
}
