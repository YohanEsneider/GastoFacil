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

@WebServlet("/api/pedidos")
public class ApiPedidosServlet extends HttpServlet {

    private void aplicarCors(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        // Cabeceras explícitas (sin allow-credentials)
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, idTienda, X-Usuario-Id, X-Usuario-Nombre, Accept, Origin, X-Requested-With");
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
            System.err.println("Audit Pedidos Ignorado: " + e.getMessage());
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

            String sql = "SELECT p.*, pr.nombre AS nombre_proveedor " +
                         "FROM pedidos p " +
                         "LEFT JOIN proveedores pr ON p.id_proveedor = pr.id_proveedor " +
                         "WHERE p.id_tienda = ? ORDER BY p.id_pedido DESC";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idTienda);
                try (ResultSet rs = ps.executeQuery()) {
                    StringBuilder jsonArr = new StringBuilder("[");
                    boolean primero = true;
                    while (rs.next()) {
                        if (!primero) jsonArr.append(",");
                        primero = false;

                        int idPedido = rs.getInt("id_pedido");
                        String desc = rs.getString("descripcion");
                        if (desc == null) desc = "";
                        double valorTotal = rs.getDouble("valor_total");

                        jsonArr.append("{");
                        jsonArr.append("\"id_pedido\":").append(idPedido).append(",");
                        jsonArr.append("\"idPedido\":").append(idPedido).append(",");
                        jsonArr.append("\"id_tienda\":").append(rs.getInt("id_tienda")).append(",");
                        jsonArr.append("\"id_proveedor\":").append(rs.getInt("id_proveedor")).append(",");
                        jsonArr.append("\"idProveedor\":").append(rs.getInt("id_proveedor")).append(",");
                        jsonArr.append("\"proveedor\":\"").append(escapeJson(rs.getString("nombre_proveedor"))).append("\",");
                        jsonArr.append("\"fecha\":\"").append(rs.getString("fecha_compra")).append("\",");
                        jsonArr.append("\"fecha_compra\":\"").append(rs.getString("fecha_compra")).append("\",");
                        jsonArr.append("\"descripcion\":\"").append(escapeJson(desc)).append("\",");
                        jsonArr.append("\"resumen\":\"").append(escapeJson(desc)).append("\",");
                        jsonArr.append("\"metodo_pago\":\"").append(escapeJson(rs.getString("metodo_pago"))).append("\",");
                        jsonArr.append("\"metodoPago\":\"").append(escapeJson(rs.getString("metodo_pago"))).append("\",");
                        jsonArr.append("\"total\":").append(valorTotal).append(",");
                        jsonArr.append("\"valor_total\":").append(valorTotal);
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

            int idPedido = extraerNumeroJson(jsonBody, "idPedido");
            if (idPedido == 0) idPedido = extraerNumeroJson(jsonBody, "id_pedido");

            int idTienda = extraerNumeroJson(jsonBody, "id_tienda");
            if (idTienda == 0) idTienda = extraerNumeroJson(jsonBody, "idTienda");
            if (idTienda == 0) {
                String hdr = request.getHeader("idTienda");
                if (hdr != null && !hdr.isEmpty()) try { idTienda = Integer.parseInt(hdr); } catch(Exception e){}
            }
            if (idTienda == 0) idTienda = 1;

            int idProveedor = extraerNumeroJson(jsonBody, "idProveedor");
            if (idProveedor == 0) idProveedor = extraerNumeroJson(jsonBody, "id_proveedor");

            String fechaCompra = extraerValorJson(jsonBody, "fecha");
            if (fechaCompra.isEmpty()) fechaCompra = extraerValorJson(jsonBody, "fecha_compra");
            if (fechaCompra.isEmpty()) fechaCompra = java.time.LocalDate.now().toString();

            String descripcion = extraerValorJson(jsonBody, "descripcion");
            if (descripcion.isEmpty()) descripcion = extraerValorJson(jsonBody, "resumen");

            String metodoPago = extraerValorJson(jsonBody, "metodoPago");
            if (metodoPago.isEmpty()) metodoPago = extraerValorJson(jsonBody, "metodo_pago");
            if (metodoPago.isEmpty()) metodoPago = "Efectivo";

            double valorTotal = extraerDecimalJson(jsonBody, "valorTotal");
            if (valorTotal == 0) valorTotal = extraerDecimalJson(jsonBody, "valor_total");

            String usuarioAuditoria = extraerValorJson(jsonBody, "usuario");
            if (usuarioAuditoria.isEmpty()) usuarioAuditoria = request.getHeader("X-Usuario-Nombre");
            if (usuarioAuditoria == null || usuarioAuditoria.isEmpty()) usuarioAuditoria = "Usuario";

            try (Connection con = ConexionBD.getConexion()) {
                if (con == null) {
                    out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error BD\"}");
                    return;
                }

                garantizarTienda(con, idTienda, usuarioAuditoria);

                if (idProveedor == 0) {
                    String sqlFirst = "SELECT id_proveedor FROM proveedores WHERE id_tienda = ? LIMIT 1";
                    try (PreparedStatement psF = con.prepareStatement(sqlFirst)) {
                        psF.setInt(1, idTienda);
                        try (ResultSet rsF = psF.executeQuery()) {
                            if (rsF.next()) idProveedor = rsF.getInt("id_proveedor");
                        }
                    }
                }

                if (idPedido > 0) {
                    String sqlUpdate = "UPDATE pedidos SET id_proveedor = ?, fecha_compra = ?, metodo_pago = ?, valor_total = ?, descripcion = ? WHERE id_pedido = ? AND id_tienda = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                        ps.setInt(1, idProveedor);
                        ps.setString(2, fechaCompra);
                        ps.setString(3, metodoPago);
                        ps.setDouble(4, valorTotal);
                        ps.setString(5, descripcion);
                        ps.setInt(6, idPedido);
                        ps.setInt(7, idTienda);
                        ps.executeUpdate();
                    }

                    registrarAuditoria(con, idTienda, usuarioAuditoria, "Pedidos", "EDICIÓN", "Actualizado #" + idPedido + " - $" + valorTotal);

                    out.print("{\"success\":true,\"status\":\"OK\",\"estatus\":\"Exitoso\",\"mensaje\":\"Pedido actualizado.\",\"id_pedido\":" + idPedido + "}");
                } else {
                    String sqlInsert = "INSERT INTO pedidos (id_tienda, id_proveedor, fecha_compra, metodo_pago, valor_total, descripcion) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idTienda);
                        ps.setInt(2, idProveedor);
                        ps.setString(3, fechaCompra);
                        ps.setString(4, metodoPago);
                        ps.setDouble(5, valorTotal);
                        ps.setString(6, descripcion);
                        ps.executeUpdate();

                        int idGen = 0;
                        try (ResultSet rsKey = ps.getGeneratedKeys()) {
                            if (rsKey.next()) idGen = rsKey.getInt(1);
                        }

                        registrarAuditoria(con, idTienda, usuarioAuditoria, "Pedidos", "CREACIÓN", "Creado #" + idGen + " - $" + valorTotal);

                        out.print("{\"success\":true,\"status\":\"OK\",\"estatus\":\"Exitoso\",\"mensaje\":\"Pedido registrado.\",\"id_pedido\":" + idGen + "}");
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
            String idPedStr = request.getParameter("id");
            if (idPedStr == null || idPedStr.isEmpty()) idPedStr = request.getParameter("idPedido");

            int idPedido = Integer.parseInt(idPedStr);
            
            String idTiendaStr = request.getParameter("idTienda");
            int idTienda = 1;
            if (idTiendaStr != null && !idTiendaStr.isEmpty()) idTienda = Integer.parseInt(idTiendaStr);
            
            String usuarioAuditoria = request.getHeader("X-Usuario-Nombre");
            if (usuarioAuditoria == null || usuarioAuditoria.isEmpty()) usuarioAuditoria = "Usuario";

            try (Connection con = ConexionBD.getConexion()) {
                if (con != null) {
                    String sqlDel = "DELETE FROM pedidos WHERE id_pedido = ?";
                    try (PreparedStatement psD = con.prepareStatement(sqlDel)) {
                        psD.setInt(1, idPedido);
                        psD.executeUpdate();
                    }

                    registrarAuditoria(con, idTienda, usuarioAuditoria, "Pedidos", "ELIMINACIÓN", "Eliminado #" + idPedido);
                    out.print("{\"success\":true,\"status\":\"OK\",\"estatus\":\"Exitoso\",\"resultado\":true,\"mensaje\":\"Pedido eliminado.\"}");
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

    private double extraerDecimalJson(String json, String clave) {
        if (json == null || json.isEmpty()) return 0.0;
        try {
            String patron = "\"" + clave + "\"";
            int idx = json.indexOf(patron);
            if (idx == -1) return 0.0;
            int idxDosPuntos = json.indexOf(":", idx);
            StringBuilder sb = new StringBuilder();
            for (int i = idxDosPuntos + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (Character.isDigit(c) || c == '.') sb.append(c);
                else if (sb.length() > 0 && c != ' ') break;
            }
            return Double.parseDouble(sb.toString());
        } catch (Exception e) { return 0.0; }
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
