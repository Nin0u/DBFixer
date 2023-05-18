package maindb;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import variable.Valeur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class Database {
    private String url;
    private String user;
    private String password;

    private Connection conn;

    /** 
     * Constructeur 
     * 
     * @param is Le flot d'entrée pour les login de la base.
     */
    public Database(InputStream is){
        Scanner sc = new Scanner(is);
        if(is.equals(System.in)) System.out.print("URL = ");
        String url = sc.nextLine();
        if(is.equals(System.in)) System.out.print("user = ");
        String user = sc.nextLine();
        if(is.equals(System.in)) System.out.print("password = ");
        String password = sc.nextLine();
        this.url = url;
        this.user = user;
        this.password = password;

        sc.close();
    }
    
    /** Connexion a la BD */
    public void connect() throws SQLException{
        conn = DriverManager.getConnection(url, user, password);
    }

    /** Fermeture de la connexion */
    public void close() throws SQLException {
        conn.close();
    }

    /** 
     * Traite une requete SELECT toute construite
     * 
     * @param request La requête
     * @return Le ResultSet contenant l'ensemble des tuples correspondant à request.
     */
    public ResultSet selectRequest(String request) {
        ResultSet res = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(request);
            res = pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    /**
     * Traite une requête SELECT avec insertion de valeur dans la requête.
     * Cette fonction est utilisée dans le cas d'une insertion d'une seule valeur 
     * dans la requête d'où l'absence de liste dans les paramètres.
     * 
     * @param request La requête.
     * @param l La Valeur à insérer dans la requête.
     * @return Le ResultSet contenant l'ensemble des tuples correspondant à request.
     */
    public ResultSet selectRequest(String request, Valeur l) {
        ResultSet res = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(request);
            l.addPreparedStatementReq(pstmt, 1);
            
            System.out.println(pstmt);
            res = pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

     /**
     * Traite une requête SELECT avec insertions de valeurs dans la requête.
     * Cette fonction est utilisée dans le cas de plusieurs insertions dans la requête.
     * 
     * @param request La requête.
     * @param values Les Valeurs à insérer dans la requête.
     * @return Le ResultSet contenant l'ensemble des tuples correspondant à request.
     */
    public ResultSet selectRequest(String request, ArrayList<Object> values) {
        ResultSet res = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(request);
            for(int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            System.out.println(stmt);
            res = stmt.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Effectue une insertions dans la base de données avec insertions de valeurs dans la requête.
     * 
     * @param request La requête d'insertion
     * @param l La liste des valeurs à insérer dans request
     * 
     * @return 
     */
    public int insertRequest(String request, ArrayList<Valeur> l) {
        int res = 0;

        try {
            PreparedStatement pstmt = conn.prepareStatement(request);
            int j = 1;
            for(int i = 0; i < l.size(); i++) {
                if(l.get(i).addPreparedStatementReq(pstmt, j)) j++;
            }
            System.out.println(pstmt);
            res = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Traite une requete UPDATE toute construite.
     * 
     * @param request La requête.
     * @return Le nombre de ligne mises à jour.
     */
    public int updateRequest(String request) {
        int res = 0;

        try {
            PreparedStatement pstmt = conn.prepareStatement(request);
            res = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return res;
    }

    /**
     * Récupère les métadonnées d'une table.
     * 
     * @param nomTable Le nom de la table
     */
    public ResultSetMetaData getMetaData(String nomTable) {
        ResultSetMetaData res = null;
        try {
            String SQL = "select * from " + nomTable + " where 1<0";
            PreparedStatement pstmt = conn.prepareStatement(SQL);
            ResultSet r = pstmt.executeQuery();
            res = r.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Execute une requête d'alteration de table pour changer 
     * le type d'une colonne.
     * 
     * @param nomTable Le nom de la table
     * @param type Le nouveau type
     * @param attr L'attribut qui change de type
     */
    public void changeType(String nomTable, String type, String attr) {
        String req = "ALTER TABLE " + nomTable + 
        " ALTER COLUMN " + attr + " TYPE " + type +
        " USING " + attr + "::" + type;
        System.out.println(req);
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(req);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
