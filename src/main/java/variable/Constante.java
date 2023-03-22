package variable;

public class Constante extends Variable{
    private String valeur;

    public Constante(String valeur){
        this.valeur = valeur;
    }

    public String getValeur(){
        return valeur;
    }

    public void setValeur(String valeur){
        this.valeur = valeur;
    }
}
