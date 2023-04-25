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

    /** Traite une requete SELECT */
    public ResultSet selectRequest(String selectSQL) {
        ResultSet res = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectSQL);
            res = pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public ResultSet selectUpdate(String selectSQL)  {
        ResultSet res = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(selectSQL,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
            res = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public int updateQuery(String nomTable, String attr, Object o, ArrayList<String> attrs, ArrayList<Object> values) {
        int res = 0;

        String sql = "UPDATE " + nomTable + " SET " + attr + " = ? WHERE ";
        for(int i = 0; i < attrs.size(); i++)
            sql += attrs.get(i) + " = ? AND ";
        sql = sql.substring(0, sql.length() - 5);

        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, o);
            for(int i = 0; i < attrs.size(); i++) {
                stmt.setObject(i + 2, values.get(i));
            }
            System.out.println(stmt);
            res = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public ResultSet selectQuery(String sql, ArrayList<Object> values) {
        ResultSet res = null;
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
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

    public int updateRequest(String updateSQL) {
        int res = 0;

        try {
            PreparedStatement pstmt = conn.prepareStatement(updateSQL);
            res = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return res;
    }

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

    public int insertReq(String sql, ArrayList<Valeur> l) {
        int res = 0;

        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
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
}
