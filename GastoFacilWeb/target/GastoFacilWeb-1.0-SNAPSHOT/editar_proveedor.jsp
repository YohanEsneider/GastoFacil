<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>GastoFácil - Editar Proveedor</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

    <%
        String idParam = request.getParameter("id");
        int idProveedor = 0;
        String nombre = "", telefono = "", direccion = "", categoriaActual = "Abarrotes", diasActuales = "";
        
        String dbUrl = "jdbc:mysql://localhost:3306/gastofacil";
        String dbUser = "root";
        String dbPass = "Admin123*";
        
        if (idParam != null) {
            idProveedor = Integer.parseInt(idParam);
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                String sql = "SELECT nombre, telefono, direccion, categoria, dias_visita FROM proveedor WHERE idProveedor = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, idProveedor);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    nombre = rs.getString("nombre");
                    telefono = rs.getString("telefono");
                    direccion = rs.getString("direccion");
                    categoriaActual = rs.getString("categoria") != null ? rs.getString("categoria") : "Abarrotes";
                    diasActuales = rs.getString("dias_visita") != null ? rs.getString("dias_visita") : "";
                }
                rs.close(); ps.close(); con.close();
            } catch(Exception e) { e.printStackTrace(); }
        }
    %>

    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card shadow-sm">
                    <div class="card-header bg-warning text-dark fw-bold">
                        ✏️ Editar Proveedor (ID: <%= idProveedor %>)
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/ProveedorServlet" method="POST">
                            <input type="hidden" name="accion" value="actualizar">
                            <input type="hidden" name="idProveedor" value="<%= idProveedor %>">
                            
                            <div class="mb-3">
                                <label class="form-label fw-semibold">Nombre del Proveedor:</label>
                                <input type="text" class="form-control" name="nombre" value="<%= nombre %>" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label fw-semibold">Teléfono / Celular:</label>
                                <input type="text" class="form-control" name="telefono" value="<%= telefono != null ? telefono : "" %>">
                            </div>
                            <div class="mb-3">
                                <label class="form-label fw-semibold">Dirección:</label>
                                <input type="text" class="form-control" name="direccion" value="<%= direccion != null ? direccion : "" %>">
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-semibold">Categoría:</label>
                                <select class="form-select" name="categoria" required>
                                    <option value="Abarrotes" <%= "Abarrotes".equals(categoriaActual) ? "selected" : "" %>>Abarrotes</option>
                                    <option value="Bebidas" <%= "Bebidas".equals(categoriaActual) ? "selected" : "" %>>Bebidas</option>
                                    <option value="Lácteos" <%= "Lácteos".equals(categoriaActual) ? "selected" : "" %>>Lácteos</option>
                                    <option value="Carnes/Embutidos" <%= "Carnes/Embutidos".equals(categoriaActual) ? "selected" : "" %>>Carnes / Embutidos</option>
                                    <option value="Otros" <%= "Otros".equals(categoriaActual) ? "selected" : "" %>>Otros</option>
                                </select>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-semibold d-block">Días de Visita:</label>
                                <div class="d-flex flex-wrap gap-2 bg-white p-2 border rounded">
                                    <% 
                                        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
                                        for(String dia : dias) {
                                            boolean marcado = diasActuales.contains(dia);
                                    %>
                                            <div class="form-check">
                                                <input class="form-check-input" type="checkbox" name="diasVisita" value="<%= dia %>" id="edit_<%= dia %>" <%= marcado ? "checked" : "" %>>
                                                <label class="form-check-label" for="edit_<%= dia %>"><%= dia.substring(0,3) %></label>
                                            </div>
                                    <%  } %>
                                </div>
                            </div>
                            
                            <div class="d-flex gap-2 justify-content-end mt-4">
                                <a href="index.jsp" class="btn btn-secondary">Cancelar</a>
                                <button type="submit" class="btn btn-warning fw-bold">Actualizar Cambios</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>