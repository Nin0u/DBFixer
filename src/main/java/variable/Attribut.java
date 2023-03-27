package variable;

public class Attribut extends Variable {
    protected String nom;
    protected int indice;

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

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Attribut)) return false;
        Attribut a = (Attribut)o;
        return (a.nom.equals(this.nom)) && (a.indice == this.indice);
    }

    @Override
    public int hashCode() {
        return indice + nom.hashCode();
    }
}
