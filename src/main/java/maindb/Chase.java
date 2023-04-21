package maindb;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import atome.Relation;
import contrainte.*;

public class Chase {
    /** Limite d'un tour d'Oblivious Chase : quand cette limite est atteinte, on demande à l'utilisateur si on veut refaire un tour. */
    private static final int OBLIVIOUS_LIMIT = 5;

    /** 
     * Execute la chase 
     * 
     * @param mode Le mode de chase.
     * @param db La base de donnée
     * @param sigma La liste des contraintes
     * 
     * @throws SQLException
     */
    public static void chase(ChaseMode mode, Database db, ArrayList<Contrainte> sigma) throws SQLException{
        switch(mode){
            case STANDARD :
                standardChase(db, sigma); 
                break;

            case OBLIVIOUS :
                obliviousChase(db, sigma);
                break;

            case SKOLEM : 
                skolemChase(db, sigma);
                break;

            case OBEGD : 
                System.out.println("Pas encore implémenté");
                break;

            case CORE : 
                coreChase(db, sigma);
                break;
        }
    }

    // Standard chase
    private static void standardChase(Database db, ArrayList<Contrainte> sigma) throws SQLException{
        boolean end = false;
        while(! end) {
            end = true;
            for(Contrainte c : sigma) {
                System.out.println("DEBUT REPAIR");
                c.repairType(db);
                System.out.println("FIN REPAIR");
                int ret = 0;
                if(c instanceof EGD) {
                    while(true) {
                        ret = c.action(c.executeCorps(db), db);

                        if(ret == -1) return;
                        if(ret == 1) end = false;
                        if(ret == 0) break;
                    }
                } else {
                    ret = c.action(c.executeCorps(db), db);
                    if(ret == -1) return;
                    if(ret == 1) end = false;
                }
                System.out.println();
            }

        }
    }

    // Oblivious chase
    private static void obliviousChase(Database db, ArrayList<Contrainte> sigma) throws SQLException{
        boolean end = false;
        int limit = 0;
        while(! end && limit < OBLIVIOUS_LIMIT) {
            end = true;
            for(Contrainte c : sigma) {
                System.out.println("DEBUT REPAIR");
                c.repairType(db);
                System.out.println("FIN REPAIR");
                int ret = 0;
                if(c instanceof EGD) {
                    while(true) {
                        ret = c.action(c.executeCorps(db), db);

                        if(ret == -1) return;
                        if(ret == 1) end = false;
                        if(ret == 0) break;
                    }
                } else {
                    ret = c.actionOblivious(c.executeCorps(db), db);
                    if(ret == -1) return;
                    if(ret == 1){
                        end = false;
                        limit++;
                        if (limit == OBLIVIOUS_LIMIT){
                            Scanner sc = new Scanner(System.in);
                            System.out.println(String.valueOf(OBLIVIOUS_LIMIT) + " itérations effectuées : Voulez-vous continuer ? [y/n]");
                            String a = sc.nextLine();
                            sc.close();
                            a = a.toLowerCase();
                            if (a.equals("y") || a.equals("o")) limit = 0;
                            else break;
                        }
                    } 
                }
                System.out.println();
            }

        }
    }

    // Oblivious Skolem chase
    private static void skolemChase(Database db, ArrayList<Contrainte> sigma) throws SQLException{
        boolean end = false;

        HashMap<Contrainte, HashMap<ArrayList<String>, Integer>> tuples_liees = new HashMap<Contrainte, HashMap<ArrayList<String>, Integer>>();
        for (Contrainte c : sigma) 
            tuples_liees.put(c, new HashMap<ArrayList<String>, Integer>());
        while(! end) {
            end = true;

            for(Contrainte c : sigma) {
                System.out.println("DEBUT REPAIR");
                c.repairType(db);
                System.out.println("FIN REPAIR");
                int ret = 0;
                if(c instanceof EGD) {
                    while(true) {
                        ret = c.action(c.executeCorps(db), db);

                        if(ret == -1) return;
                        if(ret == 1) end = false;
                        if(ret == 0) break;
                    }
                } else {
                    ret = c.actionSkolem(c.executeCorps(db), db, tuples_liees.get(c));
                    if(ret == -1) return;
                    if(ret == 1) end = false;
                }
                System.out.println();
            }
        }
    }

    // Core chase
    private static void coreChase(Database db, ArrayList<Contrainte> sigma) throws SQLException{
        boolean end = false;
        while(! end) {
            end = true;

            // Pour chaque contrainte on doit faire l'union des tuples à ajouter
            HashSet<ArrayList<Object>> toAdd = new  HashSet<ArrayList<Object>>();

            for(Contrainte c : sigma) {
                System.out.println("DEBUT REPAIR");
                c.repairType(db);
                System.out.println("FIN REPAIR");
                int ret = 0;

                if(c instanceof EGD) {
                    while(true) {
                        ret = c.action(c.executeCorps(db), db);

                        if(ret == -1) return;
                        if(ret == 1) end = false;
                        if(ret == 0) break;
                    }
                } else {
                    ret = c.actionCore(c.executeCorps(db), db, toAdd);
                    if(ret == -1) return;
                    if(ret == 1) end = false;
                }
                System.out.println();
            }

            // On ajoute les tuples
            for (ArrayList<Object> values : toAdd){
                String req = "INSERT INTO " + values.get(0) + " VALUES (";
                values.remove(0);
                for (Object o : values){
                    if (o.toString().contains("NULL")){
                        req += o.toString();
                        values.remove(o);
                    }
                    else req += "?,";
                }
                req = req.substring(0, req.length() -1) + ")";
                db.insertReq(req, values);
            }

            // On trouve le core
            findCore(db, sigma);
        }
    }

    /**
     * Trouve le core
     * 
     * @param db La base de données
     * @param sigma L'ensemble des contraintes
     * @throws SQLException
     */
    private static void findCore(Database db, ArrayList<Contrainte> sigma) throws SQLException{
        // On récupère chaque tuple de D
        // Pour cela on va récupérer chaque table intervenant dans la tête des contraintes TGD
        for(Contrainte c : sigma) {
            if (c instanceof TGD) {
                for (Relation r : ((TGD)c).getRelTete()) {
                    // On récupère tous les tuples d'une table de la tete
                    ResultSet res = db.selectRequest("SELECT * FROM " + r.getNomTable() + ";");
                    // Pour chacun de ces tuples 
                    while (res.next()) {
                        // On le retire temporairement en stockant ses valeurs en cas de rajout
                        ArrayList<Object> values = new ArrayList<Object>();
                        String delete = "DELETE FROM " + r.getNomTable() + "WHERE ";

                        for (int i = 1; i <= res.getMetaData().getColumnCount(); i++){
                            values.add(res.getObject(i));
                            delete += res.getMetaData().getColumnName(i) + "=? AND"; 
                        }
                        delete = delete.substring(0, delete.length() - 4);
                        db.insertReq(delete, values);

                        // Si D satisfait sigma on retire définitivement notre tuple
                        if (satisfy(db, sigma)) continue;

                        // Sinon on rajoute le tuple
                        else {
                            String insert = "INSERT INTO " + r.getNomTable() + "VALUES ("; 
                            for (int i = 0; i < values.size(); i++)
                                insert+= "?, ";

                            insert = insert.substring(0, insert.length() - 2) + ")";
                            db.insertReq(insert, values);
                        }
                    }
                }
            } 
        }
    }

    public static boolean satisfy(Database db, ArrayList<Contrainte> sigma) {
        try {
            boolean b = true;
            for(Contrainte c : sigma) {
                b = b && c.actionSatisfy(c.executeCorps(db), db);
            }

            return b;
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}
