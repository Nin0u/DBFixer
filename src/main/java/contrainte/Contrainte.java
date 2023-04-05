package contrainte;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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

    public String executeCorps(Database db) throws SQLException {
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
                    String type = getType(db, rel.getNomTable(), a.getNom());
                    where += map.get(rel) + "." + a.getNom() + "=" + a.getValeur();
                    if(type != null && type.startsWith("null")) {
                        where += "::" + type;
                    } 
                     
                    where += " AND ";
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
                    //TODO: REGLER CE CAS PLUS TARD
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

    // But est de mettre les types NULL_* dans les bonnes colonnes pour eviter de planter les requetes plus tard
    public void repairType(Database db) throws SQLException {
        HashMap<String, ResultSetMetaData> mapTableData = new HashMap<>();
        HashMap<Attribut, ArrayList<String>> mapAttrTable = new HashMap<>();

        // On constuit nos structures
        for(Relation rel : rlCorps) {
            if(mapTableData.get(rel.getNomTable()) == null) {
                mapTableData.put(rel.getNomTable(), db.getMetaData(rel.getNomTable()));
            }
            for(Attribut att : rel.getMembres()) {
                ArrayList<String> l = mapAttrTable.get(att);
                if(l == null) l = new ArrayList<>();
                l.add(rel.getNomTable());
                mapAttrTable.put(att, l);
            }
        }

        for(HashMap.Entry<Attribut, ArrayList<String>> entry : mapAttrTable.entrySet()) {
            String nomAttr = entry.getKey().getNom();
            boolean change = false;
            String nomType = "";
            for(String table : entry.getValue()) {
                int index = 1;
                while(index <= mapTableData.get(table).getColumnCount()) {
                    if(mapTableData.get(table).getColumnLabel(index).equals(nomAttr)) break;
                    index++;
                }
                if(index > mapTableData.get(table).getColumnCount()) {
                    System.out.println("Error Repair !");
                    System.exit(1);
                }
                if(mapTableData.get(table).getColumnTypeName(index).startsWith("null")) {
                    change = true;
                    nomType = mapTableData.get(table).getColumnTypeName(index);
                    break;
                }
            } 
            if(change) {
                changeType(db, nomAttr, entry.getValue(), mapTableData, nomType);
            }
        }

        if (egCorps != null) {
            for(Egalite eg : egCorps) {
                Attribut[] attr = eg.getMembres();
                String table1 = mapAttrTable.get(attr[0]).get(0);
                String table2 = mapAttrTable.get(attr[1]).get(0);
                int index1 = 1;
                while(index1 <= mapTableData.get(table1).getColumnCount()) {
                    if(mapTableData.get(table1).getColumnLabel(index1).equals(attr[0].getNom())) break;
                    index1++;
                }

                int index2 = 1;
                while(index2 <= mapTableData.get(table2).getColumnCount()) {
                    if(mapTableData.get(table2).getColumnLabel(index2).equals(attr[1].getNom())) break;
                    index2++;
                }

                if(mapTableData.get(table1).getColumnTypeName(index1).startsWith("null") && !mapTableData.get(table2).getColumnTypeName(index2).startsWith("null")) {
                    changeType(db, table2, mapAttrTable.get(attr[1]), mapTableData, mapTableData.get(table1).getColumnTypeName(index1));
                }

                if(mapTableData.get(table2).getColumnTypeName(index2).startsWith("null") && !mapTableData.get(table1).getColumnTypeName(index1).startsWith("null")) {
                    changeType(db, table1, mapAttrTable.get(attr[0]), mapTableData, mapTableData.get(table2).getColumnTypeName(index2));
                }
            }
        }
    }

    private void changeType(Database db, String nomAttr, ArrayList<String> tables, HashMap<String, ResultSetMetaData> mapTableData, String nomType) throws SQLException {
        for(String table : tables) {
            int index = 1;
            while(index <= mapTableData.get(table).getColumnCount()) {
                if(mapTableData.get(table).getColumnLabel(index).equals(nomAttr)) break;
                index++;
            }

            if(!mapTableData.get(table).getColumnTypeName(index).startsWith("null")) {
                db.ChangeType(table, nomType, nomAttr);
                mapTableData.put(table, db.getMetaData(table));
            }
            
        }
    }

    protected String getType(Database db, String table, String attr) throws SQLException {
        ResultSetMetaData r = db.getMetaData(table);
        int index = 1;
        while(index <= r.getColumnCount()) {
            if(r.getColumnLabel(index).equals(attr)) break;
            index++;
        }
        if(index > r.getColumnCount()) return null;
        return r.getColumnTypeName(index);
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
