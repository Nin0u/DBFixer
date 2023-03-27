package contrainte;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import atome.*;
import pied.Database;
import variable.Attribut;

public class EGD extends Contrainte {

    /** Constructeur */
    public EGD(ArrayList<Relation> rlCorps, ArrayList<Egalite> egCorps, ArrayList<Egalite> egTete){
        super(rlCorps,egCorps,egTete);
    }

    /** 
     * Egaliser les tuples de T en accord avec la contrainte this
     * @param T Un tuple qui satisfait le corps mais pas la tête de e
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
            boolean end = false;
            while(T.next()) {
                for(int i = 0; i < nb; i++) {
                    System.out.print(T.getString(i + 1) + " ");
                }
                System.out.println();

                for(Egalite eg : egTete) {
                    Attribut [] attr = eg.getMembres();
                    ArrayList<Integer> indexLeft = getIndex(orderAttribut, attr[0]);
                    ArrayList<Integer> indexRight = getIndex(orderAttribut, attr[1]);
                    //TODO: Si indexRight.size() == 0 et que attr[1] est une constante ---> à voir mais ça change un peu
                    if(indexLeft.size() == 0 || indexRight.size() == 0) {
                        System.out.println("IMPOSSIBLE ! Egalité parlant de variables non existants à gauche !");
                        return -1;
                    }
                    for(int li : indexLeft) {
                        for(int ri : indexRight) {
                            if(!T.getString(li + 1).equals(T.getString(ri + 1))) {
                                System.out.println(T.getString(li + 1) + " " + T.getString(ri + 1));
                                
                                String update = "UPDATE " + ordRelations.get(ri).getNomTable() + " SET ";
                                if(!isWriteType(rsmd.getColumnType(li + 1)))
                                    update += attr[1].getNom() + " = " + T.getString(li + 1) + " ";
                                else update += attr[1].getNom() + " = '" + T.getString(li + 1) + "' ";
                                update += "WHERE ";

                                int min = getMin(cut, ri);
                                int max = getMax(cut, ri);
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
                                //T.updateObject(ri+1, T.getObject(li + 1), rsmd.getColumnType(ri + 1));
                                end = true;
                               // return 1;             
                            }
                        }
                    }
                }

                if(end) return 1;
            }
            return 0;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return -1;
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
