package contrainte;

import java.sql.ResultSet;
import java.util.ArrayList;
import atome.*;
import pied.Database;

public class TGD extends Contrainte {
    private ArrayList<Relation> rlTete;

    /** Constructeur */
    public TGD(ArrayList<Relation> rlCorps, ArrayList<Egalite> egCorps, ArrayList<Egalite> egTete, ArrayList<Relation> rlTete){
        super(rlCorps, egCorps, egTete);
        this.rlTete = rlTete;
    }

    /** Getter */
    public ArrayList<Relation> getRelTete(){
        return rlTete;
    }

    /**
     * Ajoute un nouveau tuple u tq db union u satisfait e
     * @param T Un tuple qui satisfait le corps mais pas la tête de e
     */
    public int action(String req, Database db){
        // TODO
        return -1;
    }

    public void affiche(){
        System.out.println("===========  TGD ==============");
        System.out.println("---- Corps ----");
        for (Relation r : rlCorps)
            r.affiche();

        for(Egalite e : egCorps)
            e.affiche();

        System.out.println("---- Tete ----");
        for (Relation r : rlTete)
            r.affiche();

        for (Egalite e : egTete)
            e.affiche(); 

        System.out.println("=========== FIN TGD ==============");
    }
}
