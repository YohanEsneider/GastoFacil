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

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // LISTAR PROVEEDORES POR ID TIENDA
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        aplicarCors(request, response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String idTiendaStr = request.getParameter("idTienda");
        if (idTiendaStr == null || idTiendaStr.isEmpty()) {
            idTiendaStr = request.getHeader("idTienda");
        }
        if (idTiendaStr == null || idTiendaStr.isEmpty()) {
            idTiendaStr = "1"; // Valor por defecto
        }

        int idTienda = 1;
        try {
            idTienda = Integer.parseInt(idTiendaStr);
        } catch (NumberFormatException ignored) {}

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
                        jsonArr.append("\"id_tienda\":").append(rs.getInt("id_tienda")).append(",");
                        jsonArr.append("\"nombre\":\"").append(escapeJson(rs.getString("nombre"))).append("\",");
                        jsonArr.append("\"contacto\":\"").append(escapeJson(rs.getString("contacto"))).append("\",");
                        jsonArr.append("\"telefono\":\"").append(escapeJson(rs.getString("telefono"))).append("\",");
                        jsonArr.append("\"categoria\":\"").append(escapeJson(rs.getString("categoria"))).append("\",");
                        jsonArr.append("\"dias_atencion\":\"").append(escapeJson(rs.getString("dias_atencion"))).append("\",");
                        jsonArr.append("\"horario\":\"").append(escapeJson(rs.getString("horario"))).append("\"");
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

    // REGISTRAR NUEVO PROVEEDOR
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
        String nombre = extraerValorJson(jsonBody, "nombre");
        if (nombre.isEmpty()) nombre = extraerValorJson(jsonBody, "nombre_proveedor");

        String contacto = extraerValorJson(jsonBody, "contacto");
        String telefono = extraerValorJson(jsonBody, "telefono");
        String categoria = extraerValorJson(jsonBody, "categoria");
        String diasAtencion = extraerValorJson(jsonBody, "dias_atencion");
        if (diasAtencion.isEmpty()) diasAtencion = extraerValorJson(jsonBody, "diasAtencion");
        String horario = extraerValorJson(jsonBody, "horario");

        String idTiendaStr = extraerValorJson(jsonBody, "id_tienda");
        if (idTiendaStr.isEmpty()) idTiendaStr = extraerValorJson(jsonBody, "idTienda");
        
        int idTienda = 1;
        if (!idTiendaStr.isEmpty()) {
            try { idTienda = Integer.parseInt(idTiendaStr); } catch (NumberFormatException ignored) {}
        }

        if (nombre.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"status\":\"error\",\"mensaje\":\"El nombre del proveedor es obligatorio.\"}");
            return;
        }

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"status\":\"error\",\"mensaje\":\"Error de conexión con la base de datos.\"}");
                return;
            }

            String sql = "INSERT INTO proveedores (id_tienda, nombre, contacto, telefono, categoria, dias_atencion, horario) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idTienda);
                ps.setString(2, nombre);
                ps.setString(3, contacto);
                ps.setString(4, telefono);
                ps.setString(5, categoria);
                ps.setString(6, diasAtencion);
                ps.setString(7, horario);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    int idGenerado = 0;
                    try (ResultSet rsKey = ps.getGeneratedKeys()) {
                        if (rsKey.next()) idGenerado = rsKey.getInt(1);
                    }
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    out.print("{\"success\":true,\"status\":\"success\",\"mensaje\":\"Proveedor registrado exitosamente.\",\"id_proveedor\":" + idGenerado + "}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"status\":\"error\",\"mensaje\":\"No se pudo guardar el proveedor.\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"status\":\"error\",\"mensaje\":\"Error interno al guardar proveedor.\"}");
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
