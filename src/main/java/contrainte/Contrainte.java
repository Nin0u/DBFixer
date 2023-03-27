package contrainte;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import atome.*;
import pied.Database;
import variable.Attribut;
import variable.Constante;
import variable.Variable;

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
    protected Contrainte(ArrayList<Relation> rlCorps, ArrayList<Egalite> egCorps, ArrayList<Egalite> egTete) {
        this.rlCorps = rlCorps;
        this.egCorps = egCorps;
        this.egTete = egTete;
    }

    /** Getter */
    public ArrayList<Relation> getRelCorps() {
        return rlCorps;
    }

    /** Getter */
    public ArrayList<Egalite> getEgCorps() {
        return egCorps;
    }

    /** Getter */
    public ArrayList<Egalite> getEgTete() {
        return egTete;
    }

    public ResultSet executeCorps(Database db) {
        String select = "SELECT ";
        String from = "FROM ";
        String where = "WHERE ";

        HashMap<String, String> map = new HashMap<>();

        int i = 0;
        for(Relation rel : rlCorps) {
            String table = rel.getNomTable();
            map.put(table, "T" + i);
            i++;
        }   

        HashMap<Attribut, ArrayList<String>> mapAttrTable = new HashMap<>();

        for(Relation rel : rlCorps) {
            from += rel.getNomTable() + " " + map.get(rel.getNomTable()) + ", ";
            for(Attribut a : rel.getMembres()) {
                select += map.get(rel.getNomTable()) + "." + a.getNom() + ", ";
                if(a instanceof Constante) {
                    Constante c = (Constante)a;
                    where += map.get(rel.getNomTable()) + "." + c.getNom() + "=" + c.getValeur() + " AND ";
                }
                ArrayList<String> l = mapAttrTable.get(a);
                if(l == null) l = new ArrayList<>();
                l.add(map.get(rel.getNomTable()));
                mapAttrTable.put(a, l);
            }
        }

        if(from.length() == 5) {
            System.out.println("Probleme ! Il faut des tables !");
            return null;
        }
        from = from.substring(0, from.length() - 2);
        select = select.substring(0, select.length() - 2);

        for(Egalite eg : egCorps) {
            Attribut[] attr = eg.getMembres();
            String left = attr[0].getNom();
            ArrayList<String> tLeft = mapAttrTable.get(attr[0]);
            if(tLeft == null) {
                System.out.println("pbm left");
                return null;
            }
            left = tLeft.get(0) + "." + left;

            String right = attr[1].getNom();
            ArrayList<String> tRight = mapAttrTable.get(attr[1]);
            if(tRight == null) {
                System.out.println("pbm right");
                return null;
            }
            right = tRight.get(0) + "." + right;
            where += left + "=" + right + " AND ";
        }

        for(HashMap.Entry<Attribut, ArrayList<String>> entry : mapAttrTable.entrySet()) {
            String prev = "";
            String nom = entry.getKey().getNom();
            for(String elt : entry.getValue()) {
                if(prev.length() != 0) {
                    where += prev + "=" + elt + "." + nom + " AND ";
                }
                prev = elt + "." + nom;
            }
        }

        String req = select + " " + from;

        if(where.length() > 6) {
            where = where.substring(0, where.length() - 5);
            req += " " + where;
        }


        System.out.println(req);

        ResultSet set = db.selectRequest(req);

        return set;
    }
    
    /** 
     * Méthode abstraite qui effectue soit une egalisation soit un ajoute de tuple
     * selon si on est une TGD ou une EGD
     * 
     * @param T Tuple trouvé qui respecte le corps mais pas la tête
     */
    public abstract void action(ResultSet T, Database db);

    /** Méthode d'affichage */
    public abstract void affiche();
}
