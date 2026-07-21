<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>GastoFácil - Módulo de Proveedores</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

    <div class="container mt-4">
        <div class="card text-center p-4 shadow-sm mb-4" style="border: 1px solid #e3e6f0;">
            <h1 class="fw-bold text-primary display-4">GastoFácil</h1>
            <p class="text-muted fs-5 mb-3">Módulo de Administración de Proveedores</p>
            
            <div class="d-flex justify-content-center gap-2">
                <a href="index.jsp" class="btn btn-primary btn-sm fw-bold">Proveedores</a>
                <a href="pedidos.jsp" class="btn btn-outline-secondary btn-sm">Pedidos</a>
            </div>
        </div>
    </div>

    <div class="container">
        <div class="row">
            
            <div class="col-md-4 mb-4">
                <div class="card shadow-sm">
                    <div class="card-header bg-primary text-white fw-bold">
                        ➕ Registrar Proveedor
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/ProveedorServlet" method="POST">
                            <input type="hidden" name="accion" value="guardar">
                            
                            <div class="mb-3">
                                <label for="nombre" class="form-label fw-semibold">Nombre del Proveedor:</label>
                                <input type="text" class="form-control" id="nombre" name="nombre" placeholder="Ej: Distribuidora Bavaria" required>
                            </div>

                            <div class="mb-3">
                                <label for="telefono" class="form-label fw-semibold">Teléfono / Celular:</label>
                                <input type="tel" 
                                       class="form-control" 
                                       id="telefono" 
                                       name="telefono" 
                                       placeholder="Ej: 3151234567" 
                                       pattern="[0-9]+" 
                                       title="Por favor, ingresa únicamente números sin espacios ni guiones." 
                                       required>
                            </div>

                            <div class="mb-3">
                                <label for="direccion" class="form-label fw-semibold">Dirección:</label>
                                <input type="text" class="form-control" id="direccion" name="direccion" placeholder="Ej: Calle 10 # 5-20" required>
                            </div>

                            <div class="mb-3">
                                <label for="categoria" class="form-label fw-semibold">Categoría:</label>
                                <select class="form-select" id="categoria" name="categoria" required>
                                    <option selected disabled value="">Selecciona una categoría...</option>
                                    <option value="Abarrotes">Abarrotes</option>
                                    <option value="Bebidas">Bebidas</option>
                                    <option value="Lácteos">Lácteos</option>
                                    <option value="Carnes/Embutidos">Carnes / Embutidos</option>
                                    <option value="Otros">Otros</option>
                                </select>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-semibold d-block">Días de Visita:</label>
                                <div class="d-flex flex-wrap gap-2 bg-white p-2 border rounded">
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="diasVisita" value="Lunes" id="lunes">
                                        <label class="form-check-label" for="lunes">Lun</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="diasVisita" value="Martes" id="martes">
                                        <label class="form-check-label" for="martes">Mar</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="diasVisita" value="Miércoles" id="miercoles">
                                        <label class="form-check-label" for="miercoles">Mié</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="diasVisita" value="Jueves" id="jueves">
                                        <label class="form-check-label" for="jueves">Jue</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="diasVisita" value="Viernes" id="viernes">
                                        <label class="form-check-label" for="viernes">Vie</label>
                                    </div>
                                    <div class="form-check">
                                        <input class="form-check-input" type="checkbox" name="diasVisita" value="Sábado" id="sabado">
                                        <label class="form-check-label" for="sabado">Sáb</label>
                                    </div>
                                </div>
                            </div>

                            <div class="d-grid mt-4">
                                <button type="submit" class="btn btn-primary fw-bold">Guardar Proveedor</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-md-8 mb-4">
                <div class="card shadow-sm">
                    <div class="card-header bg-light fw-bold text-secondary">
                        Proveedores Registrados
                    </div>
                    <div class="card-body p-0">
                        
                        <%
                            String errorMsg = (String) session.getAttribute("errorProveedor");
                            if (errorMsg != null) {
                        %>
                                <div class="alert alert-danger alert-dismissible fade show mx-3 mt-3" role="alert">
                                    ⚠️ <strong>¡Atención!</strong> <%= errorMsg %>
                                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                                </div>
                        <%
                                session.removeAttribute("errorProveedor");
                            }
                        %>

                        <div class="table-responsive">
                            <table class="table table-striped table-hover align-middle mb-0">
                                <thead class="table-dark">
                                    <tr>
                                        <th class="ps-3">ID</th>
                                        <th>Nombre</th>
                                        <th>Categoría</th>
                                        <th>Días Visita</th>
                                        <th>Teléfono</th>
                                        <th>Dirección</th>
                                        <th class="pe-3 text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        String dbUrl = "jdbc:mysql://localhost:3306/gastofacil";
                                        String dbUser = "root";
                                        String dbPass = "Admin123*"; 
                                        
                                        try {
                                            Class.forName("com.mysql.cj.jdbc.Driver");
                                            Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
                                            String sql = "SELECT idProveedor, nombre, telefono, direccion, categoria, dias_visita FROM proveedor ORDER BY idProveedor DESC";
                                            PreparedStatement ps = con.prepareStatement(sql);
                                            ResultSet rs = ps.executeQuery();
                                            boolean tieneProveedores = false;
                                            
                                            while(rs.next()) {
                                                tieneProveedores = true;
                                                int idProv = rs.getInt("idProveedor");
                                                String cat = rs.getString("categoria");
                                                String dias = rs.getString("dias_visita");
                                    %>
                                                <tr>
                                                    <td class="ps-3"><%= idProv %></td>
                                                    <td class="fw-bold text-dark"><%= rs.getString("nombre") %></td>
                                                    <td><span class="badge bg-secondary"><%= cat != null ? cat : "Abarrotes" %></span></td>
                                                    <td class="small text-truncate" style="max-width: 150px;"><%= dias != null ? dias : "No asignado" %></td>
                                                    <td><%= rs.getString("telefono") != null ? rs.getString("telefono") : "-" %></td>
                                                    <td class="text-muted"><%= rs.getString("direccion") != null ? rs.getString("direccion") : "-" %></td>
                                                    
                                                    <td class="pe-3 text-center">
                                                        <div class="d-flex gap-1 justify-content-center">
                                                            <a href="editar_proveedor.jsp?id=<%= idProv %>" class="btn btn-warning btn-sm fw-bold px-2 py-1" style="font-size: 0.8rem;">✏️ Editar</a>
                                                            <a href="${pageContext.request.contextPath}/ProveedorServlet?accion=eliminar&id=<%= idProv %>" 
                                                               class="btn btn-danger btn-sm fw-bold px-2 py-1" style="font-size: 0.8rem;"
                                                               onclick="return confirm('¿Seguro que deseas eliminar este proveedor?');">
                                                                🗑️ Borrar
                                                            </a>
                                                        </div>
                                                    </td>
                                                </tr>
                                    <%
                                            }
                                            if(!tieneProveedores) {
                                    %>
                                                <tr><td colspan="7" class="text-center text-muted py-4">No hay proveedores registrados aún.</td></tr>
                                    <%
                                            }
                                            rs.close(); ps.close(); con.close();
                                        } catch(Exception e) {
                                    %>
                                            <tr><td colspan="7" class="text-center text-danger py-4">Error al cargar la tabla de proveedores.</td></tr>
                                    <%
                                        }
                                    %>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>