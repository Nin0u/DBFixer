package variable;

public class Constante extends Variable{
    private String valeur;

    /** Constructeur */
    public Constante(String valeur){
        super();
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
}
