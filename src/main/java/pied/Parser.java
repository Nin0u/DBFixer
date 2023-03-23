package pied;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import contrainte.*;
import atome.*;
import variable.*;

public class Parser {
    /**
     * Parse le DF entrées par l'utilisateur.
     * La première entrée doit forcément être le nombre DF.
     * 
     * @param in Le flot d'entrée : Entrée stansard ou fichier
     * @return Une liste contenant les contraintes sous forme d'objets
     */
    public static ArrayList<Contrainte> parse(InputStream in){
        ArrayList<Contrainte> c = new ArrayList<Contrainte>();
        Scanner sc = new Scanner(in);

        // On lit la première entrée qui est un entier
        int n;
        try{
            n = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Parser.parse() : La première entrée doit être un entier !");
            sc.close();
            return null;
        }

        for(int i = 0; i < n; i++){
            // On lit la ligne
            String line = sc.nextLine();

            // On split la ligne en deux chaine : le corps et la tete
            String[] df = line.split("->");
            if (df.length != 2) {
                System.out.println("Parser.parse() : L'entrée n° " + String.valueOf(i) + " n'est pas une DF, elle sera retirée.");
                continue;
            }

            // On parse la tete et le corps
            ArrayList<Egalite> egalite_tete = new ArrayList<Egalite>();
            ArrayList<Relation> relation_tete = new ArrayList<Relation>();
            ArrayList<Egalite> egalite_corps = new ArrayList<Egalite>();
            ArrayList<Relation> relation_corps = new ArrayList<Relation>();

            if(!parseConjonction(df[1], egalite_tete, relation_tete)) {
                System.out.println("Parser.parse() : L'entrée n° " + String.valueOf(i) + " n'est pas une DF, elle sera retirée.");
                continue;
            }
            if (!parseConjonction(df[0], egalite_corps, relation_corps)){
                System.out.println("Parser.parse() : L'entrée n° " + String.valueOf(i) + " n'est pas une DF, elle sera retirée.");
                continue;
            }

            // On insère une TGD ou une EGD dans les contraintes en fonction des elements de la tete
            if(relation_tete.size() == 0)
                c.add(new EGD(relation_corps, egalite_corps, egalite_tete));
            else
                c.add(new TGD(relation_corps, egalite_corps, egalite_tete, relation_corps));
        }

        sc.close();
        return c;
    }

    /**
     * Parse les conjonctions en remplissants des tableaux d'atomes
     * 
     * @param conj La conjonction sous forme de String
     * @param eg Une liste d'égalité à remplir
     * @param rel Une liste de relation à remplir
     * 
     * @return Un booleen indiquant si la conjonction est valide
     */
    private static boolean parseConjonction(String conj, ArrayList<Egalite> eg, ArrayList<Relation> rel){
        if(conj.equals("")) return false;

        // On créer un tableau avec chaque terme de la conjonction
        String[] atome = conj.split("\\^");

       // On parcourt les atomes
        for(String s : atome){
            s = s.trim();
            if (s.equals("")) return false;

            // Si s est de la forme (*=*) alors c'est un egalité
            if (s.matches("(*=*)")){
                // On enlève les parenthèses
                s = s.substring(1, s.length() - 1);
                // On séparé à l'égalité en vérifiant qu'on coupe bien en deux
                String[] membres = s.split("=");
                if (membres.length != 2) return false;
                // On regarde si les membres sont des attributs ou des constantes
                Variable[] variables = new Variable[2];
                for(int i = 0; i < 2; i++){
                    membres[i] = membres[i].trim();
                    if (membres[i].equals("")) return false;
                    
                    // Pour les constantes on les indique avec un _c
                    // Pour les attributs : Soit on a un indice _i soit rien et dans ce cas l'indice est 0
                    if (membres[i].contains("_")){
                        String[] var = membres[i].split("_");
                        if (var[0].equals("") || var[1].equals("")) return false;

                        if(var[1].equals("c"))
                            variables[i] =  new Constante(var[0]);
                        else if (var[1].matches("[0-9]"))
                            variables[i] = new Attribut(var[0], Integer.parseInt(var[1]));
                        else return false;

                    }
                    else
                        variables[i] = new Attribut(membres[i], 0);
                }

                eg.add(new Egalite(variables[0], variables[1]));
            }

            // Sinon c'est un relation de la forme *(*)
            else if(s.matches("*(*)")){
                // On peut couper à ( pour récupérer la table au début
                String[] membres = s.split("(");
                if (membres.length != 2) return false;

                for (int i = 0; i < 2; i++){
                    membres[i] = membres[i].trim();
                    if (membres[i].equals("")) return false;
                }

                membres[1] = membres[1].substring(0, membres[1].length() -1);

                Relation r = new Relation(membres[0]);

                String[] var = membres[1].split(",");
                for (int i = 0; i < var.length; i++){
                    var[i] = var[i].trim();
                    if (var[i].equals("")) return false;
                    
                    if (var[i].contains("_")){
                        String[] v = membres[i].split("_");
                        if (v[0].equals("") || var[1].equals("")) return false;

                        if(v[1].equals("c"))
                            r.addVar(new Constante(var[0]));
                        else if (v[1].matches("[0-9]"))
                            r.addVar(new Attribut(var[0], Integer.parseInt(v[1])));
                        else return false;

                    }
                    else
                        r.addVar(new Attribut(membres[i], 0));
                }
            }
            
            else return false;
        }

        return true;
    }
}
