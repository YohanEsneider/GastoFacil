<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.gastofacil.modelo.Proveedor"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>GastoFácil - Proveedores</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 30px; background-color: #f4f7f6; }
            h2 { color: #333; }
            .container { display: flex; gap: 40px; }
            .form-box { background: white; padding: 20px; border-radius: 8px; box-shadow: 0px 0px 10px rgba(0,0,0,0.1); width: 300px; }
            .table-box { flex-grow: 1; background: white; padding: 20px; border-radius: 8px; box-shadow: 0px 0px 10px rgba(0,0,0,0.1); }
            input[type="text"] { width: 100%; padding: 8px; margin: 8px 0 16px 0; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; }
            input[type="submit"] { background-color: #4CAF50; color: white; padding: 10px 15px; border: none; border-radius: 4px; cursor: pointer; width: 100%; font-size: 16px; }
            input[type="submit"].btn-edit { background-color: #0288d1; }
            table { width: 100%; border-collapse: collapse; margin-top: 10px; }
            th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
            th { background-color: #f2f2f2; color: #333; }
            tr:nth-child(even) { background-color: #f9f9f9; }
            .btn-action { padding: 5px 10px; text-decoration: none; border-radius: 4px; color: white; font-size: 13px; font-weight: bold; }
            .btn-delete { background-color: #d32f2f; margin-left: 5px; }
            .btn-modify { background-color: #f57c00; }
            .btn-cancel { display: block; text-align: center; margin-top: 10px; color: #666; font-size: 14px; }
        </style>
    </head>
    <body>

        <h2>Gestión de Proveedores - GastoFácil</h2>
        
        <%
            // Verificar si el Servlet nos envió un proveedor para editar
            Proveedor provEditar = (Proveedor) request.getAttribute("proveedorEditar");
            boolean modoEdicion = (provEditar != null);
        %>

        <div class="container">
            <div class="form-box">
                <h3><%= modoEdicion ? "Modificar Proveedor" : "Registrar Proveedor" %></h3>
                <form action="ProveedorServlet" method="POST">
                    
                    <input type="hidden" name="idProveedor" value="<%= modoEdicion ? provEditar.getIdProveedor() : "" %>">

                    <label>Nombre:</label>
                    <input type="text" name="nombre" value="<%= modoEdicion ? provEditar.getNombre() : "" %>" required>

                    <label>Teléfono:</label>
                    <input type="text" name="telefono" value="<%= modoEdicion ? provEditar.getTelefono() : "" %>">

                    <label>Dirección:</label>
                    <input type="text" name="direccion" value="<%= modoEdicion ? provEditar.getDireccion() : "" %>">

                    <input type="submit" class="<%= modoEdicion ? "btn-edit" : "" %>" value="<%= modoEdicion ? "Actualizar Datos" : "Guardar Proveedor" %>">
                    
                    <% if(modoEdicion) { %>
                        <a href="ProveedorServlet" class="btn-cancel">Cancelar Edición</a>
                    <% } %>
                </form>
            </div>

            <div class="table-box">
                <h3>Proveedores Registrados en el Sistema</h3>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Teléfono</th>
                            <th>Dirección</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            List<Proveedor> lista = (List<Proveedor>) request.getAttribute("proveedores");
                            if (lista != null && !lista.isEmpty()) {
                                for (Proveedor p : lista) {
                        %>
                        <tr>
                            <td><%= p.getIdProveedor() %></td>
                            <td><%= p.getNombre() %></td>
                            <td><%= p.getTelefono() %></td>
                            <td><%= p.getDireccion() %></td>
                            <td>
                                <a href="ProveedorServlet?accion=editar&id=<%= p.getIdProveedor() %>" class="btn-action btn-modify">Editar ✏️</a>
                                <a href="ProveedorServlet?accion=eliminar&id=<%= p.getIdProveedor() %>" class="btn-action btn-delete" onclick="return confirm('¿Seguro que deseas eliminar este proveedor?')">Eliminar 🗑️</a>
                            </td>
                        </tr>
                        <%
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="5" style="text-align: center; color: gray;">No hay proveedores registrados aún.</td>
                        </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>
        </div>

    </body>
</html>