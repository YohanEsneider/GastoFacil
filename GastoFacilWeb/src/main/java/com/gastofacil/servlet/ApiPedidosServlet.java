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

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // LISTAR HISTORIAL DE PEDIDOS
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

                        jsonArr.append("{");
                        jsonArr.append("\"id_pedido\":").append(rs.getInt("id_pedido")).append(",");
                        jsonArr.append("\"idPedido\":").append(rs.getInt("id_pedido")).append(",");
                        jsonArr.append("\"id_tienda\":").append(rs.getInt("id_tienda")).append(",");
                        jsonArr.append("\"id_proveedor\":").append(rs.getInt("id_proveedor")).append(",");
                        jsonArr.append("\"proveedor\":\"").append(escapeJson(rs.getString("nombre_proveedor"))).append("\",");
                        jsonArr.append("\"fecha\":\"").append(rs.getString("fecha_compra")).append("\",");
                        jsonArr.append("\"fecha_compra\":\"").append(rs.getString("fecha_compra")).append("\",");
                        jsonArr.append("\"metodo_pago\":\"").append(escapeJson(rs.getString("metodo_pago"))).append("\",");
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

    // REGISTRAR NUEVO PEDIDO
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

        int idTienda = extraerNumeroJson(jsonBody, "idTienda");
        if (idTienda == 0) idTienda = extraerNumeroJson(jsonBody, "id_tienda");
        if (idTienda == 0) idTienda = 1;

        int idProveedor = extraerNumeroJson(jsonBody, "idProveedor");
        if (idProveedor == 0) idProveedor = extraerNumeroJson(jsonBody, "id_proveedor");

        String fechaCompra = extraerValorJson(jsonBody, "fechaCompra");
        if (fechaCompra.isEmpty()) fechaCompra = extraerValorJson(jsonBody, "fecha");
        if (fechaCompra.isEmpty()) fechaCompra = java.time.LocalDate.now().toString();

        String metodoPago = extraerValorJson(jsonBody, "metodoPago");
        if (metodoPago.isEmpty()) metodoPago = extraerValorJson(jsonBody, "metodo_pago");
        if (metodoPago.isEmpty()) metodoPago = "Efectivo";

        double valorTotal = extraerDecimalJson(jsonBody, "valorTotal");
        if (valorTotal == 0) valorTotal = extraerDecimalJson(jsonBody, "total");

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error de conexión con la base de datos.\"}");
                return;
            }

            // Si el idProveedor no vino numérico en el JSON, buscar por el nombre enviado
            if (idProveedor == 0) {
                String nombreProv = extraerValorJson(jsonBody, "proveedor");
                if (!nombreProv.isEmpty()) {
                    String sqlP = "SELECT id_proveedor FROM proveedores WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(?)) AND id_tienda = ? LIMIT 1";
                    try (PreparedStatement psP = con.prepareStatement(sqlP)) {
                        psP.setString(1, nombreProv);
                        psP.setInt(2, idTienda);
                        try (ResultSet rsP = psP.executeQuery()) {
                            if (rsP.next()) {
                                idProveedor = rsP.getInt("id_proveedor");
                            }
                        }
                    }
                }
            }

            // Si aún no se encuentra, tomar el primer proveedor registrado de la tienda
            if (idProveedor == 0) {
                String sqlFirst = "SELECT id_proveedor FROM proveedores WHERE id_tienda = ? LIMIT 1";
                try (PreparedStatement psF = con.prepareStatement(sqlFirst)) {
                    psF.setInt(1, idTienda);
                    try (ResultSet rsF = psF.executeQuery()) {
                        if (rsF.next()) idProveedor = rsF.getInt("id_proveedor");
                    }
                }
            }

            if (idProveedor == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Debe registrar al menos un proveedor antes de crear un pedido.\"}");
                return;
            }

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

                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print("{\"success\":true,\"status\":\"success\",\"estatus\":\"Exitoso\",\"mensaje\":\"Pedido registrado exitosamente.\",\"id_pedido\":" + idPedidoGenerado + "}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"No se pudo guardar el pedido.\"}");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error interno al guardar el pedido.\"}");
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
