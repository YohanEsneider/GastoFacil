<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.gastofacil.modelo.Proveedor"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>GastoFácil - Lista de Proveedores</title>
    <style>
        table { width: 60%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #999; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h2>Proveedores Registrados en el Sistema</h2>
    
    <table>
        <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Teléfono</th>
            <th>Dirección</th>
        </tr>
        
        <%
            // Esto es código Java dentro de JSP (Scriptlet)
            // Extraemos la lista que el Servlet guardó en la petición
            List<Proveedor> lista = (List<Proveedor>) request.getAttribute("listaProveedores");
            
            if (lista != null && !lista.isEmpty()) {
                for (Proveedor p : lista) {
        %>
                <tr>
                    <td><%= p.getIdProveedor() %></td>
                    <td><%= p.getNombre() %></td>
                    <td><%= p.getTelefono() %></td>
                    <td><%= p.getDireccion() %></td>
                </tr>
        <%
                }
            } else {
        %>
                <tr>
                    <td colspan="4" style="text-align: center;">No hay proveedores registrados aún.</td>
                </tr>
        <%
            }
        %>
    </table>
    
    <br>
    <a href="proveedores.jsp">➕ Registrar otro proveedor</a>
</body>
</html>