package contrainte;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import atome.*;
import pied.Database;

public class EGD extends Contrainte {

    /** Constructeur */
    public EGD(ArrayList<Relation> rlCorps, ArrayList<Egalite> egCorps, ArrayList<Egalite> egTete){
        super(rlCorps,egCorps,egTete);
    }

    /** 
     * Egaliser les tuples de T en accord avec la contrainte this
     * @param T Un tuple qui satisfait le corps mais pas la tête de e
     */
    public void action(ResultSet T){
        // TODO
    }

    public void affiche(){
        System.out.println("===========  EGD ==============");
        System.out.println("---- Corps ----");
        for (Relation r : rlCorps)
            r.affiche();

        for(Egalite e : egCorps)
            e.affiche();

        System.out.println("---- Tete ----");
        for (Egalite e : egTete)
            e.affiche(); 

        System.out.println("=========== FIN EGD ==============");
    }
}
