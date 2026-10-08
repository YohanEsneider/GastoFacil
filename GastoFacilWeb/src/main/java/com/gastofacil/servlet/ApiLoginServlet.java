package com.gastofacil.servlet;

import com.gastofacil.conexion.ConexionBD;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/login")
public class ApiLoginServlet extends HttpServlet {

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
        String usuarioInput = extraerValorJson(jsonBody, "usuario").trim();
        if (usuarioInput.isEmpty()) {
            usuarioInput = extraerValorJson(jsonBody, "correo").trim();
        }
        String passwordInput = extraerValorJson(jsonBody, "password").trim();

        if (usuarioInput.isEmpty() || passwordInput.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Debe proporcionar usuario y contraseña.\"}");
            return;
        }

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error de conexión con la base de datos.\"}");
                return;
            }

            boolean autenticado = false;
            int idTiendaBD = 0;
            String nombreUsuario = "";
            String correo = "";

            String sqlTienda = "SELECT * FROM tienda WHERE LOWER(TRIM(correo)) = LOWER(?) OR LOWER(TRIM(nombre_tienda)) = LOWER(?)";
            try (PreparedStatement ps = con.prepareStatement(sqlTienda)) {
                ps.setString(1, usuarioInput);
                ps.setString(2, usuarioInput);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String passBD = rs.getString("password");
                        if (passBD != null && passBD.trim().equals(passwordInput)) {
                            autenticado = true;
                            idTiendaBD = rs.getInt("id_tienda");
                            nombreUsuario = rs.getString("nombre_tienda") != null ? rs.getString("nombre_tienda") : "Tienda";
                            correo = rs.getString("correo") != null ? rs.getString("correo") : usuarioInput;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al consultar la tabla tienda: " + e.getMessage());
            }

            if (autenticado) {
                int idTiendaFinal = (idTiendaBD > 0) ? idTiendaBD : 1;

                HttpSession session = request.getSession(true);
                session.setAttribute("idTienda", idTiendaFinal);
                session.setAttribute("nombreUsuario", nombreUsuario);

                response.setStatus(HttpServletResponse.SC_OK);
                StringBuilder jsonResp = new StringBuilder("{");
                jsonResp.append("\"success\":true,");
                jsonResp.append("\"status\":\"success\",");
                jsonResp.append("\"estatus\":\"Exitoso\",");
                jsonResp.append("\"mensaje\":\"Login exitoso\",");
                jsonResp.append("\"idUsuario\":").append(idTiendaFinal).append(",");
                jsonResp.append("\"id_usuario\":").append(idTiendaFinal).append(",");
                jsonResp.append("\"idTienda\":").append(idTiendaFinal).append(",");
                jsonResp.append("\"id_tienda\":").append(idTiendaFinal).append(",");
                jsonResp.append("\"nombreUsuario\":\"").append(escapeJson(nombreUsuario)).append("\",");
                jsonResp.append("\"usuario\":\"").append(escapeJson(nombreUsuario)).append("\",");
                jsonResp.append("\"correo\":\"").append(escapeJson(correo)).append("\",");
                jsonResp.append("\"rol\":\"ADMIN\"");
                jsonResp.append("}");

                out.print(jsonResp.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Usuario o contraseña incorrectos.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error interno en el servidor.\"}");
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

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
