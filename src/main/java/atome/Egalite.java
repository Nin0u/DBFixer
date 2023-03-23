package atome;
import variable.Variable;

public class Egalite {
    /** Un égalité contient 2 membres qui sont soit des attributs soit des constantes */
    private Variable[] membres;

    /** Constructeur */
    public Egalite(Variable var1, Variable var2){
        membres = new Variable[2];
        membres[0] = var1;
        membres[1] = var2;
    }

    /** Getter */
    Variable[] getMembres(){
        return membres;
    }

    /** Méthode d'afichage */
    public void affiche(){
        System.out.println("-- Egalite --");
        membres[0].affiche();
        membres[1].affiche();

        System.out.println("-- Fin --");
    }
}
