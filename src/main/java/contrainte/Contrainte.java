package contrainte;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import atome.*;
import pied.Database;
import variable.Attribut;

public abstract class Contrainte {
    /**
     * Le corps est une conjonction d'atomes qui sont:
     * - Des relations pour les TGD
     * - Des relations et des egalités pour les EGS
     * 
     * On stocke les relations dans la superclasse.
     */
    protected ArrayList<Relation> rlCorps;

    /** On laisse les egalites du corps ici pour executeCorps */
    protected ArrayList<Egalite> egCorps;

    /** Constructeur */
    protected Contrainte(ArrayList<Relation> rlCorps, ArrayList<Egalite> egCorps) {
        this.rlCorps = rlCorps;
        this.egCorps = egCorps;
    }

    /** Getter */
    public ArrayList<Relation> getRelCorps() {
        return rlCorps;
    }

    /** Getter */
    public ArrayList<Egalite> getEgCorps() {
        return egCorps;
    }

    public String executeCorps(Database db) {
        String select = "SELECT ";
        String from = "FROM ";
        String where = "WHERE ";

        HashMap<Relation, String> map = new HashMap<>();

        int i = 0;
        for(Relation rel : rlCorps) {
            map.put(rel, "T" + i);
            i++;
        }   

        HashMap<Attribut, ArrayList<String>> mapAttrTable = new HashMap<>();

        for(Relation rel : rlCorps) {
            from += rel.getNomTable() + " " + map.get(rel) + ", ";
            for(Attribut a : rel.getMembres()) {
                select += map.get(rel) + "." + a.getNom() + ", ";
                if(a.getValeur() != null) {
                    where += map.get(rel) + "." + a.getNom() + "=" + a.getValeur() + " AND ";
                }
                ArrayList<String> l = mapAttrTable.get(a);
                if(l == null) l = new ArrayList<>();
                l.add(map.get(rel));
                mapAttrTable.put(a, l);
            }
        }

        if(from.length() == 5) {
            System.out.println("Probleme ! Il faut des tables !");
            return null;
        }
        from = from.substring(0, from.length() - 2);
        select = select.substring(0, select.length() - 2);
        
        if (egCorps != null) {
            for(Egalite eg : egCorps) {
                Attribut[] attr = eg.getMembres();
                String left = attr[0].getNom();
                ArrayList<String> tLeft = mapAttrTable.get(attr[0]);
                if(tLeft == null) {
                    System.out.println("pbm left");
                    return null;
                }
                left = tLeft.get(0) + "." + left;

                String right = "";
                if(attr[1].getValeur() != null) {
                    right = attr[1].getValeur();
                } else {
                    right = attr[1].getNom();
                    ArrayList<String> tRight = mapAttrTable.get(attr[1]);
                    if(tRight == null) {
                        System.out.println("pbm right");
                        return null;
                    }
                    right = tRight.get(0) + "." + right;
                }
                where += left + "=" + right + " AND ";
            }
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
        return req;
    }


    protected boolean isWriteType(int t) {
        return (t == Types.VARCHAR) ||
               (t == Types.VARBINARY) ||
               (t == Types.NVARCHAR) ||
               (t == Types.LONGNVARCHAR) ||
               (t == Types.DATE);
    }
    
    /** 
     * Méthode abstraite qui effectue soit une egalisation soit un ajoute de tuple
     * selon si on est une TGD ou une EGD
     * 
     * @param T Tuple trouvé qui respecte le corps mais pas la tête
     */
    public abstract int action(String req, Database db);

    /** Méthode d'affichage */
    public abstract void affiche();
}
