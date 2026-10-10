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

    // LISTAR PEDIDOS
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

                        String desc = rs.getString("metodo_pago"); 
                        // Intentar obtener campo descripcion si existe
                        try {
                            desc = rs.getString("descripcion");
                            if (desc == null || desc.isEmpty()) desc = rs.getString("resumen");
                        } catch (Exception ignored) {}
                        if (desc == null) desc = "";

                        jsonArr.append("{");
                        jsonArr.append("\"id_pedido\":").append(rs.getInt("id_pedido")).append(",");
                        jsonArr.append("\"idPedido\":").append(rs.getInt("id_pedido")).append(",");
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
                        jsonArr.append("\"total\":").append(rs.getDouble("valor_total")).append(",");
                        jsonArr.append("\"valor_total\":").append(rs.getDouble("valor_total"));
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

    // REGISTRAR O EDITAR PEDIDO
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

        int idPedido = extraerNumeroJson(jsonBody, "idPedido");
        if (idPedido == 0) idPedido = extraerNumeroJson(jsonBody, "id_pedido");

        int idTienda = extraerNumeroJson(jsonBody, "idTienda");
        if (idTienda == 0) idTienda = extraerNumeroJson(jsonBody, "id_tienda");
        if (idTienda == 0) idTienda = 1;

        int idProveedor = extraerNumeroJson(jsonBody, "idProveedor");
        if (idProveedor == 0) idProveedor = extraerNumeroJson(jsonBody, "id_proveedor");

        String fechaCompra = extraerValorJson(jsonBody, "fechaCompra");
        if (fechaCompra.isEmpty()) fechaCompra = extraerValorJson(jsonBody, "fecha");
        if (fechaCompra.isEmpty()) fechaCompra = java.time.LocalDate.now().toString();

        String descripcion = extraerValorJson(jsonBody, "descripcion");
        if (descripcion.isEmpty()) descripcion = extraerValorJson(jsonBody, "resumen");

        String metodoPago = extraerValorJson(jsonBody, "metodoPago");
        if (metodoPago.isEmpty()) metodoPago = extraerValorJson(jsonBody, "metodo_pago");
        if (metodoPago.isEmpty()) metodoPago = "Efectivo";

        double valorTotal = extraerDecimalJson(jsonBody, "valorTotal");
        if (valorTotal == 0) valorTotal = extraerDecimalJson(jsonBody, "valor_total");

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error de conexión con la base de datos.\"}");
                return;
            }

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
                // ACTUALIZAR PEDIDO
                String sqlUpdate = "UPDATE pedidos SET id_proveedor = ?, fecha_compra = ?, metodo_pago = ?, valor_total = ? WHERE id_pedido = ? AND id_tienda = ?";
                try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, idProveedor);
                    ps.setString(2, fechaCompra);
                    ps.setString(3, metodoPago);
                    ps.setDouble(4, valorTotal);
                    ps.setInt(5, idPedido);
                    ps.setInt(6, idTienda);
                    ps.executeUpdate();
                }

                // Intentar guardar descripción si existe columna en la tabla pedidos
                try {
                    String sqlDesc = "UPDATE pedidos SET descripcion = ? WHERE id_pedido = ?";
                    try (PreparedStatement psD = con.prepareStatement(sqlDesc)) {
                        psD.setString(1, descripcion);
                        psD.setInt(2, idPedido);
                        psD.executeUpdate();
                    }
                } catch (Exception ignored) {}

                // Registrar en Auditoría
                registrarAuditoria(con, idTienda, "Pedidos", "EDICIÓN", "Se actualizó el pedido #" + idPedido + " por valor de $" + valorTotal);

                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"success\":true,\"status\":\"success\",\"estatus\":\"Exitoso\",\"mensaje\":\"Pedido actualizado exitosamente.\",\"id_pedido\":" + idPedido + "}");
            } else {
                // CREAR NUEVO PEDIDO
                String sqlInsert = "INSERT INTO pedidos (id_tienda, id_proveedor, fecha_compra, metodo_pago, valor_total) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idTienda);
                    ps.setInt(2, idProveedor);
                    ps.setString(3, fechaCompra);
                    ps.setString(4, metodoPago);
                    ps.setDouble(5, valorTotal);

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        int idPedidoGenerado = 0;
                        try (ResultSet rsKey = ps.getGeneratedKeys()) {
                            if (rsKey.next()) idPedidoGenerado = rsKey.getInt(1);
                        }

                        // Intentar guardar la descripción
                        try {
                            String sqlDesc = "UPDATE pedidos SET descripcion = ? WHERE id_pedido = ?";
                            try (PreparedStatement psD = con.prepareStatement(sqlDesc)) {
                                psD.setString(1, descripcion);
                                psD.setInt(2, idPedidoGenerado);
                                psD.executeUpdate();
                            }
                        } catch (Exception ignored) {}

                        // Registrar en Auditoría
                        registrarAuditoria(con, idTienda, "Pedidos", "CREACIÓN", "Se registró un nuevo pedido #" + idPedidoGenerado + " por valor de $" + valorTotal);

                        response.setStatus(HttpServletResponse.SC_CREATED);
                        out.print("{\"success\":true,\"status\":\"success\",\"estatus\":\"Exitoso\",\"mensaje\":\"Pedido registrado exitosamente.\",\"id_pedido\":" + idPedidoGenerado + "}");
                    } else {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"No se pudo guardar el pedido.\"}");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error interno al procesar el pedido.\"}");
        }
    }

    // ELIMINAR PEDIDO
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String idPedStr = request.getParameter("id");
        if (idPedStr == null || idPedStr.isEmpty()) idPedStr = request.getParameter("idPedido");
        if (idPedStr == null || idPedStr.isEmpty()) idPedStr = request.getParameter("id_pedido");

        int idPedido = 0;
        if (idPedStr != null) {
            try { idPedido = Integer.parseInt(idPedStr); } catch (NumberFormatException ignored) {}
        }

        if (idPedido <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"ID de pedido no válido.\"}");
            return;
        }

        int idTienda = 1;

        try (Connection con = ConexionBD.getConexion()) {
            if (con != null) {
                String sql = "DELETE FROM pedidos WHERE id_pedido = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idPedido);
                    ps.executeUpdate();
                }

                // Registrar en Auditoría
                registrarAuditoria(con, idTienda, "Pedidos", "ELIMINACIÓN", "Se eliminó el registro del pedido #" + idPedido);

                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"success\":true,\"status\":\"success\",\"estatus\":\"Exitoso\",\"mensaje\":\"Pedido eliminado con éxito.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error al eliminar el pedido.\"}");
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

    private double extraerDecimalJson(String json, String clave) {
        if (json == null || json.isEmpty()) return 0.0;
        try {
            String patron = "\"" + clave + "\"";
            int indexClave = json.indexOf(patron);
            if (indexClave == -1) return 0.0;

            int indexDosPuntos = json.indexOf(":", indexClave);
            if (indexDosPuntos == -1) return 0.0;

            StringBuilder numSb = new StringBuilder();
            for (int i = indexDosPuntos + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (Character.isDigit(c) || c == '.') {
                    numSb.append(c);
                } else if (numSb.length() > 0 && c != ' ') {
                    break;
                }
            }
            if (numSb.length() > 0) return Double.parseDouble(numSb.toString());
        } catch (Exception ignored) {}
        return 0.0;
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
