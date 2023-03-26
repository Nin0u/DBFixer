package variable;

public class Constante extends Attribut{
    private String valeur;

    /** Constructeur */
    public Constante(String nom, int indice, String valeur){
        super(nom, indice);
        this.valeur = valeur;
    }

    /** Getter */
    public String getValeur(){
        return valeur;
    }

    /** Setter */
    public void setValeur(String valeur){
        this.valeur = valeur;
    }

    /** Méthode d'affichage */
    public void affiche(){
        System.out.println("Const : " + valeur);
    }
}
