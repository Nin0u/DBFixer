package contrainte;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import atome.*;
import contrainte.TGD.Couple;
import maindb.Database;
import maindb.ChaseMode;
import variable.Attribut;

/** Super classe pour les EGD et TGD */
public abstract class Contrainte {
    /** Relations du Corps de la contrainte. LES EGD et les TGD en ont.*/
    protected ArrayList<Relation> rlCorps;

    /** Egalités du Corps : La Liste est dans la superclasse pour executeCorps */
    protected ArrayList<Egalite> egCorps;

    /** 
     * Constructeur 
     * 
     * @param rlCorps La liste des relations du Corps.
     * @param egCorps La liste des égalités du Corps.
     */
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

    public String executeCorps(Database db) throws SQLException {
        String select = "SELECT * ";
        String from = "FROM ";
        String where = "WHERE ";

        // Alias des Attributs : ils seront tous nommés T_i
        HashMap<Relation, String> map = new HashMap<>();

        int i = 0;
        for(Relation rel : rlCorps) {
            map.put(rel, "T" + i);
            i++;
        }   

        HashMap<Attribut, ArrayList<Pair>> mapAttrTable = new HashMap<>();
        HashMap<String, ResultSetMetaData> mapTableData = new HashMap<>();

        for(Relation rel : rlCorps) {
            if(mapTableData.get(rel.getNomTable()) == null) 
                mapTableData.put(rel.getNomTable(), db.getMetaData(rel.getNomTable()));
            from += rel.getNomTable() + " " + map.get(rel) + ", ";
            int num = 0;
            for(Attribut a : rel.getMembres()) {
                ArrayList<Pair> l = mapAttrTable.get(a);
                if(l == null) l = new ArrayList<>();
                Pair m = new Pair(rel, num + 1);
                l.add(m);
                mapAttrTable.put(a, l);
                num++;
            }
        }

        if(from.length() == 5) {
            System.out.println("Probleme ! Il faut des tables !");
            return null;
        }
        from = from.substring(0, from.length() - 2);
        
        if (egCorps != null) {
            for(Egalite eg : egCorps) {
                Attribut [] att = eg.getMembres();
                Pair ml = mapAttrTable.get(att[0]).get(0);
                String left = "";
                left += map.get(ml.a) + ".";
                left += mapTableData.get(ml.a.getNomTable()).getColumnLabel(ml.b);
                
                Pair mr = mapAttrTable.get(att[1]).get(0);
                String right = "";
                right += map.get(mr.a) + ".";
                right += mapTableData.get(mr.a.getNomTable()).getColumnLabel(mr.b);

                where += left + " = " + right + " AND ";
            }
        }

        for(HashMap.Entry<Attribut, ArrayList<Pair>> entry : mapAttrTable.entrySet()) {
            String prev = "";
            for(Pair elt : entry.getValue()) {
                String current = map.get(elt.a) + "." + mapTableData.get(elt.a.getNomTable()).getColumnLabel(elt.b);
                if(prev.length() != 0) {
                    where += prev + " = " + current + " AND ";
                }
                prev = current;
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

    // But est de mettre les types NULL_* dans les bonnes colonnes pour eviter de planter les requetes plus tard
    public void repairType(Database db) throws SQLException {
        HashMap<String, ResultSetMetaData> mapTableData = new HashMap<>();
        HashMap<Attribut, ArrayList<Pair>> mapAttrTable = new HashMap<>();

        // On constuit nos structures
        for(Relation rel : rlCorps) {
            if(mapTableData.get(rel.getNomTable()) == null) {
                mapTableData.put(rel.getNomTable(), db.getMetaData(rel.getNomTable()));
            }
            int i = 0;
            for(Attribut att : rel.getMembres()) {
                ArrayList<Pair> l = mapAttrTable.get(att);
                if(l == null) l = new ArrayList<>();
                Pair m = new Pair(rel, i + 1);
                l.add(m);
                mapAttrTable.put(att, l);
                i++;
            }
        }

        for(HashMap.Entry<Attribut, ArrayList<Pair>> entry : mapAttrTable.entrySet()) {
            boolean change = false;
            String nomType = "";
            for(Pair pair : entry.getValue()) {
                String table = pair.a.getNomTable();
                nomType = mapTableData.get(table).getColumnTypeName(pair.b);
                if(nomType.startsWith("null")) {
                    change = true;
                    break;
                }
            } 
            if(change) {
                changeType(db, entry.getValue(), mapTableData, nomType);
            }
        }

        if (egCorps != null) {
            for(Egalite eg : egCorps) {
                Attribut[] attr = eg.getMembres();
                Pair p1 = mapAttrTable.get(attr[0]).get(0);
                Pair p2 = mapAttrTable.get(attr[1]).get(0);
                
                String table1 = p1.a.getNomTable();
                String table2 = p2.a.getNomTable();

                if(mapTableData.get(table1).getColumnTypeName(p1.b).startsWith("null") && !mapTableData.get(table2).getColumnTypeName(p2.b).startsWith("null")) {
                    changeType(db, mapAttrTable.get(attr[1]), mapTableData, mapTableData.get(table1).getColumnTypeName(p1.b));
                }

                if(mapTableData.get(table2).getColumnTypeName(p2.b).startsWith("null") && !mapTableData.get(table1).getColumnTypeName(p1.b).startsWith("null")) {
                    changeType(db, mapAttrTable.get(attr[0]), mapTableData, mapTableData.get(table2).getColumnTypeName(p2.b));
                }
            }
        }
    }

    protected void changeType(Database db, ArrayList<Pair> pairs, HashMap<String, ResultSetMetaData> mapTableData, String nomType) throws SQLException {
        for(Pair pair : pairs) {
            String table = pair.a.getNomTable();
            String type = mapTableData.get(table).getColumnTypeName(pair.b);

            if(!type.startsWith("null")) {
                db.changeType(table, nomType, mapTableData.get(table).getColumnLabel(pair.b));
                mapTableData.put(table, db.getMetaData(table));
            }
        }
    }
    
    /** 
     * Méthode abstraite qui effectue soit une egalisation soit un ajoute de tuple
     * selon si on est une EGD ou une TGD
     */
    public abstract int action(String req, Database db) throws SQLException;

    /** Méthode  pour la oblivious ou la skolem chase. */
    public int actionOblivious(String req, Database db, ChaseMode mode) throws SQLException { return action(req, db); }

    /** Méthode pour la core chase. */
    public int actionCore(String req, Database db, HashSet<Couple> toAdd) throws SQLException { return action(req, db); }

    /** Méthode pour vérifier qu'une DB satifait des contraintes */
    public abstract boolean actionSatisfy(String req, Database db)  throws SQLException;

    /** Méthode abstraite d'affichage */
    public abstract void affiche();

    public class Pair {
        public Relation a;
        public Integer b;
        Pair(Relation a, Integer b) {
            this.a = a;
            this.b = b;
        }
    }
}
