package contrainte;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import atome.*;
import variable.*;
import pied.Database;

/**
 * Tuple Generating Dependency :
 * - Le corps est une conjonction d'atomes relationnels.
 * - La tête est une conjonction d'atomes relationnels.
 */
public class TGD extends Contrainte {
    /** 
     * Chemin vers le script qui permet de créer un nouveau type.
     * Le fichier va être formatté afin de correspondre aux attributs sont il faut créer un type NULL.
     */
    private static final Path typeSQLSCriptPath = Paths.get("src/resources/type.sql");
    
    /** Numéro de null. */
    private static int num_null = 0;

    /** Liste contenant les relations de la tete. */
    private ArrayList<Relation> rlTete;
    
    /** Constructeur */
    public TGD(ArrayList<Relation> rlCorps, ArrayList<Relation> rlTete){
        super(rlCorps, null);
        this.rlTete = rlTete;
    }

    /**
     * Ajoute un nouveau tuple u tq db union u satisfait e
     *
     * @param req la requête qui permet d'obtenir les tuples qui respectent le corps
     * @param db la base de donnée
     */
    public int action(String req, Database db){
        try {
            // On récupère les tuples qui respectent le corps
            ResultSet T = db.selectRequest(req);
            ResultSetMetaData rsmd = T.getMetaData();

            // On peut avoir plusieurs attribut avec le même nom, on a besoin donc de l'ordre dans les attributs du tuple
            ArrayList<Attribut> orderAttribut = new ArrayList<>();

            for(Relation rel : rlCorps) {
                for(Attribut a : rel.getMembres())
                    orderAttribut.add(a);
            }

            // Pour chaque tuple
            while(T.next()) {
                // On regarde les relations dans la tête
                for (Relation r2 : rlTete){
                    // On construit les variables libres et liées 
                    ArrayList<Integer> attrLies = new ArrayList<Integer>();
                    ArrayList<Integer> attrLibres = new ArrayList<Integer>();
                    for(int i = 0; i < orderAttribut.size(); i++){
                        for (Attribut a2 : r2.getMembres()){
                            if (orderAttribut.get(i).equals(a2))
                                attrLies.add(i);
                            else 
                                attrLibres.add(i);
                        }
                    }

                    // Vérifier si on a un tuple
                    // Si on en a un c'est ok on continue
                    String verifReq = buildVerifReq(r2, T, orderAttribut, attrLies, attrLibres);
                    ResultSet res = db.selectRequest(verifReq);
                    if (res.next()) continue;
                    
                    // Autre cas : on doit ajouter un tuple
                    else {
                        // Pour toutes les var libres, on regarde dans les metadonnees si le type commence par NULL_
                        // Si ce n'est pas le cas on doit alterer la table
                        for(Integer index : attrLibres){
                            String columnTypeName = rsmd.getColumnTypeName(index);
                            if(!columnTypeName.startsWith("NULL_")){
                                String alterReq = buildCreateTypeReq(rsmd.getColumnName(index), columnTypeName, r2.getNomTable());
                                db.selectRequest(alterReq);
                            }
                        }

                        // On insère le tuple
                        String insertReq = buildInsertReq(r2, T, rsmd, orderAttribut, attrLies);
                        db.selectRequest(insertReq);
                    }
                }
            }

            return 1;
        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    
    /**
     * Construit la requete vérificatrice d'une relation de la tête
     * 
     * @param r2 La relation de la tête
     * @param T L'ensemble des tuples respectant le corps
     * @param orderAttribut L'ordre des attributs dans la relation
     * @param attrLies Liste d'indice des attributs liés
     * @param attrLibres Liste d'indice des attributs libres
     * 
     * @return La requête de vérification
     * @throws SQLException
     */
    private String buildVerifReq(Relation r2, ResultSet T, ArrayList<Attribut> orderAttribut, ArrayList<Integer> attrLies, ArrayList<Integer> attrLibres) throws SQLException{
        String req = "SELECT * FROM " + r2.getNomTable() + " WHERE ";
        boolean atLeastOneCondition = false;
        for(Integer index : attrLies) {
            if (atLeastOneCondition) req += " AND ";
            else atLeastOneCondition = true;

            req += orderAttribut.get(index).getNom() + "=" + T.getObject(index);
        }

        for (Integer index : attrLibres){
            if (atLeastOneCondition) req += " AND ";
            else atLeastOneCondition = true;

            String value = orderAttribut.get(index).getValeur();
            if(value != null){
                req += orderAttribut.get(index).getNom() + "=" + value;
            }
        }
        return req;
    }

    /**
     * Fonction permettant de générer la requete créant une nouveau type NULL.
     * Cette fonction s'occupera d'incrémenter
     * 
     * @param nomvar Le nom de la variable qui nécéssite l'appel à la fonction
     * @param typevar Le type de la variable
     * @param nomTable Le nom de la table
     * 
     * @return La requête formattée 
     */
    private String buildCreateTypeReq(String nomvar, String typevar, String nomTable){
        try {
            String content = new String(Files.readAllBytes(typeSQLSCriptPath), StandardCharsets.UTF_8);
            content = content.replaceAll("\n", "")
                             .replaceAll("%nomvar", nomvar).replaceAll("%typevar", typevar)
                             .replaceAll("nomTable", nomTable).replaceAll("num_null",String.valueOf(num_null));
            num_null++;
            return content;
        } catch (IOException e){
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Construit la requete d'insertion 
     * 
     * @param r La relation
     * @param T le Tuple
     * @param rsmd les metadonnées de T
     * @param orderAttribut Liste des attributs dans l'ordre
     * @param attrLies List d'indice des attributs lies
     * 
     * @return La requête d'insertion
     * @throws SQLException
     */
    private String buildInsertReq(Relation r, ResultSet T,  ResultSetMetaData rsmd, ArrayList<Attribut> orderAttribut, ArrayList<Integer> attrLies) throws SQLException{
        String req = "INSERT INTO " + r.getNomTable() + " VALUES (";
        
        for(int i = 0; i < orderAttribut.size(); i++){
            if (indexInList(i, attrLies))
                req += T.getObject(i) + ",";
            else 
                req += rsmd.getColumnTypeName(i) + "(" + T.getObject(i) + "," + String.valueOf(num_null) + "),"; 
        }

        req = req.substring(req.length() -1) + ")";
        return req;
    }

    private boolean indexInList(int i, ArrayList<Integer> L){
        for(Integer index : L)
            if(index.intValue() == i) return true;
        return false;
    }

    /** Méthode d'affichage */
    public void affiche(){
        System.out.println("===========  TGD ==============");
        System.out.println("---- Corps ----");
        for (Relation r : rlCorps)
            r.affiche();

        System.out.println("---- Tete ----");
        for (Relation r : rlTete)
            r.affiche();

        System.out.println("=========== FIN TGD ==============");
    }
}
