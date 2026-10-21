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

@WebServlet("/api/proveedores")
public class ApiProveedoresServlet extends HttpServlet {

    private void aplicarCors(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
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

    private void registrarAuditoria(Connection con, int idTienda, String usuario, String modulo, String accion, String descripcion) {
        try {
            if (idTienda <= 0) idTienda = 1;
            if (usuario == null || usuario.trim().isEmpty()) usuario = "Usuario";

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
        } catch (Exception e) {
            System.err.println("Audit Proveedores Ignorado: " + e.getMessage());
        }
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

            String sql = "SELECT * FROM proveedores WHERE id_tienda = ? ORDER BY id_proveedor DESC";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idTienda);
                try (ResultSet rs = ps.executeQuery()) {
                    StringBuilder jsonArr = new StringBuilder("[");
                    boolean primero = true;
                    while (rs.next()) {
                        if (!primero) jsonArr.append(",");
                        primero = false;

                        jsonArr.append("{");
                        jsonArr.append("\"id_proveedor\":").append(rs.getInt("id_proveedor")).append(",");
                        jsonArr.append("\"idProveedor\":").append(rs.getInt("id_proveedor")).append(",");
                        jsonArr.append("\"id_tienda\":").append(rs.getInt("id_tienda")).append(",");
                        jsonArr.append("\"nombre\":\"").append(escapeJson(rs.getString("nombre"))).append("\",");
                        jsonArr.append("\"telefono\":\"").append(escapeJson(rs.getString("telefono"))).append("\",");
                        jsonArr.append("\"categoria\":\"").append(escapeJson(rs.getString("categoria"))).append("\",");
                        jsonArr.append("\"dias_visita\":\"").append(escapeJson(rs.getString("dias_atencion"))).append("\",");
                        jsonArr.append("\"diasVisita\":\"").append(escapeJson(rs.getString("dias_atencion"))).append("\"");
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

            int idProveedor = extraerNumeroJson(jsonBody, "idProveedor");
            if (idProveedor == 0) idProveedor = extraerNumeroJson(jsonBody, "id_proveedor");

            String nombre = extraerValorJson(jsonBody, "nombre");
            String telefono = extraerValorJson(jsonBody, "telefono");
            String categoria = extraerValorJson(jsonBody, "categoria");
            String diasVisita = extraerValorJson(jsonBody, "diasVisita");
            if (diasVisita.isEmpty()) diasVisita = extraerValorJson(jsonBody, "dias_visita");

            int idTienda = extraerNumeroJson(jsonBody, "id_tienda");
            if (idTienda == 0) idTienda = extraerNumeroJson(jsonBody, "idTienda");
            if (idTienda == 0) idTienda = 1;

            String usuarioAuditoria = extraerValorJson(jsonBody, "usuario");
            if (usuarioAuditoria.isEmpty()) usuarioAuditoria = request.getHeader("X-Usuario-Nombre");
            if (usuarioAuditoria == null || usuarioAuditoria.isEmpty()) usuarioAuditoria = "Usuario";

            try (Connection con = ConexionBD.getConexion()) {
                if (con == null) {
                    out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error BD\"}");
                    return;
                }

                garantizarTienda(con, idTienda, usuarioAuditoria);

                if (idProveedor > 0) {
                    String sqlUpdate = "UPDATE proveedores SET nombre = ?, telefono = ?, categoria = ?, dias_atencion = ? WHERE id_proveedor = ? AND id_tienda = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                        ps.setString(1, nombre);
                        ps.setString(2, telefono);
                        ps.setString(3, categoria);
                        ps.setString(4, diasVisita);
                        ps.setInt(5, idProveedor);
                        ps.setInt(6, idTienda);
                        ps.executeUpdate();
                    }

                    registrarAuditoria(con, idTienda, usuarioAuditoria, "Proveedores", "EDICIÓN", "Actualizado: " + nombre);

                    out.print("{\"success\":true,\"status\":\"OK\",\"estatus\":\"Exitoso\",\"mensaje\":\"Proveedor actualizado.\",\"id_proveedor\":" + idProveedor + "}");
                } else {
                    String sqlInsert = "INSERT INTO proveedores (id_tienda, nombre, telefono, categoria, dias_atencion) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idTienda);
                        ps.setString(2, nombre);
                        ps.setString(3, telefono);
                        ps.setString(4, categoria);
                        ps.setString(5, diasVisita.isEmpty() ? "No especificado" : diasVisita);
                        ps.executeUpdate();

                        int idGen = 0;
                        try (ResultSet rsKey = ps.getGeneratedKeys()) {
                            if (rsKey.next()) idGen = rsKey.getInt(1);
                        }

                        registrarAuditoria(con, idTienda, usuarioAuditoria, "Proveedores", "CREACIÓN", "Creado: " + nombre);

                        out.print("{\"success\":true,\"status\":\"OK\",\"estatus\":\"Exitoso\",\"mensaje\":\"Proveedor registrado.\",\"id_proveedor\":" + idGen + "}");
                    }
                }
            }
        } catch (Exception e) {
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String idProvStr = request.getParameter("id");
            if (idProvStr == null || idProvStr.isEmpty()) idProvStr = request.getParameter("idProveedor");

            int idProveedor = Integer.parseInt(idProvStr);
            int idTienda = 1;

            try (Connection con = ConexionBD.getConexion()) {
                if (con != null) {
                    String sqlCheck = "SELECT COUNT(*) FROM pedidos WHERE id_proveedor = ?";
                    try (PreparedStatement psC = con.prepareStatement(sqlCheck)) {
                        psC.setInt(1, idProveedor);
                        try (ResultSet rs = psC.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Tiene pedidos asociados.\"}");
                                return;
                            }
                        }
                    }

                    String sqlDel = "DELETE FROM proveedores WHERE id_proveedor = ?";
                    try (PreparedStatement psD = con.prepareStatement(sqlDel)) {
                        psD.setInt(1, idProveedor);
                        psD.executeUpdate();
                    }

                    registrarAuditoria(con, idTienda, "Usuario", "Proveedores", "ELIMINACIÓN", "Eliminado #" + idProveedor);
                    out.print("{\"success\":true,\"status\":\"OK\",\"estatus\":\"Exitoso\",\"resultado\":true,\"mensaje\":\"Proveedor eliminado.\"}");
                }
            }
        } catch (Exception e) {
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error al eliminar\"}");
        }
    }

    private int extraerNumeroJson(String json, String clave) {
        if (json == null || json.isEmpty()) return 0;
        try {
            String patron = "\"" + clave + "\"";
            int idx = json.indexOf(patron);
            if (idx == -1) return 0;
            int idxDosPuntos = json.indexOf(":", idx);
            StringBuilder sb = new StringBuilder();
            for (int i = idxDosPuntos + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (Character.isDigit(c)) sb.append(c);
                else if (sb.length() > 0) break;
            }
            return Integer.parseInt(sb.toString());
        } catch (Exception e) { return 0; }
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
