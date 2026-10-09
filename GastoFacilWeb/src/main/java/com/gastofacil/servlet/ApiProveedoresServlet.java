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
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, idTienda, X-Usuario-Id");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    private void registrarAuditoria(Connection con, int idTienda, String modulo, String accion, String descripcion) {
        try {
            String sql = "INSERT INTO auditoria (id_tienda, usuario, modulo, accion, descripcion) VALUES (?, 'Tienda Yohan', ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idTienda);
                ps.setString(2, modulo);
                ps.setString(3, accion);
                ps.setString(4, descripcion);
                ps.executeUpdate();
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // LISTAR PROVEEDORES
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String idTiendaStr = request.getParameter("idTienda");
        if (idTiendaStr == null || idTiendaStr.isEmpty()) {
            idTiendaStr = request.getHeader("idTienda");
        }
        
        int idTienda = 1;
        if (idTiendaStr != null && !idTiendaStr.isEmpty()) {
            try { idTienda = Integer.parseInt(idTiendaStr); } catch (NumberFormatException ignored) {}
        }

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(jsonArr.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("[]");
        }
    }

    // REGISTRAR O EDITAR PROVEEDOR
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        
        int idProveedor = extraerNumeroJson(jsonBody, "idProveedor");
        if (idProveedor == 0) idProveedor = extraerNumeroJson(jsonBody, "id_proveedor");
        if (idProveedor == 0) idProveedor = extraerNumeroJson(jsonBody, "id");

        String nombre = extraerValorJson(jsonBody, "nombre");
        String telefono = extraerValorJson(jsonBody, "telefono");
        String categoria = extraerValorJson(jsonBody, "categoria");
        String diasVisita = extraerValorJson(jsonBody, "diasVisita");
        if (diasVisita.isEmpty()) diasVisita = extraerValorJson(jsonBody, "dias_visita");

        int idTienda = extraerNumeroJson(jsonBody, "id_tienda");
        if (idTienda == 0) idTienda = extraerNumeroJson(jsonBody, "idTienda");
        if (idTienda == 0) idTienda = 1;

        if (nombre.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"El nombre del proveedor es obligatorio.\"}");
            return;
        }

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error de conexión con la base de datos.\"}");
                return;
            }

            if (idProveedor > 0) {
                // ACTUALIZAR PROVEEDOR
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

                // Registrar en Auditoría
                registrarAuditoria(con, idTienda, "Proveedores", "EDICIÓN", "Se actualizó la información del proveedor '" + nombre + "'");

                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"success\":true,\"status\":\"success\",\"estatus\":\"Exitoso\",\"mensaje\":\"Proveedor actualizado exitosamente.\",\"id_proveedor\":" + idProveedor + "}");
            } else {
                // INSERTAR NUEVO PROVEEDOR
                String sqlInsert = "INSERT INTO proveedores (id_tienda, nombre, telefono, categoria, dias_atencion) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idTienda);
                    ps.setString(2, nombre);
                    ps.setString(3, telefono);
                    ps.setString(4, categoria);
                    ps.setString(5, diasVisita.isEmpty() ? "No especificado" : diasVisita);

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        int idGenerado = 0;
                        try (ResultSet rsKey = ps.getGeneratedKeys()) {
                            if (rsKey.next()) idGenerado = rsKey.getInt(1);
                        }

                        // Registrar en Auditoría
                        registrarAuditoria(con, idTienda, "Proveedores", "CREACIÓN", "Se registró el nuevo proveedor '" + nombre + "'");

                        response.setStatus(HttpServletResponse.SC_CREATED);
                        out.print("{\"success\":true,\"status\":\"success\",\"estatus\":\"Exitoso\",\"mensaje\":\"Proveedor registrado exitosamente.\",\"id_proveedor\":" + idGenerado + "}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"No se pudo guardar el proveedor.\"}");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error interno al guardar proveedor.\"}");
        }
    }

    // ELIMINAR PROVEEDOR
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String idProvStr = request.getParameter("id");
        if (idProvStr == null || idProvStr.isEmpty()) idProvStr = request.getParameter("idProveedor");
        if (idProvStr == null || idProvStr.isEmpty()) idProvStr = request.getParameter("id_proveedor");

        int idProveedor = 0;
        if (idProvStr != null) {
            try { idProveedor = Integer.parseInt(idProvStr); } catch (NumberFormatException ignored) {}
        }

        if (idProveedor <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"ID de proveedor no válido.\"}");
            return;
        }

        int idTienda = 1;

        try (Connection con = ConexionBD.getConexion()) {
            if (con != null) {
                String sql = "DELETE FROM proveedores WHERE id_proveedor = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idProveedor);
                    ps.executeUpdate();
                }

                // Registrar en Auditoría
                registrarAuditoria(con, idTienda, "Proveedores", "ELIMINACIÓN", "Se eliminó el proveedor #" + idProveedor);

                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"success\":true,\"status\":\"success\",\"estatus\":\"Exitoso\",\"mensaje\":\"Proveedor eliminado con éxito.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error al eliminar el proveedor.\"}");
        }
    }

    private int extraerNumeroJson(String json, String clave) {
        if (json == null || json.isEmpty()) return 0;
        try {
            String patron = "\"" + clave + "\"";
            int indexClave = json.indexOf(patron);
            if (indexClave == -1) return 0;

            int indexDosPuntos = json.indexOf(":", indexClave);
            if (indexDosPuntos == -1) return 0;

            StringBuilder numSb = new StringBuilder();
            for (int i = indexDosPuntos + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (Character.isDigit(c)) {
                    numSb.append(c);
                } else if (numSb.length() > 0) {
                    break;
                }
            }
            if (numSb.length() > 0) return Integer.parseInt(numSb.toString());
        } catch (Exception ignored) {}
        return 0;
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
