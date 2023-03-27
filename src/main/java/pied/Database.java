package pied;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class Database {
    private String url;
    private String user;
    private String password;
    private Connection conn;

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;

    }
    
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return conn;
    }

    public ResultSet selectRequest(String selectSQL){
        conn = connect();
        ResultSet res = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(selectSQL);
            res = pstmt.executeQuery();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return res;
    }

    public ResultSet selectUpdate(String selectSQL) {
        ResultSet rs = null;
        conn = connect();
        try {
            PreparedStatement stmt = conn.prepareStatement(selectSQL,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery();
            //conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rs;
    }

    public void closeConnexion() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int updateRequest(String updateSQL) {
        conn = connect();
        int res = 0;

        try {
            PreparedStatement pstmt = conn.prepareStatement(updateSQL);
            res = pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return res;
    }
}
