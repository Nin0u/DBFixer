package variable;

public class Attribut extends Variable {
    private String nom;
    private int indice;

    /** Constructeur */
    public Attribut(String nom, int indice){
        super();
        this.nom = nom;
        this.indice = indice;
    }

    /** Getter */
    public String getNom(){
        return nom;
    }

    /** Getter */
    public int getIndice(){
        return indice;
    }

    /** Setter */
    public void setNom(String nom){
        this.nom = nom;
    }

    /** Setter */
    public void setIndice(int indice){
        this.indice = indice;
    }

    /** Méthode d'affichage */
    public void affiche(){
        System.out.println("Attribut : " + nom + " Indice : " + String.valueOf(indice));
    }
}
