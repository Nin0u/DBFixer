package maindb;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import atome.Relation;
import contrainte.*;
import contrainte.Contrainte.Couple;
import variable.Valeur;

public class Chase {
    /** 
     * Limite d'un tour d'Oblivious Chase.
     * Quand cette limite est atteinte, on demande à l'utilisateur si on veut refaire un tour. 
     */
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
                System.out.println("========= Execution de la Standard Chase =========");
                standardChase(db, sigma); 
                break;

            case OBLIVIOUS :
                System.out.println("======== Execution de la Oblivious Chase =========");
                obliviousChase(db, sigma);
                break;

            case SKOLEM : 
                System.out.println("========== Execution de la Skolem Chase ==========");
                skolemChase(db, sigma);
                break;

            case CORE : 
                System.out.println("=========== Execution de la Core Chase ===========");
                coreChase(db, sigma);
                break;
        }

        System.out.println("===================== FIN  ========================");
    }

    // Standard chase
    private static void standardChase(Database db, ArrayList<Contrainte> sigma) throws SQLException{
        boolean end = false;
        while(! end) {
            end = true;
            for(Contrainte c : sigma) {
                System.out.print("Réparation des types ...");
                c.repairType(db);
                System.out.println("Fait");

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
                System.out.print("Réparation des types ...");
                c.repairType(db);
                System.out.println("Fait");

                int ret = 0;
                ret = c.actionOblivious(c.executeCorps(db), db, ChaseMode.OBLIVIOUS);
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
                System.out.println();
            }
        }
        
        for(Contrainte c : sigma) {
            if(c instanceof EGD) {
                EGD e = (EGD)c;
                e.egalise(db);
            }
        }
    }

    // Oblivious Skolem chase
    private static void skolemChase(Database db, ArrayList<Contrainte> sigma) throws SQLException{
        boolean end = false;
        while(! end) {
            end = true;
            for(Contrainte c : sigma) {
                System.out.print("Réparation des types ...");
                c.repairType(db);
                System.out.println("Fait");

                int ret = 0;
                
                ret = c.actionOblivious(c.executeCorps(db), db, ChaseMode.SKOLEM);
                if(ret == -1) return;
                if(ret == 1) end = false;
                
                System.out.println();
            }
        }

        for(Contrainte c : sigma) {
            if(c instanceof EGD) {
                EGD e = (EGD)c;
                e.egalise(db);
            }
        }
    }

    // Core chase
    private static void coreChase(Database db, ArrayList<Contrainte> sigma) throws SQLException{
        boolean end = false;
        while(! end) {
            end = true;

            // Pour chaque contrainte on doit faire l'union des tuples à ajouter
            HashSet<Couple> toAdd = new  HashSet<>();

            for(Contrainte c : sigma) {
                System.out.print("Réparation des types ...");
                c.repairType(db);
                System.out.println("Fait.");

                if (c.actionCore(c.executeCorps(db), db, toAdd) < 0){
                    System.out.println("Erreur actionCore");
                    return;
                }
                System.out.println();
            }

            System.out.print("Insertion de tuples ...");
            // On ajoute les tuples
            for (Couple c : toAdd){
                ArrayList<Valeur> val = c.getList();
                ResultSetMetaData rsmd = db.getMetaData(c.getNomTable());
                String req = "INSERT INTO " + c.getNomTable() + " VALUES (";
                for (int i = 0; i < val.size(); i++)
                    req += val.get(i).addStringReq(rsmd.getColumnTypeName(i + 1)) + ", ";
                req = req.substring(0, req.length() - 2) + ")";
                db.insertReq(req, val);
            }
            System.out.println("Fait.");

            System.out.print("Egalisation ...");
            // Egaliser les EGD
            for(Contrainte c : sigma) {
                if(c instanceof EGD) {
                    EGD e = (EGD)c;
                    e.egalise(db);
                    e.clearEgalite();
                }
            }
            System.out.println("Fait.");
            
            System.out.println("FindCore");
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
                    ResultSet res = db.selectRequest("SELECT * FROM " + r.getNomTable());
                    // Pour chacun de ces tuples 
                    while (res.next()) {
                        // On le retire temporairement en stockant ses valeurs en cas de rajout
                        ArrayList<Valeur> values = new ArrayList<Valeur>();
                        String delete = "DELETE FROM " + r.getNomTable() + " WHERE ";

                        for (int i = 1; i <= res.getMetaData().getColumnCount(); i++){
                            values.add(new Valeur(res.getMetaData().getColumnTypeName(i), res.getObject(i), false));
                            delete += res.getMetaData().getColumnName(i) + " = ? AND "; 
                        }
                        delete = delete.substring(0, delete.length() - 5);
                        db.insertReq(delete, values);

                        // Si D satisfait sigma on retire définitivement notre tuple
                        if (satisfy(db, sigma)) continue;

                        // Sinon on rajoute le tuple
                        else {
                            String insert = "INSERT INTO " + r.getNomTable() + " VALUES ("; 
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

    /**
     * Vérifies si la base de données satisfait les contraintes.
     * 
     * @param db La base de données
     * @param sigma Les contraintes
     */
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
