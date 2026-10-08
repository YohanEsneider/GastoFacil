package com.gastofacil.servlet;

import com.gastofacil.conexion.ConexionBD;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet("/api/registro")
public class ApiRegistroServlet extends HttpServlet {

    private void aplicarCors(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) buffer.append(line);
        String body = buffer.toString();

        String nombre = extraerTextoJson(body, "nombre").trim();
        String usuario = extraerTextoJson(body, "usuario").trim();
        String correo = extraerTextoJson(body, "correo").trim();
        String password = extraerTextoJson(body, "password").trim();

        // Si el usuario viene vacío, generar el nombre de usuario con el correo
        if (usuario.isEmpty() && !correo.isEmpty()) {
            usuario = correo.contains("@") ? correo.split("@")[0] : correo;
        }
        if (correo.isEmpty()) {
            correo = usuario;
        }

        if (correo.isEmpty() || password.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"estatus\":\"Error\",\"mensaje\":\"Todos los campos son obligatorios\"}");
            return;
        }

        try (Connection con = ConexionBD.getConexion()) {
            if (con != null) {
                con.setAutoCommit(false);

                // 1. Validar duplicados por usuario o correo
                String sqlCheck = "SELECT COUNT(*) FROM usuarios WHERE LOWER(usuario) = LOWER(?) OR LOWER(correo) = LOWER(?)";
                try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                    psCheck.setString(1, usuario);
                    psCheck.setString(2, correo);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                            con.rollback();
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                            out.print("{\"estatus\":\"Error\",\"mensaje\":\"El correo o usuario ya se encuentra registrado.\"}");
                            return;
                        }
                    }
                }

                // 2. Crear Tienda Independiente
                int idTienda = 0;
                String sqlTienda = "INSERT INTO tienda (nombre, nombre_tienda) VALUES (?, ?)";
                try (PreparedStatement psT = con.prepareStatement(sqlTienda, Statement.RETURN_GENERATED_KEYS)) {
                    String nombreT = "Tienda " + (nombre.isEmpty() ? usuario : nombre);
                    psT.setString(1, nombreT);
                    psT.setString(2, nombreT);
                    psT.executeUpdate();
                    try (ResultSet rsKeys = psT.getGeneratedKeys()) {
                        if (rsKeys.next()) idTienda = rsKeys.getInt(1);
                    }
                } catch (Exception ignored) {}

                String passHash = BCrypt.hashpw(password, BCrypt.gensalt());

                // 3. Insertar Usuario con id_tienda único
                boolean registrado = false;
                String sqlUser = "INSERT INTO usuarios (nombre, usuario, correo, password, rol, id_tienda) VALUES (?, ?, ?, ?, 'Administrador', ?)";
                try (PreparedStatement psUser = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, nombre.isEmpty() ? usuario : nombre);
                    psUser.setString(2, usuario);
                    psUser.setString(3, correo);
                    psUser.setString(4, passHash);
                    
                    if (idTienda > 0) {
                        psUser.setInt(5, idTienda);
                    } else {
                        psUser.setNull(5, java.sql.Types.INTEGER);
                    }
                    
                    psUser.executeUpdate();
                    
                    // Si id_tienda no se generó de la tabla tienda, asignarle su id_usuario
                    try (ResultSet rsU = psUser.getGeneratedKeys()) {
                        if (rsU.next()) {
                            int idUserGenerado = rsU.getInt(1);
                            if (idTienda <= 0) {
                                String sqlUpdateT = "UPDATE usuarios SET id_tienda = ? WHERE id_usuario = ?";
                                try (PreparedStatement psUp = con.prepareStatement(sqlUpdateT)) {
                                    psUp.setInt(1, idUserGenerado);
                                    psUp.setInt(2, idUserGenerado);
                                    psUp.executeUpdate();
                                }
                            }
                        }
                    }
                    registrado = true;
                }

                if (registrado) {
                    con.commit();
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print("{\"estatus\":\"Exitoso\",\"mensaje\":\"Cuenta registrada con éxito\"}");
                } else {
                    con.rollback();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"estatus\":\"Error\",\"mensaje\":\"No se pudo completar el registro.\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"estatus\":\"Error\",\"mensaje\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String extraerTextoJson(String json, String clave) {
        try {
            String p = "\"" + clave + "\"";
            int idx = json.indexOf(p);
            if (idx == -1) return "";
            int dp = json.indexOf(":", idx);
            if (dp == -1) return "";
            String sub = json.substring(dp + 1).trim();
            if (sub.startsWith("\"")) {
                int fc = sub.indexOf("\"", 1);
                return fc != -1 ? sub.substring(1, fc) : "";
            } else {
                int ie = sub.indexOf(",");
                if (ie == -1) ie = sub.indexOf("}");
                if (ie == -1) ie = sub.length();
                return sub.substring(0, ie).replace("\"", "").trim();
            }
        } catch (Exception e) { return ""; }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
