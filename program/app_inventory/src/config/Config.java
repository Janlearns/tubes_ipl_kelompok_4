package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Config {
    private static Connection mysqlconfig;

    public static Connection configDB() throws SQLException {
        try {
            // Sesuaikan nama database, user, dan password Anda
            String url = "jdbc:mysql://localhost:3306/management_inventory";
            String user = "root";
            String pass = "";

            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            mysqlconfig = DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            System.err.println("Koneksi Gagal: " + e.getMessage());
        }
        return mysqlconfig;
    }
}