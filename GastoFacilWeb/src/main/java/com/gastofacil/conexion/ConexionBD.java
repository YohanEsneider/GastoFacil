package com.gastofacil.conexion;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexionBD {

    private static final String URL = System.getenv("DB_URL") != null 
            ? System.getenv("DB_URL") 
            : "jdbc:mysql://gastofacil-db-yohangarcia88-94cc.g.aivencloud.com:18310/defaultdb?useSSL=true&requireSSL=false&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String USER = System.getenv("DB_USER") != null 
            ? System.getenv("DB_USER") 
            : "avnadmin";

    private static final String PASSWORD = System.getenv("DB_PASS"); 

    public static Connection getConexion() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("¡Conexión exitosa a MySQL Aiven desde el entorno Web!");
        } catch (Exception e) {
            System.err.println("Error de conexión web: " + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    public static Connection conectar() {
        return getConexion();
    }
}
