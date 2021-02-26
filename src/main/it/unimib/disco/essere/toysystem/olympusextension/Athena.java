package it.unimib.disco.essere.toysystem.olympusextension;



public class Athena extends Zeus {
    private String name = "Athena";
    private Artemis artemis = new Artemis(50, 1000);
    
    private int wisdom;
    
    @Override
    public void launchThunder(){
        
    }
    
    public Athena(int rage, int wisdom){
        super(rage);
        super.power = 80;
        this.wisdom = wisdom;
    }
    
    public void saySomethingClever(int needOfSayingSomething){
        if(needOfSayingSomething * wisdom > 1000){
            System.out.println("This is a clever sentence.");
        }
    }
    @Hermes
    public double doSomeCalculation(double x, double y){
        double power = Math.pow(x, y);
        return power;
    }
    
    
}
