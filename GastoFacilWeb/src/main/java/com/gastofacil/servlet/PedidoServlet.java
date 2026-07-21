package com.gastofacil.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/PedidoServlet")
public class PedidoServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        
        if ("eliminar".equals(accion)) {
            String idParam = request.getParameter("id");
            if (idParam != null) {
                int idPedido = Integer.parseInt(idParam);
                String url = "jdbc:mysql://localhost:3306/gastofacil";
                String user = "root";
                String pass = "Admin123*";

                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection con = DriverManager.getConnection(url, user, pass);
                    String query = "DELETE FROM pedidos WHERE id_pedido = ?";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setInt(1, idPedido);
                    ps.executeUpdate();
                    ps.close();
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/pedidos.jsp");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        int idProveedor = Integer.parseInt(request.getParameter("idProveedor"));
        String fechaStr = request.getParameter("fechaPedido");
        Date fechaPedido = Date.valueOf(fechaStr); 
        String descripcion = request.getParameter("descripcion");
        double valorTotal = Double.parseDouble(request.getParameter("valorTotal"));
        String metodoPago = request.getParameter("metodoPago");
        String estado = "Recibido"; 

        String url = "jdbc:mysql://localhost:3306/gastofacil"; 
        String user = "root";
        String pass = "Admin123*"; 
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            PreparedStatement ps = null;
            
            if ("actualizar".equals(accion)) {
                // Opción: UPDATE pedido
                int idPedido = Integer.parseInt(request.getParameter("idPedido"));
                String query = "UPDATE pedidos SET id_proveedor = ?, fecha_pedido = ?, descripcion = ?, valor_total = ?, metodo_pago = ? WHERE id_pedido = ?";
                ps = con.prepareStatement(query);
                ps.setInt(1, idProveedor);
                ps.setDate(2, fechaPedido);
                ps.setString(3, descripcion);
                ps.setDouble(4, valorTotal);
                ps.setString(5, metodoPago);
                ps.setInt(6, idPedido);
            } else {
                // Opción por defecto: INSERT pedido
                String query = "INSERT INTO pedidos (id_proveedor, fecha_pedido, descripcion, valor_total, metodo_pago, estado) VALUES (?, ?, ?, ?, ?, ?)";
                ps = con.prepareStatement(query);
                ps.setInt(1, idProveedor);
                ps.setDate(2, fechaPedido);
                ps.setString(3, descripcion);
                ps.setDouble(4, valorTotal);
                ps.setString(5, metodoPago);
                ps.setString(6, estado);
            }
            
            ps.executeUpdate();
            ps.close();
            con.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/pedidos.jsp");
    }
}