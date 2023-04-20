package maindb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

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
            for(Contrainte c : sigma) {
                System.out.println("DEBUT REPAIR");
                c.repairType(db);
                System.out.println("FIN REPAIR");
                int ret = 0;

                // Pour chaque contrainte on doit faire l'union des tuples à ajouter
                HashSet<ArrayList<Object>> toAdd = new  HashSet<ArrayList<Object>>();

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

        // Pour chaque tuple de D
        
        // On retire le tuple qu'on stocke dans t

        // Si D satisfait sigma on retire définitivement t

        // Sinon on rajoute t
    }
}
