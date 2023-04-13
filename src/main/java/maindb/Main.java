package maindb;

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
    public static final String MODE = "-mode="; // Chase Mode
    public static final String HELP = "-help";

    public static InputStream is = System.in;
    public static InputStream login = System.in;
    public static ChaseMode mode = ChaseMode.STANDARD;

    public static void main(String[] args) throws SQLException{
        if (args.length > 3){
            System.out.println("Nombre d'arguments invalide. Lancer le programme avec -help pour plus de détails");
            return;
        }

        if (args.length == 1 && args[0].equals(HELP)) {
            printHelp();
            return;
        }

        // Parsing des options
        try {
            for(int i = 0; i < args.length; i++){
                if (args[i].startsWith(DFP)){
                    String a = args[i].substring(DFP.length());
                    is = new FileInputStream(new File(a));
                } else if (args[i].startsWith(DBLP)){
                    String a = args[i].substring(DBLP.length());
                    login = new FileInputStream(new File(a));
                } else if (args[i].startsWith(MODE)){
                    String a = args[i].substring(MODE.length());
                    try {
                        mode = ChaseMode.getMode(Integer.parseInt(a));
                        if(mode == null){
                            System.out.println("Mode de Chase Invalide : valeur non valide. Lancer le programme avec -help pour plus de détails");
                            return;
                        }
                    } catch (NumberFormatException e){
                        System.out.println("Mode de Chase Invalide : doit être un nombre. Lancer le programme avec -help pour plus de détails");
                        return;
                    }
                } else {
                    System.out.println("Arguments Invalides. Lancer le programme avec -help pour plus de détails");
                    return;
                }
            }
        } catch (FileNotFoundException e){ 
            e.printStackTrace();
            return;
        }

        // Connexion à la BD
        Database db = new Database(login);

        // Parsing des DF
        // S'il n'y a pas de contrainte (ou toutes invalides) aucune raison de lancer la chase.
        ArrayList<Contrainte> contraintes = Parser.parse(is);
        if (contraintes == null) {
            System.out.println("Contraintes nulles. Lancement de la chase annulée.");
            return;
        }
        
        // On lance la chase
        db.connect();
        Chase.chase(mode, db, contraintes);
        db.close();
    }


    /** Affiche l'aide */
    public static void printHelp(){
        System.out.println("Le programme prend 3 options au maximum de la forme -option (sans espace).");
        System.out.println("Certaines options ayant une valeur, il ne faut aucun espace entre -option, = et la valeur.\n");
        System.out.println("Options acceptés :");
        System.out.println("* " + HELP + ": Doit être appelé sans aucune autre option et n'a pas de valeur.");
        System.out.println("* " + DFP + "[path] : indique au programme un fichier dans lequel se trouve les DF voulues.");
        System.out.println("* " + DBLP + "[path] : indique au programme un fichier dans lequel se trouve les login de la base de donnée que vous voulez manipuler.");
        System.out.println("* " + MODE + "[mode] : [mode] est par défault 0. La liste qui suit indique le mode selon le nombre :");
        System.out.println("\t* 0 : Standard Chase");
        System.out.println("\t* 1 : Oblivious Chase");
        System.out.println("\t* 2 : Oblivious Skolem Chase");
        System.out.println("\t* 3 : Oblivious et Oblivious Skolem pour les EGD");
        System.out.println("\t* 4 : Core chase");
    }
}