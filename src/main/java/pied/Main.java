package pied;

import java.util.ArrayList;
import contrainte.*;

public class Main{
    public static void main(String[] args){
        ArrayList<Contrainte> contraintes = Parser.parse(System.in);

        if (contraintes == null) System.out.println("contraintes null");
        else {
            for(Contrainte c : contraintes){
                c.affiche();
                System.out.println("\n");
            }        
        }
    }
}