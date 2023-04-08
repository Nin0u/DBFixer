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
import java.util.HashMap;

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
    private static final Path typeSQLSCriptPath = Paths.get("src/main/resources/type.sql");
    
    /** Numéro de null. */
    private static int num_null = 0;

    /** Liste contenant les relations de la tete. */
    private ArrayList<Relation> rlTete;
    
    /** Constructeur */
    public TGD(ArrayList<Relation> rlCorps, ArrayList<Relation> rlTete){
        super(rlCorps, null);
        this.rlTete = rlTete;
    }

    public void repairType(Database db) throws SQLException {
        super.repairType(db);

        HashMap<String, ResultSetMetaData> mapTableData = new HashMap<>();
        HashMap<Attribut, ArrayList<String>> mapAttrTable = new HashMap<>();

        // On constuit nos structures
        for(Relation rel : rlCorps) {
            if(mapTableData.get(rel.getNomTable()) == null) {
                mapTableData.put(rel.getNomTable(), db.getMetaData(rel.getNomTable()));
            }
            for(Attribut att : rel.getMembres()) {
                ArrayList<String> l = mapAttrTable.get(att);
                if(l == null) l = new ArrayList<>();
                l.add(rel.getNomTable());
                mapAttrTable.put(att, l);
            }
        }

        for(Relation rel : rlTete) {
            if(mapTableData.get(rel.getNomTable()) == null) {
                mapTableData.put(rel.getNomTable(), db.getMetaData(rel.getNomTable()));
            }

            for(Attribut att : rel.getMembres()) {
                ArrayList<String> l = mapAttrTable.get(att);
                if(l == null) l = new ArrayList<>();
                l.add(rel.getNomTable());
                mapAttrTable.put(att, l);
            }
        }

        for(HashMap.Entry<Attribut, ArrayList<String>> entry : mapAttrTable.entrySet()) {
            String nomAttr = entry.getKey().getNom();
            boolean change = false;
            String nomType = "";
            for(String table : entry.getValue()) {
                int index = 1;
                while(index <= mapTableData.get(table).getColumnCount()) {
                    if(mapTableData.get(table).getColumnLabel(index).equals(nomAttr)) break;
                    index++;
                }
                if(index > mapTableData.get(table).getColumnCount()) {
                    System.out.println("Error Repair !");
                    System.exit(1);
                }
                if(mapTableData.get(table).getColumnTypeName(index).startsWith("null")) {
                    change = true;
                    nomType = mapTableData.get(table).getColumnTypeName(index);
                    break;
                }
            } 
            if(change) {
                changeType(db, nomAttr, entry.getValue(), mapTableData, nomType);
            }
        }
    }

    /**
     * Ajoute un nouveau tuple u tq db union u satisfait e
     *
     * @param req la requête qui permet d'obtenir les tuples qui respectent le corps
     * @param db la base de donnée
     */
    public int action(String req, Database db){
        try {
            int ret = 0;

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
                for(int i = 0; i < orderAttribut.size(); i++) {
                    System.out.print(T.getString(i + 1) + " ");
                }
                System.out.println();


                // On regarde les relations dans la tête
                for (Relation r2 : rlTete){
                    // On construit les variables libres et liées 
                    ArrayList<Integer> attrLies = new ArrayList<Integer>();
                    ArrayList<Integer> attrLibres = new ArrayList<Integer>();

                    int j = 0;
                    for(Attribut a2 : r2.getMembres()) {
                        boolean find = false;
                        for(int i = 0; i < orderAttribut.size(); i++) {
                            if(a2.equals(orderAttribut.get(i))) {
                                attrLies.add(i);
                                find = true;
                                break;
                            }
                        }
                        if(!find) attrLibres.add(j); 
                        j++;
                    }

                    // Vérifier si on a un tuple
                    // Si on en a un c'est ok on continue
                    ResultSet res  = VerifReq(db, r2, T, rsmd, orderAttribut, attrLies, attrLibres);
                    

                    // On doit ajouter un tuple si il n'y a rien
                    if (!res.next()) {
                        System.out.println("Ajout de tuple");
                        ret = 1;
                        // Pour toutes les var libres, on regarde dans les metadonnees si le type commence par NULL_
                        // Si ce n'est pas le cas on doit alterer la table
                        ResultSetMetaData metaData = db.getMetaData(r2.getNomTable());                        

                        for(Integer index : attrLibres){
                            String columnTypeName = metaData.getColumnTypeName(index + 1);
                            System.out.println(columnTypeName);
                            if(!columnTypeName.startsWith("null_")){
                                String alterReq = buildCreateTypeReq(r2.getMembres().get(index).getNom(), columnTypeName, r2.getNomTable());
                                db.updateRequest(alterReq);
                                System.out.println("Create Type null_" + columnTypeName);
                            }
                        }

                        // On insère le tuple
                        String insertReq = buildInsertReq(r2, T, attrLies, attrLibres);
                        System.out.println(insertReq);
                        db.updateRequest(insertReq);
                    }

                }
            }

            return ret;
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
    private ResultSet VerifReq(Database db, Relation r2, ResultSet T, ResultSetMetaData rsmd, ArrayList<Attribut> orderAttribut, ArrayList<Integer> attrLies, ArrayList<Integer> attrLibres) throws SQLException{
        ArrayList<String> attr = new ArrayList<>();
        ArrayList<Object> values = new ArrayList<>();

        for(Integer index : attrLies) {
            attr.add(orderAttribut.get(index).getNom());
            values.add(T.getObject(index + 1));
        }

        return db.SelectQuery(r2.getNomTable(), attr, values);
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
                             .replaceAll("%nomTable", nomTable);
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
    private String buildInsertReq(Relation r, ResultSet T, ArrayList<Integer> attrLies, ArrayList<Integer> attrLibres) throws SQLException{
        String req = "INSERT INTO " + r.getNomTable() + " (";
        
        
        for(Attribut a : r.getMembres()) {
            req += a.getNom() + ", ";
        }
        req = req.substring(0, req.length() - 2);
        req += ")";
        req += " VALUES (";

        int jlies = 0;
        for(int i = 0; i < r.getMembres().size(); i++) {
            if(indexInList(i, attrLibres)) {
                num_null++;
                req += "(" + String.valueOf(num_null) + ", NULL), ";
            } else {
                req += T.getObject(attrLies.get(jlies) + 1) + ", ";
                jlies++;
            }
        }

        req = req.substring(0, req.length() - 2) + ")";
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
