package com.gastofacil.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ProveedorServlet")
public class ProveedorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        
        if ("eliminar".equals(accion)) {
            String idParam = request.getParameter("id");
            if (idParam != null) {
                int idProveedor = Integer.parseInt(idParam);
                String url = "jdbc:mysql://localhost:3306/gastofacil";
                String user = "root";
                String pass = "Admin123*"; 

                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection con = DriverManager.getConnection(url, user, pass);
                    String query = "DELETE FROM proveedor WHERE idProveedor = ?";
                    PreparedStatement ps = con.prepareStatement(query);
                    ps.setInt(1, idProveedor);
                    
                    ps.executeUpdate();
                    ps.close();
                    con.close();
                    
                } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                    // 🛑 Captura si la Base de Datos bloquea el borrado por restricción de llave foránea
                    request.getSession().setAttribute("errorProveedor", "No se puede eliminar el proveedor porque tiene pedidos o facturas asociadas en el sistema.");
                    response.sendRedirect(request.getContextPath() + "/index.jsp");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        String nombre = request.getParameter("nombre");
        String telefono = request.getParameter("telefono");
        String direccion = request.getParameter("direccion");
        String categoria = request.getParameter("categoria");
        
        // Procesar los múltiples checkboxes recibidos de los días de visita
        String[] diasArray = request.getParameterValues("diasVisita");
        String diasVisita = "No asignado";
        if (diasArray != null && diasArray.length > 0) {
            diasVisita = String.join(", ", diasArray); 
        }

        String url = "jdbc:mysql://localhost:3306/gastofacil";
        String user = "root";
        String pass = "Admin123*";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);
            PreparedStatement ps = null;

            if ("actualizar".equals(accion)) {
                int idProveedor = Integer.parseInt(request.getParameter("idProveedor"));
                String query = "UPDATE proveedor SET nombre = ?, telefono = ?, direccion = ?, categoria = ?, dias_visita = ? WHERE idProveedor = ?";
                ps = con.prepareStatement(query);
                ps.setString(1, nombre);
                ps.setString(2, telefono);
                ps.setString(3, direccion);
                ps.setString(4, categoria);
                ps.setString(5, diasVisita);
                ps.setInt(6, idProveedor);
            } else {
                String query = "INSERT INTO proveedor (nombre, telefono, direccion, categoria, dias_visita) VALUES (?, ?, ?, ?, ?)";
                ps = con.prepareStatement(query);
                ps.setString(1, nombre);
                ps.setString(2, telefono);
                ps.setString(3, direccion);
                ps.setString(4, categoria);
                ps.setString(5, diasVisita);
            }

            ps.executeUpdate();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }
}