package pied;

import java.io.InputStream;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import contrainte.*;

public class Main{
    // Arguments qu'on peut passer au main
    public static final String DFP = "-dfp="; // DF Path
    public static final String DBLP = "-dblp="; // DB Login Path
    public static InputStream is = System.in;
    public static InputStream login = System.in;

    public static void main(String[] args) throws SQLException{
        if (args.length > 2){
            System.out.println("Nombre d'arguments invalide.");
            printHelp();
            return;
        } 

        try {
            if(args.length == 1){
                if (args[0].startsWith(DFP)){
                    String a = args[0].substring(DFP.length());
                    is = new FileInputStream(new File(a));
                } else if (args[0].startsWith(DBLP)){
                    String a = args[0].substring(DBLP.length());
                    login = new FileInputStream(new File(a));
                } else {
                    System.out.println("Arguments Invalides");
                    printHelp();
                }
            }
            else if (args.length == 2){
                if (args[0].startsWith(DFP)){
                    String a = args[0].substring(DFP.length());
                    is = new FileInputStream(new File(a));
                } else if (args[0].startsWith(DBLP)){
                    String a = args[0].substring(DBLP.length());
                    login = new FileInputStream(new File(a));
                } else {
                    System.out.println("Arguments Invalides");
                    printHelp();
                }

                if (args[1].startsWith(DFP)){
                    String a = args[1].substring(DFP.length());
                    is = new FileInputStream(new File(a));
                } else if (args[1].startsWith(DBLP)){
                    String a = args[1].substring(DBLP.length());
                    login = new FileInputStream(new File(a));
                } else {
                    System.out.println("Arguments Invalides");
                    printHelp();
                }
            }
        } catch (FileNotFoundException e){ 
            e.printStackTrace();
            return;
        }

        // "jdbc:postgresql://localhost/nico", "nico", "nico"
        Database db = new Database(login);
        ArrayList<Contrainte> contraintes = Parser.parse(is);

        if (contraintes == null) System.out.println("Contraintes null");
        
        db.connect();
        Chase.standardChase(db, contraintes);
        // for(Contrainte c : contraintes) {
        //     c.affiche();
        //     c.executeCorps(db);
        //     c.action(c.executeCorps(db), db);
        // }
        db.close();
    }

    public static void printHelp(){
        System.out.println("Arguments acceptés :");
        System.out.println(DFP + "[path]");
        System.out.println(DBLP + "[path]");
        System.out.println("ATTENTION : il ne faut aucun espace entre -, option, = et [path].");
    }
}