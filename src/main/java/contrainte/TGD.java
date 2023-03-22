package contrainte;
import java.sql.ResultSet;

public class TGD extends Contrainte {
    public TGD(){
        super();
    }

    /**
     * Ajoute un nouveau tuple u tq db union u satisfait e
     * @param T Un tuple issus d'une requête SQL qui satisfait le corps mais pas la tête de e
     */
    public void action(ResultSet T){

    }
}
