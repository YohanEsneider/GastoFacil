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
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, idTienda, X-Usuario-Id, X-Usuario-Nombre");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    private void registrarAuditoria(Connection con, int idTienda, String usuario, String modulo, String accion, String descripcion) {
        try {
            if (usuario == null || usuario.trim().isEmpty() || usuario.equalsIgnoreCase("Usuario")) {
                usuario = obtenerNombreUsuarioPorTienda(con, idTienda);
            }
            if (usuario.length() > 50) {
                usuario = usuario.substring(0, 50);
            }

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
            System.err.println("Advertencia no crítica en auditoría de proveedores: " + e.getMessage());
        }
    }

    private String obtenerNombreUsuarioPorTienda(Connection con, int idTienda) {
        String nombre = "Tienda";
        try {
            String sql = "SELECT nombre FROM usuarios WHERE id_usuario = ? OR id_tienda = ? LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idTienda);
                ps.setInt(2, idTienda);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getString("nombre") != null) {
                        nombre = rs.getString("nombre");
                    }
                }
            }
        } catch (Exception ignored) {}
        return nombre;
    }

    private String resolverUsuarioRequest(HttpServletRequest request, String jsonBody, Connection con, int idTienda) {
        String usr = extraerValorJson(jsonBody, "usuario");
        if (usr.isEmpty()) usr = extraerValorJson(jsonBody, "nombreUsuario");
        if (usr.isEmpty()) usr = request.getHeader("X-Usuario-Nombre");
        if (usr.isEmpty() || usr.equalsIgnoreCase("Usuario")) {
            usr = obtenerNombreUsuarioPorTienda(con, idTienda);
        }
        return usr;
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
        if (idTiendaStr == null || idTiendaStr.isEmpty()) {
            idTiendaStr = request.getHeader("X-Usuario-Id");
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
        if (idTienda == 0) {
            String idTStr = request.getHeader("idTienda");
            if (idTStr == null || idTStr.isEmpty()) idTStr = request.getHeader("X-Usuario-Id");
            if (idTStr != null && !idTStr.isEmpty()) {
                try { idTienda = Integer.parseInt(idTStr); } catch (Exception ignored) {}
            }
        }
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

            String usuarioAuditoria = resolverUsuarioRequest(request, jsonBody, con, idTienda);

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

                // Registrar en Auditoría dinámicamente
                registrarAuditoria(con, idTienda, usuarioAuditoria, "Proveedores", "EDICIÓN", "Se actualizó la información del proveedor '" + nombre + "'");

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

                        // Registrar en Auditoría dinámicamente
                        registrarAuditoria(con, idTienda, usuarioAuditoria, "Proveedores", "CREACIÓN", "Se registró el nuevo proveedor '" + nombre + "'");

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

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String jsonBody = sb.toString();

        int idProveedor = 0;
        if (idProvStr != null && !idProvStr.isEmpty()) {
            try { idProveedor = Integer.parseInt(idProvStr); } catch (NumberFormatException ignored) {}
        }
        if (idProveedor == 0) {
            idProveedor = extraerNumeroJson(jsonBody, "idProveedor");
            if (idProveedor == 0) idProveedor = extraerNumeroJson(jsonBody, "id_proveedor");
            if (idProveedor == 0) idProveedor = extraerNumeroJson(jsonBody, "id");
        }

        if (idProveedor <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"ID de proveedor no válido.\"}");
            return;
        }

        String idTiendaStr = request.getParameter("idTienda");
        if (idTiendaStr == null || idTiendaStr.isEmpty()) idTiendaStr = request.getHeader("idTienda");
        if (idTiendaStr == null || idTiendaStr.isEmpty()) idTiendaStr = request.getHeader("X-Usuario-Id");
        int idTienda = 1;
        if (idTiendaStr != null && !idTiendaStr.isEmpty()) {
            try { idTienda = Integer.parseInt(idTiendaStr); } catch (NumberFormatException ignored) {}
        }
        int idTiendaJson = extraerNumeroJson(jsonBody, "id_tienda");
        if (idTiendaJson > 0) idTienda = idTiendaJson;

        try (Connection con = ConexionBD.getConexion()) {
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Sin conexión a la base de datos.\"}");
                return;
            }

            String usuarioAuditoria = resolverUsuarioRequest(request, jsonBody, con, idTienda);

            // Validar Integridad Referencial
            String sqlCheck = "SELECT COUNT(*) FROM pedidos WHERE id_proveedor = ?";
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, idProveedor);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                        int cantidadPedidos = rsCheck.getInt(1);
                        registrarAuditoria(con, idTienda, usuarioAuditoria, "Proveedores", "BLOQUEO", "Intento fallido de eliminar proveedor #" + idProveedor + " con " + cantidadPedidos + " pedidos.");

                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"No se puede eliminar el proveedor porque tiene " + cantidadPedidos + " pedido(s) registrado(s).\"}");
                        return;
                    }
                }
            }

            // Obtener nombre para Auditoría
            String nombreProveedor = "Proveedor #" + idProveedor;
            String sqlNombre = "SELECT nombre FROM proveedores WHERE id_proveedor = ?";
            try (PreparedStatement psN = con.prepareStatement(sqlNombre)) {
                psN.setInt(1, idProveedor);
                try (ResultSet rsN = psN.executeQuery()) {
                    if (rsN.next()) nombreProveedor = rsN.getString("nombre");
                }
            }

            // Ejecutar la eliminación
            String sqlDelete = "DELETE FROM proveedores WHERE id_proveedor = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlDelete)) {
                ps.setInt(1, idProveedor);
                int filasAfectadas = ps.executeUpdate();

                if (filasAfectadas > 0) {
                    registrarAuditoria(con, idTienda, usuarioAuditoria, "Proveedores", "ELIMINACIÓN", "Se eliminó el proveedor '" + nombreProveedor + "'");
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print("{\"success\":true,\"status\":\"success\",\"estatus\":\"Exitoso\",\"mensaje\":\"Proveedor eliminado con éxito.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"No se encontró el proveedor en la base de datos para eliminar.\"}");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"status\":\"error\",\"estatus\":\"Error\",\"mensaje\":\"Error interno al intentar eliminar el proveedor.\"}");
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
