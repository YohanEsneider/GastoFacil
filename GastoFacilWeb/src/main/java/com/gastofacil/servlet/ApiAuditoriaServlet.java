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

@WebServlet({"/api/auditoria", "/api/historial/auditoria"})
public class ApiAuditoriaServlet extends HttpServlet {

    private void aplicarCors(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    private void garantizarTienda(Connection con, int idTienda, String nombre) {
        if (idTienda <= 0) return;
        try {
            String sqlCheck = "SELECT COUNT(*) FROM tienda WHERE id_tienda = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlCheck)) {
                ps.setInt(1, idTienda);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        String sqlInsert = "INSERT INTO tienda (id_tienda, nombre) VALUES (?, ?)";
                        try (PreparedStatement psI = con.prepareStatement(sqlInsert)) {
                            psI.setInt(1, idTienda);
                            psI.setString(2, (nombre != null && !nombre.isEmpty()) ? nombre : "Tienda " + idTienda);
                            psI.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String idTiendaStr = request.getParameter("idTienda");
        if (idTiendaStr == null || idTiendaStr.isEmpty()) idTiendaStr = request.getHeader("idTienda");
        if (idTiendaStr == null || idTiendaStr.isEmpty()) idTiendaStr = request.getHeader("X-Usuario-Id");

        int idTienda = 1;
        if (idTiendaStr != null && !idTiendaStr.isEmpty()) {
            try { idTienda = Integer.parseInt(idTiendaStr); } catch (Exception ignored) {}
        }

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                out.print("[]");
                return;
            }

            String sql = "SELECT * FROM auditoria WHERE id_tienda = ? ORDER BY id_auditoria DESC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idTienda);
                try (ResultSet rs = ps.executeQuery()) {
                    StringBuilder jsonArr = new StringBuilder("[");
                    boolean primero = true;
                    while (rs.next()) {
                        if (!primero) jsonArr.append(",");
                        primero = false;

                        jsonArr.append("{");
                        jsonArr.append("\"id_auditoria\":").append(rs.getInt("id_auditoria")).append(",");
                        jsonArr.append("\"fechaHora\":\"").append(rs.getString("fecha_hora")).append("\",");
                        jsonArr.append("\"fecha_hora\":\"").append(rs.getString("fecha_hora")).append("\",");
                        jsonArr.append("\"usuario\":\"").append(escapeJson(rs.getString("usuario"))).append("\",");
                        jsonArr.append("\"modulo\":\"").append(escapeJson(rs.getString("modulo"))).append("\",");
                        jsonArr.append("\"accion\":\"").append(escapeJson(rs.getString("accion"))).append("\",");
                        jsonArr.append("\"descripcion\":\"").append(escapeJson(rs.getString("descripcion"))).append("\"");
                        jsonArr.append("}");
                    }
                    jsonArr.append("]");
                    out.print(jsonArr.toString());
                }
            }
        } catch (Exception e) {
            out.print("[]");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) sb.append(line);
            }

            String jsonBody = sb.toString();

            String usuario = extraerValorJson(jsonBody, "usuario");
            if (usuario.isEmpty()) usuario = "Usuario";

            String modulo = extraerValorJson(jsonBody, "modulo");
            if (modulo.isEmpty()) modulo = "General";

            String accion = extraerValorJson(jsonBody, "accion");
            if (accion.isEmpty()) accion = "SISTEMA";

            String descripcion = extraerValorJson(jsonBody, "descripcion");

            int idTienda = 1;
            try {
                String idT = extraerValorJson(jsonBody, "idTienda");
                if (!idT.isEmpty()) idTienda = Integer.parseInt(idT);
            } catch (Exception ignored) {}

            try (Connection con = ConexionBD.getConexion()) {
                if (con != null) {
                    garantizarTienda(con, idTienda, usuario);

                    String sql = "INSERT INTO auditoria (id_tienda, usuario, modulo, accion, descripcion) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setInt(1, idTienda);
                        ps.setString(2, usuario);
                        ps.setString(3, modulo);
                        ps.setString(4, accion);
                        ps.setString(5, descripcion);
                        ps.executeUpdate();
                    }
                }
                out.print("{\"success\":true,\"status\":\"OK\",\"mensaje\":\"Auditoría registrada.\"}");
            }
        } catch (Exception e) {
            out.print("{\"success\":false,\"status\":\"error\",\"mensaje\":\"Error al guardar auditoría.\"}");
        }
    }

    private String extraerValorJson(String json, String clave) {
        if (json == null || json.isEmpty()) return "";
        try {
            String patron = "\"" + clave + "\"";
            int idx = json.indexOf(patron);
            if (idx == -1) return "";
            int p1 = json.indexOf("\"", json.indexOf(":", idx));
            int p2 = json.indexOf("\"", p1 + 1);
            return json.substring(p1 + 1, p2).trim();
        } catch (Exception e) { return ""; }
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
