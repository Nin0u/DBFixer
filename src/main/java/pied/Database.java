package pied;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private String url;
    private String user;
    private String password;
    private Connection conn;

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;

        conn = connect();
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
}
