package pied;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import contrainte.*;

public class Main{
    public static void main(String[] args){
        InputStream is = System.in;
        if (args.length > 1){
            System.out.println("Nombre d'arguments invalides : accepte 0 ou 1 argument.");
            return;
        } else if (args.length == 1){
            try {
                is = new FileInputStream(new File(args[0]));
            } catch (FileNotFoundException e) {
                System.out.println("File not found : " + args[0]);
                return;
            }
        }
        //TODO: Faire en sorte qu'on puisse rentrer ces données dedans via un fichier ou dans le terminal
        Database db = new Database("jdbc:postgresql://localhost/bd", "yoan", "yoyo");
        ArrayList<Contrainte> contraintes = Parser.parse(is);

        if (contraintes == null) System.out.println("contraintes null");
        else {
            for(Contrainte c : contraintes){
                c.affiche();
                c.action(c.executeCorps(db), db);
                System.out.println("\n");
            }    
        }
    }
}