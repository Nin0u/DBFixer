package contrainte;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import atome.*;
import maindb.Database;
import variable.Attribut;
import variable.Valeur;

/**
 * Equality Generating Dependency :
 * - Le corps est une conjonction d'atomes relationnels et d'égalités.
 * - La tête est une conjonction d'atomes d'égalité.
 */
public class EGD extends Contrainte {
    /** Liste des égalités dans la tête. */
    private ArrayList<Egalite> egTete;

    /** 
     * Constructeur 
     * 
     * @param rlCorps La liste des relations du Corps.
     * @param egCorps La liste des égalités du Corps.
     * @param egTete La liste des égalités de la Tete.
     */
    public EGD(ArrayList<Relation> rlCorps, ArrayList<Egalite> egCorps, ArrayList<Egalite> egTete){
        super(rlCorps, egCorps);
        this.egTete = egTete;
    }

    /** Getter */
    public ArrayList<Egalite> getEgTete() {
        return egTete;
    }

    public void repairType(Database db) throws SQLException {
        super.repairType(db);
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
        
        if (egTete != null) {
            for(Egalite eg : egTete) {
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

    /** 
     * Egaliser les tuples de T en accord avec la contrainte this
     * 
     * @param req la requête permettant d'obtenir les tuples qui respecte le corps mais pas la tête d'une DF
     * @param db la base de donnée
     */
    public int action(String req, Database db) throws SQLException {
        int nb = 0;
        
        ResultSet T = db.selectRequest(req);
        
        ArrayList<Attribut> orderAttribut = new ArrayList<>();

        ArrayList<Relation> ordRelations = new ArrayList<>();

        for(Relation rel : rlCorps) {
            for(Attribut a : rel.getMembres()) {
                orderAttribut.add(a);
                ordRelations.add(rel);
                nb++;
            }
        }
        
        try {
            boolean end = false; // TODO Sert à r ? 
            while(T.next()) {
                for(int i = 0; i < nb; i++) {
                    System.out.print(T.getString(i + 1) + " ");
                }
                System.out.println();

                HashMap<Relation, ArrayList<Two>> tuples = new HashMap<>();

                for(Egalite eg : egTete) {
                    Attribut [] attr = eg.getMembres();
                    ArrayList<Integer> indexLeft = getIndex(orderAttribut, attr[0]);
                    ArrayList<Integer> indexRight = getIndex(orderAttribut, attr[1]);

                    if(indexLeft.size() == 0 || indexRight.size() == 0) {
                        System.out.println("IMPOSSIBLE ! Egalité parlant de variables non existants à gauche !");
                        return -1;
                    }
                    for(int li : indexLeft) {
                        for(int ri : indexRight) {
                            if(!T.getObject(li + 1).equals(T.getObject(ri + 1))) {
                                if(!T.getString(li + 1).endsWith(",)")) {
                                    ArrayList<Two> l = tuples.get(ordRelations.get(ri));
                                    if(l == null) l = new ArrayList<>();
                                    l.add(new Two(T.getMetaData().getColumnName(ri + 1), new Valeur(T.getMetaData().getColumnTypeName(li + 1), T.getObject(li + 1), false)));
                                    tuples.put(ordRelations.get(ri), l);
                                }
                                else {
                                    ArrayList<Two> l = tuples.get(ordRelations.get(li));
                                    if(l == null) l = new ArrayList<>();
                                    l.add(new Two(T.getMetaData().getColumnName(li + 1), new Valeur(T.getMetaData().getColumnTypeName(ri + 1), T.getObject(ri + 1), false)));
                                    tuples.put(ordRelations.get(li), l);
                                }
                                end = true;          
                            }
                        }
                    }
                }

                if(end) {
                    updateBD(db, tuples, T, ordRelations);
                    return 1;
                }
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }


    
    public boolean actionSatisfy(String req, Database db) throws SQLException {
        int nb = 0;

        ResultSet T = db.selectRequest(req);

        ArrayList<Attribut> orderAttribut = new ArrayList<>();

        ArrayList<Relation> ordRelations = new ArrayList<>();
        ArrayList<Integer> cut = new ArrayList<>();
        cut.add(0);

        for(Relation rel : rlCorps) {
            for(Attribut a : rel.getMembres()) {
                orderAttribut.add(a);
                ordRelations.add(rel);
                nb++;
            }
            cut.add(nb);
        }
        
        try {
            while(T.next()) {
                for(int i = 0; i < nb; i++) {
                    System.out.print(T.getString(i + 1) + " ");
                }
                System.out.println();

                for(Egalite eg : egTete) {
                    Attribut [] attr = eg.getMembres();
                    ArrayList<Integer> indexLeft = getIndex(orderAttribut, attr[0]);
                    ArrayList<Integer> indexRight = getIndex(orderAttribut, attr[1]);

                    if(indexLeft.size() == 0 || indexRight.size() == 0) {
                        System.out.println("IMPOSSIBLE ! Egalité parlant de variables non existants à gauche !");
                        return true;
                    }
                    for(int li : indexLeft) {
                        for(int ri : indexRight) {
                            if(!T.getObject(li + 1).equals(T.getObject(ri + 1))) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    private void updateDBBIS(ResultSet T, int li, int ri, Database db, ResultSetMetaData rsmd, ArrayList<Attribut> orderAttribut, ArrayList<Integer> cut,  ArrayList<Relation> ordRelations) throws SQLException {
        int min = getMin(cut, ri);
        int max = getMax(cut, ri);

        ArrayList<String> attrs = new ArrayList<>();
        ArrayList<Object> values = new ArrayList<>();

        for(int j = min; j < max; j++) {
            attrs.add(rsmd.getColumnLabel(j + 1));
            values.add(T.getObject(j + 1));
        }

        db.updateQuery(ordRelations.get(ri).getNomTable(), rsmd.getColumnLabel(ri + 1),  T.getObject(li + 1), attrs, values);
    }

    private void updateBD(Database db, HashMap<Relation, ArrayList<Two>> tuples, ResultSet T,  ArrayList<Relation> ordRelations) throws SQLException {
        int min = -1, max = -1;

        for(HashMap.Entry<Relation, ArrayList<Two>> entry : tuples.entrySet()) {
            Relation r = entry.getKey();
            for(int i = 0; i < ordRelations.size(); i++) {
                if(ordRelations.get(i) == (r) && min == -1) min = i;
                if(ordRelations.get(i) != (r) && min != -1 && max == -1) max = i;
                
            }
            if(max == -1) max = ordRelations.size();

            ArrayList<Valeur> val = new ArrayList<>();
            String req = "UPDATE " + r.getNomTable() + " SET ";

            for(Two two : entry.getValue()) {
                req += two.attr + " = ?, ";
                val.add(two.val);
            }

            System.out.println(min);
            System.out.println(max);

            req = req.substring(0, req.length() - 2);

            req += " WHERE ";

            for(int i = min; i < max; i++) {
                val.add(new Valeur(T.getMetaData().getColumnTypeName(i + 1), T.getObject(i + 1), false));
                req += T.getMetaData().getColumnName(i + 1) + " = ? AND ";
            }

            req = req.substring(0, req.length() - 5);
            System.out.println(req);
            db.insertReq(req, val);
        }

    }

    private ArrayList<Integer> getIndex(ArrayList<Attribut> list, Attribut a) {
        ArrayList<Integer> l = new ArrayList<>();
        int index = 0;
        for(Attribut at : list) {
            if(at.equals(a)) l.add(index);
            index++;
        }
        return l;
    }

    private int getMax(ArrayList<Integer> list, int a) {
        for(int elt : list) {
            if(a < elt) return elt;
        }
        return list.get(list.size() - 1);
    }

    private int getMin(ArrayList<Integer> list, int a) {
        int i = 0;
        for(int elt : list) {
            if(a < elt) 
                if(i != 0)
                    return list.get(i - 1);
                else return list.get(0);
            i++;
        }
        return list.get(list.size() - 1);
    }

    /** Méthode d'affichage */
    public void affiche(){
        System.out.println("===========  EGD ==============");
        System.out.println("---- Corps ----");
        for (Relation r : rlCorps)
            r.affiche();

        for(Egalite e : egCorps)
            e.affiche();

        System.out.println("---- Tete ----");
        for (Egalite e : egTete)
            e.affiche(); 

        System.out.println("=========== FIN EGD ==============");
    }

    public class Two {
        String attr;
        Valeur val;

        public Two(String attr, Valeur val) {
            this.attr = attr;
            this.val = val;
        }
    }
}
