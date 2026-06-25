<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="com.gastofacil.modelo.Proveedor"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>GastoFácil - Gestión de Proveedores</title>
        <!-- ⚡ INCORPORACIÓN DE FRAMEWORK BOOTSTRAP 5 -->
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">

        <%
            // Verificar si el Servlet nos envió un proveedor para editar
            Proveedor provEditar = (Proveedor) request.getAttribute("proveedorEditar");
            boolean modoEdicion = (provEditar != null);
        %>

        <div class="container mt-5">
            <!-- Encabezado Principal estilizado con Bootstrap -->
            <div class="text-center mb-5 p-3 bg-white rounded shadow-sm">
                <h1 class="display-5 fw-bold text-primary">GastoFácil</h1>
                <p class="lead text-muted">Módulo de Administración de Proveedores</p>
            </div>
        
            <div class="row g-4">
                <!-- COLUMNA DEL FORMULARIO (Componente Card de Bootstrap) -->
                <div class="col-md-4">
                    <div class="card shadow-sm border-0">
                        <div class="card-header <%= modoEdicion ? "bg-warning text-dark" : "bg-success text-white" %> fw-bold py-3">
                            <%= modoEdicion ? "✏️ Modificar Proveedor" : "➕ Registrar Proveedor" %>
                        </div>
                        <div class="card-body p-4">
                            <form action="ProveedorServlet" method="POST">
                                
                                <!-- ID oculto para la actualización de datos -->
                                <input type="hidden" name="idProveedor" value="<%= modoEdicion ? provEditar.getIdProveedor() : "" %>">

                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Nombre del Proveedor:</label>
                                    <input type="text" name="nombre" class="form-control" value="<%= modoEdicion ? provEditar.getNombre() : "" %>" required placeholder="Ej. Distribuidora Alfa">
                                </div>

                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Teléfono:</label>
                                    <input type="text" name="telefono" class="form-control" value="<%= modoEdicion ? provEditar.getTelefono() : "" %>" placeholder="Ej. 3001234567">
                                </div>

                                <div class="mb-3">
                                    <label class="form-label fw-semibold">Dirección:</label>
                                    <input type="text" name="direccion" class="form-control" value="<%= modoEdicion ? provEditar.getDireccion() : "" %>" placeholder="Ej. Calle 10 #5-20">
                                </div>

                                <div class="d-grid gap-2 mt-4">
                                    <button type="submit" class="btn <%= modoEdicion ? "btn-warning" : "btn-success" %> fw-bold shadow-sm">
                                        <%= modoEdicion ? "Actualizar Datos" : "Guardar Proveedor" %>
                                    </button>
                                    
                                    <% if(modoEdicion) { %>
                                        <a href="ProveedorServlet" class="btn btn-outline-secondary btn-sm mt-1">Cancelar Edición</a>
                                    <% } %>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>

                <!-- COLUMNA DE LA TABLA (Tabla Estilizada con Bootstrap) -->
                <div class="col-md-8">
                    <div class="card shadow-sm border-0 p-4 bg-white">
                        <h3 class="h4 text-secondary mb-4 fw-bold">Proveedores Registrados</h3>
                        <div class="table-responsive">
                            <table class="table table-striped table-hover align-middle">
                                <thead class="table-dark">
                                    <tr>
                                        <th scope="col" style="width: 8%">ID</th>
                                        <th scope="col">Nombre</th>
                                        <th scope="col" style="width: 20%">Teléfono</th>
                                        <th scope="col">Dirección</th>
                                        <th scope="col" style="width: 25%" class="text-center">Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <%
                                        List<Proveedor> lista = (List<Proveedor>) request.getAttribute("proveedores");
                                        if (lista != null && !lista.isEmpty()) {
                                            for (Proveedor p : lista) {
                                    %>
                                    <tr>
                                        <td class="fw-bold"><%= p.getIdProveedor() %></td>
                                        <td><%= p.getNombre() %></td>
                                        <td><%= p.getTelefono() %></td>
                                        <td><%= p.getDireccion() %></td>
                                        <td class="text-center">
                                            <div class="btn-group" role="group">
                                                <a href="ProveedorServlet?accion=editar&id=<%= p.getIdProveedor() %>" class="btn btn-sm btn-outline-warning fw-semibold">Editar</a>
                                                <a href="ProveedorServlet?accion=eliminar&id=<%= p.getIdProveedor() %>" class="btn btn-sm btn-danger text-white fw-semibold" onclick="return confirm('¿Seguro que deseas eliminar este proveedor?')">Eliminar</a>
                                            </div>
                                        </td>
                                    </tr>
                                    <%
                                            }
                                        } else {
                                    %>
                                    <tr>
                                        <td colspan="5" class="text-center text-muted py-4">No hay proveedores registrados aún.</td>
                                    </tr>
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

        <!-- Scripts opcionales de Bootstrap para componentes interactivos -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
