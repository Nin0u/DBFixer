package atome;
import variable.Variable;

public class Egalite extends Atome {
    private Variable[] membres;

    public Egalite(){
        membres = new Variable[2];
    }

    Variable[] getMembres(){
        return membres;
    }

}
