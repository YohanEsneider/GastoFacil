package com.gastofacil.servlet;

import com.gastofacil.conexion.ConexionBD;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/api/dashboard")
public class ApiDashboardServlet extends HttpServlet {

    private void aplicarCors(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, idTienda, X-Usuario-Id");
        response.setHeader("Access-Control-Max-Age", "3600");
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
        if (idTiendaStr == null || idTiendaStr.isEmpty()) {
            idTiendaStr = request.getHeader("idTienda");
        }
        int idTienda = 1;
        if (idTiendaStr != null && !idTiendaStr.isEmpty()) {
            try { idTienda = Integer.parseInt(idTiendaStr); } catch (NumberFormatException ignored) {}
        }

        double gastoMesActual = 0.0;
        double gastoMesAnterior = 0.0;
        int totalPedidos = 0;
        int totalProveedores = 0;

        try (Connection con = ConexionBD.getConexion()) {
            if (con != null) {
                // 1. Gasto del Mes Actual
                String sqlGastoMes = "SELECT COALESCE(SUM(valor_total), 0) FROM pedidos " +
                                     "WHERE id_tienda = ? " +
                                     "AND MONTH(fecha_compra) = MONTH(CURRENT_DATE()) " +
                                     "AND YEAR(fecha_compra) = YEAR(CURRENT_DATE())";
                try (PreparedStatement psGasto = con.prepareStatement(sqlGastoMes)) {
                    psGasto.setInt(1, idTienda);
                    try (ResultSet rs = psGasto.executeQuery()) {
                        if (rs.next()) gastoMesActual = rs.getDouble(1);
                    }
                }

                // 2. Gasto del Mes Anterior
                String sqlGastoAnt = "SELECT COALESCE(SUM(valor_total), 0) FROM pedidos " +
                                     "WHERE id_tienda = ? " +
                                     "AND MONTH(fecha_compra) = MONTH(CURRENT_DATE() - INTERVAL 1 MONTH) " +
                                     "AND YEAR(fecha_compra) = YEAR(CURRENT_DATE() - INTERVAL 1 MONTH)";
                try (PreparedStatement psAnt = con.prepareStatement(sqlGastoAnt)) {
                    psAnt.setInt(1, idTienda);
                    try (ResultSet rs = psAnt.executeQuery()) {
                        if (rs.next()) gastoMesAnterior = rs.getDouble(1);
                    }
                }

                // 3. Contar pedidos registrados (Total histórico de la tienda)
                String sqlPedidos = "SELECT COUNT(*) FROM pedidos WHERE id_tienda = ?";
                try (PreparedStatement psPed = con.prepareStatement(sqlPedidos)) {
                    psPed.setInt(1, idTienda);
                    try (ResultSet rsPed = psPed.executeQuery()) {
                        if (rsPed.next()) totalPedidos = rsPed.getInt(1);
                    }
                }

                // 4. Contar proveedores activos
                String sqlProv = "SELECT COUNT(*) FROM proveedores WHERE id_tienda = ?";
                try (PreparedStatement psProv = con.prepareStatement(sqlProv)) {
                    psProv.setInt(1, idTienda);
                    try (ResultSet rsProv = psProv.executeQuery()) {
                        if (rsProv.next()) totalProveedores = rsProv.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder json = new StringBuilder("{");
        json.append("\"success\":true,");
        json.append("\"status\":\"success\",");
        json.append("\"gastosMes\":").append(gastoMesActual).append(",");
        json.append("\"gastoTotalMes\":").append(gastoMesActual).append(",");
        json.append("\"gastoTotal\":").append(gastoMesActual).append(",");
        json.append("\"gastoMesAnterior\":").append(gastoMesAnterior).append(",");
        json.append("\"totalPedidos\":").append(totalPedidos).append(",");
        json.append("\"pedidosRegistrados\":").append(totalPedidos).append(",");
        json.append("\"totalProveedores\":").append(totalProveedores).append(",");
        json.append("\"proveedoresActivos\":").append(totalProveedores);
        json.append("}");

        response.setStatus(HttpServletResponse.SC_OK);
        out.print(json.toString());
    }
}
