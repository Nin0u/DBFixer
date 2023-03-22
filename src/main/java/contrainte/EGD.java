package contrainte;
import java.sql.ResultSet;

public class EGD extends Contrainte {
    public EGD(){
        super();
    }

    /** 
     * Egaliser les tuples de T en accord avec la contrainte this
     * @param T Un tuple issus d'une requête SQL qui satisfait le corps mais pas la tête de e
     */
    public void action(ResultSet T){
        // TODO
    }
}
