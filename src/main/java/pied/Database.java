package pied;

import java.io.InputStream;
import java.util.Scanner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

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
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return conn;
    }

    /** Traite une requete SELECT */
    public ResultSet selectRequest(String selectSQL){
        conn = connect();
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
        conn = connect();
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
        conn = connect();
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
}
