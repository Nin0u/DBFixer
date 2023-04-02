package variable;

public class Attribut{
    private String nom;
    private int indice;
    private String valeur;


    /** Constructeur */
    public Attribut(String nom, int indice){
        super();
        this.nom = nom;
        this.indice = indice;
        this.valeur = null;
    }

    /** Constructeur */
    public Attribut(String nom, int indice, String valeur){
        super();
        this.nom = nom;
        this.indice = indice;
        this.valeur = valeur;
    }

    /** Getter */
    public String getNom(){
        return nom;
    }

    /** Getter */
    public int getIndice(){
        return indice;
    }

    /** Getter */
    public String getValeur(){
        return valeur;
    }

    /** Setter */
    public void setNom(String nom){
        this.nom = nom;
    }

    /** Setter */
    public void setIndice(int indice){
        this.indice = indice;
    }

    /** Setter */
    public void setValeur(String valeur){
        this.valeur = valeur;
    }

    /** Méthode d'affichage */
    public void affiche(){
        System.out.print("Attribut : " + nom + " Indice : " + String.valueOf(indice));
        if (valeur != null) System.out.print(" Valeur : " + valeur);
        System.out.println("");
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Attribut)) return false;
        Attribut a = (Attribut)o;
        if ((a.valeur == null && this.valeur != null) || (a.valeur != null && this.valeur == null)) return false;
        if ((a.valeur == null && this.valeur == null))
            return (a.nom.equals(this.nom)) && (a.indice == this.indice);
        else 
            return (a.nom.equals(this.nom)) && (a.indice == this.indice) && (a.valeur == this.valeur);
    }

    @Override
    public int hashCode() {
        return indice + nom.hashCode();
    }
}
