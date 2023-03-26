package atome;
import variable.Attribut;

public class Egalite {
    /** Un égalité contient 2 membres qui sont soit des attributs soit des constantes */
    private Attribut[] membres;

    /** Constructeur */
    public Egalite(Attribut var1, Attribut var2){
        membres = new Attribut[2];
        membres[0] = var1;
        membres[1] = var2;
    }

    /** Getter */
    public Attribut[] getMembres() {
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
