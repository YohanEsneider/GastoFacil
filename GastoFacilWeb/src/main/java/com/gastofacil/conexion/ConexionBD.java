package com.gastofacil.conexion;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexionBD {
    // 🔗 URL CORREGIDA: Apuntando al nombre real de tu esquema 'gastofacil'
    private static final String URL = "jdbc:mysql://localhost:3306/gastofacil?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    
    // 🔐 La contraseña de 8 caracteres que guardaste con éxito en tu Workbench
    private static final String PASSWORD = "Admin123*"; 

    public static Connection getConexion() {
        Connection con = null;
        try {
            // Carga obligatoria del Driver para entornos Web
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("¡Conexión exitosa a MySQL desde el entorno Web!");
        } catch (Exception e) {
            System.out.println("Error de conexión web: " + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }
}