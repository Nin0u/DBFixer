package contrainte;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import atome.*;
import pied.Database;
import variable.Attribut;

/**
 * Equality Generating Dependency :
 * - Le corps est une conjonction d'atomes relationnels et d'égalités.
 * - La tête est une conjonction d'atomes d'égalité.
 */
public class EGD extends Contrainte {
    /** Liste des égalités dans la tête. */
    private ArrayList<Egalite> egTete;

    /** Constructeur */
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

        
        if (egTete != null) {
            for(Egalite eg : egTete) {
                Attribut[] attr = eg.getMembres();
                String table1 = mapAttrTable.get(attr[0]).get(0);
                String table2 = mapAttrTable.get(attr[1]).get(0);
                System.out.println(table1 + " " + table2);
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
                    changeType(db, attr[1].getNom(), mapAttrTable.get(attr[1]), mapTableData, mapTableData.get(table1).getColumnTypeName(index1));
                }

                if(mapTableData.get(table2).getColumnTypeName(index2).startsWith("null") && !mapTableData.get(table1).getColumnTypeName(index1).startsWith("null")) {
                    changeType(db, attr[0].getNom(), mapAttrTable.get(attr[0]), mapTableData, mapTableData.get(table2).getColumnTypeName(index2));
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
    public int action(String req, Database db){
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
            ResultSetMetaData rsmd = T.getMetaData();
            boolean end = false; // Sert à r 
            while(T.next()) {
                for(int i = 0; i < nb; i++) {
                    System.out.print(T.getString(i + 1) + " ");
                }
                System.out.println();

                for(Egalite eg : egTete) {
                    Attribut [] attr = eg.getMembres();
                    ArrayList<Integer> indexLeft = getIndex(orderAttribut, attr[0]);

                    //Cas constante
                    if(attr[1].getValeur() != null) {
                        String val = attr[1].getValeur();
                        if(attr[1].getValeur().charAt(0) == '\'') {
                            val = val.substring(1, val.length() - 1);
                        }
                        for(int li : indexLeft) {
                            String type = rsmd.getColumnTypeName(li + 1); 
                            if(type.startsWith("null"))
                                val = "(0," + val + ")";                         

                            if(!T.getString(li + 1).equals(val)) {
                                System.out.println(T.getString(li + 1) + " " + val);
                                updateDBCons(T, db, li, rsmd, attr, 1, orderAttribut, cut, ordRelations);
                                end = true;
                                return 1;
                            }
                        }
                    } else {
                        // Cas non constante
                        ArrayList<Integer> indexRight = getIndex(orderAttribut, attr[1]);
                        if(indexLeft.size() == 0 || indexRight.size() == 0) {
                            System.out.println("IMPOSSIBLE ! Egalité parlant de variables non existants à gauche !");
                            return -1;
                        }
                        for(int li : indexLeft) {
                            for(int ri : indexRight) {
                                if(!T.getObject(li + 1).equals(T.getObject(ri + 1))) {
                                    updateDBBIS(T, li, ri, db, rsmd, attr, orderAttribut, cut, ordRelations);
                                    end = true;
                                    return 1;             
                                }
                            }
                        }
                    }
                }

                if(end) return 1;
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void updateDBBIS(ResultSet T, int li, int ri, Database db, ResultSetMetaData rsmd, Attribut[] attr, ArrayList<Attribut> orderAttribut, ArrayList<Integer> cut,  ArrayList<Relation> ordRelations) throws SQLException {
        System.out.println(T.getString(li + 1) + " " + T.getString(ri + 1));
        
        int min = getMin(cut, ri);
        int max = getMax(cut, ri);
        System.out.println(min);
        System.out.println(max);

        ArrayList<String> attrs = new ArrayList<>();
        ArrayList<Object> values = new ArrayList<>();

        for(int j = min; j < max; j++) {
            attrs.add(orderAttribut.get(j).getNom());
            values.add(T.getObject(j + 1));
        }

        db.UpdateQuery(ordRelations.get(ri).getNomTable(), attr[1].getNom(),  T.getObject(li + 1), attrs, values);
    }

    private void updateDBCons(ResultSet T, Database db, int li, ResultSetMetaData rsmd, Attribut[] attr, int index, ArrayList<Attribut> orderAttribut, ArrayList<Integer> cut,  ArrayList<Relation> ordRelations) throws SQLException {
        String update = "UPDATE " + ordRelations.get(li).getNomTable() + " SET ";
        update += attr[0].getNom() + " = " ;
        
        String type = rsmd.getColumnTypeName(li + 1); 
        if(!type.startsWith("null"))
            update += attr[index].getValeur() + " ";
        else  update += attr[index].getValeur() + "::" + type + "  ";
        update += "WHERE ";

        int min = getMin(cut, li);
        int max = getMax(cut, li);
        System.out.println(min);
        System.out.println(max);
        
        for(int j = min; j < max; j++) {
            int t = rsmd.getColumnType(j + 1);
            if(!isWriteType(t))
                update += orderAttribut.get(j).getNom() + "=" + T.getString(j + 1) + " AND ";
            else 
                update += orderAttribut.get(j).getNom() + "='" + T.getString(j + 1) + "' AND ";
        }

        update = update.substring(0, update.length() - 5);
        System.out.println(update);
        
        db.updateRequest(update);   
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
}
