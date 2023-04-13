package maindb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import contrainte.*;

public class Chase {
    private static final int OBLIVIOUS_LIMIT = 5;

    public static void chase(ChaseMode mode, Database db, ArrayList<Contrainte> sigma) throws SQLException{
        switch(mode){
            case STANDARD :
                standardChase(db, sigma); 
                break;

            case OBLIVIOUS :
                obliviousChase(db, sigma);
                break;

            case SKOLEM : 
                System.out.println("Pas encore implémenté");
                break;

            case OBEGD : 
                System.out.println("Pas encore implémenté");
                break;

            case CORE : 
                System.out.println("Pas encore implémenté");
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
                            System.out.println("Lu :" + a);
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
}
