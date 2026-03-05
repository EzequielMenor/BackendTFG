import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        String url = args[0];
        String user = args[1];
        String pass = args[2];
        System.out.println("Testing URL: " + url + " | User: " + user);
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connection successful!");
            conn.close();
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}