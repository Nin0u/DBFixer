package contrainte;

import java.sql.ResultSet;
import java.util.ArrayList;
import atome.Atome;

public abstract class Contrainte {
    // La tete et le corps sont des conjonctions d'atomes qui sont 
    // soit des relations soit des egalités
    protected ArrayList<Atome> tete;
    protected ArrayList<Atome> corps;

    // Constructeur
    protected Contrainte(){
        this.tete = new ArrayList<Atome>();
        this.corps = new ArrayList<Atome>();
    }
    
    // Méthode abstraite qui effectue soit une egalisation soit un ajoute de tuple
    public abstract void action(ResultSet T);
}
