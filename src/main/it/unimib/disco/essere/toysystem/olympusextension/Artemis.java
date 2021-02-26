package it.unimib.disco.essere.toysystem.olympusextension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Artemis extends Zeus {
    private String name = "Artemis";
    private int power = 75;
    private int rage;
    private int wildness;
    private RiverNymph nymph = new RiverNymph();

    public Artemis(int rage, int wildness) {
        super(rage);
        this.wildness = wildness;
    }

    public void shotArrows(int[] arrows, String target) {
        name = "CambioNome";
        PrintWriter pw;
        try {
            pw = new PrintWriter(new File(target));

            for (int arrowDamage : arrows) {
                pw.write(arrowDamage + "\n");
            }
            pw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
