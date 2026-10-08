package com.gastofacil.servlet;

import com.gastofacil.conexion.ConexionBD;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/api/registro")
public class ApiRegistroServlet extends HttpServlet {

    private void aplicarCors(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        aplicarCors(request, response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String jsonBody = sb.toString();
        String nombreTienda = extraerValorJson(jsonBody, "nombre_tienda");
        if (nombreTienda.isEmpty()) {
            nombreTienda = extraerValorJson(jsonBody, "nombreTienda");
        }
        if (nombreTienda.isEmpty()) {
            nombreTienda = extraerValorJson(jsonBody, "nombre");
        }
        if (nombreTienda.isEmpty()) {
            nombreTienda = extraerValorJson(jsonBody, "usuario");
        }

        String correo = extraerValorJson(jsonBody, "correo");
        if (correo.isEmpty()) {
            correo = extraerValorJson(jsonBody, "email");
        }

        String password = extraerValorJson(jsonBody, "password");

        if (correo.isEmpty() || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Debe completar todos los campos obligatorios.\"}");
            return;
        }

        if (nombreTienda.isEmpty()) {
            nombreTienda = "Tienda " + (correo.contains("@") ? correo.split("@")[0] : correo);
        }

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error de conexión con la base de datos.\"}");
                return;
            }

            // Verificar si el correo ya existe
            String sqlCheck = "SELECT id_tienda FROM tienda WHERE LOWER(TRIM(correo)) = LOWER(?)";
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setString(1, correo);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"El correo electrónico ya está registrado.\"}");
                        return;
                    }
                }
            }

            // Insertar directamente en la tabla 'tienda'
            String sqlInsert = "INSERT INTO tienda (nombre_tienda, correo, password) VALUES (?, ?, ?)";
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setString(1, nombreTienda);
                psInsert.setString(2, correo);
                psInsert.setString(3, password);

                int rows = psInsert.executeUpdate();
                if (rows > 0) {
                    int idGenerado = 0;
                    try (ResultSet rsKey = psInsert.getGeneratedKeys()) {
                        if (rsKey.next()) {
                            idGenerado = rsKey.getInt(1);
                        }
                    }

                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print("{\"success\":true,\"status\":\"success\",\"estatus\":\"Exitoso\",\"mensaje\":\"Registro exitoso. Ya puedes iniciar sesión.\",\"id_tienda\":" + idGenerado + "}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"No se pudo registrar la tienda.\"}");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error en el servidor al registrar usuario.\"}");
        }
    }

    private String extraerValorJson(String json, String clave) {
        if (json == null || json.isEmpty()) return "";
        String patron = "\"" + clave + "\"";
        int indexClave = json.indexOf(patron);
        if (indexClave == -1) return "";

        int indexDosPuntos = json.indexOf(":", indexClave);
        if (indexDosPuntos == -1) return "";

        int primerComilla = json.indexOf("\"", indexDosPuntos);
        if (primerComilla == -1) return "";

        int segundaComilla = json.indexOf("\"", primerComilla + 1);
        if (segundaComilla == -1) return "";

        return json.substring(primerComilla + 1, segundaComilla).trim();
    }
}
