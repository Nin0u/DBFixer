package contrainte;

import java.sql.ResultSet;
import java.util.ArrayList;
import atome.*;

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
    public void action(ResultSet T){
        // TODO
    }
}
