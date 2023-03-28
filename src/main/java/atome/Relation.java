package atome;
import java.util.ArrayList;

import variable.Attribut;

public class Relation{
    /** Un relation contient plusieurs variables qui sont soit des constantes soit des attributs */
    private ArrayList<Attribut> membres;

    /** Nom de la table liée à la relation */
    private String nomTable;

    /** Constructeur */
    public Relation(String nomTable){
        this.nomTable = nomTable;
        this.membres = new ArrayList<Attribut>();
    }

    /** Getter */
    public String getNomTable() {
        return nomTable;
    }
    
    /** Getter */
    public ArrayList<Attribut> getMembres() {
        return membres;
    }

    /** Méthode d'ajout */
    public void addVar(Attribut v) {
        membres.add(v);
    }

    /** Méthode d'afichage */
    public void affiche(){
        System.out.println("-- Relation --");
        System.out.println("Table = " + nomTable);
        for (Attribut a : membres) 
            a.affiche();

        System.out.println("-- Fin --");
    }
}
