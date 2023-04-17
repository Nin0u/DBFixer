package maindb;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import contrainte.*;
import atome.*;
import variable.*;

public class Parser {
    // Regex pour test si egalite ou relation
    private static final String var_regex = "[a-zA-Z]+[a-zA-Z0-9]*(?:_[0-9]+)?";
    private static final Pattern p_egalite = Pattern.compile("\\([ ]*" + var_regex + "[ ]*=[ ]*" + var_regex + "[ ]*\\)");
    private static final Pattern p_relation = Pattern.compile("[a-zA-Z]+[a-zA-Z0-9]*[ ]*\\([ ]*" + var_regex + "(,[ ]*"  + var_regex + "[ ]*)*" + "[ ]*\\)");
            
    /**
     * Parse le DF entrées par l'utilisateur.
     * La première entrée doit forcément être le nombre DF.
     * 
     * @param in Le flot d'entrée : Entrée standard ou fichier
     * @return Une liste contenant les contraintes sous forme d'objets
     */
    public static ArrayList<Contrainte> parse(InputStream in){
        ArrayList<Contrainte> c = new ArrayList<Contrainte>();
        Scanner sc = new Scanner(in);

        // On lit la première entrée qui est un entier
        int n;
        if (in.equals(System.in)) System.out.print("n = ");
        try{
            n = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Parser.parse() : La première entrée doit être un entier !");
            sc.close();
            return null;
        }

        // On lit les lignes une par une
        for(int i = 0; i < n; i++){
            // On lit une ligne
            if(in.equals(System.in)) System.out.print("DF n°" + String.valueOf(i) + " : ");
            String line = sc.nextLine();
            line = line.trim();

            // On split la ligne en deux chaine : le corps et la tete
            String[] df = line.split("->");
            if (df.length != 2) {
                System.out.println("Parser.parse() : -> : L'entrée n° " + String.valueOf(i) + " n'est pas une DF , elle sera retirée.");
                continue;
            }
            
            // On parse la tete et le corps en 4 listes 
            ArrayList<Egalite> egalite_tete = new ArrayList<Egalite>();
            ArrayList<Relation> relation_tete = new ArrayList<Relation>();
            ArrayList<Egalite> egalite_corps = new ArrayList<Egalite>();
            ArrayList<Relation> relation_corps = new ArrayList<Relation>();
            
            if(!parseConjonction(df[1], egalite_tete, relation_tete)) {
                System.out.println("Parser.parse() : parseConjonction(tete) : L'entrée n° " + String.valueOf(i) + " n'est pas une DF, elle sera retirée.");
                continue;
            }

            if (!parseConjonction(df[0], egalite_corps, relation_corps)){
                System.out.println("Parser.parse() : parseConjonction(corps) : L'entrée n° " + String.valueOf(i) + " n'est pas une DF, elle sera retirée.");
                continue;
            }

            // On insère une TGD ou une EGD dans les contraintes en fonction des elements de la tete
            if(egalite_tete.size() == 0 && egalite_corps.size() == 0)
                c.add(new TGD(relation_corps, relation_tete));
            else if (relation_tete.size() == 0)
                c.add(new EGD(relation_corps, egalite_corps, egalite_tete));
            else 
                System.out.println("Parser.parse() : L'entrée n° " + String.valueOf(i) + " n'est pas une TGD ou EGD , elle sera retirée.");
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
        conj = conj.trim();
        if(conj.equals("")) return false;

        // On créer un tableau avec chaque terme de la conjonction
        String[] atomes = conj.split("\\^");
        
        // On parcourt les atomes
        for(String atome : atomes){
            atome = atome.trim();
            if (atome.equals("")) return false;
            
            // Si l'atome est un égalité
            if (p_egalite.matcher(atome).find()){
                // On enlève les parenthèses
                atome = atome.substring(1, atome.length() - 1);

                // On sépare à l'égalité
                String[] membres = atome.split("=");
                
                // On parse chaque variable de l'égalité en regardant si elles ont un indice ou non
                // Soit on a un indice _i soit rien et dans ce cas l'indice est 0 par défaut
                Attribut[] variables = new Attribut[2];
                for(int i = 0; i < 2; i++){
                    membres[i] = membres[i].trim();
                    String[] var = membres[i].split("_");

                    if (var.length == 1 || var[1].trim().equals(""))
                        variables[i] = new Attribut(var[0], 0);
                    else if (var[1].matches("[0-9]+"))
                        variables[i] = new Attribut(var[0], Integer.parseInt(var[1]));
                    else return false;
                }
                
                eg.add(new Egalite(variables[0], variables[1]));
            }
            
            // si l'atome est une relation
            else if(p_relation.matcher(atome).find()){

                // On sépare à ( pour récupérer la table au début
                String[] sep = atome.split("\\(");
                if (sep.length != 2) return false;

                String table = sep[0].trim();
                String membres = sep[1].substring(0, sep[1].length() -1); // on enlève ) aux membres de la relation
                if (table.equals("") || membres.equals("")) return false;

                // On balaye chaque attribut intervenant dans la relation
                Relation r = new Relation(table); 
                
                String[] attr = membres.split(",");
                for (int i = 0; i < attr.length; i++){
                    attr[i] = attr[i].trim();
                    if (attr[i].equals("")) return false;

                    String[] var = attr[i].split("_");

                    if (var.length == 1 || var[1].trim().equals(""))
                        r.addVar(new Attribut(var[0], 0));
                    else if (var[1].matches("[0-9]+"))
                        r.addVar(new Attribut(var[0], Integer.parseInt(var[1])));
                    else return false;
                }
                    
                rel.add(r);
            }
        }

        return true;
    }
}
    