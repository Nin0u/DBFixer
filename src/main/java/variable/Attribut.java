package variable;

public class Attribut extends Variable {
    private String nomTable;
    private String nom;
    private int indice;

    public Attribut(String nomTable, String nom, int indice){
        super();
        this.nomTable = nomTable;
        this.nom = nom;
        this.indice = indice;
    }

    public String getNomTable(){
        return nomTable;
    }

    public String nom(){
        return nom;
    }

    public int getIndice(){
        return indice;
    }

    public void setNomTable(String nomTable){
        this.nomTable = nomTable;
    }

    public void setNom(String nom){
        this.nom = nom;
    }

    public void setIndice(int indice){
        this.indice = indice;
    }
}
