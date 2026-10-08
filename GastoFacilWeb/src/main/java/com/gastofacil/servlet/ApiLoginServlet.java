package com.gastofacil.servlet;

import com.gastofacil.conexion.ConexionBD;
import com.gastofacil.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, X-Usuario-Id, idTienda");
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
        String passwordInput = extraerValorJson(jsonBody, "password").trim();

        if (usuarioInput.isEmpty() || passwordInput.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Debe proporcionar usuario y contraseña.\"}");
            return;
        }

        String passwordHashSHA = generarSHA256(passwordInput);

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error de conexión con la base de datos.\"}");
                return;
            }

            boolean autenticado = false;
            int idTiendaBD = 0;
            int idUsuarioBD = 0;
            String nombreUsuario = "";
            String correo = "";
            String rol = "ADMIN";

            // Búsqueda en la tabla 'usuarios'
            String sqlUsuarios = "SELECT * FROM usuarios WHERE LOWER(TRIM(usuario)) = LOWER(?) OR LOWER(TRIM(correo)) = LOWER(?) OR LOWER(TRIM(nombre)) = LOWER(?)";
            try (PreparedStatement ps = con.prepareStatement(sqlUsuarios)) {
                ps.setString(1, usuarioInput);
                ps.setString(2, usuarioInput);
                ps.setString(3, usuarioInput);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String passBD = rs.getString("password");
                        if (validarPassword(passwordInput, passwordHashSHA, passBD)) {
                            autenticado = true;
                            try { idUsuarioBD = rs.getInt("id_usuario"); } catch (Exception e) { 
                                try { idUsuarioBD = rs.getInt("id"); } catch (Exception ignored) {} 
                            }
                            try { idTiendaBD = rs.getInt("id_tienda"); } catch (Exception ignored) {}
                            nombreUsuario = rs.getString("nombre") != null && !rs.getString("nombre").trim().isEmpty() 
                                    ? rs.getString("nombre") : rs.getString("usuario");
                            correo = rs.getString("correo") != null ? rs.getString("correo") : usuarioInput;
                        }
                    }
                }
            } catch (Exception ignored) {}

            // Búsqueda en la tabla 'tienda'
            if (!autenticado) {
                String sqlTienda = "SELECT * FROM tienda WHERE LOWER(TRIM(correo)) = LOWER(?) OR LOWER(TRIM(nombre_tienda)) = LOWER(?) OR LOWER(TRIM(nombre)) = LOWER(?) OR LOWER(TRIM(usuario)) = LOWER(?)";
                try (PreparedStatement ps = con.prepareStatement(sqlTienda)) {
                    ps.setString(1, usuarioInput);
                    ps.setString(2, usuarioInput);
                    ps.setString(3, usuarioInput);
                    ps.setString(4, usuarioInput);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String passBD = rs.getString("password");
                            if (validarPassword(passwordInput, passwordHashSHA, passBD)) {
                                autenticado = true;
                                try { idTiendaBD = rs.getInt("id_tienda"); } catch (Exception ignored) {}
                                idUsuarioBD = idTiendaBD;
                                nombreUsuario = rs.getString("nombre_tienda") != null ? rs.getString("nombre_tienda") : "Tienda";
                                correo = rs.getString("correo") != null ? rs.getString("correo") : usuarioInput;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            if (autenticado) {
                int idTiendaFinal = (idTiendaBD > 0) ? idTiendaBD : ((idUsuarioBD > 0) ? idUsuarioBD : 1);

                Usuario u = new Usuario();
                u.setNombre(nombreUsuario);
                u.setUsuario(nombreUsuario);
                u.setRol(rol);

                HttpSession session = request.getSession(true);
                session.setAttribute("usuarioLogueado", u);
                session.setAttribute("idTienda", idTiendaFinal);
                session.setAttribute("nombreUsuario", nombreUsuario);
                session.setAttribute("rolUsuario", rol);

                response.setStatus(HttpServletResponse.SC_OK);
                StringBuilder jsonResp = new StringBuilder("{");
                jsonResp.append("\"success\":true,");
                jsonResp.append("\"status\":\"success\",");
                jsonResp.append("\"estatus\":\"Exitoso\",");
                jsonResp.append("\"mensaje\":\"Login exitoso\",");
                jsonResp.append("\"idUsuario\":").append(idUsuarioBD).append(",");
                jsonResp.append("\"id_usuario\":").append(idUsuarioBD).append(",");
                jsonResp.append("\"idTienda\":").append(idTiendaFinal).append(",");
                jsonResp.append("\"id_tienda\":").append(idTiendaFinal).append(",");
                jsonResp.append("\"nombreUsuario\":\"").append(escapeJson(nombreUsuario)).append("\",");
                jsonResp.append("\"usuario\":\"").append(escapeJson(nombreUsuario)).append("\",");
                jsonResp.append("\"correo\":\"").append(escapeJson(correo)).append("\",");
                jsonResp.append("\"rol\":\"").append(escapeJson(rol)).append("\"");
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

    private boolean validarPassword(String plainPass, String shaPass, String dbPass) {
        if (dbPass == null) return false;
        if (plainPass.equals(dbPass) || shaPass.equalsIgnoreCase(dbPass)) {
            return true;
        }
        try {
            return BCrypt.checkpw(plainPass, dbPass);
        } catch (Exception e) {
            return false;
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

    private String generarSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            return input;
        }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
