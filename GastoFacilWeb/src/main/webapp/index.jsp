<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<%
    // Control de sesión: Redirigir al login si no se ha autenticado
    if (session.getAttribute("nombreUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>GastoFácil - Módulo de Proveedores</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa !important; }
    </style>
</head>
<body>

    <!-- Barra de Navegación Unificada Oscura con Dropdown de Usuario -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark mb-4 shadow-sm">
        <div class="container-fluid px-5">
            <span class="navbar-brand fw-bold text-primary">GastoFácil</span>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <div class="navbar-nav me-auto">
                    <a class="nav-link" href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
                    <a class="nav-link active fw-bold border-bottom border-warning" href="${pageContext.request.contextPath}/index.jsp">Proveedores</a>
                    <a class="nav-link" href="${pageContext.request.contextPath}/pedidos.jsp">Pedidos</a>
                    <a class="nav-link" href="${pageContext.request.contextPath}/historial.jsp">Historial</a>
                    <a class="nav-link" href="${pageContext.request.contextPath}/ReporteServlet">Reportes</a>
                </div>
                <!-- Menú Desplegable del Usuario -->
                <div class="dropdown">
                    <button class="btn btn-outline-light dropdown-toggle border-0 fw-semibold" type="button" id="userMenu" data-bs-toggle="dropdown" aria-expanded="false">
                        👤 <%= session.getAttribute("nombreUsuario") != null ? session.getAttribute("nombreUsuario") : "Usuario" %>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 mt-2" aria-labelledby="userMenu">
                        <li><span class="dropdown-item-text text-muted small fw-bold text-uppercase"><%= session.getAttribute("rolUsuario") != null ? session.getAttribute("rolUsuario") : "Rol" %></span></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger fw-semibold" href="${pageContext.request.contextPath}/AuthServlet?accion=logout">🚪 Cerrar sesión</a></li>
                    </ul>
                </div>
            </div>
        </div>
    </nav>

    <div class="container mb-5">
        <div class="mb-4">
            <h2 class="text-secondary fw-bold m-0">🚚 Gestión de Proveedores</h2>
            <p class="text-muted m-0">Administra, registra o modifica los proveedores de tu negocio.</p>
        </div>

        <div class="row g-4">
            <!-- COLUMNA DEL FORMULARIO -->
            <div class="col-md-4">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white fw-bold py-3">
                        ➕ Registrar Proveedor
                    </div>
                    <div class="card-body p-4">
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
                                <div class="d-flex flex-wrap gap-2 bg-light p-2 border rounded">
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

            <!-- COLUMNA DE LA TABLA -->
            <div class="col-md-8">
                <div class="card shadow-sm border-0 bg-white">
                    <div class="card-header bg-white fw-bold text-secondary py-3 border-bottom">
                        📋 Proveedores Registrados
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
                                                    <td class="ps-3 fw-bold"><%= idProv %></td>
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