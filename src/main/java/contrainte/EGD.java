package contrainte;

import java.sql.ResultSet;
import java.util.ArrayList;

import atome.*;

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
}
