package com.mycompany.gastofacil.conexion;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3306/gastofacil";
    private static final String USER = "root";
    
    
    private static final String PASSWORD = "brallan0929"; 

    public static Connection conectar() {
        try {
            // Carga el conector de memoria
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("¡Conexión exitosa a la base de datos gastofacil!");
            return con;
        } catch (Exception e) {
            System.out.println("Error al conectar: " + e.getMessage());
            return null;
        }
    }
}