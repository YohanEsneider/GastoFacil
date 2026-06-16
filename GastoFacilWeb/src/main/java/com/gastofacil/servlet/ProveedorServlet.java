package com.gastofacil.servlet;

import com.gastofacil.dao.ProveedorDAO;
import com.gastofacil.modelo.Proveedor;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "ProveedorServlet", urlPatterns = {"/ProveedorServlet"})
public class ProveedorServlet extends HttpServlet {
    
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        
        if (accion != null) {
            if (accion.equals("eliminar")) {
                // Capturar ID y eliminar
                int id = Integer.parseInt(request.getParameter("id"));
                proveedorDAO.eliminar(id);
                response.sendRedirect("ProveedorServlet");
                return;
                
            } else if (accion.equals("editar")) {
                // Buscar el proveedor y enviarlo de regreso al formulario index.jsp
                int id = Integer.parseInt(request.getParameter("id"));
                Proveedor proveedorAEditar = proveedorDAO.buscarPorId(id);
                request.setAttribute("proveedorEditar", proveedorAEditar);
            }
        }
        
        // Listar por defecto
        List<Proveedor> lista = proveedorDAO.listar();
        request.setAttribute("proveedores", lista);
        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Capturar los datos del formulario
        String idStr = request.getParameter("idProveedor");
        String nombre = request.getParameter("nombre");
        String telefono = request.getParameter("telefono");
        String direccion = request.getParameter("direccion");

        Proveedor prov = new Proveedor();
        prov.setNombre(nombre);
        prov.setTelefono(telefono);
        prov.setDireccion(direccion);

        if (idStr != null && !idStr.isEmpty()) {
            // SI EL ID EXISTE: Estamos ACTUALIZANDO
            prov.setIdProveedor(Integer.parseInt(idStr));
            proveedorDAO.actualizar(prov);
        } else {
            // SI EL ID NO EXISTE: Estamos GUARDANDO UNO NUEVO
            proveedorDAO.insertar(prov);
        }

        // Redireccionar para recargar la lista limpia
        response.sendRedirect("ProveedorServlet");
    }
}