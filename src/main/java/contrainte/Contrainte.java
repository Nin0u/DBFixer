package contrainte;

import java.sql.ResultSet;
import java.util.ArrayList;
import atome.*;

public abstract class Contrainte {
    /**
     * Le corps est une conjonction d'atomes qui sont soit des relations soit des egalités
     * On décide de stocker ça dans deux listes
     */
    protected ArrayList<Relation> rlCorps;
    protected ArrayList<Egalite> egCorps;

    /**
     * La tête est une conjonction d'égalité si la contrainte est une EGD.
     * Dans le cas d'une TGD il faut une liste de Relation en plus.
     * 
     *  Pour profiter de l'héritage on définit la conjonction d'égalité ici.
     */
    protected ArrayList<Egalite> egTete;

    /** Constructeur */
    protected Contrainte(ArrayList<Relation> rlCorps, ArrayList<Egalite> egCorps, ArrayList<Egalite> egTete){
        this.rlCorps = rlCorps;
        this.egCorps = egCorps;
        this.egTete = egTete;
    }

    /** Getter */
    public ArrayList<Relation> getRelCorps(){
        return rlCorps;
    }

    /** Getter */
    public ArrayList<Egalite> getEgCorps(){
        return egCorps;
    }

    /** Getter */
    public ArrayList<Egalite> getEgTete(){
        return egTete;
    }
    
    /** 
     * Méthode abstraite qui effectue soit une egalisation soit un ajoute de tuple
     * selon si on est une TGD ou une EGD
     * 
     * @param T Tuple trouvé qui respecte le corps mais pas la tête
     */
    public abstract void action(ResultSet T);
}
