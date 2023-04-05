package pied;

import java.io.InputStream;
import java.util.Scanner;

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

    // ! On doit se déconecter à chaque fois
    private Connection conn;

    /** Constructeur */
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
    public void connect() {
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void close() throws SQLException {
        conn.close();
    }

    /** Traite une requete SELECT */
    public ResultSet selectRequest(String selectSQL){
        connect();
        ResultSet res = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectSQL);
            res = pstmt.executeQuery();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public ResultSet selectUpdate(String selectSQL) {
        ResultSet res = null;
        connect();
        try {
            PreparedStatement stmt = conn.prepareStatement(selectSQL,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
            res = stmt.executeQuery();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public int updateRequest(String updateSQL) {
        connect();
        int res = 0;

        try {
            PreparedStatement pstmt = conn.prepareStatement(updateSQL);
            res = pstmt.executeUpdate();
            conn.close();
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

    public void ChangeType(String nomTable, String type, String attr) {
        String req = "ALTER TABLE " + nomTable + 
        " ALTER COLUMN " + attr + " TYPE " + type +
        " USING " + attr + "::" + type;
        System.out.println(req);
        PreparedStatement pstmt;
        try {
            pstmt = conn.prepareStatement(req);
            pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
