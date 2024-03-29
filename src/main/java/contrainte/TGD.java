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
import java.util.HashSet;

import atome.*;
import variable.*;
import maindb.ChaseMode;
import maindb.Database;

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

    private ArrayList<ArrayList<Valeur>> treated; 

    /** Liste contenant les relations de la tete. */
    private ArrayList<Relation> rlTete;

    /** Map des nulls générés. Sert pour Skolem. */
    private HashMap<ArrayList<Valeur>, Integer> nullGeneres;
    
    /** 
     * Constructeur 
     * 
     * @param rlCorps La liste des relations du Corps.
     * @param rlTete La liste des relations de la Tete.
     */
    public TGD(ArrayList<Relation> rlCorps, ArrayList<Relation> rlTete){
        super(rlCorps, null);
        this.rlTete = rlTete;
        nullGeneres = new HashMap<ArrayList<Valeur>, Integer>();
        treated = new ArrayList<>();
    }

    /** Getter */
    public ArrayList<Relation> getRelTete() { return rlTete; }

    public void repairType(Database db) throws SQLException {
        //super.repairType(db);
        HashMap<String, ResultSetMetaData> mapTableData = new HashMap<>();
        HashMap<Attribut, ArrayList<Pair>> mapAttrTable = new HashMap<>();

        // On constuit nos structures
        for(Relation rel : rlCorps) {
            if(mapTableData.get(rel.getNomTable()) == null) {
                mapTableData.put(rel.getNomTable(), db.getMetaData(rel.getNomTable()));
            }
            int i = 0;
            for(Attribut att : rel.getMembres()) {
                ArrayList<Pair> l = mapAttrTable.get(att);
                if(l == null) l = new ArrayList<>();
                Pair m = new Pair(rel, i + 1);
                l.add(m);
                mapAttrTable.put(att, l);
                i++;
            }
        }

        for(Relation rel : rlTete) {
            if(mapTableData.get(rel.getNomTable()) == null) {
                mapTableData.put(rel.getNomTable(), db.getMetaData(rel.getNomTable()));
            }
            int i = 0;
            for(Attribut att : rel.getMembres()) {
                ArrayList<Pair> l = mapAttrTable.get(att);
                if(l == null) l = new ArrayList<>();
                Pair m = new Pair(rel, i + 1);
                l.add(m);
                mapAttrTable.put(att, l);
                i++;
            }
        }

        for(HashMap.Entry<Attribut, ArrayList<Pair>> entry : mapAttrTable.entrySet()) {
            boolean change = false;
            String nomType = "";
            for(Pair pair : entry.getValue()) {
                String table = pair.a.getNomTable();
                nomType = mapTableData.get(table).getColumnTypeName(pair.b);
                if(nomType.startsWith("null")) {
                    change = true;
                    break;
                }
            } 
            if(change) {
                changeType(db, entry.getValue(), mapTableData, nomType);
            }
        }
    }

    /**
     * Ajoute un nouveau tuple u tq db union u satisfait e
     *
     * @param req La requête qui permet d'obtenir les tuples qui respectent le corps
     * @param db La base de donnée
     * 
     * @return -1 en cas d'erreur. 0 si la chase doit terminer. 1 Si la chase doit continuer.
     */
    public int action(String req, Database db) throws SQLException {
        try {
            // Valeur de retour
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
                ArrayList<Valeur> tuple = new ArrayList<>();
                for(int i = 1; i <= rsmd.getColumnCount(); i++) {
                    tuple.add(new Valeur(rsmd.getColumnTypeName(i), T.getObject(i), false));
                }
                if(treated.contains(tuple)) continue;
                treated.add(tuple);

                printTuple(T, orderAttribut.size());
                
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

                    ResultSetMetaData metaData = db.getMetaData(r2.getNomTable());
                    // Vérifier si on a un tuple
                    // Si on en a un c'est ok on continue
                    ResultSet res  = verifReq(db, r2, T, metaData, orderAttribut, attrLies, attrLibres, rsmd);
                    
                    // On doit ajouter un tuple si il n'y a rien
                    if (!res.next()) {
                        System.out.println("Ajout de tuple");
                        ret = 1;
                        // Pour toutes les var libres, on regarde dans les metadonnees si le type commence par NULL_
                        // Si ce n'est pas le cas on doit alterer la table                        

                        for(Integer index : attrLibres){
                            String columnTypeName = metaData.getColumnTypeName(index + 1);
                            String nom_attr = metaData.getColumnLabel(index + 1);
                            System.out.println(columnTypeName);
                            if(!columnTypeName.startsWith("null_")){
                                String alterReq = buildCreateTypeReq(nom_attr, columnTypeName, r2.getNomTable());
                                db.updateRequest(alterReq);
                                System.out.println("Create Type null_" + columnTypeName);
                            }
                        }

                        // On insère le tuple
                        insertReq(db, r2, T, attrLies, attrLibres);
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
     * Ne vérifie pas que la tête d’une contrainte n’est pas satisfaite pour l’appliquer.
     * 
     * @param req La requête qui permet d'obtenir les tuples qui respectent le corps
     * @param db La base de donnée
     * @param mode OBLIVIOUS ou SKOLEM
     * 
     * @return -1 en cas d'erreur. 0 si la chase doit terminer. 1 Si la chase doit continuer.
     */
    @Override
    public int actionOblivious(String req, Database db, ChaseMode mode) throws SQLException {
        try {
            // Valeur de retour
            int ret = 0;

            // Regarde si on ajoute au moins un tuple dans la contrainte
            boolean tupleAjoute = true;
            if (mode == ChaseMode.SKOLEM) tupleAjoute = false;

            // On récupère les tuples qui respectent le corps
            ResultSet T = db.selectRequest(req);
            ResultSetMetaData rT = T.getMetaData();

            // On peut avoir plusieurs attribut avec le même nom, on a besoin donc de l'ordre dans les attributs du tuple
            ArrayList<Attribut> orderAttribut = new ArrayList<>();
            for(Relation rel : rlCorps) {
                for(Attribut a : rel.getMembres())
                    orderAttribut.add(a);
            }

            // Pour chaque tuple
            while(T.next()) {
                ArrayList<Valeur> tuple = new ArrayList<>();
                for(int i = 1; i <= T.getMetaData().getColumnCount(); i++) {
                    tuple.add(new Valeur(T.getMetaData().getColumnTypeName(i), T.getObject(i), false));
                }
                if(treated.contains(tuple)) continue;
                treated.add(tuple);
                printTuple(T, orderAttribut.size());

                if (mode == ChaseMode.SKOLEM) {
                    if(!needToAdd(T, rT, db, nullGeneres, orderAttribut))
                    continue;
                }

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

                    // La différence entre Standard Chase et Oblivious Chase est ici :
                    // une contrainte peut s’appliquer alors qu’il y a déjà un tuple dans la base de donnée qui la satisfait.
                    // On a donc plus besoin de faire une verifReq et on applique directement l'ajout de tuple.
                    ResultSetMetaData metaData = db.getMetaData(r2.getNomTable());
        
                    System.out.println("Ajout de tuple");
                    ret = 1;
                    if (mode == ChaseMode.SKOLEM) tupleAjoute = true;

                    // Pour toutes les var libres, on regarde dans les metadonnees si le type commence par NULL_
                    // Si ce n'est pas le cas on doit alterer la table                        
                    for(Integer index : attrLibres){
                        String columnTypeName = metaData.getColumnTypeName(index + 1);
                        String nom_attr = metaData.getColumnLabel(index + 1);
                        if(!columnTypeName.startsWith("null_")){
                            String alterReq = buildCreateTypeReq(nom_attr, columnTypeName, r2.getNomTable());
                            db.updateRequest(alterReq);
                            System.out.println("Create Type null_" + columnTypeName);
                        }
                    }

                    // On insère le tuple
                    insertReq(db, r2, T, attrLies, attrLibres);

                }
            }
            // Si on a ajouté aucun tuple on peut stopper la chase
            if(mode == ChaseMode.SKOLEM  && !tupleAjoute) return 0;
            return ret;

        } catch (SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    public boolean needToAdd(ResultSet T, ResultSetMetaData rT, Database db, HashMap<ArrayList<Valeur>, Integer> nullGeneres, ArrayList<Attribut> orderAttribut) throws SQLException {
        ArrayList<Valeur> valeursLiees = new ArrayList<Valeur>();
        
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

            // Comme pour Oblivious on ne vérifie pas si un tuple vérifie la contrainte
            // On essaye directement d'ajouter le tuple : il faut maintenant vérifier si le tuple est présent dans null_generes ou non

            // On récupère les valeurs des attributs liés
            for(int i : attrLies) {
                valeursLiees.add(new Valeur(rT.getColumnTypeName(i + 1), T.getObject(i + 1), false));
                System.out.print("+ " + rT.getColumnTypeName(i + 1) + " " + T.getString(i + 1) + " ");
            }
            System.out.println();
        }  

        if(nullGeneres.get(valeursLiees) == null) {
            nullGeneres.put(valeursLiees, num_null);
            return true;
        }
        return false;
    }

    /**
     * Fait l'union des tuples à ajouter avant d'ajouter les tuples en question
     * L'utilisation de HashSet gère les doublons automatiquement.
     *
     * @param req La requête qui permet d'obtenir les tuples qui respectent le corps
     * @param db La base de donnée
     * 
     * @return -1 en cas d'erreur. 0 si la chase doit terminer. 1 Si la chase doit continuer.
     */
    @Override
    public int actionCore(String req, Database db, HashSet<Tuple> toAdd) throws SQLException {
        try {
            // Valeur de retour
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
                ArrayList<Valeur> tuple = new ArrayList<>();
                for(int i = 1; i <= T.getMetaData().getColumnCount(); i++) {
                    tuple.add(new Valeur(T.getMetaData().getColumnTypeName(i), T.getObject(i), false));
                }
                if(treated.contains(tuple)) continue;
                treated.add(tuple);
                printTuple(T, orderAttribut.size());

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

                    ResultSetMetaData metaData = db.getMetaData(r2.getNomTable());
                    // Vérifier si on a un tuple
                    // Si on en a un c'est ok on continue
                    ResultSet res  = verifReq(db, r2, T, metaData, orderAttribut, attrLies, attrLibres, rsmd);
                    
                    // On doit ajouter un tuple si il n'y a rien
                    // Ici l'ajout de tuple se fait non pas dans la BD mais dans la liste
                    if (!res.next()) {
                        ret = 1;
                        // Pour toutes les var libres, on regarde dans les metadonnees si le type commence par NULL_
                        // Si ce n'est pas le cas on doit alterer la table                        
                        for(Integer index : attrLibres){
                            String columnTypeName = metaData.getColumnTypeName(index + 1);
                            String nom_attr = metaData.getColumnLabel(index + 1);
                            System.out.println(columnTypeName);
                            if(!columnTypeName.startsWith("null_")){
                                String alterReq = buildCreateTypeReq(nom_attr, columnTypeName, r2.getNomTable());
                                db.updateRequest(alterReq);
                                System.out.println("Create Type null_" + columnTypeName);
                            }
                        }
                            
                        // On ajoute notre tuple dans la liste de tuple
                        // Le 1e element sera le nom de la table !
                        int jlies = 0;
                        ArrayList<Valeur> add = new ArrayList<>();
                        for(int i = 0; i < metaData.getColumnCount(); i++) {
                            if (indexInList(i, attrLibres)) {
                                num_null++;
                                add.add(new Valeur("un type", "(" + String.valueOf(num_null) + ", NULL)", true));
                            } 
                            else {
                                add.add(new Valeur(metaData.getColumnTypeName(attrLies.get(jlies) + 1), T.getObject(attrLies.get(jlies) + 1),false));
                                jlies++;
                            }
                        }
                        toAdd.add(new Tuple(r2.getNomTable(), add));
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
     * Vérifies si la TGD est satisfaite par la db
     * 
     * @param req La requête qui permet d'obtenir les tuples qui respectent le corps
     * @param db La base de donnée
     * 
     * @return Un booléen
     */
    public boolean actionSatisfy(String req, Database db) throws SQLException {
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
                printTuple(T, orderAttribut.size());

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

                    ResultSetMetaData metaData = db.getMetaData(r2.getNomTable());
                    // Vérifier si on a un tuple
                    // Si on en a un c'est ok on continue
                    ResultSet res  = verifReq(db, r2, T, metaData, orderAttribut, attrLies, attrLibres, rsmd);
                    
                    // Si on doit ajouter un tuple ça veut dire que la TGD n'est pas satisfaite
                    if (!res.next()) return false;
                }
            }

            return true;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construit la requete vérificatrice d'une relation de la tête
     * 
     * @param r2 La relation de la tête
     * @param T L'ensemble des tuples respectant le corps
     * @param rsmd Les métadonées
     * @param orderAttribut L'ordre des attributs dans la relation
     * @param attrLies Liste d'indice des attributs liés
     * @param attrLibres Liste d'indice des attributs libres
     * 
     * @return La requête de vérification
     * @throws SQLException
     */
    private ResultSet verifReq(Database db, Relation r2, ResultSet T, ResultSetMetaData rsmd, ArrayList<Attribut> orderAttribut, ArrayList<Integer> attrLies, ArrayList<Integer> attrLibres, ResultSetMetaData rsmdT) throws SQLException{
        ArrayList<Object> values = new ArrayList<>();

        String sql = "SELECT * FROM " + r2.getNomTable() + " WHERE ";

        int jlies = 0;
        for(int i = 0; i<r2.getMembres().size(); i++) {
            if(!indexInList(i, attrLibres)) {
                Object o = T.getObject(attrLies.get(jlies) + 1);
                values.add(o);

                if(!rsmdT.getColumnTypeName(attrLies.get(jlies) + 1).startsWith("null")
                   && rsmd.getColumnTypeName(i + 1).startsWith("null")) {
                    sql += rsmd.getColumnLabel(i + 1) + " = to_" + rsmd.getColumnTypeName(i + 1) + "(?) AND "; 
                } else
                    sql += rsmd.getColumnLabel(i + 1) + " = ? AND ";
                jlies++;
            }
        }

        sql = sql.substring(0, sql.length() - 5);

        return db.selectRequest(sql, values);
    }

    /**
     * Génére la requete créant une nouveau type NULL.
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
     * Construit la requete d'insertion et l'exécute.
     * Cette fonction s'occupe d'incrémenter num_null.
     * 
     * @param db La base de donnée
     * @param r La relation
     * @param T Le Tuple
     * @param attrLies Liste d'indice des attributs lies
     * @param attrLibres Liste d'indice des attributs libres
     * 
     * @throws SQLException
     */
    private void insertReq(Database db, Relation r, ResultSet T, ArrayList<Integer> attrLies, ArrayList<Integer> attrLibres) throws SQLException{
        String req = "INSERT INTO " + r.getNomTable() + " VALUES (";
        ArrayList<Valeur> values = new ArrayList<Valeur>();
        int jlies = 0;

        ResultSetMetaData rsmd = T.getMetaData();
        ResultSetMetaData rTable = db.getMetaData(r.getNomTable());

        for(int i = 0; i < r.getMembres().size(); i++) {
            if(indexInList(i, attrLibres)) {
                num_null++;
                Valeur v = new Valeur("un type", "(" + num_null + ", NULL)", true);
                values.add(v);
                req += v.addStringReq("un type") + ", ";
            } else {
                Valeur v = new Valeur(rsmd.getColumnTypeName(attrLies.get(jlies) + 1), T.getObject(attrLies.get(jlies) + 1), false);
                values.add(v);
                req += v.addStringReq(rTable.getColumnTypeName(i + 1)) + ", ";
                jlies++;
            }
        }
        
        req = req.substring(0, req.length() - 2) + ")";
        db.insertRequest(req, values);
    }

    /** Vérifies si un index se trouve dans une liste d'index */
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
