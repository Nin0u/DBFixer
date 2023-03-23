package atome;
import java.util.ArrayList;

import variable.Variable;

public class Relation{
    /** Un relation contient plusieurs variables qui sont soit des constantes soit des attributs */
    private ArrayList<Variable> membres;

    /** Nom de la table liée à la relation */
    private String nomTable;

    /** Constructeur */
    public Relation(String nomTable){
        this.nomTable = nomTable;
        this.membres = new ArrayList<Variable>();
    }

    /** Getter */
    public String getNomTable() {
        return nomTable;
    }
    
    /** Getter */
    public ArrayList<Variable> getMembres() {
        return membres;
    }

    /** Méthode d'ajout */
    public void addVar(Variable v) {
        membres.add(v);
    }

    /** Méthode d'afichage */
    public void affiche(){
        System.out.println("-- Relation --");
        System.out.println("Table = " + nomTable);
        for (Variable v : membres) 
            v.affiche();

        System.out.println("-- Fin --");
    }
}
